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

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UserAdminView {
  public final long id;
  public final String name;
  public final Boolean isNonLocked;
  public final Boolean isEnabled;


  public static UserAdminView from(User user) {
    return new UserAdminView(user.id, user.name(), user.isNonLocked, user.isEnabled);
  }

  public static List<UserAdminView> from(Users users) {
    return users.stream()
            .map(UserAdminView::from)
            .collect(Collectors.toList());
  }
}
