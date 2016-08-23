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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
      Request request = services.request().create(user.id, requestCreationView.getRequestName());
      log.info("createRequest: username = {} / request name = {}", user.username, requestCreationView.getRequestName());
    } else {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
    }
  }

}
