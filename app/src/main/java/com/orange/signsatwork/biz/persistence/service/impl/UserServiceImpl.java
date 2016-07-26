package com.orange.signsatwork.biz.persistence.service.impl;

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

import com.orange.signsatwork.biz.domain.Favorite;
import com.orange.signsatwork.biz.domain.Request;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.domain.Users;
import com.orange.signsatwork.biz.persistence.model.*;
import com.orange.signsatwork.biz.persistence.repository.*;
import com.orange.signsatwork.biz.persistence.service.*;
import com.orange.signsatwork.biz.security.AppSecurityAdmin;
import com.orange.signsatwork.biz.security.AppSecurityRoles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService, ApplicationListener<AuthenticationSuccessEvent> {
  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final CommunityRepository communityRepository;
  private final RequestRepository requestRepository;
  private final FavoriteRepository favoriteRepository;
  private final PasswordEncoder passwordEncoder;
  private final Services services;

  @Override
  public Users all() {
    return usersFrom(userRepository.findAll());
  }

  @Override
  public User withId(long id) {
    return userFrom(withDBId(id));
  }

  @Override
  public User withUserName(String userName) {
    List<UserDB> userDBList = userRepository.findByUsername(userName);
    if (userDBList.size() == 1) {
      return userFrom(userRepository.findByUsername(userName).get(0));
    } else {
      String err = "Error while retrieving user with username = '" + userName + "' (list size = " + userDBList.size() + ")";
      RuntimeException e = new IllegalStateException(err);
      log.error(err, e);
      throw e;
    }
  }

  @Override
  public User create(User user, String password) {
    UserDB userDB = userRepository.save(userDBFrom(user, password));
    return userFrom(userDB);
  }

  @Override
  public User changeUserCommunities(long userId, List<Long> communitiesIds) {
    UserDB userDB = withDBId(userId);
    List<CommunityDB> userCommunities = userDB.getCommunities();
    userCommunities.clear();
    communityRepository.findAll(communitiesIds).forEach(userCommunities::add);
    userDB = userRepository.save(userDB);
    return userFrom(userDB);
  }

  @Override
  public Request createUserRequest(long userId, String requestName) {
    UserDB userDB = withDBId(userId);

    RequestDB requestDB = new RequestDB();
    requestDB.setRequestDate(new Date());
    requestDB.setName(requestName);
    requestRepository.save(requestDB);

    userDB.getRequests().add(requestDB);
    userRepository.save(userDB);
    return RequestServiceImpl.requestFrom(requestDB, services);
  }

  @Override
  public Favorite createUserFavorite(long userId, String favoriteName) {
    UserDB userDB = withDBId(userId);

    FavoriteDB favoriteDB = new FavoriteDB();

    favoriteDB.setName(favoriteName);
    favoriteRepository.save(favoriteDB);

    userDB.getFavorites().add(favoriteDB);
    userRepository.save(userDB);
    return FavoriteServiceImpl.favoriteFrom(favoriteDB, services);
  }

  @Override
  public void delete(User user) {
    UserDB userDB = userRepository.findOne(user.id);

    List<RequestDB> requestDBs = new ArrayList<>();
    requestDBs.addAll(userDB.getRequests());
    List<CommentDB> commentDBs = new ArrayList<>();
    commentDBs.addAll(userDB.getComments());
    List<FavoriteDB> favoriteDBs = new ArrayList<>();
    favoriteDBs.addAll(userDB.getFavorites());
    List<VideoDB> videoDBs = new ArrayList<>();
    videoDBs.addAll(userDB.getVideos());

    requestDBs.stream().map(r -> services.request().withId(r.getId())).forEach(r -> services.request().delete(r));
    commentDBs.stream().map(c -> services.comment().withId(c.getId())).forEach(c -> services.comment().delete(c));
    favoriteDBs.stream().map(f -> services.favorite().withId(f.getId())).forEach(f -> services.favorite().delete(f));
    videoDBs.stream().map(v -> services.video().withId(v.getId())).forEach(v -> services.video().delete(v));

    userDB.getCommunities().forEach(c -> c.getUsers().remove(userDB));

    userRepository.delete(userDB);
  }

  @Override
  public void onApplicationEvent(AuthenticationSuccessEvent authenticationSuccessEvent) {
    String userName = ((UserDetails) authenticationSuccessEvent.getAuthentication().getPrincipal()).getUsername();
    if (!AppSecurityAdmin.isAdmin(userName)) {
      UserDB userDB = userRepository.findByUsername(userName).get(0);
      userDB.setLastConnectionDate(new Date());
      userRepository.save(userDB);
    }
  }

  private UserDB withDBId(long id) {
    return userRepository.findOne(id);
  }

  private Users usersFrom(Iterable<UserDB> usersDB) {
    List<User> users = new ArrayList<>();
    usersDB.forEach(userDB -> users.add(userFrom(userDB)));
    return new Users(users);
  }

  private User userFrom(UserDB userDB) {
    return User.create(
            userDB.getId(),
            userDB.getUsername(), userDB.getFirstName(), userDB.getLastName(),
            userDB.getEmail(), userDB.getEntity(), userDB.getActivity(), userDB.getLastConnectionDate(),
            services);
  }

  static User userFromSignView(UserDB userDB) {
    return User.create(
            userDB.getId(),
            userDB.getUsername(), userDB.getFirstName(), userDB.getLastName(),
            userDB.getEmail(), userDB.getEntity(), userDB.getActivity(), userDB.getLastConnectionDate());
  }
  /**
   * Create a transient UserDB with a hashed password and a ROLE_USER as default
   * @param user user domain object
   * @param password raw password
   * @return the UserDB object to persist
   */
  private UserDB userDBFrom(User user, String password) {
    UserDB userDB = new UserDB(user.username, passwordEncoder.encode(password), user.firstName, user.lastName, user.email, user.entity, user.activity);
    addUserRole(userDB);
    return userDB;
  }

  private void addUserRole(UserDB userDB) {
    userDB.getUserRoles().add(
            userRoleRepository.findByRole(AppSecurityRoles.Role.ROLE_USER.toString()).get(0)
    );
  }
}
