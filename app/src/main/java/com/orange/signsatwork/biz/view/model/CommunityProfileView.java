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


import com.orange.signsatwork.biz.domain.Community;
import com.orange.signsatwork.biz.domain.CommunityType;
import com.orange.signsatwork.biz.domain.Favorite;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.persistence.service.UserService;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class CommunityProfileView {
  private Community community;
  private List<Long> communityUsersIds;
  private List<User> allUsers;

  public CommunityProfileView(Community communityWithoutUsers, UserService userService) {
    community = communityWithoutUsers;
    this.communityUsersIds = community.usersIds();
    if (community.type == CommunityType.Job) {
      this.allUsers = userService.all().stream().filter(u -> u.job == null || u.job.equals(community.name)).collect(Collectors.toList());
    } else {
      this.allUsers = userService.all().list();
    }
  }

}
