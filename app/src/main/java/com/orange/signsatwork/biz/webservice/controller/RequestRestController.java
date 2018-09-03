package com.orange.signsatwork.biz.webservice.controller;

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

import com.orange.signsatwork.DalymotionToken;
import com.orange.signsatwork.SpringRestClient;
import com.orange.signsatwork.biz.domain.AuthTokenInfo;
import com.orange.signsatwork.biz.domain.Request;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.view.model.RequestCreationView;
import com.orange.signsatwork.biz.webservice.model.RequestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;

/**
 * Types that carry this annotation are treated as controllers where @RequestMapping
 * methods assume @ResponseBody semantics by default, ie return json body.
 */
@Slf4j
@RestController
/** Rest controller: returns a json body */
public class RequestRestController {

  @Autowired
  Services services;
  @Autowired
  DalymotionToken dalymotionToken;
  @Autowired
  private SpringRestClient springRestClient;
  @Autowired
  MessageByLocaleService messageByLocaleService;

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_REQUEST_CREATE, method = RequestMethod.POST)
  public RequestResponse createRequest(@RequestBody RequestCreationView requestCreationView, Principal principal, HttpServletResponse response) {
    List<String> emails;
    String title, bodyMail;
    Request request;
    RequestResponse requestResponse = new RequestResponse();
    User user = services.user().withUserName(principal.getName());
    if (services.sign().withName(requestCreationView.getRequestName()).list().isEmpty()) {
      if (services.request().withName(requestCreationView.getRequestName()).list().isEmpty()) {
         request = services.request().create(user.id, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription());
        log.info("createRequest: username = {} / request name = {}", user.username, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription());
        emails = services.user().findEmailForUserHaveSameCommunityAndCouldCreateSign(user.id);
        title = messageByLocaleService.getMessage("request_created_by_user_title", new Object[]{user.name()});
        bodyMail = messageByLocaleService.getMessage("request_created_by_user_body", new Object[]{user.name(), request.name, "https://signsatwork.orange-labs.fr"});

        Runnable task = () -> {
          log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
          services.emailService().sendRequestMessage(emails.toArray(new String[emails.size()]), title, user.name(), request.name, "https://signsatwork.orange-labs.fr" );
        };

        new Thread(task).start();


      } else {
          response.setStatus(HttpServletResponse.SC_CONFLICT);
          requestResponse.errorType = 1;
          requestResponse.errorMessage = messageByLocaleService.getMessage("request.already_exists");
          return requestResponse;
      }
    } else {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      requestResponse.errorType = 2;
      requestResponse.errorMessage = messageByLocaleService.getMessage("sign.already_exists");
      requestResponse.signId = services.sign().withName(requestCreationView.getRequestName()).list().get(0).id;
      return requestResponse;
    }

    requestResponse.requestId = request.id;
    return requestResponse;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_REQUEST_RENAME, method = RequestMethod.POST)
  public RequestResponse renameRequest(@RequestBody RequestCreationView requestCreationView, @PathVariable long requestId, HttpServletResponse response) {
    RequestResponse requestResponse = new RequestResponse();
    Request request = services.request().withId(requestId);
    if (!request.name.equals(requestCreationView.getRequestName())) {
      if (services.sign().withName(requestCreationView.getRequestName()).list().isEmpty()) {
        if (services.request().withName(requestCreationView.getRequestName()).list().isEmpty()) {
          services.request().rename(requestId, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription());
          log.info("renameRequest:  request name  = {} / request requestTextDescription = {} ", requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription());

        } else {
          response.setStatus(HttpServletResponse.SC_CONFLICT);
          requestResponse.errorType = 1;
          requestResponse.errorMessage = messageByLocaleService.getMessage("request.already_exists");
          return requestResponse;
        }
      } else {
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        requestResponse.errorType = 2;
        requestResponse.errorMessage = messageByLocaleService.getMessage("sign.already_exists");
        requestResponse.signId = services.sign().withName(requestCreationView.getRequestName()).list().get(0).id;
        return requestResponse;
      }

    } else {
      if (!request.requestTextDescription.equals(requestCreationView.getRequestTextDescription())) {
        services.request().rename(requestId, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription());
      }
    }
    requestResponse.requestId = request.id;
    return requestResponse;
  }




  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_REQUEST_PRIORISE, method = RequestMethod.POST)
  public void  requestPriorised(@PathVariable long requestId, HttpServletResponse response) {
    services.request().priorise(requestId);
    response.setStatus(HttpServletResponse.SC_OK);
    return;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_REQUEST_DELETE, method = RequestMethod.POST)
  public String  requestDeleted(@PathVariable long requestId, HttpServletResponse response) {
    String dailymotionId;
    Request request = services.request().withId(requestId);
    services.request().delete(request);
    if (request.requestVideoDescription !=  null) {
      dailymotionId = request.requestVideoDescription.substring(request.requestVideoDescription.lastIndexOf('/') + 1);
      try {
        DeleteVideoOnDailyMotion(dailymotionId);
      } catch (Exception errorDailymotionDeleteVideo) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
      }
    }
    response.setStatus(HttpServletResponse.SC_OK);
    return "/sec/requests";
  }

  private void DeleteVideoOnDailyMotion(String dailymotionId) {

    AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
    if (authTokenInfo.isExpired()) {
      dalymotionToken.retrieveToken();
      authTokenInfo = dalymotionToken.getAuthTokenInfo();
    }

    final String uri = "https://api.dailymotion.com/video/"+dailymotionId;
    RestTemplate restTemplate = springRestClient.buildRestTemplate();

    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add("Authorization", "Bearer " + authTokenInfo.getAccess_token());

    HttpEntity<?> request = new HttpEntity<Object>(headers);

    restTemplate.exchange(uri, HttpMethod.DELETE, request, String.class );

    return;
  }
}
