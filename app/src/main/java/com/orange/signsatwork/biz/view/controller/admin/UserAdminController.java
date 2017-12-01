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
import com.orange.signsatwork.biz.persistence.service.CommunityService;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.UserService;
import com.orange.signsatwork.biz.view.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserAdminController {

  @Autowired
  private UserService userService;
  @Autowired
  private CommunityService communityService;
  @Autowired
  MessageByLocaleService messageByLocaleService;



  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/user/{id}")
  public String userDetails(@PathVariable long id, Model model) {
    User user = userService.withId(id);

    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("user_details"));

    UserProfileView userProfileView = new UserProfileView(user, communityService);
    model.addAttribute("userProfileView", userProfileView);

    model.addAttribute("requestView", new RequestView());
    model.addAttribute("favoriteView", new FavoriteModalView());
    model.addAttribute("signView", new SignView());
    model.addAttribute("userCreationView", new UserCreationView());

    return "admin/user";
  }


}
