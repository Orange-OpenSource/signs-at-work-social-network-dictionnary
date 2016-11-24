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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.*;

@Controller
@Slf4j
public class HomeController {

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
    AuthentModel.addAuthentModelWithUserDetails(model, principal, services.user());

    model.addAttribute("title", messageByLocaleService.getMessage("app_name"));
    //List<SignView> signsView = new ArrayList<>();
    List<SignHomeView> signsHomeView = new ArrayList<>();

    if (AuthentModel.isAuthenticated(principal)) {
      User user = services.user().withUserName(principal.getName());
      if (user.firstName.isEmpty() && user.lastName.isEmpty() && user.job.isEmpty() && user.entity.isEmpty() && user.jobTextDescription.isEmpty() ){
        model.addAttribute("isUserEmpty", true);
      } else {
        model.addAttribute("isUserEmpty", false);
      }

      if (user.lastConnectionDate != null) {

        signsHomeView = SignHomeView.from(services.sign().allOrderByCreateDateAsc(), services, user.lastConnectionDate);

//        List<SignView> signsRecentView, signNotRecentView;
//        signsRecentView = SignView.fromRecent(services.sign().createAfterLastDateConnection(user.lastConnectionDate));
//        signNotRecentView = SignView.from(services.sign().createBeforeLastDateConnection(user.lastConnectionDate));
//        signsView.addAll(signsRecentView);
//        signsView.addAll(signNotRecentView);
      }
      else {
        //signsView = SignView.from(services.sign().all());
        signsHomeView = SignHomeView.from(services.sign().allOrderByCreateDateAsc(), services, null);
      }
    } else {
      //signsView = SignView.from(services.sign().all());
      signsHomeView = SignHomeView.from(services.sign().allOrderByCreateDateAsc(), services, null);
    }

    Collections.sort(signsHomeView, new Comparator<SignHomeView>() {
      @Override
      public int compare(SignHomeView signHomeView1, SignHomeView signHomeView2) {
        boolean signCreateAfterLastDateConnection1 = signHomeView1.isSignCreateAfterLastDateConnection();
        boolean videoHasComment1 = signHomeView1.isVideoHasComment();
        boolean signCreateAfterLastDateConnection2 = signHomeView2.isSignCreateAfterLastDateConnection();
        boolean videoHasComment2 = signHomeView2.isVideoHasComment();

        if (signCreateAfterLastDateConnection1 == true && videoHasComment1 == true) {
          if (signCreateAfterLastDateConnection2 == true && videoHasComment2 == true) {
            return +1;
          } else if (signCreateAfterLastDateConnection2 == true && videoHasComment2 == false) {
            return -1;
          } else if (signCreateAfterLastDateConnection2 == false && videoHasComment2 == false) {
            return -1;
          } else if (signCreateAfterLastDateConnection2 == false && videoHasComment2 == true) {
            return -1;
          }
        } else if (signCreateAfterLastDateConnection1 == true && videoHasComment1 == false) {
          if (signCreateAfterLastDateConnection2 == true && videoHasComment2 == true) {
            return -1;
          } else if (signCreateAfterLastDateConnection2 == true && videoHasComment2 == false) {
            return +1;
          } else if (signCreateAfterLastDateConnection2 == false && videoHasComment2 == false) {
            return -1;
          } else if (signCreateAfterLastDateConnection2 == false && videoHasComment2 == true) {
            return -1;
          }
        } else if (signCreateAfterLastDateConnection1 == false && videoHasComment1 == false) {
          if (signCreateAfterLastDateConnection2 == true && videoHasComment2 == true) {
            return +1;
          } else if (signCreateAfterLastDateConnection2 == true && videoHasComment2 == false) {
            return +1;
          } else if (signCreateAfterLastDateConnection2 == false && videoHasComment2 == false) {
            return +1;
          } else if (signCreateAfterLastDateConnection2 == false && videoHasComment2 == true) {
            return +1;
          }
        } else if (signCreateAfterLastDateConnection1 == false && videoHasComment1 == true) {
            if (signCreateAfterLastDateConnection2 == true && videoHasComment2 == true) {
              return +1;
            } else if (signCreateAfterLastDateConnection2 == true && videoHasComment2 == false) {
              return +1;
            } else if (signCreateAfterLastDateConnection2 == false && videoHasComment2 == false) {
              return -1;
            } else if (signCreateAfterLastDateConnection2 == false && videoHasComment2 == true) {
              return +1;
            }
          }
          return 0;
      }

    });

    //model.addAttribute("signs", signsView);
    model.addAttribute("signsHomeView", signsHomeView);
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
