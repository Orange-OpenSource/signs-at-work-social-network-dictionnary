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

import com.orange.signsatwork.biz.persistence.model.CommunityDB;
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

    @Query(value="select distinct A.id,A.name, A.type, A.user_id, A.id_for_name from favorites A, favorites_communities B, communities_users C where A.id = B.favorites_id and B.communities_id = C.communities_id and A.user_id != C.users_id and C.users_id = :userId and C.users_id not in (select users_id from favorites_users D where D.users_id = C.users_id and D.favorites_id = A.id)", nativeQuery = true)
    List<FavoriteDB> findNewFavoritesShareToUser(@Param("userId") long userId);

    @Query(value="select distinct A.id,A.name, A.type, A.user_id, A.id_for_name from favorites A, favorites_communities B, communities_users C where A.id = B.favorites_id and B.communities_id = C.communities_id and A.user_id != C.users_id and C.users_id = :userId and C.users_id in (select users_id from favorites_users D where D.users_id = C.users_id and D.favorites_id = A.id)", nativeQuery = true)
    List<FavoriteDB> findOldFavoritesShareToUser(@Param("userId") long userId);

    @Query(value="select distinct A.id,A.name, A.type, A.user_id, A.id_for_name from favorites A, favorites_communities B, communities_users C, favorites_videos D where A.id = B.favorites_id and B.communities_id = C.communities_id and A.user_id != C.users_id and C.users_id = :userId and C.users_id not in (select users_id from favorites_users D where D.users_id = C.users_id and D.favorites_id = A.id) and A.id = D.favorites_id", nativeQuery = true)
    List<FavoriteDB> findNewFavoritesShareToUserForSignFilter(@Param("userId") long userId);

    @Query(value="select distinct A.id,A.name, A.type, A.user_id, A.id_for_name from favorites A, favorites_communities B, communities_users C, favorites_videos D where A.id = B.favorites_id and B.communities_id = C.communities_id and A.user_id != C.users_id and C.users_id = :userId and C.users_id in (select users_id from favorites_users D where D.users_id = C.users_id and D.favorites_id = A.id) and A.id = D.favorites_id", nativeQuery = true)
    List<FavoriteDB> findOldFavoritesShareToUserForSignFilter(@Param("userId") long userId);


    @Query(value="select distinct A.id,A.name, A.type, A.user_id, A.id_for_name from favorites A, favorites_videos B where A.user_id = :userId and A.id = B.favorites_id order by A.id", nativeQuery = true)
    List<FavoriteDB> findByUserForSignFilter(@Param("userId") long userId);

    @Query(value="select max(id_for_name) from favorites A, favorites_communities B where replace(replace(upper(A.name),'Œ','OE'),'Æ','AE') = :favoriteName and A.id != :favoriteId and A.id = B.favorites_id", nativeQuery = true)
      Long findMaxIdForName(@Param("favoriteName") String favoriteName, @Param("favoriteId") Long favoriteId);

    public default FavoriteDB findOne(long id) {
      return findById(id).orElse(null);
    }
}
