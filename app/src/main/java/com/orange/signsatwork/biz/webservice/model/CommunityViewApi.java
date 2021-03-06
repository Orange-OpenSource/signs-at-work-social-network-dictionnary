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

import com.orange.signsatwork.biz.domain.Community;
import com.orange.signsatwork.biz.domain.CommunityType;
import com.orange.signsatwork.biz.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public class CommunityViewApi {
  private Long id;
  private String type;
  private String name;
  private Long ownerId;
  private String descriptionText;
  private String descriptionVideo;

  public CommunityViewApi(Community community) {
    this.id = community.id;
    this.type = community.type.toString();
    this.name = community.name;
    this.ownerId = community.user.id;
    this.descriptionText = community.descriptionText;
    this.descriptionVideo = community.descriptionVideo;
  }

  public static CommunityViewApi fromMe(Community community) {
    String type = null;
    if (community.type == CommunityType.Job) {
      type = "JobIBelow";
    } else if (community.type == CommunityType.Project) {
      type = "ProjectIBelow";
    }
    return new CommunityViewApi(community.id, type, community.name, community.user.id, community.descriptionText, community.descriptionVideo);
  }
}
