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

import com.orange.signsatwork.biz.domain.CommunityType;
import com.orange.signsatwork.biz.persistence.model.CommunityDB;
import com.orange.signsatwork.biz.persistence.model.FavoriteDB;
import com.orange.signsatwork.biz.persistence.model.UserDB;
import com.orange.signsatwork.biz.persistence.model.VideoDB;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityRepository extends CrudRepository<CommunityDB, Long> {
    List<CommunityDB> findByName(String name);

    List<CommunityDB> findByNameStartingWith(String name);

    @Query(value="select id, type, name, user_id, description_text, description_video from communities  where replace(replace(upper(name),'Œ','OE'),'Æ','AE') collate utf8_unicode_ci like concat('%',:name,'%')", nativeQuery = true)
    List<Object[]> findStartByNameIgnoreCase(@Param("name") String name);

    @Query("select distinct c FROM CommunityDB c inner join c.users user where user = :userDB")
    List<CommunityDB> findByUser(@Param("userDB") UserDB userDB);

    @Query(value="select A.id, 'ProjectIBelow', A.name, A.user_id from communities A where A.type='Project' and A.id in (select B.communities_id from communities_users B where B.users_id= :userId) union select A.id, 'JobIBelow', A.name, A.user_id from communities A where A.type='Job' and A.id in (select B.communities_id from communities_users B where B.users_id= :userId)",  nativeQuery = true)
    List<Object[]> findCommunitiesByUser(@Param("userId") long userId);

    @Query("select distinct s FROM CommunityDB s inner join s.favorites favorite where favorite = :favoriteDB")
    List<CommunityDB> findByFavorite(@Param("favoriteDB") FavoriteDB favoriteDB);

    @Query(value="select A.id, 'ProjectIBelow', A.name, A.user_id from communities A where A.type='Project' and A.id in (select B.communities_id from communities_users B where B.users_id= :userId) union select A.id, 'Project', A.name, A.user_id from communities A where A.type='Project' and A.id not in (select B.communities_id from communities_users B where B.users_id = :userId) union select A.id, 'JobIBelow', A.name, A.user_id from communities A where A.type='Job' and A.id in (select B.communities_id from communities_users B where B.users_id= :userId) union select A.id, 'Job', A.name, A.user_id from communities A where A.type='Job' and A.id not in (select B.communities_id from communities_users B where B.users_id= :userId)",  nativeQuery = true)
    List<Object[]> findAllForFavorite(@Param("userId") long userId);

    @Query(value="select A.id, 'JobIBelow', A.name, A.user_id from communities A where A.type='Job' and A.id in (select B.communities_id from communities_users B where B.users_id= :userId) union select A.id, 'Job', A.name, A.user_id from communities A where A.type='Job' and A.id not in (select B.communities_id from communities_users B where B.users_id= :userId)",  nativeQuery = true)
    List<Object[]> findAllForJob(@Param("userId") long userId);

    @Query("select distinct A FROM CommunityDB A where A.type= :communityType order by A.name")
    List<CommunityDB> findAll(@Param("communityType") CommunityType communityType);
    public default CommunityDB findOne(long id) {
      return findById(id).orElse(null);
    }
}
