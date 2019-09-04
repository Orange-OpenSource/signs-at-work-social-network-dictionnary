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

import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.persistence.service.SignService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class Favorite {
    public final long id;
    public final String name;
    public final long idForName;
    public final FavoriteType type;
    public final Videos videos;
    public final Communities communities;
    public final User user;
    public final Users users;

    private final Services services;


  public Favorite loadVideos() {
    return videos != null ?
      this :
      new Favorite(id, name, idForName, type, services.video().forFavorite(id), null,null,  null, services);
  }

  public Favorite loadCommunities() {
    return communities != null ?
      this :
      new Favorite(id, name, idForName, type, null, services.community().forFavorite(id), null,  null, services);
  }

  public Favorite loadUsers() {
    return users != null ?
      this :
      new Favorite(id, name, idForName, type, null, null, null,  services.user().forFavorite(id), services);
  }

  public Favorite addCommunity(Long communityId) {
    Communities communities = this.communities;
    Community community = services.community().withId(communityId);
    communities.list().add(community);
    return new Favorite(id, name, idForName, type, null, communities, null, null, services);
  }

  public List<Long> videosIds() {
    return videos != null ? videos.ids() : null;
  }

  public List<Long> communitiesIds() {
    return communities != null ? communities.ids() : null;
  }

  public String favoriteName() {
    if (this.idForName != 0) {
      return this.name  + "_" + this.idForName;
    } else {
      return this.name;
    }
  }
}
