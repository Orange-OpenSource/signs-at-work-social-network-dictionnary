package com.orange.signsatwork.biz.persistence.repository;

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

import com.orange.signsatwork.biz.persistence.model.FavoriteDB;
import com.orange.signsatwork.biz.persistence.model.RequestDB;
import com.orange.signsatwork.biz.persistence.model.UserDB;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends CrudRepository<UserDB, Long> {
  List<UserDB> findByUsername(String username);

  @Query(value="select A.email from userdb A, userdb_user_roles B  where A.id != :userId and A.id = B.userdb_id and B.user_roles_id in (select id from user_roledb where role='ROLE_USER_A') and A.id in (select distinct(users_id) from  communities_users where communities_id in (select communities_id from communities_users where users_id= :userId)) and A.email is not null", nativeQuery = true)
  List<String> findEmailForUserHaveSameCommunityAndCouldCreateSign(@Param("userId") long userId);


  @Query("select distinct s FROM UserDB s inner join s.favorites favorite where favorite = :favoriteDB")
  List<UserDB> findByFavorite(@Param("favoriteDB") FavoriteDB favoriteDB);

  public default UserDB findOne(long id) {
    return findById(id).orElse(null);
  }

}
