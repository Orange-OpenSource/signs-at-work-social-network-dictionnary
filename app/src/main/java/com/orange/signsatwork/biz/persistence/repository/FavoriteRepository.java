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
import com.orange.signsatwork.biz.persistence.model.UserDB;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoriteRepository extends CrudRepository<FavoriteDB, Long> {
    List<FavoriteDB> findByName(String name);

    @Query("select distinct c FROM FavoriteDB c inner join c.user user where user = :userDB order by c.id")
    List<FavoriteDB> findByUser(@Param("userDB") UserDB userDB);

    @Query(value="select  a.video_id, count(a.text) as nbr from comments a inner join favorites_videos b on a.video_id = b.videos_id  and b.favorites_id = :favoriteId group by a.video_id order by nbr asc", nativeQuery = true)
    Long[] findNbCommentForAllVideoByFavorite(@Param("favoriteId") long favoriteId);

    @Query(value=" select  a.video_id, count(a.rating) as nbr from ratings a inner join favorites_videos b on a.video_id = b.videos_id  and a.rating='Positive' and b.favorites_id = :favoriteId group by a.video_id order by nbr asc", nativeQuery = true)
    Long[] findNbPositiveRateForAllVideoByFavorite(@Param("favoriteId") long favoriteId);

}
