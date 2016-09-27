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
import com.orange.signsatwork.biz.domain.*;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserServiceDeleteTest {

  @Autowired
  Services services;

  @Autowired
  TestUser testUser;

  @Test
  public void canRemoveUser() {
    // given
    User user = testUser.get("user-canRemoveUser");
    Sign sign = services.sign().create(user.id, "sign-canRemoveUser", "//video-canRemoveUser", "");
    Video video = sign.loadVideos().videos.list().get(0);
    Favorite favorite = services.user().createUserFavorite(user.id, "favorite-canRemoveUser");
    Request request = services.user().createUserRequest(user.id, "request-canRemoveUser");
    services.video().createVideoRating(video.id, user.id, Rating.Positive);
    Comment comment = services.video().createVideoComment(video.id, user.id, "comment-canRemoveUser");

    // then, check we have a correct testing context
    Assertions.assertThat(services.user().withId(user.id)).isNotNull();
    Assertions.assertThat(services.favorite().withId(favorite.id)).isNotNull();
    Assertions.assertThat(services.request().withId(request.id)).isNotNull();
    Assertions.assertThat(services.comment().withId(comment.id)).isNotNull();
    Assertions.assertThat(services.video().withId(video.id)).isNotNull();
    Assertions.assertThat(services.rating().all().stream().filter(r -> r.primaryKey.user.id == user.id).count()).isEqualTo(1);

    // do
    services.user().delete(user);

    // then
    Assertions.assertThat(services.user().all().stream().filter(u -> u.id == user.id).count()).isEqualTo(0);
    Assertions.assertThat(services.favorite().all().stream().filter(f -> f.id == favorite.id).count()).isEqualTo(0);
    Assertions.assertThat(services.request().all().stream().filter(r -> r.id == request.id).count()).isEqualTo(0);
    Assertions.assertThat(services.comment().all().stream().filter(c -> c.id == comment.id).count()).isEqualTo(0);
    Assertions.assertThat(services.video().all().stream().filter(v -> v.id == video.id).count()).isEqualTo(0);
    Assertions.assertThat(services.rating().all().stream().filter(r -> r.primaryKey.user.id == user.id).count()).isEqualTo(0);
  }
}
