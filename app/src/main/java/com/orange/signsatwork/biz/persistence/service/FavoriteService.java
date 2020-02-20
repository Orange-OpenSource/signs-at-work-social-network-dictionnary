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

import com.orange.signsatwork.biz.domain.Favorite;
import com.orange.signsatwork.biz.domain.Favorites;

import java.util.List;

public interface FavoriteService {
  Favorites all();

  Favorites favoritesforUser(long id);

  Favorites favoritesforUserForSignFilter(long userId);

  Favorites oldFavoritesShareToUser(long userId);

  Favorites newFavoritesShareToUser(long userId);

  Favorites oldFavoritesShareToUserForSignFilter(long userId);

  Favorites newFavoritesShareToUserForSignFilter(long userId);

  Favorite withId(long id);

  Favorites withName(String name);

  Favorite create(Favorite favorite);

  Favorite create(long userId, String favoriteName);

  Favorite updateName(long favoriteId, String favoriteName);

  Favorite changeFavoriteVideos(long favoriteId, List<Long> videosIds);

  Favorite changeFavoriteCommunities(long favoriteId, List<Long> communitiesIds, String userName, String url);

  void delete(Favorite favorite);

  Long[] NbCommentForAllVideoByFavorite(long favoriteId);

  Favorite addUserOpenFavoritePage(long favoriteId, long userId);

  Long maxIdForName(String name, long id);

  Favorite removeMeFromSeeFavorite(long favoriteId, long userId);

}
