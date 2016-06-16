package com.orange.spring.demo.biz.view.controller;

/*
 * #%L
 * Spring demo
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

import com.orange.spring.demo.biz.domain.Community;
import com.orange.spring.demo.biz.domain.User;
import com.orange.spring.demo.biz.persistence.service.CommunityService;
import com.orange.spring.demo.biz.persistence.service.MessageByLocaleService;
import com.orange.spring.demo.biz.persistence.service.impl.UserServiceImpl;
import com.orange.spring.demo.biz.security.AppSecurityAdmin;
import com.orange.spring.demo.biz.view.model.CommunityView;
import com.orange.spring.demo.biz.view.model.UserView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.Principal;

@Controller
public class HomeController {

  @Autowired
  private UserServiceImpl userService;
  @Autowired
  private CommunityService communityService;
  @Autowired
  MessageByLocaleService messageByLocaleService;

  @RequestMapping("/")
  public String index(Principal principal, Model model) {
    setAuthenticated(principal, model);
    model.addAttribute("title", messageByLocaleService.getMessage("welcome"));
    return "index";
  }

  @Secured("ROLE_USER")
  @RequestMapping("/users")
  public String users(Model model) {

    setAuthenticated(true, model);
    model.addAttribute("title", messageByLocaleService.getMessage("users"));
    model.addAttribute("users", UserView.from(userService.all()));
    return "users";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping("/communities")
  public String communities(Model model) {

    setAuthenticated(true, model);
    model.addAttribute("title", messageByLocaleService.getMessage("communities"));
    model.addAttribute("communities", CommunityView.from(communityService.all()));
    return "communities";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/user/{id}")
  public String user(@PathVariable long id, Model model) {

    User user = userService.withId(id);
    setAuthenticated(true, model);
    model.addAttribute("title", messageByLocaleService.getMessage("user_details"));
    model.addAttribute("user", user);
    return "user";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/community/{id}")
  public String community(@PathVariable long id, Model model) {

    Community community = communityService.withId(id);
    setAuthenticated(true, model);
    model.addAttribute("title", messageByLocaleService.getMessage("community_details"));
    model.addAttribute("community", community);
    return "community";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping("/admin")
  public String admin(Model model) {
    setAuthenticated(true, model);
    model.addAttribute("title", messageByLocaleService.getMessage("admin_page"));
    // for thymeleaf form management
    model.addAttribute("user", new UserView());
    model.addAttribute("community", new CommunityView());
    return "admin";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/admin/user/create", method = RequestMethod.POST)
  public String user(@ModelAttribute UserView userView, Model model) {
    User user = userService.create(userView.toUser(), userView.getPassword());
    return user(user.id, model);
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/admin/community/create", method = RequestMethod.POST)
  public String community(@ModelAttribute CommunityView communityView, Model model) {
    Community community = communityService.create(communityView.toCommunity());
    return  community(community.id, model);

  }


  private void setAuthenticated(Principal principal, Model model) {
    boolean authenticated = principal != null && principal.getName() != null;
    setAuthenticated(authenticated, model);
    model.addAttribute("authenticatedUsername",
            authenticated ? principal.getName() : "Please sign in");
    model.addAttribute("isAdmin", authenticated && isAdmin(principal));
  }

  private void setAuthenticated(boolean isAuthenticated, Model model) {
    model.addAttribute("isAuthenticated", isAuthenticated);
  }

  private boolean isAdmin(Principal principal) {
    return AppSecurityAdmin.isAdmin(principal.getName());
  }
}
