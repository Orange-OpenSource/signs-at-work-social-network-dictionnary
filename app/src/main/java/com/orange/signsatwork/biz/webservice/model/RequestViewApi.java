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
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigInteger;
import java.util.Date;

@Getter
@AllArgsConstructor

public class RequestViewApi {
  private long id;
  private String name;
  private String textDescription;
  private String videoDescription;
  private Date date;
  private Long signId;
  private String signName;
  private String userName;
  private long lastVideoId;
  private long nbVideo;
  private Boolean isCreatedByMe;


  public RequestViewApi(Request request) {
    if (request.sign != null) {
      this.id = request.id;
      this.name = request.name;
      this.textDescription = request.requestTextDescription;
      this.videoDescription = request.requestVideoDescription;
      this.date = request.requestDate;
      this.signId = request.sign.id;
      this.signName = request.sign.name;
      this.userName = request.user.name();
      this.lastVideoId = request.sign.lastVideoId;
      this.nbVideo = request.sign.nbVideo;
    } else {
      this.id = request.id;
      this.name = request.name;
      this.textDescription = request.requestTextDescription;
      this.videoDescription = request.requestVideoDescription;
      this.date = request.requestDate;
      this.userName = request.user.name();
    }
  }

  public RequestViewApi(Request request, Boolean isCreatedByMe) {
    if (request.sign != null) {
      this.id = request.id;
      this.name = request.name;
      this.textDescription = request.requestTextDescription;
      this.videoDescription = request.requestVideoDescription;
      this.date = request.requestDate;
      this.signId = request.sign.id;
      this.signName = request.sign.name;
      this.userName = request.user.name();
      this.lastVideoId = request.sign.lastVideoId;
      this.nbVideo = request.sign.nbVideo;
      this.isCreatedByMe = isCreatedByMe;
    } else {
      this.id = request.id;
      this.name = request.name;
      this.textDescription = request.requestTextDescription;
      this.videoDescription = request.requestVideoDescription;
      this.date = request.requestDate;
      this.userName = request.user.name();
      this.isCreatedByMe = isCreatedByMe;
    }
  }

  public RequestViewApi(Object[] queryResultItem) {
    String t = toString(queryResultItem[1]);
    String idString = t.substring(t.lastIndexOf("/")+1);

    if (t.contains("my-request-detail")) {
      this.isCreatedByMe = true;
    } else {
      this.isCreatedByMe = false;
    }

    id = Long.parseLong(idString);
    name = toString(queryResultItem[0]);
  }

  private String toString(Object o) {
    return (String) o;
  }

  private long toLong(Object o) {
    if (o == null) {
      return 0;
    };
    return ((BigInteger)o).longValue();
  }
}
