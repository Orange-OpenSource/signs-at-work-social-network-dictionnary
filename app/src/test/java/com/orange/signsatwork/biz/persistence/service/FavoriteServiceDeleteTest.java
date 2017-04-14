package com.orange.signsatwork.biz.persistence.service;

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
import com.orange.signsatwork.biz.domain.Favorite;
import com.orange.signsatwork.biz.domain.Sign;
import com.orange.signsatwork.biz.domain.User;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FavoriteServiceDeleteTest {

  @Autowired
  Services services;

  @Autowired
  TestUser testUser;

  @Test
  public void canRemoveFavorite() {
    // given
    User user = testUser.get("user-canRemoveFavorite");
    Sign sign = services.sign().create(user.id, "sign-canRemoveFavorite", "//video-canRemoveFavorite", "");

    // do/then
    Favorite favorite = services.user().createUserFavorite(user.id, "favorite-canRemoveFavorite");
    //services.favorite().changeFavoriteSigns(favorite.id, Arrays.asList(new Long[]{sign.id}));
    Assertions.assertThat(services.favorite().all().stream().filter(f -> f.id == favorite.id).count()).isEqualTo(1);
    Assertions.assertThat(services.user().withId(user.id).loadCommunitiesRequestsFavorites().favorites.ids()).contains(favorite.id);

    // do/then
    services.favorite().delete(favorite);
    Assertions.assertThat(services.favorite().all().stream().filter(f -> f.id == favorite.id).count()).isEqualTo(0);
    Assertions.assertThat(services.user().withId(user.id).loadCommunitiesRequestsFavorites().favorites.ids()).doesNotContain(favorite.id);
  }
}
