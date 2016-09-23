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
import com.orange.signsatwork.biz.domain.Comment;
import com.orange.signsatwork.biz.domain.Sign;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.domain.Video;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommentServiceDeleteTest {

  @Autowired
  Services services;

  @Autowired
  TestUser testUser;

  @Test
  public void canRemoveComment() {
    // given
    User user = testUser.get("user-canRemoveComment");
    Sign sign = services.sign().create(user.id, "sign-canRemoveComment", "//video-canRemoveComment", "");
    Video video = sign.loadVideos().videos.list().get(0);

    // do/then
    Comment comment = services.video().createVideoComment(video.id, user.id, "comment-canRemoveComment");
    Assertions.assertThat(services.comment().all().stream().filter(c -> c.id == comment.id).count()).isEqualTo(1);

    // do/then
    services.comment().delete(comment);
    Assertions.assertThat(services.comment().all().stream().filter(c -> c.id == comment.id).count()).isEqualTo(0);
  }
}
