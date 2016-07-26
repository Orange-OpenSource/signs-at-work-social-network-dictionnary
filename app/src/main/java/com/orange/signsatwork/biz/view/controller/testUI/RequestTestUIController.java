package com.orange.signsatwork.biz.view.controller.testUI;

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

import com.orange.signsatwork.biz.domain.Request;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.RequestService;
import com.orange.signsatwork.biz.persistence.service.SignService;
import com.orange.signsatwork.biz.view.model.AuthentModel;
import com.orange.signsatwork.biz.view.model.RequestProfileView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

@Controller
public class RequestTestUIController {

  @Autowired
  private RequestService requestService;
  @Autowired
  private SignService signService;
  @Autowired
  MessageByLocaleService messageByLocaleService;

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/testUI/request/{id}")
  public String requestDetails(@PathVariable long id, Model model) {
    Request request = requestService.withId(id);

    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("request_details"));
    RequestProfileView requestProfileView = new RequestProfileView(request, signService);
    model.addAttribute("requestProfileView", requestProfileView);

    return "testUI/request";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/testUI/request/{requestId}/add/sign", method = RequestMethod.POST)
  public String changeSignRequest(
          HttpServletRequest req, @PathVariable long requestId, Model model) {

    Long signId = Long.parseLong(req.getParameter("requestSignId"));

    requestService.changeSignRequest(requestId, signId);

    return requestDetails(requestId, model);
  }
}
