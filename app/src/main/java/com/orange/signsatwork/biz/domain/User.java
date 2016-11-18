package com.orange.signsatwork.biz.domain;

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

import com.orange.signsatwork.biz.persistence.service.*;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class User {
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
  public final Date lastConnectionDate;
  public final Communities communities;
  public final Requests requests;
  public final Favorites favorites;
  public final Videos videos;

  private final Services services;

  public String name() {
    return firstName + " " + lastName;
  }

  public String firstName() {
    return firstName ;
  }

  public String lastName() {
    return lastName ;
  }



  public User loadCommunitiesRequestsFavorites() {
    return communities != null || requests != null || favorites != null ?
            this :
            new User(
                    id, username, firstName, lastName, nameVideo, email, entity, job, jobTextDescription, jobVideoDescription, lastConnectionDate,
                    services.community().forUser(id),  services.request().requestsforUser(id), services.favorite().favoritesforUser(id), videos,
                    services);
  }

  public User loadVideos() {
    return videos != null ? this :
            new User(
                    id, username, firstName, lastName, nameVideo, email, entity, job, jobTextDescription, jobVideoDescription, lastConnectionDate,
                    communities, requests, favorites, services.video().forUser(id),
                    services);
  }

  public List<Long> communitiesIds() {
    return communities.ids();
  }


  public static User create(String username, String firstName, String lastName, String nameVideo, String email, String entity, String job, String jobTextDescription, String jobVideoDescription) {
    return create(username, firstName, lastName, nameVideo, email, entity, job, jobTextDescription, jobVideoDescription,  null);
  }

  public static User create(String username, String firstName, String lastName, String nameVideo, String email, String entity, String job, String jobTextDescription, String jobVideoDescription, Date lastConnectionDate) {
    return create(-1, username, firstName, lastName, nameVideo, email, entity, job, jobTextDescription, jobVideoDescription, lastConnectionDate);
  }

  public static User create(long id, String username, String firstName, String lastName, String nameVideo, String email, String entity, String job, String jobTextDescription, String jobVideoDescription, Date lastConnectionDate) {
    return create(
            id, username, firstName, lastName, nameVideo, email, entity, job, jobTextDescription, jobVideoDescription, lastConnectionDate,
            null);
  }

  public static User create(long id, String username, String firstName, String lastName, String nameVideo, String email, String entity, String job, String jobTextDescription, String jobVideoDescription, Date lastConnectionDate,
                            Services services) {
    return new User(
            id, username, firstName, lastName, nameVideo, email, entity, job, jobTextDescription, jobVideoDescription, lastConnectionDate,
            null, null, null, null, services);
  }
}
