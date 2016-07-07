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


import com.orange.spring.demo.biz.domain.Favorite;
import com.orange.spring.demo.biz.domain.Sign;
import com.orange.spring.demo.biz.domain.Signs;
import com.orange.spring.demo.biz.domain.User;
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
public class SignServiceIntegrationTest {


  @Autowired
  private SignService signService;
  @Autowired
  private UserService userService;

  private long id = 1234;

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
  private String sign3Name = "architecture";
  private String sign3Url = "//www.dailymotion.com/embed/video/k6Ekk8a95ZT36waMEi0";

  @Test
  public void changeSignAssociates() {

    //given
    User user = userService.create(
            new User(id, username, firstName, lastName, email, entity, activity, null, null, null, null, null, null, null), password);
    userService.createUserSignVideo(user.id, sign1Name, sign1Url);
    userService.createUserSignVideo(user.id, sign2Name, sign2Url);
    userService.createUserSignVideo(user.id, sign3Name, sign3Url);
    Signs signs = signService.all();
    long idSign1 = signs.list().get(0).id;
    long idSign2 = signs.list().get(1).id;
    long idSign3 = signs.list().get(2).id;
    List<Long> associateSignsIds = new ArrayList<>();
    associateSignsIds.add(idSign3);
    associateSignsIds.add(idSign2);


    // do
    signService.changeSignAssociates(idSign1, associateSignsIds);
    Sign sign1 = signService.withIdForAssociate(idSign1);
    Sign sign2 = signService.withIdForAssociate(idSign2);
    Sign sign3 = signService.withIdForAssociate(idSign3);


    // then
    Assertions.assertThat(sign1.associateSignsIds.size()).isEqualTo(2);
    Assertions.assertThat(sign1.associateSignsIds).contains(idSign3);
  }
}
