package com.orange.signsatwork.biz.persistence.service.impl;

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
import com.orange.signsatwork.biz.persistence.model.*;
import com.orange.signsatwork.biz.persistence.repository.*;
import com.orange.signsatwork.biz.persistence.service.FavoriteService;
import com.orange.signsatwork.biz.persistence.service.Services;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteServiceImpl implements FavoriteService {
  private final UserRepository userRepository;
  private final FavoriteRepository favoriteRepository;
  private final SignRepository signRepository;
  private final VideoRepository videoRepository;
  private final CommunityRepository communityRepository;
  private final Services services;

  @Override
  public Favorites all() {
    return favoritesFrom(favoriteRepository.findAll());
  }

  @Override
  public Favorite withId(long id) {
    return favoriteFrom(favoriteRepository.findOne(id), services);
  }

  @Override
  public Favorites withName(String name) {
    return favoritesFrom(favoriteRepository.findByName(name));
  }

  @Override
  public Favorites favoritesforUser(long userId) {
    return favoritesFrom(
            favoriteRepository.findByUser(userRepository.findOne(userId))
    );
  }


  @Override
  public Favorites favoritesShareToUser(long userId) {
    return favoritesFrom(
      favoriteRepository.findFavoritesShareToUser(userId)
    );
  }

  @Override
  public Favorite changeFavoriteVideos(long favoriteId, List<Long> videosIds) {
    FavoriteDB favoriteDB = withDBId(favoriteId);
    List<VideoDB> favoriteVideos = favoriteDB.getVideos();
    favoriteVideos.clear();
    videoRepository.findAll(videosIds).forEach(favoriteVideos::add);
    favoriteDB = favoriteRepository.save(favoriteDB);
    return favoriteFrom(favoriteDB, services);
  }


  private FavoriteDB withDBId(long id) {
    return favoriteRepository.findOne(id);
  }

  @Override
  public Favorite create(Favorite favorite) {
    FavoriteDB actualFavoriteDB = favoriteDBFrom(favorite);
    FavoriteDB newFavoriteDB = actualFavoriteDB;
    newFavoriteDB.setType(FavoriteType.Individual);
    FavoriteDB favoriteDB = favoriteRepository.save(newFavoriteDB);
    return favoriteFrom(favoriteDB, services);
  }

  @Override
  public Favorite create(long userId, String favoriteName) {
    FavoriteDB favoriteDB;
    UserDB userDB = userRepository.findOne(userId);

    favoriteDB = new FavoriteDB();
    favoriteDB.setName(favoriteName);
    favoriteDB.setType(FavoriteType.Individual);
    favoriteRepository.save(favoriteDB);

    userDB.getFavorites().add(favoriteDB);
    userRepository.save(userDB);

    return favoriteFrom(favoriteDB, services);
  }

  @Override
  public Favorite updateName(long favoriteId, String favoriteName) {
    FavoriteDB favoriteDB = favoriteRepository.findOne(favoriteId);

    favoriteDB.setName(favoriteName);
    favoriteRepository.save(favoriteDB);

    return favoriteFrom(favoriteDB, services);
  }

  @Override
  public void delete(Favorite favorite) {
    FavoriteDB favoriteDB = favoriteRepository.findOne(favorite.id);
    favoriteDB.getUser().getFavorites().remove(favoriteDB);
    favoriteDB.getVideos().stream().forEach(videoDB -> videoDB.getFavorites().remove(favoriteDB));
    favoriteRepository.delete(favoriteDB);
  }

  private Favorites favoritesFrom(Iterable<FavoriteDB> favoritesDB) {
    List<Favorite> favorites = new ArrayList<>();
    favoritesDB.forEach(favoriteDB -> favorites.add(favoriteFrom(favoriteDB, services)));
    return new Favorites(favorites);
  }

  static Favorite favoriteFrom(FavoriteDB favoriteDB, Services services) {
    return favoriteDB == null ? null :
      new Favorite(favoriteDB.getId(), favoriteDB.getName(), favoriteDB.getType(), null, null, UserServiceImpl.userFromSignView(favoriteDB.getUser()), services);
  }

  private FavoriteDB favoriteDBFrom(Favorite favorite) {
    return new FavoriteDB(favorite.name);
  }

  @Override
  public Long[] NbCommentForAllVideoByFavorite(long favoriteId) {
    return favoriteRepository.findNbCommentForAllVideoByFavorite(favoriteId);
  }


  @Override
  public Favorite changeFavoriteCommunities(long favoriteId, List<Long> communitiesIds) {
    FavoriteDB favoriteDB = withDBId(favoriteId);
    List<CommunityDB> favoriteCommunities = favoriteDB.getCommunities();
    favoriteCommunities.clear();
    communityRepository.findAll(communitiesIds).forEach(favoriteCommunities::add);
    if (favoriteCommunities.size() != 0) {
      favoriteDB.setType(FavoriteType.Share);
    } else {
      favoriteDB.setType(FavoriteType.Individual);
    }
    favoriteDB = favoriteRepository.save(favoriteDB);
    return favoriteFrom(favoriteDB, services);
  }

}
