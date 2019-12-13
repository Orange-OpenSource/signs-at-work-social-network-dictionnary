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

import com.orange.signsatwork.biz.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public class UserViewApi {
  private String username;
  private String firstName;
  private String lastName;
  private String nameVideo;
  private String email;
  private String entity;
  private String job;
  private String jobTextDescription;
  private String jobVideoDescription;

  public UserViewApi(User user) {
    this.username = user.username;
    this.firstName = user.firstName;
    this.lastName = user.lastName;
    this.nameVideo = user.nameVideo;
    this.email = user.email;
    this.entity = user.entity;
    this.job = user.job;
    this.jobTextDescription = user.jobDescriptionText;
    this.jobVideoDescription = user.jobDescriptionVideo;
  }
}
