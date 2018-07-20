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

import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.persistence.model.VideoViewData;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.persistence.service.UserService;
import com.orange.signsatwork.biz.storage.StorageService;
import com.orange.signsatwork.biz.view.model.*;
import org.jcodec.api.JCodecException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
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

  /*  VideosViewSort videosViewSort = new VideosViewSort();
    videoViews = videosViewSort.sort(videoViews);
*/
    model.addAttribute("videosView", videoViews);

    return "new-profil";
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/your-job")
  public String yourJob(Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());
    model.addAttribute("title", messageByLocaleService.getMessage("your_job_title"));
    model.addAttribute("user", user);
    model.addAttribute("userCreationView", new UserCreationView());
    model.addAttribute("backUrl", "/sec/new-profil");

    return "your-job";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/profil/job", method = RequestMethod.POST)
  public String changeUserJob(
 @ModelAttribute UserCreationView userCreationView, Principal principal, Model model) {
    UserService userService = services.user();

    User user = userService.withUserName(principal.getName());
    userService.changeJob(user, userCreationView.getJob());
    model.addAttribute("user", user);
    model.addAttribute("userCreationView", new UserCreationView());
    model.addAttribute("backUrl", "/sec/new-profil");

    return "redirect:/sec/your-job";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/profil/entity", method = RequestMethod.POST)
  public String changeUserEntity(
 @ModelAttribute UserCreationView userCreationView, Principal principal, Model model) {
    UserService userService = services.user();

    User user = userService.withUserName(principal.getName());
    userService.changeEntity(user, userCreationView.getEntity());

    model.addAttribute("user", user);
    model.addAttribute("userCreationView", new UserCreationView());
    model.addAttribute("backUrl", "/sec/new-profil");

    return "redirect:/sec/your-job";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/profil/description", method = RequestMethod.POST)
  public String changeUserDescription(
 @ModelAttribute UserCreationView userCreationView, Principal principal, Model model) {
    UserService userService = services.user();

    User user = userService.withUserName(principal.getName());
    userService.changeDescription(user, userCreationView.getJobTextDescription());
    model.addAttribute("user", user);

    return "redirect:/sec/your-job-description";
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
  @RequestMapping(value = "/sec/who-are-you")
  public String whoAreYou(Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());
    model.addAttribute("title", messageByLocaleService.getMessage("who_are_you") );
    model.addAttribute("user", user);
    model.addAttribute("backUrl", "/sec/new-profil");

    return "who-are-you";
  }



  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/profil/lastName", method = RequestMethod.POST)
  public String changeUserLastName(
    @ModelAttribute UserCreationView userCreationView, Principal principal, Model model) {
    UserService userService = services.user();

    User user = userService.withUserName(principal.getName());
    userService.changeLastName(user, userCreationView.getLastName());
    model.addAttribute("user", user);

    return "redirect:/sec/who-are-you";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/profil/firstName", method = RequestMethod.POST)
  public String changeUserFirstName(
    @ModelAttribute UserCreationView userCreationView, Principal principal, Model model) {
    UserService userService = services.user();

    User user = userService.withUserName(principal.getName());
    userService.changeFirstName(user, userCreationView.getFirstName());
    model.addAttribute("user", user);

    return "redirect:/sec/who-are-you";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/profil/email", method = RequestMethod.POST)
  public String changeEmail(
    @ModelAttribute UserCreationView userCreationView, Principal principal, Model model) {
    UserService userService = services.user();

    User user = userService.withUserName(principal.getName());
    userService.changeEmail(user, userCreationView.getEmail());
    model.addAttribute("user", user);

    return "redirect:/sec/who-are-you";
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/profile-from-community/{communityId}/{userId}")
  public String userDetails(@PathVariable long userId, @PathVariable long communityId, Model model) {
    User user = services.user().withId(userId);
    model.addAttribute("title", user.name());
    model.addAttribute("backUrl", "/sec/community/"+communityId);

    List<Object[]> queryVideos = services.video().AllVideosCreateByUser(user.id);
    List<VideoViewData> videoViewsData = queryVideos.stream()
      .map(objectArray -> new VideoViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> videoInFavorite = Arrays.asList(services.video().VideosForAllFavoriteByUser(user.id));

    List<VideoView2> videoViews = videoViewsData.stream()
      .map(videoViewData -> buildVideoView(videoViewData, videoInFavorite, user))
      .collect(Collectors.toList());

    VideosViewSort videosViewSort = new VideosViewSort();
    videoViews = videosViewSort.sort(videoViews);

    model.addAttribute("videosView", videoViews);

    model.addAttribute("user", user);

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


}
