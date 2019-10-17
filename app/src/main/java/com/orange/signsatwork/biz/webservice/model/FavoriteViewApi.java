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


  public static FavoriteViewApi from(Favorite favorite) {
    return new FavoriteViewApi(favorite.id, favorite.name, favorite.idForName, favorite.type);
  }

  public static List<FavoriteViewApi> from(Favorites favorites) {
    return favorites.stream()
            .map(FavoriteViewApi::from)
            .collect(Collectors.toList());
  }

  public static FavoriteViewApi fromNewShare(Favorite favorite) {
    return new FavoriteViewApi(favorite.id, favorite.name, favorite.idForName, FavoriteType.NewShare);
  }

  public static List<FavoriteViewApi> fromNewShare(Favorites favorites) {
    return favorites.stream()
      .map(FavoriteViewApi::fromNewShare)
      .collect(Collectors.toList());
  }
}
