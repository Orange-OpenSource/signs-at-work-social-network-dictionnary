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

import com.orange.signsatwork.biz.domain.Communities;
import com.orange.signsatwork.biz.domain.Community;
import com.orange.signsatwork.biz.domain.CommunityType;
import com.orange.signsatwork.biz.persistence.model.CommunityDB;
import com.orange.signsatwork.biz.persistence.model.UserDB;
import com.orange.signsatwork.biz.persistence.repository.CommunityRepository;
import com.orange.signsatwork.biz.persistence.repository.FavoriteRepository;
import com.orange.signsatwork.biz.persistence.repository.UserRepository;
import com.orange.signsatwork.biz.persistence.service.CommunityService;
import com.orange.signsatwork.biz.persistence.service.Services;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommunityServiceImpl implements CommunityService {
  private final UserRepository userRepository;
  private final CommunityRepository communityRepository;
  private final FavoriteRepository favoriteRepository;
  private final Services services;

  @Override
  public Communities all() {
    return communitiesFrom(communityRepository.findAll());
  }

  @Override
  public List<Object[]> allForFavorite(long userId) {
    return communityRepository.findAllForFavorite(userId);
  }

  @Override
  public List<Object[]> allForJob(long userId) {
    return communityRepository.findAllForJob(userId);
  }

  @Override
  public Community withId(long id) {
    return communityFrom(communityRepository.findOne(id));
  }

  @Override
  public List<Object[]> forCommunitiesUser(long userId) {
    return communityRepository.findCommunitiesByUser(userId);
  }

  @Override
  public Communities forUser(long userId) {
    UserDB userDB = userRepository.findOne(userId);
    return communitiesFrom(
      communityRepository.findByUser(userDB)
    );
  }

  @Override
  public Community create(long userId, Community community) {
    CommunityDB communityDB;
    UserDB userDB = userRepository.findOne(userId);

    communityDB = new CommunityDB(community.name, community.type);
    communityDB.setUser(userDB);
    communityRepository.save(communityDB);


    return communityFrom(communityDB);
  }

  @Override
  public void delete(Community community) {
    CommunityDB communityDB = communityRepository.findOne(community.id);
    communityDB.getFavorites().forEach(favoriteDB -> favoriteDB.getCommunities().remove(communityDB));
    communityRepository.delete(communityDB);
  }


  @Override
  public Community withCommunityName(String communityName) {
    List<CommunityDB> communityDBList = communityRepository.findByName(communityName);
    if (communityDBList.size() == 1) {
      return communityFrom(communityDBList.get(0));
    } else if (communityDBList.size() > 1){
      String err = "Error while retrieving community with communityName = '" + communityName + "' (list size = " + communityDBList.size() + ")";
      RuntimeException e = new IllegalStateException(err);
      log.error(err, e);
      throw e;
    } else {
      return null;
    }
  }

  private Communities communitiesFrom(Iterable<CommunityDB> communitiesDB) {
    List<Community> communities = new ArrayList<>();
    communitiesDB.forEach(communityDB -> communities.add(communityFrom(communityDB)));
    return new Communities(communities);
  }

  private Community communityFrom(CommunityDB communityDB) {
    if (communityDB == null) {
      return null;
    }
    return new Community(communityDB.getId(), communityDB.getName(), communityDB.getDescriptionText(), communityDB.getDescriptionVideo(), UserServiceImpl.usersFromCommunityView(communityDB.getUsers()), communityDB.getType(), UserServiceImpl.userFromCommunityView(communityDB.getUser()));
  }

  private Communities communitiesFromFavoriteView(Iterable<CommunityDB> communitiesDB) {
    List<Community> communities = new ArrayList<>();
    communitiesDB.forEach(communityDB -> communities.add(communityFromFavoriteView(communityDB)));
    return new Communities(communities);
  }

  private Community communityFromFavoriteView(CommunityDB communityDB) {
    return new Community(communityDB.getId(), communityDB.getName(), null, null, null, communityDB.getType(), null);
  }

  private CommunityDB communityDBFrom(Community community) {
    return new CommunityDB(community.name, community.type);
  }

  @Override
  public Communities forFavorite(long favoriteId) {
    return communitiesFromFavoriteView(
      communityRepository.findByFavorite(favoriteRepository.findOne(favoriteId))
    );
  }

  @Override
  public Community changeCommunityUsers(long communityId, List<Long> usersIds) {
    CommunityDB communityDB = withDBId(communityId);
    List<UserDB> communityUsers = communityDB.getUsers();
    communityUsers.clear();
    userRepository.findAll(usersIds).forEach(communityUsers::add);
    communityDB = communityRepository.save(communityDB);
    return communityFrom(communityDB);
  }


  @Override
  public Community removeMeFromCommunity(long communityId, long userId) {
    CommunityDB communityDB = withDBId(communityId);
    List<UserDB> communityUsers = communityDB.getUsers();
    UserDB userDB = userRepository.findOne(userId);
    communityUsers.remove(userDB);
    communityDB = communityRepository.save(communityDB);
    return communityFrom(communityDB);
  }

  private CommunityDB withDBId(long id) {
    return communityRepository.findOne(id);
  }

  @Override
  public Communities search(String communityName) {
    List<CommunityDB> communitiesMatches = communityRepository.findByNameStartingWith(communityName);

    return communitiesFromFavoriteView(communitiesMatches);
  }

  @Override
  public List<Object[]> searchBis(String communityName) {
    List<Object[]>  communitiesMatches = communityRepository.findStartByNameIgnoreCase(communityName);

    return communitiesMatches;
  }

  @Override
  public Community updateName(long communityId, String communityName) {
    CommunityDB communityDB = communityRepository.findOne(communityId);

    communityDB.setName(communityName);
    communityRepository.save(communityDB);

    return communityFrom(communityDB);
  }


  @Override
  public Community updateDescriptionText(long communityId, String communityDescriptionText) {
    CommunityDB communityDB = communityRepository.findOne(communityId);

    communityDB.setDescriptionText(communityDescriptionText);
    communityRepository.save(communityDB);

    return communityFrom(communityDB);
  }

  @Override
  public Community changeDescriptionVideo(long communityId, String communityDescriptionVideo) {
    CommunityDB communityDB = communityRepository.findOne(communityId);

    communityDB.setDescriptionVideo(communityDescriptionVideo);
    communityRepository.save(communityDB);

    return communityFrom(communityDB);
  }

}
