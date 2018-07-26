package com.orange.signsatwork.biz.persistence.service;

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

import com.orange.signsatwork.biz.security.AppSecurityAdmin;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter(AccessLevel.PACKAGE)
@Accessors(fluent = true)
public class Services {
  private CommentService comment;
  private CommunityService community;
  private FavoriteService favorite;
  private RatingService rating;
  private RequestService request;
  private SignService sign;
  private UserService user;
  private VideoService video;
  private EmailService emailService;

  @Autowired
  private AppSecurityAdmin appSecurityAdmin;

  public void clearPersistence() {
    comment.all().stream().forEach(c -> comment.delete(c));
    community.all().stream().forEach(c -> community.delete(c));
    favorite.all().stream().forEach(f -> favorite.delete(f));
    rating.deleteAll();
    video.all().stream().forEach(v -> video.delete(v));
    request.all().stream().forEach(r -> request.delete(r));
    sign.all().stream().forEach(s -> sign.delete(s));
    user.all().stream().filter(u -> !appSecurityAdmin.isAdmin(u.username)).forEach(u -> user.delete(u));
  }
}
