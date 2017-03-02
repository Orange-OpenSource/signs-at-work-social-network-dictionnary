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

import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.domain.Users;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UserView {
  public final long id;
  public final String username;
  public final String firstName;
  public final String lastName;
  public final String nameVideo;
  public final String email;
  public final String entity;
  public final String job;
  public final String jobTextDescription;
  public final String jobVideoDescription;

  public final Date lastDeconnectionDate;

  public User toUser() {
    return User.create(username, firstName, lastName, nameVideo, email, entity, job, jobTextDescription, jobVideoDescription, lastDeconnectionDate);
  }

  public static UserView from(User user) {
    return new UserView(
            user.id, user.username, user.firstName, user.lastName, user.nameVideo,
            user.email, user.entity, user.job, user.jobTextDescription, user.jobVideoDescription, user.lastDeconnectionDate);
  }

  public static List<UserView> from(Users users) {
    return users.stream()
            .map(UserView::from)
            .collect(Collectors.toList());
  }
}
