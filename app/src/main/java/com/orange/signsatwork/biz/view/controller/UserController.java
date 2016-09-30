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
import com.orange.signsatwork.biz.persistence.service.*;
import com.orange.signsatwork.biz.view.model.FavoriteCreationView;
import com.orange.signsatwork.biz.view.model.FavoriteView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class UserController {

  @Autowired
  private Services services;
  @Autowired
  MessageByLocaleService messageByLocaleService;

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/profile")
  public String userDetails(Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());
    model.addAttribute("title", messageByLocaleService.getMessage("profile"));
    model.addAttribute("user", user);
    fillModelWithFavorites(model, principal);
    model.addAttribute("favoriteCreationView", new FavoriteCreationView());

    return "profile";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/job-detail")
  public String jobDetails(Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());
    model.addAttribute("title", messageByLocaleService.getMessage("job"));
    model.addAttribute("user", user);
    model.addAttribute("backUrl", "/sec/profile");

    return "job-detail";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/activity-detail")
  public String activityDetails(Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());
    model.addAttribute("title", messageByLocaleService.getMessage("activity"));
    model.addAttribute("user", user);
    model.addAttribute("backUrl", "/sec/profile");

    return "activity-detail";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/profile-from-community/{communityId}/{userId}")
  public String userDetails(@PathVariable long userId, @PathVariable long communityId, Principal principal, Model model) {
    User user = services.user().withId(userId);
    user = user.loadVideos();
    model.addAttribute("title", user.firstName + ' ' + user.lastName);
    model.addAttribute("backUrl", "/sec/community/"+communityId);
    model.addAttribute("user", user);

    return "profile-from-community";
  }

  private void fillModelWithFavorites(Model model, Principal principal) {
    User user = services.user().withUserName(principal.getName());
    List<FavoriteView> myFavorites = FavoriteView.from(services.favorite().favoritesforUser(user.id));
    model.addAttribute("myFavorites", myFavorites);
  }
}
