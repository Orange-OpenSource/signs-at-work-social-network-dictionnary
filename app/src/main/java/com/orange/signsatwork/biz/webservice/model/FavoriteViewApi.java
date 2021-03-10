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

import com.orange.signsatwork.biz.domain.Favorite;
import com.orange.signsatwork.biz.domain.FavoriteType;
import com.orange.signsatwork.biz.domain.Favorites;
import com.orange.signsatwork.biz.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteViewApi {
  private long id;
  private String name;
  private long idForName;
  private FavoriteType type;
  private String ownerName;
  private long ownerId;
  private long nbShareCommunity;
  private long nbVideos;


  public static FavoriteViewApi from(Favorite favorite) {
    long nbVideos = 0L;
    if (favorite.videos == null) {
      nbVideos = favorite.loadVideos().videos.list().size();
    }
    return new FavoriteViewApi(favorite.id, favorite.name, favorite.idForName, favorite.type, favorite.user.name(), favorite.user.id, 0L, nbVideos);
  }

  public static List<FavoriteViewApi> from(Favorites favorites) {
    return favorites.stream()
            .map(FavoriteViewApi::from)
            .collect(Collectors.toList());
  }

  public static FavoriteViewApi fromNewShare(Favorite favorite) {
    long nbVideos = 0L;
    if (favorite.videos == null) {
      nbVideos = favorite.loadVideos().videos.list().size();
    }
    return new FavoriteViewApi(favorite.id, favorite.name, favorite.idForName, FavoriteType.NewShare, favorite.user.name(), favorite.user.id, 0L, nbVideos);
  }

  public static List<FavoriteViewApi> fromNewShare(Favorites favorites) {
    return favorites.stream()
      .map(FavoriteViewApi::fromNewShare)
      .collect(Collectors.toList());
  }

  public static FavoriteViewApi fromShare(Favorite favorite) {
    long nbShareCommunity = 0L;
    if (favorite.communities != null) {
      nbShareCommunity = favorite.communities.list().size();
    }
    long nbVideos = 0L;
    if (favorite.videos == null) {
      nbVideos = favorite.loadVideos().videos.list().size();
    }
    return new FavoriteViewApi(favorite.id, favorite.name, favorite.idForName, FavoriteType.NewShare, favorite.user.name(), favorite.user.id, nbShareCommunity, nbVideos);
  }

  public static List<FavoriteViewApi> fromShare(Favorites favorites) {
    return favorites.stream()
      .map(FavoriteViewApi::fromShare)
      .collect(Collectors.toList());
  }
}
