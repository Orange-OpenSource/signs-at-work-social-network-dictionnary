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
import com.orange.signsatwork.biz.domain.Community;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.persistence.model.UserDB;
import com.orange.signsatwork.biz.persistence.model.UserRoleDB;
import com.orange.signsatwork.biz.persistence.repository.UserRepository;
import com.orange.signsatwork.biz.security.AppSecurityRoles;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserServiceIntegrationTest {

  @Autowired
  Services services;

  @Autowired
  TestUser testUser;

  @Autowired
  UserRepository userRepository;

  @Autowired
  PasswordEncoder passwordEncoder;

  String username = "Duchess";

  String communityName = "gangstercat";

  String requestName = "chat";

  String favoriteName = "favoris";

  String signName = "chat";
  String signUrl ="//www.dailymotion.com/embed/video/k4h7GSlUDZQUvkaMF5s";

  long userId;

  @Before
  public void setup() {
    services.clearPersistence();
    userId = testUser.get(username).id;
  }

  @Test
  public void createUser() {
    // given
    // do
    UserDB userDB = userRepository.findOne(userId);
    String passwordHash = userDB.getPasswordHash();
    Set<UserRoleDB> roles = userDB.getUserRoles();

    // then
    Assertions.assertThat(userDB.getUsername()).isEqualTo(username);
    Assertions.assertThat(passwordEncoder.matches(TestUser.PASSWORD, passwordHash)).isTrue();
    Assertions.assertThat(passwordEncoder.matches("", passwordHash)).isFalse();
    Assertions.assertThat(roles).hasSize(1);
    Assertions.assertThat(roles.stream().findFirst().get().getRole()).isEqualTo(AppSecurityRoles.Role.ROLE_USER.toString());
  }

  @Test
  public void changeUserCommunities() {
    //given
    Community community = services.community().create(Community.create(communityName));
    List<Long> commmunitiesIds = new ArrayList<>();
    commmunitiesIds.add(community.id);
    User user = services.user().withId(userId);

    //do
    services.user().changeUserCommunities(userId, commmunitiesIds);

    User userWithCommunitiesRequestsFavorites = user.loadCommunitiesRequestsFavorites();

    //then
    Assertions.assertThat(userWithCommunitiesRequestsFavorites.communities.list().size()).isEqualTo(1);
    Assertions.assertThat(userWithCommunitiesRequestsFavorites.communities.list().get(0).name).isEqualTo(communityName);
  }

  @Test
  public void createUserRequest() {
    //given
    User user = services.user().withId(userId);
    //do
    services.user().createUserRequest(userId, requestName);

    User userWithCommunitiesRequestsFavorites = user.loadCommunitiesRequestsFavorites();

    //then
    Assertions.assertThat(userWithCommunitiesRequestsFavorites.requests.list().size()).isEqualTo(1);
    Assertions.assertThat(userWithCommunitiesRequestsFavorites.requests.list().get(0).name).isEqualTo(requestName);
  }

  @Test
  public void createUserFavorite() {
    //given
    User user = services.user().withId(userId);
    //do
    services.user().createUserFavorite(userId, favoriteName);

    User userWithCommunitiesRequestsFavorites = user.loadCommunitiesRequestsFavorites();

    //then
    Assertions.assertThat(userWithCommunitiesRequestsFavorites.favorites.list().size()).isEqualTo(1);
    Assertions.assertThat(userWithCommunitiesRequestsFavorites.favorites.list().get(0).name).isEqualTo(favoriteName);
  }

  @Test
  public void createUserSignVideo() {
    //given
    //do
    services.sign().create(userId, signName, signUrl, "");
    User user = services.user().withId(userId);
    user = user.loadVideos();

    //then
    Assertions.assertThat(user.videos.list()).hasSize(1);
    Assertions.assertThat(user.videos.list().get(0).url).isEqualTo(signUrl);
  }
}
