package com.orange.signsatwork.biz.domain;

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

import com.orange.signsatwork.biz.TestUser;
import com.orange.signsatwork.biz.persistence.service.Services;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SignRatingTest {

  @Autowired
  Services services;
  @Autowired
  TestUser testUser;

  long userId;
  long signId;

  @Before
  public void setup() {
    services.clearPersistence();

    userId = testUser.get("user").id;
    signId = services.sign().create(userId, "mySign", "//video", "").id;
  }

  @Test
  public void defaultRatingIsNoRate() {
    // given
    User user = services.user().withId(userId);
    Sign sign = services.sign().withId(signId);
    // do
    Rating rating = sign.rating(user);
    // then
    Assertions.assertThat(rating).isEqualTo(Rating.NoRate);
  }

  @Test
  public void changeAndReadRating() {
    testChangeAndReadRating(Rating.Negative);
    testChangeAndReadRating(Rating.Positive);
    testChangeAndReadRating(Rating.Neutral);
  }

  private void testChangeAndReadRating(Rating rating) {
    // given
    User user = services.user().withId(userId);
    Sign sign = services.sign().withId(signId);
    sign.changeUserRating(user, rating);
    // do
    Sign reloadedSign = services.sign().withId(sign.id);
    Rating newRating = reloadedSign.rating(user);
    // then
    Assertions.assertThat(newRating).isEqualTo(rating);
  }
}
