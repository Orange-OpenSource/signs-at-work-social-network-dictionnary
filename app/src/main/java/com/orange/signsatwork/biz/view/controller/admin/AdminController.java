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

import com.orange.signsatwork.biz.domain.Community;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.persistence.service.CommunityService;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.persistence.service.UserService;
import com.orange.signsatwork.biz.view.model.AuthentModel;
import com.orange.signsatwork.biz.view.model.CommunityView;
import com.orange.signsatwork.biz.view.model.UserCreationView;
import com.orange.signsatwork.biz.view.model.UserView;
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
import java.util.stream.Collectors;

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
    model.addAttribute("communities", CommunityView.from(communityService.all()));
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
  public String user(@ModelAttribute UserCreationView userCreationView, Model model) throws IOException, JCodecException {

    User user = userService.create(userCreationView.toUser(), userCreationView.getPassword(), userCreationView.getRole());
    userService.createUserFavorite(user.id, messageByLocaleService.getMessage("default_favorite"));
    return userAdminController.userDetails(user.id, model);
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

    userService.changeUserCommunities(userId, communitiesIds);

    return userAdminController.userDetails(userId, model);
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
}
