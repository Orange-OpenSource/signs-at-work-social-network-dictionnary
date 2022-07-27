package com.orange.signsatwork.biz.view.controller.admin;

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

import com.orange.signsatwork.biz.domain.*;
import com.orange.signsatwork.biz.persistence.model.CommentData;
import com.orange.signsatwork.biz.persistence.model.RatingData;
import com.orange.signsatwork.biz.persistence.model.VideoHistoryData;
import com.orange.signsatwork.biz.persistence.model.VideoViewData;
import com.orange.signsatwork.biz.persistence.service.CommunityService;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.persistence.service.UserService;
import com.orange.signsatwork.biz.view.model.*;
import lombok.extern.slf4j.Slf4j;
import org.jcodec.api.JCodecException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class AdminController {

  @Autowired
  private UserAdminController userAdminController;

  @Autowired
  private Services services;
  @Autowired
  private UserService userService;
  @Autowired
  private CommunityService communityService;
  @Autowired
  MessageByLocaleService messageByLocaleService;
  @Autowired
  private Environment environment;

  @Value("${app.admin.username}")
  String adminUsername;

  @Value("${app.name}")
  String appName;

  @Secured("ROLE_ADMIN")
  @RequestMapping("/sec/admin")
  public String admin(Model model) {

    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("admin_page"));
    model.addAttribute("appName", appName);
    return "admin/index";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping("/sec/admin/users")
  public String users(Model model) {

    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("users"));
    model.addAttribute("users", UserAdminView.from(userService.all()));
    model.addAttribute("adminUserName", adminUsername);
    model.addAttribute("appName", appName);
    return "admin/manage_users";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping("/sec/admin/communities")
  public String communities(Model model) {
    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("communities"));
    List<Community> communities = communityService.all().stream().filter(c -> c.type == CommunityType.Job).collect(Collectors.toList());
    model.addAttribute("communities", CommunityView.from(communities));
    model.addAttribute("appName", appName);
    return "admin/communities";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping("/sec/admin/manage_communities_users")
  public String manage_communities_users(Model model) {
    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("manage_communities_users"));
    model.addAttribute("user", new UserCreationView());
    model.addAttribute("community", new CommunityView());
    model.addAttribute("appName", appName);
    return "admin/manage_communities_users";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/community/{id}")
  public String community(@PathVariable long id, Model model) {
    Community community = communityService.withId(id);
    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("community_details"));
    model.addAttribute("community", community);
    CommunityProfileView communityProfileView = new CommunityProfileView(community, userService);
    model.addAttribute("communityProfileView", communityProfileView);
    model.addAttribute("appName", appName);
    return "admin/community";
  }


  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/community/create", method = RequestMethod.POST)
  public String community(@ModelAttribute CommunityView communityView, Model model, Principal principal) {
    User user = userService.withUserName(principal.getName());
    communityView.setType(CommunityType.Job);
    Community community = communityService.create(user.id, communityView.toCommunity());
    return community(community.id, model);
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/user/create", method = RequestMethod.POST)
  public String user(@ModelAttribute UserCreationView userCreationView, Model model, HttpServletRequest request) throws IOException, JCodecException {
    String title, bodyMail;
    User user = userService.create(userCreationView.toUser(), userCreationView.getPassword(), userCreationView.getRole(), userCreationView.getUsername());
    if (user != null) {
      userService.createUserFavorite(user.id, messageByLocaleService.getMessage("default_favorite"));
      final String token = UUID.randomUUID().toString();
      services.user().createPasswordResetTokenForUser(user, token);
      final String url = getAppUrl() + "/user/createPassword?id=" + user.id + "&token=" + token;
      title = messageByLocaleService.getMessage("password_create_title",new Object[]{appName});
      bodyMail = messageByLocaleService.getMessage("password_create_body", new Object[]{appName, userCreationView.getUsername(), url});

      Runnable task = () -> {
        log.info("send mail email = {} / title = {} / body = {}", userCreationView.getUsername(), title, bodyMail);
        services.emailService().sendCreatePasswordMessage(userCreationView.getUsername(), title, userCreationView.getUsername(), url, request.getLocale());
      };

      new Thread(task).start();
    }
    return userAdminController.userDetails(user.id, model);
  }

  private String getAppUrl() {
    return environment.getProperty("app.url");
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/user/{userId}/add/communities", method = RequestMethod.POST)
  /**
   * We retrieve all form parameters directly from the raw request since in this case
   * we can not rely on a json object deserialization.
   * Indeed, POST form parameters look like this:
   *  - userCommunitiesIds -> "12"
   *  - userCommunitiesIds -> "34"
   *  - ...
   *  which in this case means that the user belongs to communities with id 12 & 34
   *
   *  Then we resend the user details page
   */
  public String changeUserCommunities(
    HttpServletRequest req, @PathVariable long userId, Model model) {

    List<Long> communitiesIds =
      transformCommunitiesIdsToLong(req.getParameterMap().get("userCommunitiesIds"));

 /*   userService.changeUserCommunities(userId, communitiesIds);*/

    return userAdminController.userDetails(userId, model);
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/community/{communityId}/add/users", method = RequestMethod.POST)
  public String changeCommunityUsers(
    HttpServletRequest req, @PathVariable long communityId, Model model) {

    List<Long> usersIds =
      transformUsersIdsToLong(req.getParameterMap().get("communityUsersIds"));

    communityService.changeCommunityUsers(communityId, usersIds);

    return community(communityId, model);
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/user/{userId}/changePassword", method = RequestMethod.POST)
  public String changeUserPassword(@ModelAttribute UserCreationView userCreationView, @PathVariable long userId, Model model) {

   userService.changeUserPassword(userService.withId(userId), userCreationView.getPassword());

    return userAdminController.userDetails(userId, model);
  }

  /** The form POST provides Ids as String, we convert it back to Long */
  private List<Long> transformCommunitiesIdsToLong(String[] userCommunitiesIds) {
    if (userCommunitiesIds == null) {
      return new ArrayList<>();
    }
    return Arrays.asList(userCommunitiesIds).stream()
      .map(Long::parseLong)
      .collect(Collectors.toList());
  }

  private List<Long> transformUsersIdsToLong(String[] communityUsersIds) {
    if (communityUsersIds == null) {
      return new ArrayList<>();
    }
    return Arrays.asList(communityUsersIds).stream()
      .map(Long::parseLong)
      .collect(Collectors.toList());
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/profile-from-admin/{userId}")
  public String userDetails(@PathVariable long userId, HttpServletRequest request, Model model, Principal principal) {
    Boolean isConnectedUser = false;
    String userAgent = request.getHeader("User-Agent");
    User connectedUser = services.user().withUserName(principal.getName());

    User user = services.user().withId(userId);
    model.addAttribute("title", user.name());
/*    model.addAttribute("backUrl", "/sec/community/"+communityId);*/

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
    model.addAttribute("appName", appName);
    model.addAttribute("action", "/ws/sec/users/" + userId + "/datas");
    model.addAttribute("isAdmin", true);
    model.addAttribute("isConnectedUser", true);
    model.addAttribute("actionForDeleteVideoName", "/ws/sec/deleteVideoFileForName/" + userId);
    model.addAttribute("actionForDeleteVideoJob", "/ws/sec/deleteVideoFileForJob/" + userId);
    model.addAttribute("isIOSDevice", isIOSDevice(userAgent));

    return "profile-from-community";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/sign/{signId}/{videoId}")
  public String video(HttpServletRequest req, @PathVariable long signId, @PathVariable long videoId, Principal principal, Model model) {

/*    Boolean isVideoCreatedByMe = false;
    String referer = req.getHeader("Referer");

    model.addAttribute("videoBelowToFavorite", false);
    StringBuffer location = req.getRequestURL();

    AuthentModel.addAuthenticatedModel(model, AuthentModel.isAuthenticated(principal));
    model.addAttribute("showAddFavorite", SHOW_ADD_FAVORITE && AuthentModel.isAuthenticated(principal));
    model.addAttribute("commentCreationView", new CommentCreationView());
    model.addAttribute("favoriteCreationView", new FavoriteCreationView());*/


    Sign sign = services.sign().withIdSignsView(signId);
    if (sign == null) {
      return "redirect:/signs/mostrecent?isMostRecent=false&isSearch=false";
    }

    Video video = services.video().withId(videoId);
    if (video == null) {
      return "redirect:/sign/" + signId;
    }

    model.addAttribute("title", messageByLocaleService.getMessage("card"));
    List<Object[]> queryAllComments = services.video().AllCommentsForVideo(videoId);
    List<CommentData> commentDatas = queryAllComments.stream()
      .map(objectArray -> new CommentData(objectArray))
      .collect(Collectors.toList());
    model.addAttribute("commentDatas", commentDatas);
    List<Object[]> queryAllVideosHistory = services.sign().AllVideosHistoryForSign(signId);
    List<VideoHistoryData> videoHistoryDatas = queryAllVideosHistory.stream()
      .map(objectArray -> new VideoHistoryData(objectArray))
      .collect(Collectors.toList());
    model.addAttribute("videoHistoryDatas", videoHistoryDatas);

/*    if (principal != null) {
      User user = services.user().withUserName(principal.getName());
      Object[] queryRating = services.video().RatingForVideoByUser(videoId, user.id);
      RatingData ratingData = new RatingData(queryRating);
      model.addAttribute("ratingData", ratingData);
      List<Object[]> queryAllComments = services.video().AllCommentsForVideo(videoId);
      List<CommentData> commentDatas = queryAllComments.stream()
        .map(objectArray -> new CommentData(objectArray))
        .collect(Collectors.toList());
      model.addAttribute("commentDatas", commentDatas);
      model.addAttribute("title", messageByLocaleService.getMessage("card"));
      fillModelWithFavorites(model, user);
      if (video.user.id == user.id) {
        isVideoCreatedByMe = true;
      }
      Long nbFavorite = services.video().NbFavoriteBelowVideoForUser(videoId, user.id);
      if (nbFavorite >= 1) {
        model.addAttribute("videoBelowToFavorite", true);
      }
    }

    if (video.averageRate > 0) {
      model.addAttribute("videoHasPositiveRate", true);
    } else {
      model.addAttribute("videoHasPositiveRate", false);
    }

    if (!isVideoCreatedByMe) {
      if ((referer != null) && referer.contains("detail")) {
      } else {
        services.video().increaseNbView(videoId);
      }
    }*/

    if ((video.idForName == 0) || (sign.nbVideo == 1)) {
      model.addAttribute("videoName", sign.name);
    } else {
      model.addAttribute("videoName", sign.name + "_" + video.idForName);
    }

    model.addAttribute("signView", sign);
    model.addAttribute("videoView", video);
   /* model.addAttribute("isVideoCreatedByMe", isVideoCreatedByMe);

    Long nbRating = services.sign().NbRatingForSign(signId);
    model.addAttribute("nbRating", nbRating);*/
    model.addAttribute("appName", appName);
    model.addAttribute("isAuthenticated", AuthentModel.isAuthenticated(principal));
    return "admin/sign";
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
