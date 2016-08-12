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
import com.orange.signsatwork.biz.persistence.model.CommunityDB;
import com.orange.signsatwork.biz.persistence.repository.CommunityRepository;
import com.orange.signsatwork.biz.persistence.repository.UserRepository;
import com.orange.signsatwork.biz.persistence.service.CommunityService;
import com.orange.signsatwork.biz.persistence.service.Services;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityServiceImpl implements CommunityService {
  private final UserRepository userRepository;
  private final CommunityRepository communityRepository;
  private final Services services;

  @Override
  public Communities all() {
    return communitiesFrom(communityRepository.findAll());
  }

  @Override
  public Community withId(long id) {
    return communityFrom(communityRepository.findOne(id));
  }

  @Override
  public Communities forUser(long userId) {
    return communitiesFrom(
            communityRepository.findByUser(userRepository.findOne(userId))
    );
  }


  @Override
  public Community create(Community community) {
    CommunityDB communityDB = communityRepository.save(communityDBFrom(community));
    return communityFrom(communityDB);
  }

  @Override
  public void delete(Community community) {
    CommunityDB communityDB = communityRepository.findOne(community.id);
    communityDB.getUsers().forEach(userDB -> userDB.getCommunities().remove(communityDB));
    communityRepository.delete(communityDB);
  }

  private Communities communitiesFrom(Iterable<CommunityDB> communitiesDB) {
    List<Community> communities = new ArrayList<>();
    communitiesDB.forEach(communityDB -> communities.add(communityFrom(communityDB)));
    return new Communities(communities);
  }

  private Community communityFrom(CommunityDB communityDB) {
    return new Community(communityDB.getId(), communityDB.getName(), UserServiceImpl.usersFromCommunityView(communityDB.getUsers()));
  }

  private CommunityDB communityDBFrom(Community community) {
    return new CommunityDB(community.name);
  }
}
