package com.orange.spring.demo.biz.persistence.service.impl;

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

import com.orange.spring.demo.biz.domain.Favorite;
import com.orange.spring.demo.biz.domain.Favorites;
import com.orange.spring.demo.biz.persistence.model.FavoriteDB;
import com.orange.spring.demo.biz.persistence.model.SignDB;
import com.orange.spring.demo.biz.persistence.repository.FavoriteRepository;
import com.orange.spring.demo.biz.persistence.repository.SignRepository;
import com.orange.spring.demo.biz.persistence.repository.UserRepository;
import com.orange.spring.demo.biz.persistence.service.FavoriteService;
import com.orange.spring.demo.biz.persistence.service.SignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
  private final UserRepository userRepository;
  private final FavoriteRepository favoriteRepository;
  private final SignRepository signRepository;
  private final SignService signService;

  @Override
  public Favorites all() {
    return favoritesFrom(favoriteRepository.findAll());
  }

  @Override
  public Favorite withId(long id) {
    return favoriteFrom(favoriteRepository.findOne(id));
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
  public Favorite changeFavoriteSigns(long favoriteId, List<Long> signsIds) {
    FavoriteDB favoriteDB = withDBId(favoriteId);
    List<SignDB> favoriteSigns = favoriteDB.getSigns();
    favoriteSigns.clear();
    signRepository.findAll(signsIds).forEach(favoriteSigns::add);
    favoriteDB = favoriteRepository.save(favoriteDB);
    return favoriteFrom(favoriteDB);
  }

  private FavoriteDB withDBId(long id) {
    return favoriteRepository.findOne(id);
  }

  @Override
  public Favorite create(Favorite favorite) {
    FavoriteDB favoriteDB = favoriteRepository.save(favoriteDBFrom(favorite));
    return favoriteFrom(favoriteDB);
  }

  private Favorites favoritesFrom(Iterable<FavoriteDB> favoritesDB) {
    List<Favorite> favorites = new ArrayList<>();
    favoritesDB.forEach(favoriteDB -> favorites.add(favoriteFrom(favoriteDB)));
    return new Favorites(favorites);
  }

  private Favorite favoriteFrom(FavoriteDB favoriteDB) {
    return new Favorite(favoriteDB.getId(), favoriteDB.getName(), null, signService);
  }

  private FavoriteDB favoriteDBFrom(Favorite favorite) {
    FavoriteDB favoriteDB = new FavoriteDB(favorite.name);
    return favoriteDB;
  }
}
