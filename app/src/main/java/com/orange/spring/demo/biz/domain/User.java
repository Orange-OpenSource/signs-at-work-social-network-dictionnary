package com.orange.spring.demo.biz.domain;

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

import com.orange.spring.demo.biz.persistence.service.CommunityService;
import com.orange.spring.demo.biz.persistence.service.FavoriteService;
import com.orange.spring.demo.biz.persistence.service.RequestService;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class User {
  public final long id;
  public final String username;
  public final String firstName;
  public final String lastName;
  public final String email;
  public final String entity;
  public final String activity;
  public final Date lastConnectionDate;
  public final Communities communities;
    public final Requests requests;
  public final Favorites favorites;

  private final CommunityService communityService;
  private final RequestService requestService;
  private final FavoriteService favoriteService;

  public User loadCommunitiesRequestsFavorites() {
    return communities != null || requests != null || favorites != null ?
            this :
            new User(
                    id, username, firstName, lastName, email, entity, activity, lastConnectionDate,
                    communityService.forUser(id),  requestService.requestsforUser(id), favoriteService.favoritesforUser(id),
                    communityService, requestService, favoriteService);
  }

  public List<Long> communitiesIds() {
    return communities.ids();
  }

}
