package com.orange.signsatwork.biz;

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
import com.orange.signsatwork.biz.persistence.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestUser {

  public static final String USER = "user";
  public static final String PASSWORD = "pass";

  @Autowired
  UserService userService;

  public User get(String username) {
    return userService.create(User.create(username, "titi", "toto", "nameVideo", "titi@toto.org", "entity", "job", "jobTextDescription", "jobVideoDescription"), PASSWORD, "USER");
  }

  public User get() {
    return get(USER);
  }
}
