package com.orange.signsatwork.biz.view.model;

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
import com.orange.signsatwork.biz.persistence.service.Services;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignsView implements ComparableSign {
  private long id;
  private String name;
  private Date createDate;
  private Video lastVideo;
  private boolean signCreateAfterLastDateConnection;
  private boolean videoHasComment;

  public static SignsView from(Sign sign, Services services, Date lastConnectionDate) {
    boolean signCreateAfterLastDateConnection = false;
    boolean videoHasComment = false;

    Video lastVideo = services.video().withIdFromSignsView(sign.lastVideoId);
    long nbComments = services.comment().forVideoSignsView(lastVideo.id);
    if (nbComments > 0) {
      videoHasComment = true;
    }
     if (lastConnectionDate != null) {
       if (sign.createDate.compareTo(lastConnectionDate) >= 0) {
         signCreateAfterLastDateConnection = true;
       }
     }
    return new SignsView(sign.id, sign.name, sign.createDate, lastVideo, signCreateAfterLastDateConnection, videoHasComment);
  }

  public static List<SignsView> from(Signs signs, Services services, Date lastConnectionDate) {
    return signs
      .stream()
      .map(sign -> from(sign, services, lastConnectionDate))
      .collect(Collectors.toList());
  }

  @Override
  public long id() {
    return id;
  }

  @Override
  public boolean createdSinceLastConnexion() {
    return signCreateAfterLastDateConnection;
  }

  @Override
  public boolean modifiedSinceLastConnexion() {
    return videoHasComment;
  }
}
