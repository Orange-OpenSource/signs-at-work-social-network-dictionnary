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

import com.orange.signsatwork.biz.domain.Sign;
import com.orange.signsatwork.biz.domain.Signs;
import com.orange.signsatwork.biz.domain.Videos;
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
public class SignView {
  private long id;
  private String name;
  private String textDefinition;
  private String videoDefinition;
  private String url;
  private Date createDate;
  private Videos videos;
  private boolean hasComment;
  private boolean recent;

  public Sign toSign() {
    return new Sign(id, name, textDefinition, videoDefinition, url, createDate, 0, 0, null, null, null);
  }

  public static SignView from(Sign sign) {
    boolean hasComment = false;
    if (sign.listComments().list().isEmpty()) {
      hasComment = false;
    } else {
      hasComment = true;
    }
    return new SignView(sign.id, sign.name, sign.textDefinition, sign.videoDefinition, sign.url, sign.createDate, sign.videos, hasComment, false);
  }

  public static List<SignView> from(Signs signs) {
    return signs
            .stream()
            .map(SignView::from)
            .collect(Collectors.toList());
  }

  public static SignView fromRecent(Sign sign) {
    boolean hasComment = false;
    if (sign.listComments().list().isEmpty()) {
      hasComment = false;
    } else {
      hasComment = true;
    }
    return new SignView(sign.id, sign.name, sign.textDefinition, sign.videoDefinition, sign.url, sign.createDate, sign.videos, hasComment, true);
  }

  public static List<SignView> fromRecent(Signs signs) {
    return signs
            .stream()
            .map(SignView::fromRecent)
            .collect(Collectors.toList());
  }

}
