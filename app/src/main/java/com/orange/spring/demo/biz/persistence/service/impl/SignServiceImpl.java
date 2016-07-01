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
import com.orange.spring.demo.biz.domain.Sign;
import com.orange.spring.demo.biz.domain.Signs;
import com.orange.spring.demo.biz.persistence.model.FavoriteDB;
import com.orange.spring.demo.biz.persistence.model.SignDB;
import com.orange.spring.demo.biz.persistence.model.VideoDB;
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
public class SignServiceImpl implements SignService {
  private final FavoriteRepository favoriteRepository;
  private final SignRepository signRepository;

  @Override
  public Signs all() {
    return signsFrom(signRepository.findAll());
  }

  @Override
  public Sign withId(long id) {
    return signFrom(signRepository.findOne(id));
  }

  @Override
  public Sign withIdForAssociate(long id) {
    return signFromAssociate(signRepository.findOne(id));
  }


  @Override
  public Signs withName(String name) {
    return signsFrom(signRepository.findByName(name));
  }

  @Override
  public Signs forFavorite(long favoriteId) {
    return signsFrom(
            signRepository.findByFavorite(favoriteRepository.findOne(favoriteId))
    );
  }

  @Override
  public Sign changeSignAssociates(long signId, List<Long> associateSignsIds) {
    SignDB signDB = withDBId(signId);
    List<SignDB> signAssociates = signDB.getAssociates();
    signAssociates.clear();
    signRepository.findAll(associateSignsIds).forEach(signAssociates::add);
    signDB = signRepository.save(signDB);
    return signFrom(signDB);
  }

  @Override
  public Sign create(Sign sign) {
    SignDB signDB = signRepository.save(signDBFrom(sign));
    return signFrom(signDB);
  }


  private SignDB withDBId(long id) {
    return signRepository.findOne(id);
  }

  static Signs signsFrom(Iterable<SignDB> signsDB) {
    List<Sign> signs = new ArrayList<>();
    signsDB.forEach(signDB -> signs.add(signFrom(signDB)));
    return new Signs(signs);
  }

  static Sign signFrom(SignDB signDB) {
    if (signDB == null) {
      return null;
    }
    else {
      Sign sign = new Sign(signDB.getId(), signDB.getName(), signDB.getUrl(), VideoServiceImpl.videosFrom(signDB.getVideos()), null, null);
      return sign;
    }
  }

  static Sign signFromAssociate(SignDB signDB) {
    if (signDB == null) {
      return null;
    }
    else {
      Sign sign = new Sign(signDB.getId(), signDB.getName(), signDB.getUrl(), null, signsFrom(signDB.getAssociates()).ids(), signsFrom(signDB.getReferenceBy()).ids());
      return sign;
    }
  }

  private SignDB signDBFrom(Sign sign) {
    SignDB signDB = new SignDB(sign.name, sign.url);
    return signDB;
  }
}
