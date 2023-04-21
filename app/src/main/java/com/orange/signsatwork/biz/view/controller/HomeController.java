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
import com.orange.signsatwork.biz.domain.Article;
import com.orange.signsatwork.biz.domain.ArticleType;
import com.orange.signsatwork.biz.domain.Articles;
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
import java.util.Locale;
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

  @Value("${app.name}")
  String appName;

  @Value("${app.manifest}")
  private String manifest;
  @Value("${app.version}")
  String appVersion;

  @Value("${app.contact.support}")
  String appContactSupport;

  @Autowired
  public EmailServiceImpl emailService;

  @RequestMapping("/")
  public String index(HttpServletRequest req, Principal principal, Model model) {
    long t0 = System.currentTimeMillis();
    String pageName;
    List<Object[]> querySigns;

    model.addAttribute("title", appName);
    model.addAttribute("appName", appName);
    if (AuthentModel.isAuthenticated(principal)) {
      pageName = doIndex(req, principal, model);
    } else {
      querySigns = services.sign().mostRecentWithoutDate();
      if (!querySigns.isEmpty()) {
        pageName="redirect:/signs/mostrecent?isMostRecent=false&isSearch=false";
      } else {
        pageName = "login";
      }
    }

    long dt = System.currentTimeMillis() - t0;
    log.info("[PERF] took " + dt + " ms to process root page request");
    return pageName;
  }

  private String doIndex(HttpServletRequest req, Principal principal, Model model) {
    boolean admin = appSecurityAdmin.isAdmin(principal);

    AuthentModel.addAuthentModelWithUserDetails(model, principal, admin, services.user());

    model.addAttribute("isDevProfile", appProfile.isDevProfile());
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("display_url", display_url);
    model.addAttribute("manifest", manifest);
    model.addAttribute("appName", appName);


    return "index";
  }




  @RequestMapping("/cgu")
  public String cgu(Model model, HttpServletRequest request) {

    model.addAttribute("title", messageByLocaleService.getMessage("condition_of_use"));
    model.addAttribute("backUrl", HOME_URL);
    /*model.addAttribute("cgu_url", cgu_url);*/
    model.addAttribute("user", new UserCreationView());
    model.addAttribute("appName", appName);
    Locale locale = request.getLocale();
    String language = locale.getLanguage();

    Articles articlesCgu = services.article().findByLanguageAndType(language, ArticleType.Cgu);
    model.addAttribute("cguArticles", articlesCgu.list());
    Articles articlesPersonalData = services.article().findByLanguageAndType(language, ArticleType.PersonalData);
    model.addAttribute("personalDataArticles", articlesPersonalData.list());

    return "cgu";
  }

  @RequestMapping("/sec/about")
  public String about(Model model) {

    model.addAttribute("title", messageByLocaleService.getMessage("about"));
    model.addAttribute("appVersion", appVersion);
    model.addAttribute("appName", appName);
    model.addAttribute("appContactSupport", appContactSupport);
    model.addAttribute("appName", appName);

    return "about";
  }

  @RequestMapping("/sec/about-cgu")
  public String aboutCgu(Model model, HttpServletRequest request) {

    model.addAttribute("title", messageByLocaleService.getMessage("about.cgu"));
    /*model.addAttribute("cgu_url", cgu_url);*/
    model.addAttribute("appName", appName);
    Locale locale = request.getLocale();
    String language = locale.getLanguage();

    Articles articles = services.article().findByLanguageAndType(language, ArticleType.Cgu);
    model.addAttribute("cguArticles", articles.list());
    return "about-cgu";
  }

  @RequestMapping("/about-cgu-lsf")
  public String aboutCguLsf(Model model, HttpServletRequest request) {

    model.addAttribute("title", messageByLocaleService.getMessage("see.condition_of_use"));
    model.addAttribute("appName", appName);
    Locale locale = request.getLocale();
    String language = locale.getLanguage();

    Articles articles = services.article().findByLanguageAndType(language, ArticleType.Cgu);
    List<Article> articlesWithLsf = articles.stream().filter(a -> a.descriptionPicture != null).collect(Collectors.toList());
    model.addAttribute("cguArticles", articlesWithLsf);
    return "about-cgu-lsf";
  }

  @RequestMapping("/sec/personal-data")
  public String personalData(Model model, HttpServletRequest request) {

    model.addAttribute("title", messageByLocaleService.getMessage("read.personnal_data_modal_title"));
    model.addAttribute("appName", appName);
    Locale locale = request.getLocale();
    String language = locale.getLanguage();

    Articles articles = services.article().findByLanguageAndType(language, ArticleType.PersonalData);
    model.addAttribute("personalDataArticles", articles.list());

    return "personal-data";
  }

  @RequestMapping("/personal-data-lsf")
  public String personalDataLsf(Model model, HttpServletRequest request) {

    model.addAttribute("title", messageByLocaleService.getMessage("see.personal_data_orange"));
    model.addAttribute("appName", appName);
    Locale locale = request.getLocale();
    String language = locale.getLanguage();

    Articles articles = services.article().findByLanguageAndType(language, ArticleType.PersonalData);
    List<Article> articlesWithLsf = articles.stream().filter(a -> a.descriptionPicture != null).collect(Collectors.toList());
    model.addAttribute("personalDataArticles", articlesWithLsf);
    return "personal-data-lsf";
  }
}
