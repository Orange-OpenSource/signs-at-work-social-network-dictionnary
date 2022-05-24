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

import com.orange.signsatwork.biz.domain.*;
import com.orange.signsatwork.biz.persistence.model.UserDB;

import java.util.List;

public interface UserService {


  List<String> findEmailForUserHaveSameCommunityAndCouldCreateSign(long userId);

  User getAdmin();

  Users all();

  User withId(long id);

  User withUserName(String userName);

  User create(User user, String password, String role, String email);

  void changeUserPassword(User user, String password);

  void changeUserLogin(User user, String login);

/*  User changeUserCommunities(long userId, List<Long> communitiesIds);*/

  Request createUserRequest(long userId, String requestName);

  Favorite createUserFavorite(long userId, String favoriteName);

  void delete(User user);

  void enable(User user);

  void disable(User user);

  void changeJob(User user, String job);

  void changeEntity(User user, String entity);

  void changeDescription(User user, String jobTextDescription);

  void changeLastName(User user, String lastName);

  void changeFirstName(User user, String firstName);

  void changeEmail(User user, String email);

  void createProfile(User user, String lastName, String firstName, String nameVideo, String job, String entity, String jobTextDescription, String jobVideoDescription);

  void changeNameVideoUrl(User user, String videoWebPath, String pictureUri);

  void changeDescriptionVideoUrl(User user, String videoWebPath, String pictureUri);

  void changeLastDeconnectionDate(String userName);

  Users allForCreateCommunity();

  Users forFavorite(long favoriteId);

  void createPasswordResetTokenForUser(User user, String token);

  PasswordResetToken getPasswordResetToken(String token);
}
