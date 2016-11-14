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
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.view.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

  @Autowired
  private Services services;
  @Autowired
  MessageByLocaleService messageByLocaleService;

  @RequestMapping("/")
  public String index(Principal principal, Model model) {
    AuthentModel.addAuthentModelWithUserDetails(model, principal, services.user());

    model.addAttribute("title", messageByLocaleService.getMessage("app_name"));
    List<SignView> signsView = new ArrayList<>();


    if (AuthentModel.isAuthenticated(principal)) {
      User user = services.user().withUserName(principal.getName());
      if (user.firstName.isEmpty() && user.lastName.isEmpty() && user.job.isEmpty() && user.entity.isEmpty() && user.jobTextDescription.isEmpty() ){
        model.addAttribute("isUserEmpty", true);
      } else {
        model.addAttribute("isUserEmpty", false);
      }

      if (user.lastConnectionDate != null) {
        List<SignView> signsRecentView, signNotRecentView;
        signsRecentView = SignView.fromRecent(services.sign().createAfterLastDateConnection(user.lastConnectionDate));
        signNotRecentView = SignView.from(services.sign().createBeforeLastDateConnection(user.lastConnectionDate));
        signsView.addAll(signsRecentView);
        signsView.addAll(signNotRecentView);
      }
      else {
        signsView = SignView.from(services.sign().all());
      }
    } else {
        signsView = SignView.from(services.sign().all());
    }

    model.addAttribute("signs", signsView);
    model.addAttribute("signCreationView", new SignCreationView());
    if (AuthentModel.isAuthenticated(principal)) {
      fillModelWithFavorites(model, principal);
    }
    model.addAttribute("favoriteCreationView", new FavoriteCreationView());
    model.addAttribute("signSearchView", new SignSearchView());

    return "index";
  }

  @RequestMapping("/search")
  public String search(@ModelAttribute SignCreationView signCreationView, Principal principal, Model model) {
    AuthentModel.addAuthentModelWithUserDetails(model, principal, services.user());

    model.addAttribute("title", messageByLocaleService.getMessage("app_name"));
    List<SignView> signsView = new ArrayList<>();

    if (AuthentModel.isAuthenticated(principal)) {
      User user = services.user().withUserName(principal.getName());
      if (user.lastConnectionDate != null) {
        List<SignView> signsRecentView, signNotRecentView;
        signsRecentView = SignView.fromRecent(services.sign().createAfterLastDateConnectionBySearchTerm(user.lastConnectionDate, signCreationView.getSignName() ));
        signNotRecentView = SignView.from(services.sign().createBeforeLastDateConnectionBySearchTerm(user.lastConnectionDate, signCreationView.getSignName()));
        signsView.addAll(signsRecentView);
        signsView.addAll(signNotRecentView);
      }
      else {
        signsView = SignView.from(services.sign().allBySearchTerm(signCreationView.getSignName()));
      }
    } else {
      signsView = SignView.from(services.sign().allBySearchTerm(signCreationView.getSignName()));
    }

    model.addAttribute("signs", signsView);
    if (AuthentModel.isAuthenticated(principal)) {
      fillModelWithFavorites(model, principal);
    }
    model.addAttribute("favoriteCreationView", new FavoriteCreationView());
    model.addAttribute("signSearchView", signCreationView);

    return "index";
  }

  private void fillModelWithFavorites(Model model, Principal principal) {
    User user = services.user().withUserName(principal.getName());
    List<FavoriteView> myFavorites = FavoriteView.from(services.favorite().favoritesforUser(user.id));
    model.addAttribute("myFavorites", myFavorites);
  }
}
