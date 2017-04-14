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

import com.orange.signsatwork.biz.domain.*;
import com.orange.signsatwork.biz.persistence.model.RequestViewData;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.view.model.AuthentModel;
import com.orange.signsatwork.biz.view.model.RequestCreationView;
import com.orange.signsatwork.biz.view.model.RequestView;
import com.orange.signsatwork.biz.view.model.SignCreationView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static jdk.nashorn.internal.runtime.JSType.toLong;

@Slf4j
@Controller
public class RequestController {

  private static final String HOME_URL = "/";
  private static final String REQUEST_URL = "/sec/request";

  @Autowired
  private Services services;

  @Autowired
  MessageByLocaleService messageByLocaleService;

  @RequestMapping(value = REQUEST_URL)
  public String signs(Principal principal, Model model) {
    fillModelWithContext(model, "sign.request", principal, HOME_URL);
    fillModelWithRequests(model, principal);
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("signCreationView", new SignCreationView());

    return "request";
  }




  @RequestMapping(value = "/sec/my-request-detail/{requestId}")
  public String requestDetails(@PathVariable long requestId, Principal principal, Model model) {
    Request request = services.request().withId(requestId);
    model.addAttribute("title", request.name);
    model.addAttribute("backUrl", REQUEST_URL );
    AuthentModel.addAuthenticatedModel(model, AuthentModel.isAuthenticated(principal));
    RequestView requestView = RequestView.from(request);

    model.addAttribute("requestView", requestView);
    RequestCreationView requestCreationView = new RequestCreationView();
    requestCreationView.setRequestName(request.name);
    model.addAttribute("requestCreationView", requestCreationView);


    return "my-request-detail";
  }

  @RequestMapping(value = "/sec/other-request-detail/{requestId}")
  public String OtherRequestDetails(@PathVariable long requestId, Principal principal, Model model) {
    Request request = services.request().withId(requestId);
    model.addAttribute("title", request.name);
    model.addAttribute("backUrl", REQUEST_URL );
    AuthentModel.addAuthenticatedModel(model, AuthentModel.isAuthenticated(principal));
    RequestView requestView = RequestView.from(request);

    model.addAttribute("requestView", requestView);

    model.addAttribute("signCreationView", new SignCreationView());


    return "other-request-detail";
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/request/{requestId}/add/sign", method = RequestMethod.POST)
  public String changeSignRequest(
          javax.servlet.http.HttpServletRequest req, @PathVariable long requestId, Model model, @ModelAttribute SignCreationView signCreationView, Principal principal) {

    User user = services.user().withUserName(principal.getName());
    Sign sign = services.sign().create(user.id, signCreationView.getSignName(), signCreationView.getVideoUrl(), "");
    services.request().changeSignRequest(requestId, sign.id);
    log.info("createSign: username = {} / sign name = {} / video url = {} and associate to request = {} ", user.username, signCreationView.getSignName(), signCreationView.getVideoUrl(),requestId);

    return "redirect:/sign/" + sign.id;
  }

  private void fillModelWithContext(Model model, String messageEntry, Principal principal, String backUrl) {
    model.addAttribute("title", messageByLocaleService.getMessage(messageEntry));
    model.addAttribute("backUrl", backUrl);
    AuthentModel.addAuthenticatedModel(model, AuthentModel.isAuthenticated(principal));
  }

  private void fillModelWithRequests(Model model, Principal principal) {
    User user = services.user().withUserName(principal.getName());
    List<RequestView> myrequestsViewWithoutSignAssociate = RequestView.from(services.request().requestsforUserWithoutSignAssociate(user.id));
    model.addAttribute("myRequestsWithoutSignAssociate", myrequestsViewWithoutSignAssociate);

    List<RequestView> myrequestsViewWithSignAssociate = RequestView.from(services.request().requestsforUserWithSignAssociate(user.id));
    model.addAttribute("myRequestsWithSignAssociate", myrequestsViewWithSignAssociate);

    List<RequestView> otherrequestsViewWithoutSignAssociate = RequestView.from(services.request().requestsforOtherUserWithoutSignAssociate(user.id));
    model.addAttribute("otherRequestsWithoutSignAssociate", otherrequestsViewWithoutSignAssociate);

  }


  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/request/search")
  public String showSignsRequest(Model model, @ModelAttribute RequestCreationView requestCreationView, Principal principal) {
    String name = requestCreationView.getRequestName();
    model.addAttribute("backUrl", "/sec/request");
    model.addAttribute("title", messageByLocaleService.getMessage("sign.modal.request"));
    AuthentModel.addAuthenticatedModel(model, AuthentModel.isAuthenticated(principal));
    User user = services.user().withUserName(principal.getName());
    Signs signs = services.sign().search(name);


    model.addAttribute("signName", name);
    model.addAttribute("isSignAlreadyExist", false);
    List<Sign> signsWithSameName = new ArrayList<>();
    for (Sign sign: signs.list()) {
      if (sign.name.equals(name) ) {
        model.addAttribute("isSignAlreadyExist", true);
        model.addAttribute("signMatche", sign);
      } else {
        signsWithSameName.add(sign);
      }
    }

    model.addAttribute("signsWithSameName", signsWithSameName);

    model.addAttribute("isRequestAlreadyExist", false);
    List<Object[]> queryRequestsWithNoASsociateSign = services.request().requestsByNameWithNoAssociateSign(name, user.id);
    List<RequestViewData> requestViewDatasWithNoAssociateSign =  queryRequestsWithNoASsociateSign.stream()
      .map(objectArray -> new RequestViewData(objectArray))
      .collect(Collectors.toList());
    List<RequestViewData> requestsWithNoAssociateSignWithSameName = new ArrayList<>();
    for( RequestViewData requestViewData: requestViewDatasWithNoAssociateSign) {
      if (requestViewData.requestName.equals(name)) {
        model.addAttribute("isRequestAlreadyExist", true);
        model.addAttribute("requestMatche", requestViewData);
      } else {
        requestsWithNoAssociateSignWithSameName.add(requestViewData);
      }
    }

    model.addAttribute("requestsWithSameName", requestsWithNoAssociateSignWithSameName);

    model.addAttribute("isRequestWithAssociateSignAlreadyExist", false);
    List<Object[]> queryRequestsWithASsociateSign = services.request().requestsByNameWithAssociateSign(name, user.id);
    List<RequestViewData> requestViewDatasWithAssociateSign =  queryRequestsWithASsociateSign.stream()
      .map(objectArray -> new RequestViewData(objectArray))
      .collect(Collectors.toList());
    List<RequestViewData> requestsWithAssociateSignWithSameName = new ArrayList<>();
    for( RequestViewData requestViewData: requestViewDatasWithAssociateSign) {
      if (requestViewData.requestName.equals(name)) {
        model.addAttribute("isRequestWithAssociateSignAlreadyExist", true);
        model.addAttribute("requestWithAssociateSignMatche", requestViewData);
      } else {
        requestsWithAssociateSignWithSameName.add(requestViewData);
      }
    }

    model.addAttribute("requestsWithAssociateSignWithSameName", requestsWithAssociateSignWithSameName);


    model.addAttribute("requestCreationView", requestCreationView);
    model.addAttribute("requestView", new RequestView());

    return "signs-request";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/request/{requestId}/description", method = RequestMethod.POST)
  public String changeDescriptionnRequest(@PathVariable long requestId, @ModelAttribute RequestCreationView requestCreationView) {

    services.request().changeRequestTextDescription(requestId, requestCreationView.getRequestTextDescription());

    return "redirect:/sec/my-request-detail/" + requestId;
  }

}
