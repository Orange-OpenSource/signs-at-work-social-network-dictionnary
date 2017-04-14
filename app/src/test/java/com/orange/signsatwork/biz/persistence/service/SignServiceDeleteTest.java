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
import com.orange.signsatwork.biz.domain.Video;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SignServiceDeleteTest {

  @Autowired
  Services services;

  @Autowired
  TestUser testUser;

  @Test
  public void canRemoveSign() {
    // given
    User user = testUser.get("user-canRemoveSign");
    Sign sign1 = services.sign().create(user.id, "sign-canRemoveSign", "//video-canRemoveSign", "");
    Sign sign2 = services.sign().create(user.id, "sign-canRemoveSign2", "//video-canRemoveSign2", "");
    Video video = sign1.loadVideos().videos.list().get(0);
    Favorite favorite = services.user().createUserFavorite(user.id, "favorite-canRemoveSign");

    //services.favorite().changeFavoriteSigns(favorite.id, Arrays.asList(new Long[]{sign1.id}));

    // then, check we have a correct testing context
    Assertions.assertThat(services.sign().withId(sign1.id)).isNotNull();
    Assertions.assertThat(services.video().withId(video.id)).isNotNull();
    //Assertions.assertThat(services.favorite().withId(favorite.id).loadSigns().signsIds()).contains(sign1.id);

    // do
    services.sign().delete(sign1);

    // then
    Assertions.assertThat(services.sign().withId(sign1.id)).isNull();
    Assertions.assertThat(services.video().all().stream().filter(v -> v.id == video.id).count()).isEqualTo(0);
    //Assertions.assertThat(services.favorite().withId(favorite.id).loadSigns().signsIds()).doesNotContain(sign1.id);
  }
}
