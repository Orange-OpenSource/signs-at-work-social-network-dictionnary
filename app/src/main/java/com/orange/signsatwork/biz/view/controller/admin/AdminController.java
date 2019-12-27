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

import com.orange.signsatwork.biz.domain.Communities;
import com.orange.signsatwork.biz.domain.Community;
import com.orange.signsatwork.biz.domain.CommunityType;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.persistence.service.CommunityService;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.persistence.service.UserService;
import com.orange.signsatwork.biz.view.model.*;
import lombok.extern.slf4j.Slf4j;
import org.jcodec.api.JCodecException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
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


  @Secured("ROLE_ADMIN")
  @RequestMapping("/sec/admin")
  public String admin(Model model) {

    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("admin_page"));

    return "admin/index";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping("/sec/admin/users")
  public String users(Model model) {

    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("users"));
    model.addAttribute("users", UserView.from(userService.all()));
    return "admin/users";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping("/sec/admin/communities")
  public String communities(Model model) {
    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("communities"));
    List<Community> communities = communityService.all().stream().filter(c -> c.type == CommunityType.Job).collect(Collectors.toList());
    model.addAttribute("communities", CommunityView.from(communities));
    return "admin/communities";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping("/sec/admin/manage_communities_users")
  public String manage_communities_users(Model model) {
    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("manage_communities_users"));
    model.addAttribute("user", new UserCreationView());
    model.addAttribute("community", new CommunityView());
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

    return "admin/community";
  }


  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/community/create", method = RequestMethod.POST)
  public String community(@ModelAttribute CommunityView communityView, Model model) {
    Community community = communityService.create(communityView.toCommunity());
    return community(community.id, model);
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/user/create", method = RequestMethod.POST)
  public String user(@ModelAttribute UserCreationView userCreationView, Model model, HttpServletRequest request) throws IOException, JCodecException {
    String title, bodyMail;
    User user = userService.create(userCreationView.toUser(), userCreationView.getPassword(), userCreationView.getRole());
    if (user != null) {
      userService.createUserFavorite(user.id, messageByLocaleService.getMessage("default_favorite"));
      final String token = UUID.randomUUID().toString();
      services.user().createPasswordResetTokenForUser(user, token);
      final String url = getAppUrl(request) + "/user/createPassword?id=" + user.id + "&token=" + token;
      title = messageByLocaleService.getMessage("password_create_title");
      bodyMail = messageByLocaleService.getMessage("password_create_body", new Object[]{userCreationView.getUsername(), url});

      Runnable task = () -> {
        log.info("send mail email = {} / title = {} / body = {}", userCreationView.getUsername(), title, bodyMail);
        services.emailService().sendCreatePasswordMessage(userCreationView.getUsername(), title, userCreationView.getUsername(), url);
      };

      new Thread(task).start();
    }
    return userAdminController.userDetails(user.id, model);
  }

  private String getAppUrl(HttpServletRequest request) {
    return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
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

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/user/{userId}/changeLogin", method = RequestMethod.POST)
  public String changeUserLogin(@ModelAttribute UserCreationView userCreationView, @PathVariable long userId, Model model) {

    User user = userService.withId(userId);
    userService.changeUserLogin(user , userCreationView.getUsername());
    if (!userCreationView.getFirstName().isEmpty()) {
      userService.changeFirstName(user, userCreationView.getFirstName());
    }

    if (!userCreationView.getLastName().isEmpty()) {
      userService.changeLastName(user, userCreationView.getLastName());
    }

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
}
