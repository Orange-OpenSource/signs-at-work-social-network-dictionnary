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
public class FavoriteModalView {
  private long id;
  private String name;
  private long idForName;
  private FavoriteType type;

  public Favorite toFavorite() {
    return new Favorite(id, name, idForName, type, null, null, null, null, null);
  }

  public static FavoriteModalView from(Favorite favorite) {
    return new FavoriteModalView(favorite.id, favorite.name, favorite.idForName, favorite.type);
  }

  public static List<FavoriteModalView> from(Favorites favorites) {
    return favorites.stream()
            .map(FavoriteModalView::from)
            .collect(Collectors.toList());
  }


  public static FavoriteModalView fromNewShare(Favorite favorite) {
    return new FavoriteModalView(favorite.id, favorite.name, favorite.idForName, FavoriteType.NewShare);
  }

  public static List<FavoriteModalView> fromNewShare(Favorites favorites) {
    return favorites.stream()
      .map(FavoriteModalView::fromNewShare)
      .collect(Collectors.toList());
  }

  public String favoriteName() {
    if (this.idForName != 0) {
      return this.name  + "_" + this.idForName;
    } else {
      return this.name;
    }
  }
}
