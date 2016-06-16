package com.orange.spring.demo.biz.persistence.service.impl;

/*
 * #%L
 * Spring demo
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

import com.orange.spring.demo.biz.domain.Communities;
import com.orange.spring.demo.biz.domain.Community;
import com.orange.spring.demo.biz.domain.User;
import com.orange.spring.demo.biz.domain.Users;
import com.orange.spring.demo.biz.persistence.model.UserDB;
import com.orange.spring.demo.biz.persistence.repository.UserRepository;
import com.orange.spring.demo.biz.persistence.repository.UserRoleRepository;
import com.orange.spring.demo.biz.persistence.service.CommunityService;
import com.orange.spring.demo.biz.persistence.service.UserService;
import com.orange.spring.demo.biz.security.AppSecurityAdmin;
import com.orange.spring.demo.biz.security.AppSecurityRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, ApplicationListener<AuthenticationSuccessEvent> {
  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final CommunityService communityService;
  private final PasswordEncoder passwordEncoder;

  @Override
  public Users all() {
    return usersFrom(userRepository.findAll());
  }

  @Override
  public User withId(long id) {
    return userFrom(userRepository.findOne(id));
  }

  @Override
  public User create(User user, String password) {
    UserDB userDB = userRepository.save(userDBFrom(user, password));
    return userFrom(userDB);
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

  private Users usersFrom(Iterable<UserDB> usersDB) {
    List<User> users = new ArrayList<>();
    usersDB.forEach(userDB -> users.add(userFrom(userDB)));
    return new Users(users);
  }

  private User userFrom(UserDB userDB) {
    return new User(
            userDB.getId(),
            userDB.getUsername(), userDB.getFirstName(), userDB.getLastName(),
            userDB.getEmail(), userDB.getEntity(), userDB.getActivity(),
            communitiesOf(userDB),
            userDB.getLastConnectionDate(),
            communityService);
  }

  private Communities communitiesOf(UserDB userDB) {
    return new Communities(
      userDB.getCommunities().stream()
            .map(communityDB -> new Community(communityDB.getId(), communityDB.getName()))
            .collect(Collectors.toList())
    );
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
