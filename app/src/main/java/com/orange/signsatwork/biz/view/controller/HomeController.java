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

import com.orange.signsatwork.AppProfile;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.persistence.model.SignViewData;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.security.AppSecurityAdmin;
import com.orange.signsatwork.biz.view.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class HomeController {

  @Autowired
  private AppSecurityAdmin appSecurityAdmin;
  @Autowired
  private AppProfile appProfile;
  @Autowired
  private Services services;
  @Autowired
  MessageByLocaleService messageByLocaleService;

  @RequestMapping("/")
  public String index(Principal principal, Model model) {
    long t0 = System.currentTimeMillis();
    String pageName = doIndex(principal, model);
    long dt = System.currentTimeMillis() - t0;
    log.info("[PERF] took " + dt + " ms to process root page request");
    return pageName;
  }

  private String doIndex(Principal principal, Model model) {
    boolean admin = appSecurityAdmin.isAdmin(principal);
    User user = AuthentModel.addAuthentModelWithUserDetails(model, principal, admin, services.user());

    model.addAttribute("title", messageByLocaleService.getMessage("app_name"));


    if (AuthentModel.isAuthenticated(principal)) {
      if (user.firstName.isEmpty() && user.lastName.isEmpty() && user.job.isEmpty() && user.entity.isEmpty() && user.jobTextDescription.isEmpty() ){
        model.addAttribute("isUserEmpty", true);
      } else {
        model.addAttribute("isUserEmpty", false);
      }
    }



    List<Object[]> querySigns = services.sign().SignsForSignsView();
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithCommentList = Arrays.asList(services.sign().lowCommented());

    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<SignView2> signViews = signViewsData.stream()
      .map(signViewData -> new SignView2(
          signViewData,
          signWithCommentList.contains(signViewData.id),
          SignView2.createdAfterLastDeconnection(signViewData.createDate, user == null ? null : user.lastDeconnectionDate),
          signWithView.contains(signViewData.id))
      )
      .collect(Collectors.toList());

    SignsViewSort2 signsViewSort2 = new SignsViewSort2();
    signViews = signsViewSort2.sort(signViews);


    model.addAttribute("signsView", signViews);
    model.addAttribute("signCreationView", new SignCreationView());
    if (AuthentModel.isAuthenticated(principal)) {
      fillModelWithFavorites(model, user);
    }
    model.addAttribute("favoriteCreationView", new FavoriteCreationView());
    model.addAttribute("signSearchView", new SignSearchView());
    model.addAttribute("isDevProfile", appProfile.isDevProfile());

    return "index";
  }


  @RequestMapping("/search")
  public String search(@ModelAttribute SignCreationView signCreationView, Principal principal, Model model) {
    boolean admin = appSecurityAdmin.isAdmin(principal);
    User user =  AuthentModel.addAuthentModelWithUserDetails(model, principal, admin, services.user());

    model.addAttribute("title", messageByLocaleService.getMessage("app_name"));

    if (AuthentModel.isAuthenticated(principal)) {
      if (user.firstName.isEmpty() && user.lastName.isEmpty() && user.job.isEmpty() && user.entity.isEmpty() && user.jobTextDescription.isEmpty() ){
        model.addAttribute("isUserEmpty", true);
      } else {
        model.addAttribute("isUserEmpty", false);
      }
    }

    List<Object[]> querySigns = services.sign().SignsForSignsViewBySearchTerm(signCreationView.getSignName());
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithCommentList = Arrays.asList(services.sign().lowCommented());

    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<SignView2> signViews = signViewsData.stream()
      .map(signViewData -> new SignView2(
        signViewData,
        signWithCommentList.contains(signViewData.id),
        SignView2.createdAfterLastDeconnection(signViewData.createDate, user == null ? null : user.lastDeconnectionDate),
        signWithView.contains(signViewData.id))
      )
      .collect(Collectors.toList());

    SignsViewSort2 signsViewSort2 = new SignsViewSort2();
    signViews = signsViewSort2.sort(signViews);

    model.addAttribute("signsView", signViews);
    if (AuthentModel.isAuthenticated(principal)) {
      fillModelWithFavorites(model, user);
    }
    model.addAttribute("favoriteCreationView", new FavoriteCreationView());
    model.addAttribute("signSearchView", signCreationView);
    model.addAttribute("isDevProfile", appProfile.isDevProfile());

    return "index";
  }

  private void fillModelWithFavorites(Model model, User user) {
    List<FavoriteModalView> myFavorites = FavoriteModalView.from(services.favorite().favoritesforUser(user.id));
    model.addAttribute("myFavorites", myFavorites);
  }
}
