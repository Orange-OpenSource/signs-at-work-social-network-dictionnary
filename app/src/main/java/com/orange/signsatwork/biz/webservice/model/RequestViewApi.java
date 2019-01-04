package com.orange.signsatwork.biz.webservice.model;

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
import com.orange.signsatwork.biz.domain.Sign;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class RequestViewApi {
  private long id;
  private String name;
  private String requestTextDescription;
  private String requestVideoDescription;
  private Date requestDate;
  private Long signId;
  private String signName;
  private String username;
  private String firstName;
  private String lastName;

  public RequestViewApi(Request request) {
      this(request.id, request.name, request.requestTextDescription, request.requestVideoDescription, request.requestDate, request.sign.id, request.sign.name, request.user.username, request.user.firstName, request.user.lastName);
  }
  public RequestViewApi(long id, String name, String requestTextDescription, String requestVideoDescription, Date requestDate, String username, String firstName, String lastName) {
    this.id = id;
    this.name = name;
    this.requestTextDescription = requestTextDescription;
    this.requestVideoDescription = requestVideoDescription;
    this.requestDate = requestDate;
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
  }
}
