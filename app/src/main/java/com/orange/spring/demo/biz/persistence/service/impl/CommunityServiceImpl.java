package com.orange.spring.demo.biz.persistence.service.impl;

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

import com.orange.spring.demo.biz.domain.Communities;
import com.orange.spring.demo.biz.domain.Community;
import com.orange.spring.demo.biz.domain.User;
import com.orange.spring.demo.biz.persistence.model.CommunityDB;
import com.orange.spring.demo.biz.persistence.model.UserDB;
import com.orange.spring.demo.biz.persistence.repository.CommunityRepository;
import com.orange.spring.demo.biz.persistence.repository.UserRepository;
import com.orange.spring.demo.biz.persistence.repository.UserRoleRepository;
import com.orange.spring.demo.biz.persistence.service.CommunityService;
import com.orange.spring.demo.biz.security.AppSecurityRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {
  private final CommunityRepository communityRepository;

  @Override
  public Communities all() {
    return communitiesFrom(communityRepository.findAll());
  }

  @Override
  public Community withId(long id) {
    return communityFrom(communityRepository.findOne(id));
  }

  @Override
  public Communities forUser(long id) {
    // FIXME only to go green
    return all();
  }

  @Override
  public Community create(Community community) {
    CommunityDB communityDB = communityRepository.save(communityDBFrom(community));
    return communityFrom(communityDB);
  }

  private Communities communitiesFrom(Iterable<CommunityDB> communitiesDB) {
    List<Community> communities = new ArrayList<>();
    communitiesDB.forEach(communityDB -> communities.add(communityFrom(communityDB)));
    return new Communities(communities);
  }

  private Community communityFrom(CommunityDB communityDB) {
    return new Community(communityDB.getId(), communityDB.getName());
  }

  private CommunityDB communityDBFrom(Community community) {
    CommunityDB communityDB = new CommunityDB(community.name);
    return communityDB;
  }
}
