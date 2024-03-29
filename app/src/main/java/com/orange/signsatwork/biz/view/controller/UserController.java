package com.orange.signsatwork.biz.view.controller;

/*
 * #%L
 * Signs at work
 * %%
 * Copyright (C) 2016 Orange
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import com.orange.signsatwork.biz.domain.Community;
import com.orange.signsatwork.biz.domain.Favorite;
import com.orange.signsatwork.biz.domain.PasswordResetToken;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.persistence.model.CommunityViewData;
import com.orange.signsatwork.biz.persistence.model.VideoViewData;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.persistence.service.UserService;
import com.orange.signsatwork.biz.storage.StorageService;
import com.orange.signsatwork.biz.view.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UserController {

  @Autowired
  private StorageService storageService;

  @Autowired
  private Services services;
  @Autowired
  MessageByLocaleService messageByLocaleService;

  @Value("${app.name}")
  String appName;

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/new-profil")
  public String userProfil(Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    model.addAttribute("title", messageByLocaleService.getMessage("profile"));
    model.addAttribute("user", user);
    fillModelWithFavorites(model, user);
    model.addAttribute("backUrl", "/");

    List<Object[]> queryVideos = services.video().AllVideosCreateByUser(user.id);
    List<VideoViewData> videoViewsData = queryVideos.stream()
      .map(objectArray -> new VideoViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> videoInFavorite = Arrays.asList(services.video().VideosForAllFavoriteByUser(user.id));

    List<VideoView2> videoViews = videoViewsData.stream()
      .map(videoViewData -> buildVideoView(videoViewData, videoInFavorite, user))
      .collect(Collectors.toList());

    model.addAttribute("videosView", videoViews);

    return "new-profil";
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/my-profil")
  public String myProfil(HttpServletRequest request, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());
    String userAgent = request.getHeader("User-Agent");

    model.addAttribute("isIOSDevice", isIOSDevice(userAgent));

    model.addAttribute("user", user);
    fillModelWithFavorites(model, user);

    model.addAttribute("title", user.name());

    List<Object[]> queryVideos = services.video().AllVideosCreateByUser(user.id);
    List<VideoViewData> videoViewsData = queryVideos.stream()
      .map(objectArray -> new VideoViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> videoInFavorite = Arrays.asList(services.video().VideosForAllFavoriteByUser(user.id));

    List<VideoView2> videoViews = videoViewsData.stream()
      .map(videoViewData -> buildVideoView(videoViewData, videoInFavorite, user))
      .collect(Collectors.toList());


    model.addAttribute("videosView", videoViews);

    model.addAttribute("isConnectedUser", true);
    model.addAttribute("appName", appName);
    model.addAttribute("action", "/ws/sec/users/me/datas");
    model.addAttribute("isAdmin", false);
    model.addAttribute("actionForDeleteVideoName", "/ws/sec/deleteVideoFileForName");
    model.addAttribute("actionForDeleteVideoJob", "/ws/sec/deleteVideoFileForJob");

    return "profile-from-community";
  }

  private boolean isIOSDevice(String userAgent) {
    boolean isIOSDevice = false;
    String osType = "Unknown";
    String osVersion = "Unknown";
    String deviceType = "Unknown";

    if (userAgent.indexOf("Mac OS") >= 0) {
      osType = "Mac";
      osVersion = userAgent.substring(userAgent.indexOf("Mac OS ") + 7, userAgent.indexOf(")"));

      if (userAgent.indexOf("iPhone") >= 0) {
        deviceType = "iPhone";
        isIOSDevice = true;
      } else if (userAgent.indexOf("iPad") >= 0) {
        deviceType = "iPad";
        isIOSDevice = true;
      }
    }
    return isIOSDevice;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/your-job-description")
  public String yourJobDescription(Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());
    model.addAttribute("title", messageByLocaleService.getMessage("your_job_description_title"));
    model.addAttribute("user", user);
    model.addAttribute("backUrl", "/sec/new-profil");

    return "your-job-description";
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/profile-from-community/{communityId}/{userId}")
  public String userDetails(@PathVariable long userId, @PathVariable long communityId, HttpServletRequest request, Model model, Principal principal) {
    Boolean isConnectedUser = false;
    User connectedUser = services.user().withUserName(principal.getName());
    String userAgent = request.getHeader("User-Agent");

    model.addAttribute("isIOSDevice", isIOSDevice(userAgent));

    User user = services.user().withId(userId);
    model.addAttribute("title", user.name());
    model.addAttribute("backUrl", "/sec/community/"+communityId);

    List<Object[]> queryVideos = services.video().AllVideosCreateByUser(user.id);
    List<VideoViewData> videoViewsData = queryVideos.stream()
      .map(objectArray -> new VideoViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> videoInFavorite = Arrays.asList(services.video().VideosForAllFavoriteByUser(user.id));

    List<VideoView2> videoViews = videoViewsData.stream()
      .map(videoViewData -> buildVideoView(videoViewData, videoInFavorite, connectedUser))
      .collect(Collectors.toList());

    VideosViewSort videosViewSort = new VideosViewSort();
    videoViews = videosViewSort.sort(videoViews);

    model.addAttribute("videosView", videoViews);

    model.addAttribute("user", user);
    if (connectedUser.id == user.id) {
      isConnectedUser = true;
    }
    model.addAttribute("isConnectedUser", isConnectedUser);
    model.addAttribute("appName", appName);
    model.addAttribute("action", "/ws/sec/users/me/datas");
    model.addAttribute("isAdmin", false);
    model.addAttribute("actionForDeleteVideoName", "/ws/sec/deleteVideoFileForName");
    model.addAttribute("actionForDeleteVideoJob", "/ws/sec/deleteVideoFileForJob");

    return "profile-from-community";
  }

  private void fillModelWithFavorites(Model model, User user) {
    List<FavoriteModalView> myFavorites = FavoriteModalView.from(services.favorite().favoritesforUser(user.id));
    model.addAttribute("myFavorites", myFavorites);
  }

  private VideoView2 buildVideoView(VideoViewData videoViewData, List<Long> videoBelowToFavorite, User user) {
    return new VideoView2(
      videoViewData,
      videoViewData.nbComment > 0,
      VideoView2.createdAfterLastDeconnection(videoViewData.createDate, user == null ? null : user.lastDeconnectionDate),
      videoViewData.nbView > 0,
      videoViewData.averageRate > 0,
      videoBelowToFavorite.contains(videoViewData.videoId));
  }

  @RequestMapping(value = "/user/changePassword", method = RequestMethod.GET)
  public String changePassword(Model model, @RequestParam("id") final long userId, @RequestParam("token") final String token) {
    User user = services.user().withId(userId);

    PasswordResetToken passToken = services.user().getPasswordResetToken(token);
    if ((passToken == null) || (user.id != passToken.user.id)) {
      return "redirect:/login";
    }

    Calendar cal = Calendar.getInstance();
    if ((passToken.expiryDate.getTime() - cal.getTime().getTime()) <= 0) {
      return "redirect:/login";
    }
    model.addAttribute("title", appName);
    model.addAttribute("userId", userId);
    model.addAttribute("userCreationView", new UserCreationView());
    model.addAttribute("appName", appName);
    model.addAttribute("token", token);

    return "update-password";
  }

  @RequestMapping(value = "/user/createPassword", method = RequestMethod.GET)
  public String createPassword(Model model, @RequestParam("id") final long userId, @RequestParam("token") final String token) {
    User user = services.user().withId(userId);

    PasswordResetToken passToken = services.user().getPasswordResetToken(token);
    if ((passToken == null) || (user.id != passToken.user.id)) {
      return "redirect:/login";
    }

    Calendar cal = Calendar.getInstance();
    if ((passToken.expiryDate.getTime() - cal.getTime().getTime()) <= 0) {
      return "redirect:/login";
    }

    model.addAttribute("title", appName);
    model.addAttribute("userId", userId);
    model.addAttribute("userCreationView", new UserCreationView());
    model.addAttribute("appName", appName);
    model.addAttribute("token", token);

    return "create-password";
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/my-job")
  public String myJob(Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    model.addAttribute("title", messageByLocaleService.getMessage("your_job_title"));
    UserJobView userJobView = new UserJobView(user, services.community());
    model.addAttribute("userJobView", userJobView);

    model.addAttribute("user", user);
    List<Object[]> queryCommunities = services.community().allForJob(user.id);
    List<CommunityViewData> communitiesViewData = queryCommunities.stream()
      .map(objectArray -> new CommunityViewData(objectArray))
      .collect(Collectors.toList());
    communitiesViewData = communitiesViewData.stream().map(c -> {
      Community community = services.community().withId(c.id);
      c.descriptionText = community.descriptionText;
      c.descriptionVideo = community.descriptionVideo;
      return c;
    }).collect(Collectors.toList());
    communitiesViewData = communitiesViewData.stream().sorted((c1, c2) -> c1.name.compareTo(c2.name)).collect(Collectors.toList());
    model.addAttribute("communities", communitiesViewData);
    model.addAttribute("appName", appName);
    model.addAttribute("isAdmin", false);
    model.addAttribute("action", "/ws/sec/users/me/datas");

    return "job-community";
  }
}
