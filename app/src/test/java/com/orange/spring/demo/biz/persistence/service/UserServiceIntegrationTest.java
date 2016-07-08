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

import com.orange.spring.demo.biz.domain.Community;
import com.orange.spring.demo.biz.domain.User;
import com.orange.spring.demo.biz.persistence.model.UserDB;
import com.orange.spring.demo.biz.persistence.model.UserRoleDB;
import com.orange.spring.demo.biz.persistence.repository.UserRepository;
import com.orange.spring.demo.biz.security.AppSecurityRoles;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class UserServiceIntegrationTest {

  @Autowired
  private UserService userService;
  @Autowired
  private CommunityService communityService;
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private long id = 1234;
  private String username = "Duchess";
  private String password = "aristocats";
  private String firstName = "Duchess";
  private String lastName = "Aristocats";
  private String email = "duchess@cats.com";
  private String entity = "CATS";
  private String activity = "mother";

  private String communityName = "gangstercat";

  private String requestName = "chat";

  private String favoriteName = "favoris";

  private String signName = "chat";
  private String signUrl ="//www.dailymotion.com/embed/video/k4h7GSlUDZQUvkaMF5s";


  @Test
  public void createUser() {
    // given
    // do
    User user = userService.create(
            new User(id, username, firstName, lastName, email, entity, activity, null, null, null, null, null, null, null), password);

    UserDB userDB = userRepository.findOne(user.id);
    String passwordHash = userDB.getPasswordHash();
    Set<UserRoleDB> roles = userDB.getUserRoles();

    // then
    Assertions.assertThat(user.username).isEqualTo(username);
    Assertions.assertThat(passwordEncoder.matches(password, passwordHash)).isTrue();
    Assertions.assertThat(passwordEncoder.matches("", passwordHash)).isFalse();
    Assertions.assertThat(roles).hasSize(1);
    Assertions.assertThat(roles.stream().findFirst().get().getRole()).isEqualTo(AppSecurityRoles.Role.ROLE_USER.toString());
  }

  @Test
  public void changeUserCommunities() {
    //given
    User user = userService.create(
            new User(id, username, firstName, lastName, email, entity, activity, null, null, null, null, null, null, null), password);


    Community community = communityService.create(new Community(id, communityName));
    List<Long> commmunitiesIds = new ArrayList<>();
    commmunitiesIds.add(community.id);

    //do
    userService.changeUserCommunities(user.id, commmunitiesIds);

    User userWithCommunitiesRequestsFavorites = user.loadCommunitiesRequestsFavorites();

    //then
    Assertions.assertThat(userWithCommunitiesRequestsFavorites.communities.list().size()).isEqualTo(1);
    Assertions.assertThat(userWithCommunitiesRequestsFavorites.communities.list().get(0).name).isEqualTo(communityName);


  }

  @Test
  public void createUserRequest() {
    //given
    User user = userService.create(
            new User(id, username, firstName, lastName, email, entity, activity, null, null, null, null, null, null, null), password);

    //do
    userService.createUserRequest(user.id, requestName);

    User userWithCommunitiesRequestsFavorites = user.loadCommunitiesRequestsFavorites();

    //then
    Assertions.assertThat(userWithCommunitiesRequestsFavorites.requests.list().size()).isEqualTo(1);
    Assertions.assertThat(userWithCommunitiesRequestsFavorites.requests.list().get(0).name).isEqualTo(requestName);


  }

  @Test
  public void createUserFavorite() {
    //given
    User user = userService.create(
            new User(id, username, firstName, lastName, email, entity, activity, null, null, null, null, null, null, null), password);

    //do
    userService.createUserFavorite(user.id, favoriteName);

    User userWithCommunitiesRequestsFavorites = user.loadCommunitiesRequestsFavorites();


    //then
    Assertions.assertThat(userWithCommunitiesRequestsFavorites.favorites.list().size()).isEqualTo(1);
    Assertions.assertThat(userWithCommunitiesRequestsFavorites.favorites.list().get(0).name).isEqualTo(favoriteName);

  }

  @Test
  public void createUserSignVideo() {
    //given
    User user = userService.create(
            new User(id, username, firstName, lastName, email, entity, activity, null, null, null, null, null, null, null), password);

    //do
    userService.createUserSignVideo(user.id, signName, signUrl);
    UserDB userDB = userRepository.findOne(user.id);


    //then
    Assertions.assertThat(userDB.getVideos()).hasSize(1);
    Assertions.assertThat(userDB.getVideos().get(0).getUrl()).isEqualTo(signUrl);

  }
}