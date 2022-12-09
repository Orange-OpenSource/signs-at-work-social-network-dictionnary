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
import com.orange.signsatwork.biz.security.ClearXss;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserCreationView {
  public static final String EMPTY_PASSWORD = "";

  private String username;
  private String password;
  private String role;
  private String firstName;
  private String lastName;
  private String nameVideo;
  private String namePicture;
  private String email;
  private String entity;
  private String job;
  private String jobDescriptionText;
  private String jobDescriptionVideo;
  private String jobDescriptionPicture;
  private String token;
  private Long messageServerId;
  private Boolean isNonLocked;
  private Boolean isEnabled;


  public User toUser() {
    return User.create(username, firstName, lastName, nameVideo, namePicture, email, entity, job, jobDescriptionText, jobDescriptionVideo, jobDescriptionPicture, isNonLocked, isEnabled);
  }

  public void clearXss() {
    if (username != null) {
      username = ClearXss.cleanFormString(username);
    }
    if (role != null) {
      role = ClearXss.cleanFormString(role);
    }
    if (firstName != null) {
      firstName = ClearXss.cleanFormString(firstName);
    }
    if (lastName != null) {
      lastName = ClearXss.cleanFormString(lastName);
    }
    if (email != null) {
      email = ClearXss.cleanFormString(email);
    }
    if (entity != null) {
      entity = ClearXss.cleanFormString(entity);
    }
    if (job != null) {
      job = ClearXss.cleanFormString(job);
    }
    if (jobDescriptionText != null) {
      jobDescriptionText = ClearXss.cleanFormString(jobDescriptionText);
    }
  }
}
