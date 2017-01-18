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

import com.orange.signsatwork.biz.domain.Request;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.view.model.RequestCreationView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

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

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_REQUEST_CREATE, method = RequestMethod.POST)
  public void createRequest(@RequestBody RequestCreationView requestCreationView, Principal principal, HttpServletResponse response) {
    User user = services.user().withUserName(principal.getName());
    if (services.request().withName(requestCreationView.getRequestName()).list().isEmpty()) {
      Request request = services.request().create(user.id, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription());
      log.info("createRequest: username = {} / request name = {}", user.username, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription());
    } else {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
    }
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_REQUEST_RENAME, method = RequestMethod.POST)
  public void renameRequest(@RequestBody RequestCreationView requestCreationView, @PathVariable long requestId, HttpServletResponse response) {
  Request request = services.request().withId(requestId);
    if (!request.name.equals(requestCreationView.getRequestName()) ) {

     if (services.request().withName(requestCreationView.getRequestName()).list().isEmpty()) {
      services.request().rename(requestId, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription());

      log.info("renameRequest:  request name  = {} / request requestTextDescription = {} ", requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription());
      } else {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      }
    } else {
      if (!request.requestTextDescription.equals(requestCreationView.getRequestTextDescription())) {
        services.request().rename(requestId, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription());
      }
    }
  }




  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_REQUEST_PRIORISE, method = RequestMethod.POST)
  public void  requestPriorised(@PathVariable long requestId) {
    services.request().priorise(requestId);

    return;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_REQUEST_DELETE, method = RequestMethod.POST)
  public void  requestDeleted(@PathVariable long requestId, Principal principal) {
    Request request = services.request().withId(requestId);
    services.request().delete(request);

    return;
  }

}
