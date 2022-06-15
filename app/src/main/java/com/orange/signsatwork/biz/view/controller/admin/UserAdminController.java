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

import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.persistence.model.CommunityViewData;
import com.orange.signsatwork.biz.persistence.service.CommunityService;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.persistence.service.UserService;
import com.orange.signsatwork.biz.view.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UserAdminController {

  @Autowired
  MessageByLocaleService messageByLocaleService;
  @Autowired
  private Services services;

  @Value("${app.name}")
  String appName;

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/user/{id}")
  public String userDetails(@PathVariable long id, Model model) {
    User user = services.user().withId(id);

    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("user_details"));

    UserProfileView userProfileView = new UserProfileView(user, services.community());
    model.addAttribute("userProfileView", userProfileView);

    model.addAttribute("requestView", new RequestView());
    model.addAttribute("favoriteView", new FavoriteModalView());
    model.addAttribute("signView", new SignView());
    model.addAttribute("userCreationView", new UserCreationView());
    model.addAttribute("appName", appName);
    return "admin/user";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/users/{id}/job")
  public String userJob(@PathVariable long id, Model model) {
    User user = services.user().withId(id);

    model.addAttribute("title", messageByLocaleService.getMessage("admin_job_title"));
    UserJobView userJobView = new UserJobView(user, services.community());
    model.addAttribute("userJobView", userJobView);

    model.addAttribute("user", user);
    List<Object[]> queryCommunities = services.community().allForJob(user.id);
    List<CommunityViewData> communitiesViewData = queryCommunities.stream()
      .map(objectArray -> new CommunityViewData(objectArray))
      .collect(Collectors.toList());
    communitiesViewData = communitiesViewData.stream().sorted((c1, c2) -> c1.name.compareTo(c2.name)).collect(Collectors.toList());
    model.addAttribute("communities", communitiesViewData);
    model.addAttribute("appName", appName);
    model.addAttribute("isAdmin", true);
    model.addAttribute("action", "/ws/sec/users/" + user.id + "/datas");

    return "job-community";
  }

}
