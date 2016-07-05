package com.orange.spring.demo.biz.persistence.service;

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


import com.orange.spring.demo.biz.domain.*;
import com.orange.spring.demo.biz.persistence.model.UserDB;
import com.orange.spring.demo.biz.persistence.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class FavoriteServiceIntegrationTest {

  @Autowired
  private FavoriteService favoriteService;
  @Autowired
  private SignService signService;
  @Autowired
  private UserService userService;
  @Autowired
  private UserRepository userRepository;

  private long id = 1234;
  private String favoriteName = "favoris";

  private String username = "Duchess";
  private String password = "aristocats";
  private String firstName = "Duchess";
  private String lastName = "Aristocats";
  private String email = "duchess@cats.com";
  private String entity = "CATS";
  private String activity = "mother";


  private String sign1Name = "cloud";
  private String sign1Url = "//www.dailymotion.com/embed/video/x2mnl8q";
  private String sign2Name = "chat";
  private String sign2Url = "//www.dailymotion.com/embed/video/k4h7GSlUDZQUvkaMF5s";


  @Test
  public void changeFavoriteSigns() {

    //given
    User user = userService.create(
            new User(id, username, firstName, lastName, email, entity, activity, null, null, null, null, null, null, null), password);
    userService.createUserSignVideo(user.id, sign1Name, sign1Url);
    userService.createUserSignVideo(user.id, sign2Name, sign2Url);
    Signs signs = signService.all();

    Favorite favorite = favoriteService.create(new Favorite(id, favoriteName, null, signService));


    // do
    favoriteService.changeFavoriteSigns(favorite.id, signs.ids());
    Favorite favoriteWithSign = favorite.loadSigns();

    // then
    Assertions.assertThat(favoriteWithSign.name).isEqualTo(favoriteName);
    Assertions.assertThat(favoriteWithSign.signs.list().size()).isEqualTo(2);
    Assertions.assertThat(favoriteWithSign.signs.list().containsAll(signs.list()));

  }
}
