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
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {
  private final UserRepository userRepository;
  private final FavoriteRepository favoriteRepository;
  private final SignRepository signRepository;
  private final VideoRepository videoRepository;
  private final CommunityRepository communityRepository;
  private final Services services;
  @Autowired
  MessageByLocaleService messageByLocaleService;

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
  public Favorites favoritesforUserForSignFilter(long userId) {
    return favoritesFrom(
      favoriteRepository.findByUserForSignFilter(userId)
    );
  }

  @Override
  public Favorites oldFavoritesShareToUser(long userId) {
    return favoritesFrom(
      favoriteRepository.findOldFavoritesShareToUser(userId)
    );
  }

  @Override
  public Favorites newFavoritesShareToUser(long userId) {
    return favoritesFrom(
      favoriteRepository.findNewFavoritesShareToUser(userId)
    );
  }

  @Override
  public Favorites oldFavoritesShareToUserForSignFilter(long userId) {
    return favoritesFrom(
      favoriteRepository.findOldFavoritesShareToUserForSignFilter(userId)
    );
  }

  @Override
  public Favorites newFavoritesShareToUserForSignFilter(long userId) {
    return favoritesFrom(
      favoriteRepository.findNewFavoritesShareToUserForSignFilter(userId)
    );
  }


  @Override
  public Favorite changeFavoriteVideos(long favoriteId, List<Long> videosIds) {
    FavoriteDB favoriteDB = withDBId(favoriteId);
    List<VideoDB> favoriteVideos = favoriteDB.getVideos();
    favoriteVideos.clear();
    videoRepository.findAllById(videosIds).forEach(favoriteVideos::add);
    favoriteDB = favoriteRepository.save(favoriteDB);
    return favoriteFrom(favoriteDB, services);
  }


  private FavoriteDB withDBId(long id) {
    return favoriteRepository.findOne(id);
  }


  @Override
  public Long maxIdForName(String name, long id) {
    return favoriteRepository.findMaxIdForName(name.toUpperCase(), id);
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
    favoriteDB.setIdForName(0L);
    favoriteDB.setUser(userDB);
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
      new Favorite(favoriteDB.getId(), favoriteDB.getName(), favoriteDB.getIdForName(), favoriteDB.getType(), null, null, UserServiceImpl.userFromSignView(favoriteDB.getUser()), null, services);
  }

  private FavoriteDB favoriteDBFrom(Favorite favorite) {
    return new FavoriteDB(favorite.name);
  }

  @Override
  public Long[] NbCommentForAllVideoByFavorite(long favoriteId) {
    return favoriteRepository.findNbCommentForAllVideoByFavorite(favoriteId);
  }


  @Override
  public Favorite changeFavoriteCommunities(long favoriteId, List<Long> communitiesIds, String userName, String url, Locale locale) {
    List<String> emails;
    String title, bodyMail;
    FavoriteDB favoriteDB = withDBId(favoriteId);
    List<CommunityDB> favoriteCommunities = favoriteDB.getCommunities();
    List<Long> oldFavoriteCommunitiesIds = favoriteCommunities.stream().map(c-> c.getId()).collect(Collectors.toList());

    favoriteCommunities.clear();

    communityRepository.findAllById(communitiesIds).forEach(favoriteCommunities::add);
    if (favoriteDB.getType() != FavoriteType.Share) {
      Long maxIdForName = maxIdForName(favoriteDB.getName().replace("œ", "oe").replace("æ", "ae"), favoriteId);
      if (maxIdForName != null) {
        if (maxIdForName == 0) {
          favoriteDB.setIdForName(maxIdForName + 2);
        } else {
          favoriteDB.setIdForName(maxIdForName + 1);
        }
      }
    }



    if (favoriteCommunities.size() != 0) {
      favoriteDB.setType(FavoriteType.Share);
    } else {
      favoriteDB.setType(FavoriteType.Individual);
    }
    favoriteRepository.save(favoriteDB);
    Favorite favorite = favoriteFrom(favoriteDB, services);

    List<Long> addedFavoriteCommunitiesIds = communitiesIds
      .stream()
      .filter(id1 -> oldFavoriteCommunitiesIds.stream().noneMatch(id2 -> id2.equals(id1)))
      .collect(Collectors.toList());

    List<UserDB> userDBList = new ArrayList<>();
    favoriteCommunities.forEach(c -> { if (addedFavoriteCommunitiesIds.contains(c.getId())) {
      userDBList.addAll(c.getUsers());
    }
    });


    if (userDBList.size() != 0) {
      emails = userDBList.stream().filter(u-> u.getEmail() != null).map(u -> u.getEmail()).collect(Collectors.toList());
      emails = emails.stream().distinct().collect(Collectors.toList());
      if (emails.size() != 0) {
        title = messageByLocaleService.getMessage("favorite_share_by_user_title", new Object[]{userName});
        bodyMail = messageByLocaleService.getMessage("favorite_share_by_user_body", new Object[]{userName, favorite.favoriteName(), url + "/sec/favorite/" + favorite.id});

        List<String> finalEmails = emails;
        Runnable task = () -> {
          log.info("send mail email = {} / title = {} / body = {}", finalEmails.toString(), title, bodyMail);
          services.emailService().sendFavoriteShareMessage(finalEmails.toArray(new String[finalEmails.size()]), title, userName, favorite.favoriteName(), url + "/sec/favorite/" + favorite.id, locale);
        };

        new Thread(task).start();
      }
    }

    return favorite;
  }

  @Override
  public Favorite addUserOpenFavoritePage(long favoriteId, long userId) {
    FavoriteDB favoriteDB = withDBId(favoriteId);
    List<UserDB> favoriteUsers = favoriteDB.getUsers();
    UserDB userDB = userRepository.findOne(userId);
    if (!favoriteUsers.contains(userDB)) {
      favoriteUsers.add(userDB);
      favoriteDB = favoriteRepository.save(favoriteDB);
    }

    return favoriteFrom(favoriteDB, services);
  }

  @Override
  public Favorite removeMeFromSeeFavorite(long favoriteId, long userId) {
    FavoriteDB favoriteDB = withDBId(favoriteId);
    List<UserDB> favoriteUsers = favoriteDB.getUsers();
    UserDB userDB = userRepository.findOne(userId);
    favoriteUsers.remove(userDB);
    favoriteDB = favoriteRepository.save(favoriteDB);
    return favoriteFrom(favoriteDB, services);
  }

  @Override
  public Long[] FavoriteIdsBelowVideoId(long videoId, List<Long> favoriteIds) {
    return favoriteRepository.findFavoriteIdsBelowVideoId(videoId, favoriteIds);
  }
}
