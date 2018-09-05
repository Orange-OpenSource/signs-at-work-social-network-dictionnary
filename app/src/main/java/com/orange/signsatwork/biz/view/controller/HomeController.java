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
import com.orange.signsatwork.biz.persistence.service.impl.EmailServiceImpl;
import com.orange.signsatwork.biz.security.AppSecurityAdmin;
import com.orange.signsatwork.biz.view.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
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


  private static final String HOME_URL = "/";

  @Value("${cgu-url}")
  private String cgu_url;

  @Value("${display-url}")
  private String display_url;

  @Autowired
  public EmailServiceImpl emailService;

  @RequestMapping("/")
  public String index(HttpServletRequest req, Principal principal, Model model) {
    long t0 = System.currentTimeMillis();
    String pageName;
    if (AuthentModel.isAuthenticated(principal)) {
      pageName = doIndex(req, principal, model);
    } else {
      pageName = "login";
    }

    long dt = System.currentTimeMillis() - t0;
    log.info("[PERF] took " + dt + " ms to process root page request");
    return pageName;
  }

  private String doIndex(HttpServletRequest req, Principal principal, Model model) {
    boolean admin = appSecurityAdmin.isAdmin(principal);
    User user = AuthentModel.addAuthentModelWithUserDetails(model, principal, admin, services.user());
    StringBuffer location = req.getRequestURL();

    model.addAttribute("title", messageByLocaleService.getMessage("app_name"));
    if(user != null) {
      model.addAttribute("mail_body", messageByLocaleService.getMessage("share_application_body", new Object[]{user.name(), location}));
    }

    List<Long> signInFavorite = null;

    List<Object[]> querySigns = services.sign().SignsForSignsView();
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithCommentList = Arrays.asList(services.sign().mostCommented());

    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<Long> signWithPositiveRate = Arrays.asList(services.sign().mostRating());
    List<SignView2> signViews;

    if (user != null) {
      signInFavorite = Arrays.asList(services.sign().SignsBellowToFavoriteByUser(user.id));
      List<Long> finalSignInFavorite = signInFavorite;
      signViews = signViewsData.stream()
        .map(signViewData -> new SignView2(
          signViewData,
          signWithCommentList.contains(signViewData.id),
          SignView2.createdAfterLastDeconnection(signViewData.createDate, user == null ? null : user.lastDeconnectionDate),
          signWithView.contains(signViewData.id),
          signWithPositiveRate.contains(signViewData.id),
          finalSignInFavorite.contains(signViewData.id))
        )
        .collect(Collectors.toList());
    } else {
      signViews = signViewsData.stream()
        .map(signViewData -> new SignView2(
          signViewData,
          signWithCommentList.contains(signViewData.id),
          SignView2.createdAfterLastDeconnection(signViewData.createDate, user == null ? null : user.lastDeconnectionDate),
          signWithView.contains(signViewData.id),
          signWithPositiveRate.contains(signViewData.id),
          false)
        )
        .collect(Collectors.toList());
    }



    SignsViewSort2 signsViewSort2 = new SignsViewSort2();
    signViews = signsViewSort2.sort(signViews, true);

    List<SignView2> createdSinceLastDeconnection = signViews.stream()
      .filter(SignView2::createdSinceLastDeconnection)
      .collect(Collectors.toList());

    model.addAttribute("nbRecentSign", createdSinceLastDeconnection.size());
//    model.addAttribute("signsView", signViews);
//    model.addAttribute("signCreationView", new SignCreationView());
    if (AuthentModel.isAuthenticated(principal)) {
      fillModelWithFavorites(model, user);
    }
    model.addAttribute("favoriteCreationView", new FavoriteCreationView());
//    model.addAttribute("signSearchView", new SignSearchView());
    model.addAttribute("isDevProfile", appProfile.isDevProfile());
    model.addAttribute("isAlphabeticAsc", true);
    model.addAttribute("isSearch", true);
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("display_url", display_url);


    return "index";
  }



  private void fillModelWithFavorites(Model model, User user) {
    List<FavoriteModalView> myFavorites = FavoriteModalView.from(services.favorite().favoritesforUser(user.id));
    model.addAttribute("myFavorites", myFavorites);
  }

  @RequestMapping("/cgu")
  public String cgu(Model model) {

    model.addAttribute("title", messageByLocaleService.getMessage("condition_of_use"));
    model.addAttribute("backUrl", HOME_URL);
    model.addAttribute("cgu_url", cgu_url);
    model.addAttribute("user", new UserCreationView());
    return "cgu";
  }

  @RequestMapping("/sendMail")
  public String sendMail(@ModelAttribute UserCreationView userCreationView) {

    User admin = services.user().getAdmin();

    String body = messageByLocaleService.getMessage("ask_to_create_user_text", new Object[]{userCreationView.getLastName(), userCreationView.getFirstName(), userCreationView.getEntity(),  userCreationView.getEmail(), userCreationView.getUsername(), userCreationView.getPassword()});

    emailService.sendSimpleMessage(admin.email.split(""), messageByLocaleService.getMessage("ask_to_create_user_title"), body );

    return "redirect:/";
  }
}
