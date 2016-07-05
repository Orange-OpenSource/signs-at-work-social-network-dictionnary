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
import com.orange.spring.demo.biz.persistence.model.VideoDB;
import com.orange.spring.demo.biz.persistence.repository.UserRepository;
import com.orange.spring.demo.biz.persistence.repository.VideoRepository;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class VideoServiceIntegrationTest {

  @Autowired
  private VideoService videoService;
  @Autowired
  private CommentService commentService;
  @Autowired
  private UserService userService;

  private long id = 1234;
  private String commentText1="super signe";
  private String commentText2="pas super signe";
  private String commentText3="signe doit être améliorée";

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
  public void createVideoComment() {

    //given
    User user = userService.create(
            new User(id, username, firstName, lastName, email, entity, activity, null, null, null, null, null, null, null), password);
    userService.createUserSignVideo(user.id, sign1Name, sign1Url);
    userService.createUserSignVideo(user.id, sign2Name, sign2Url);
    Videos videos = videoService.all();
    long idVideo1 = videos.list().get(0).id;
    long idVideo2 = videos.list().get(1).id;

    // do
    videoService.createVideoComment(idVideo1, user.id, commentText1);
    Comments commentsVideo1 = commentService.forVideo(idVideo1);

    videoService.createVideoComment(idVideo2, user.id, commentText2);
    videoService.createVideoComment(idVideo2, user.id, commentText3);
    Comments commentsVideo2 = commentService.forVideo(idVideo2);


    // then
    Assertions.assertThat(commentsVideo1.list().size()).isEqualTo(1);
    Assertions.assertThat(commentsVideo1.list().get(0).text).isEqualTo(commentText1);
    Assertions.assertThat(commentsVideo2.list().size()).isEqualTo(2);
    Assertions.assertThat(commentsVideo2.list().get(0).text).isEqualTo(commentText2);
    Assertions.assertThat(commentsVideo2.list().get(1).text).isEqualTo(commentText3);

  }

}
