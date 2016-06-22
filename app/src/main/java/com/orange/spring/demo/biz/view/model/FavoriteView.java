package com.orange.spring.demo.biz.view.model;

/*
 * #%L
 * Spring demo
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

import com.orange.spring.demo.biz.domain.Favorite;
import com.orange.spring.demo.biz.domain.Favorites;
import com.orange.spring.demo.biz.domain.Request;
import com.orange.spring.demo.biz.domain.Requests;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteView {
  private long id;
  private String name;

  public Favorite toFavorite() {
    return new Favorite(id, name);
  }

  public static FavoriteView from(Favorite favorite) {
    return new FavoriteView(favorite.id, favorite.name);
  }

  public static List<FavoriteView> from(Favorites favorites) {
    return favorites.stream()
            .map(FavoriteView::from)
            .collect(Collectors.toList());
  }
}
