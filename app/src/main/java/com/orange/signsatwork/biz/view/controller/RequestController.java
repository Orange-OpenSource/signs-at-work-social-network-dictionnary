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
import com.orange.signsatwork.biz.persistence.model.SignViewData;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.security.AppSecurityAdmin;
import com.orange.signsatwork.biz.view.model.AuthentModel;
import com.orange.signsatwork.biz.view.model.RequestCreationView;
import com.orange.signsatwork.biz.view.model.RequestView;
import com.orange.signsatwork.biz.view.model.SignCreationView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Principal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Controller
public class RequestController {


  @Autowired
  private AppSecurityAdmin appSecurityAdmin;
  @Autowired
  private Services services;

  @Autowired
  MessageByLocaleService messageByLocaleService;
  @Value("${app.name}")
  String appName;

  @Value("${app.admin.username}")
  String adminUsername;

  @RequestMapping(value = "/sec/requests")
  public String requests(Principal principal, Model model) {

    fillModelWithContext(model, "sign.requests", principal);
    fillModelWithRequests(model, principal);
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("appName", appName);
    return "requests";
  }

  private boolean isIOSDevice(String userAgent) {
    boolean isIOSDevice = false;
    String osType = "Unknown";
    String osVersion = "Unknown";
    String deviceType = "Unknown";

    if (userAgent.indexOf("Mac OS") >= 0) {
      osType = "Mac";
      osVersion = userAgent.substring(userAgent.indexOf("Mac OS ") + 7, userAgent.indexOf(")"));

      if (userAgent.indexOf("iPhone") >= 0) {
        deviceType = "iPhone";
        isIOSDevice = true;
      } else if (userAgent.indexOf("iPad") >= 0) {
        deviceType = "iPad";
        isIOSDevice = true;
      }
    }
    return isIOSDevice;
  }


  @RequestMapping(value = "/sec/my-request-detail/{requestId}")
  public String requestDetails(@PathVariable long requestId, HttpServletRequest  requestHttp, Principal principal, Model model) {
    Request request = services.request().withId(requestId);
    if (request == null) {
      return "redirect:/sec/my-requests/alphabetic?isAlphabetic=false&isSearch=false";
    }

    String userAgent = requestHttp.getHeader("User-Agent");

    model.addAttribute("isIOSDevice", isIOSDevice(userAgent));

    model.addAttribute("title", request.name);

    AuthentModel.addAuthenticatedModel(model, AuthentModel.isAuthenticated(principal));
    RequestView requestView = RequestView.from(request);

    model.addAttribute("requestView", requestView);
    RequestCreationView requestCreationView = new RequestCreationView();
    requestCreationView.setRequestName(request.name);
    model.addAttribute("requestCreationView", requestCreationView);
    model.addAttribute("appName", appName);

    return "my-request-detail";
  }

  @RequestMapping(value = "/sec/other-request-detail/{requestId}")
  public String OtherRequestDetails(@PathVariable long requestId, Principal principal, Model model) {
    Request request = services.request().withId(requestId);

    model.addAttribute("title", request.name);

    AuthentModel.addAuthenticatedModel(model, AuthentModel.isAuthenticated(principal));
    RequestView requestView = RequestView.from(request);

    model.addAttribute("requestView", requestView);

    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("appName", appName);

    return "other-request-detail";
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/request/{requestId}/add/sign", method = RequestMethod.POST)
  public String changeSignRequest(@PathVariable long requestId, Model model, @ModelAttribute SignCreationView signCreationView, Principal principal) {

    User user = services.user().withUserName(principal.getName());
    signCreationView.clearXss();
    Sign sign = services.sign().create(user.id, signCreationView.getSignName(), signCreationView.getVideoUrl(), "");
    services.request().changeSignRequest(requestId, sign.id);
    log.info("createSign: username = {} / sign name = {} / video url = {} and associate to request = {} ", user.username, signCreationView.getSignName(), signCreationView.getVideoUrl(),requestId);

    return "redirect:/sign/" + sign.id;
  }

  private void fillModelWithContext(Model model, String messageEntry, Principal principal) {
    model.addAttribute("title", messageByLocaleService.getMessage(messageEntry));
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
  public String searchRequestSign(@ModelAttribute RequestCreationView requestCreationView) {

    String name = requestCreationView.getRequestName();
    return "redirect:/sec/signs-request?name="+ URLEncoder.encode(name);
  }

  public static String stripAccents(String s)
  {
    s = Normalizer.normalize(s, Normalizer.Form.NFD);
    s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    return s;
  }

  @Secured("ROLE_USER_A")
  @RequestMapping(value = "/sec/signs-request")
  public String showSignsRequest(Model model,@RequestParam("name") String name,  HttpServletRequest request, Principal principal) {
    String decodeName = URLDecoder.decode(name);
    String userAgent = request.getHeader("User-Agent");

    model.addAttribute("isIOSDevice", isIOSDevice(userAgent));
    model.addAttribute("title", messageByLocaleService.getMessage("sign.modal.request"));
    AuthentModel.addAuthenticatedModel(model, AuthentModel.isAuthenticated(principal));
    User user = services.user().withUserName(principal.getName());
    List<Object[]> querySigns = services.sign().searchBis(decodeName.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE").toUpperCase());
    List<SignViewData> signViewData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());


    model.addAttribute("signName", decodeName.trim());
    model.addAttribute("isSignAlreadyExist", false);
    List<SignViewData> signsWithSameName = new ArrayList<>();
    for (SignViewData sign: signViewData) {
      if (sign.name.trim().replace("œ", "oe").replace("æ", "ae").equalsIgnoreCase(decodeName.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE"))) {
        model.addAttribute("isSignAlreadyExist", true);
        model.addAttribute("signMatche", sign);
      } else {
        signsWithSameName.add(sign);
      }
    }

    model.addAttribute("signsWithSameName", signsWithSameName);

    model.addAttribute("isRequestAlreadyExist", false);
    List<Object[]> queryRequestsWithNoASsociateSign = services.request().requestsByNameWithNoAssociateSign(decodeName.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE"), user.id);
    List<RequestViewData> requestViewDatasWithNoAssociateSign =  queryRequestsWithNoASsociateSign.stream()
      .map(objectArray -> new RequestViewData(objectArray))
      .collect(Collectors.toList());
    List<RequestViewData> requestsWithNoAssociateSignWithSameName = new ArrayList<>();
    for( RequestViewData requestViewData: requestViewDatasWithNoAssociateSign) {
      if (requestViewData.requestName.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE").equalsIgnoreCase(decodeName.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE"))) {
        model.addAttribute("isRequestAlreadyExist", true);
        model.addAttribute("requestMatche", requestViewData);
      } else {
        requestsWithNoAssociateSignWithSameName.add(requestViewData);
      }
    }

    model.addAttribute("requestsWithSameName", requestsWithNoAssociateSignWithSameName);

    model.addAttribute("isRequestWithAssociateSignAlreadyExist", false);
    List<Object[]> queryRequestsWithASsociateSign = services.request().requestsByNameWithAssociateSign(decodeName.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE"), user.id);
    List<RequestViewData> requestViewDatasWithAssociateSign =  queryRequestsWithASsociateSign.stream()
      .map(objectArray -> new RequestViewData(objectArray))
      .collect(Collectors.toList());
    List<RequestViewData> requestsWithAssociateSignWithSameName = new ArrayList<>();
    for( RequestViewData requestViewData: requestViewDatasWithAssociateSign) {
      if (requestViewData.requestName.trim().replace("œ", "oe").replace("æ", "ae").equalsIgnoreCase(decodeName.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE"))) {
        model.addAttribute("isRequestWithAssociateSignAlreadyExist", true);
        model.addAttribute("requestWithAssociateSignMatche", requestViewData);
      } else {
        requestsWithAssociateSignWithSameName.add(requestViewData);
      }
    }

    model.addAttribute("requestsWithAssociateSignWithSameName", requestsWithAssociateSignWithSameName);


    RequestCreationView requestCreationView = new RequestCreationView();
    requestCreationView.setRequestName(decodeName.trim());
    model.addAttribute("requestCreationView", requestCreationView);
    RequestView requestView = new RequestView();
    requestView.setName(decodeName.trim());
    model.addAttribute("requestView", requestView);
    model.addAttribute("appName", appName);
    return "signs-request";
  }

  @Secured({"ROLE_USER","ROLE_ADMIN"})
  @RequestMapping(value = "/sec/request/{requestId}/description", method = RequestMethod.POST)
  public String changeDescriptionRequest(@PathVariable long requestId, @ModelAttribute RequestCreationView requestCreationView, Principal principal, HttpServletRequest requestHttp) {
    String title = null, bodyMail = null, messageType = null;
    Boolean isAdmin = appSecurityAdmin.isAdmin(principal);
    requestCreationView.clearXss();
    Request request = services.request().withId(requestId);
    services.request().changeRequestTextDescription(requestId, requestCreationView.getRequestTextDescription());

    if (isAdmin) {
      String email = request.user.email;
      if (request.requestTextDescription != null && !request.requestTextDescription.isEmpty()) {
        title = messageByLocaleService.getMessage("update_request_description_text_title", new Object[]{request.name});
        bodyMail = messageByLocaleService.getMessage("update_request_description_text_body", new Object[]{request.name});
        messageType = "UpdateRequestDescriptionTextMessage";
      } else {
        title = messageByLocaleService.getMessage("add_request_description_text_title", new Object[]{request.name});
        bodyMail = messageByLocaleService.getMessage("add_request_description_text_body", new Object[]{request.name});
        messageType = "AddRequestDescriptionTextMessage";
      }
      if (!email.isEmpty()) {
        if (request.requestTextDescription != null && !request.requestTextDescription.isEmpty()) {
          messageType = "UpdateRequestDescriptionTextSendEmailMessage";
        } else {
          messageType = "AddRequestDescriptionTextSendEmailMessage";
        }
        final String finalTitle = title;
        final String finalBodyMail = bodyMail;
        final String finalMessageType = messageType;
        final String finalRequestName = request.name;
        String finalEmail = email;
        Runnable task = () -> {
          log.info("send mail email = {} / title = {} / body = {}", finalEmail, finalTitle, finalBodyMail);
          services.emailService().sendRequestDescriptionMessage(finalEmail, finalTitle, finalBodyMail, finalRequestName, finalMessageType, requestHttp.getLocale());
        };
        new Thread(task).start();
      } else {
        String values = adminUsername + ';' + request.name;
        MessageServer messageServer = new MessageServer(new Date(), messageType, values, ActionType.NO);
        services.messageServerService().addMessageServer(messageServer);
      }
      return "redirect:/sec/admin/request/" + requestId;
    } else {
      return "redirect:/sec/my-request-detail/" + requestId;
    }
  }

  @RequestMapping(value = "/sec/my-requests/mostrecent")
  public String myRequestsMostRecent(@RequestParam("isMostRecent") boolean isMostRecent, @RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.my-requests", principal);

    Requests queryRequests;
    if (isMostRecent == true) {
      queryRequests = services.request().myRequestlowRecent(user.id);
      model.addAttribute("isLowRecent", true);
      model.addAttribute("isMostRecent", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");
    } else {
      queryRequests = services.request().myRequestMostRecent(user.id);
      model.addAttribute("isMostRecent", true);
      model.addAttribute("isLowRecent", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }

    List<RequestView> myrequestsView = RequestView.from(queryRequests);

    model.addAttribute("myRequests", myrequestsView);

    model.addAttribute("isAll", false);
    model.addAttribute("isAlphabeticAsc", false);
    model.addAttribute("isAlphabeticDesc", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("most_recent_request"));
    model.addAttribute("classDropdownTitle", "  new_blue pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);
    return "my-requests";
  }

  @RequestMapping(value = "/sec/my-requests/mostrecent/frame")
  public String myRequestsMostRecentFrame(@RequestParam("isMostRecent") boolean isMostRecent, @RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.my-requests", principal);

    Requests queryRequests;
    if (isMostRecent == true) {
      queryRequests = services.request().myRequestlowRecent(user.id);
      model.addAttribute("isLowRecent", true);
      model.addAttribute("isMostRecent", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");
    } else {
      queryRequests = services.request().myRequestMostRecent(user.id);
      model.addAttribute("isMostRecent", true);
      model.addAttribute("isLowRecent", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }

    List<RequestView> myrequestsView = RequestView.from(queryRequests);

    model.addAttribute("myRequests", myrequestsView);

    model.addAttribute("isAll", false);
    model.addAttribute("isAlphabeticAsc", false);
    model.addAttribute("isAlphabeticDesc", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("most_recent_request"));
    model.addAttribute("classDropdownTitle", "  new_blue pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);
    return "fragments/frame-my-requests";
  }

  @RequestMapping(value = "/sec/my-requests/alphabetic")
  public String myRequestsInAlphabeticalOrder(@RequestParam("isAlphabeticAsc") boolean isAlphabeticAsc, @RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.my-requests", principal);

    Requests queryRequests;
    if (isAlphabeticAsc == true) {
      queryRequests = services.request().myRequestAlphabeticalOrderDesc(user.id);
      model.addAttribute("isAlphabeticDesc", true);
      model.addAttribute("isAlphabeticAsc", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");
    } else {
      queryRequests = services.request().myRequestAlphabeticalOrderAsc(user.id);
      model.addAttribute("isAlphabeticAsc", true);
      model.addAttribute("isAlphabeticDesc", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }

    List<RequestView> myrequestsView = RequestView.from(queryRequests);

    model.addAttribute("myRequests", myrequestsView);

    model.addAttribute("isAll", false);
    model.addAttribute("isMostRecent", false);
    model.addAttribute("isLowRecent", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("alphabetic"));
    model.addAttribute("classDropdownTitle", " sort_alpha_blue pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);
    return "my-requests";
  }

  @RequestMapping(value = "/sec/my-requests/alphabetic/frame")
  public String myRequestsInAlphabeticalOrderFrame(@RequestParam("isAlphabeticAsc") boolean isAlphabeticAsc, @RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.my-requests", principal);

    Requests queryRequests;
    if (isAlphabeticAsc == true) {
      queryRequests = services.request().myRequestAlphabeticalOrderDesc(user.id);
      model.addAttribute("isAlphabeticDesc", true);
      model.addAttribute("isAlphabeticAsc", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");
    } else {
      queryRequests = services.request().myRequestAlphabeticalOrderAsc(user.id);
      model.addAttribute("isAlphabeticAsc", true);
      model.addAttribute("isAlphabeticDesc", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }

    List<RequestView> myrequestsView = RequestView.from(queryRequests);

    model.addAttribute("myRequests", myrequestsView);

    model.addAttribute("isAll", false);
    model.addAttribute("isMostRecent", false);
    model.addAttribute("isLowRecent", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("alphabetic"));
    model.addAttribute("classDropdownTitle", " sort_alpha_blue pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);
    return "fragments/frame-my-requests";
  }

  @RequestMapping(value = "/sec/my-requests/frame")
  public String myRequestsFrame(@RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.my-requests", principal);

    Requests queryRequests = services.request().requestsforUser(user.id);
    List<RequestView> myrequestsView = RequestView.from(queryRequests);
    List<RequestView> myrequestsViewWithSignAssociate = myrequestsView.stream().filter(requestView -> requestView.getSign() != null).collect(Collectors.toList());
    List<RequestView> myrequestsViewWithoutSignAssociate = myrequestsView.stream().filter(requestView -> requestView.getSign() == null).collect(Collectors.toList());
    myrequestsView.removeAll(myrequestsViewWithSignAssociate);
    myrequestsView.removeAll(myrequestsViewWithoutSignAssociate);
    myrequestsView.addAll(myrequestsViewWithSignAssociate);
    myrequestsView.addAll(myrequestsViewWithoutSignAssociate);

    model.addAttribute("myRequests", myrequestsView);

    model.addAttribute("isAll", true);
    model.addAttribute("isAlphabeticAsc", false);
    model.addAttribute("isAlphabeticDesc", false);
    model.addAttribute("isMostRecent", false);
    model.addAttribute("isLowRecent", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("allRequests"));
    model.addAttribute("classDropdownTitle", " all-signs_blue pull-left");
    model.addAttribute("classDropdownSize", "adjust_size btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);
    return "fragments/frame-my-requests";
  }


  @RequestMapping(value = "/sec/other-requests/mostrecent")
  public String otherRequestsMostRecent(@RequestParam("isMostRecent") boolean isMostRecent, @RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.other-requests", principal);

    Requests queryRequests;
    if (isMostRecent == true) {
      queryRequests = services.request().otherRequestWithNoSignlowRecent(user.id);
      model.addAttribute("isLowRecent", true);
      model.addAttribute("isMostRecent", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");
    } else {
      queryRequests = services.request().otherRequestWithNoSignMostRecent(user.id);
      model.addAttribute("isMostRecent", true);
      model.addAttribute("isLowRecent", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }

    List<RequestView> myrequestsView = RequestView.from(queryRequests);

    model.addAttribute("otherRequests", myrequestsView);

    model.addAttribute("isAll", false);
    model.addAttribute("isAlphabeticAsc", false);
    model.addAttribute("isAlphabeticDesc", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("most_recent_request"));
    model.addAttribute("classDropdownTitle", "  new_blue pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);
    return "other-requests";
  }

  @RequestMapping(value = "/sec/other-requests/mostrecent/frame")
  public String otherRequestsMostRecentFrame(@RequestParam("isMostRecent") boolean isMostRecent, @RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.other-requests", principal);

    Requests queryRequests;
    if (isMostRecent == true) {
      queryRequests = services.request().otherRequestWithNoSignlowRecent(user.id);
      model.addAttribute("isLowRecent", true);
      model.addAttribute("isMostRecent", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");
    } else {
      queryRequests = services.request().otherRequestWithNoSignMostRecent(user.id);
      model.addAttribute("isMostRecent", true);
      model.addAttribute("isLowRecent", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }

    List<RequestView> myrequestsView = RequestView.from(queryRequests);

    model.addAttribute("otherRequests", myrequestsView);

    model.addAttribute("isAll", false);
    model.addAttribute("isAlphabeticAsc", false);
    model.addAttribute("isAlphabeticDesc", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("most_recent_request"));
    model.addAttribute("classDropdownTitle", "  new_blue pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);
    return "fragments/frame-other-requests";
  }

  @RequestMapping(value = "/sec/other-requests/alphabetic")
  public String otherRequestsInAlphabeticalOrder(@RequestParam("isAlphabeticAsc") boolean isAlphabeticAsc, @RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.other-requests", principal);

    Requests queryRequests;
    if (isAlphabeticAsc == true) {
      queryRequests = services.request().otherRequestWithNoSignAlphabeticalOrderDesc(user.id);
      model.addAttribute("isAlphabeticDesc", true);
      model.addAttribute("isAlphabeticAsc", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");
    } else {
      queryRequests = services.request().otherRequestWithNoSignAlphabeticalOrderAsc(user.id);
      model.addAttribute("isAlphabeticAsc", true);
      model.addAttribute("isAlphabeticDesc", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }

    List<RequestView> myrequestsView = RequestView.from(queryRequests);

    model.addAttribute("otherRequests", myrequestsView);

    model.addAttribute("isAll", false);
    model.addAttribute("isMostRecent", false);
    model.addAttribute("isLowRecent", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("alphabetic"));
    model.addAttribute("classDropdownTitle", " sort_alpha_blue pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);
    return "other-requests";
  }

  @RequestMapping(value = "/sec/other-requests/alphabetic/frame")
  public String otherRequestsInAlphabeticalOrderFrame(@RequestParam("isAlphabeticAsc") boolean isAlphabeticAsc, @RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.other-requests", principal);

    Requests queryRequests;
    if (isAlphabeticAsc == true) {
      queryRequests = services.request().otherRequestWithNoSignAlphabeticalOrderDesc(user.id);
      model.addAttribute("isAlphabeticDesc", true);
      model.addAttribute("isAlphabeticAsc", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");
    } else {
      queryRequests = services.request().otherRequestWithNoSignAlphabeticalOrderAsc(user.id);
      model.addAttribute("isAlphabeticAsc", true);
      model.addAttribute("isAlphabeticDesc", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }

    List<RequestView> myrequestsView = RequestView.from(queryRequests);

    model.addAttribute("otherRequests", myrequestsView);

    model.addAttribute("isAll", false);
    model.addAttribute("isMostRecent", false);
    model.addAttribute("isLowRecent", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("alphabetic"));
    model.addAttribute("classDropdownTitle", " sort_alpha_blue pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);
    return "fragments/frame-other-requests";
  }



}
