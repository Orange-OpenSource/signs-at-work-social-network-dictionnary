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
import com.orange.signsatwork.biz.view.model.AuthentModel;
import com.orange.signsatwork.biz.view.model.RequestView;
import com.orange.signsatwork.biz.view.model.SignCreationView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
public class SuggestController {

  private static final String HOME_URL = "/";
  private static final String REQUEST_URL = "/sec/suggest";

  @Autowired
  private Services services;

  @Autowired
  MessageByLocaleService messageByLocaleService;

  @RequestMapping(value = REQUEST_URL)
  public String signs(Principal principal, Model model) {
    fillModelWithContext(model, "sign.suggest", principal, HOME_URL);
    fillModelWithRequests(model, principal);
    model.addAttribute("signCreationView", new SignCreationView());

    return "suggest";
  }


  private void fillModelWithContext(Model model, String messageEntry, Principal principal, String backUrl) {
    model.addAttribute("title", messageByLocaleService.getMessage(messageEntry));
    model.addAttribute("backUrl", backUrl);
    AuthentModel.addAuthenticatedModel(model, AuthentModel.isAuthenticated(principal));
  }

  private void fillModelWithRequests(Model model, Principal principal) {
    User user = services.user().withUserName(principal.getName());

    List<RequestView> otherrequestsViewWithoutSignAssociate = RequestView.from(services.request().requestsforOtherUserWithoutSignAssociate(user.id));
    model.addAttribute("otherRequestsWithoutSignAssociate", otherrequestsViewWithoutSignAssociate);

  }

}
