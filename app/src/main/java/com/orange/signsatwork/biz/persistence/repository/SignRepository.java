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
import com.orange.signsatwork.biz.persistence.model.SignDB;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface SignRepository extends CrudRepository<SignDB, Long> {
    List<SignDB> findByName(String name);

    List<SignDB> findByNameStartingWith(String name);

    @Query("select distinct s FROM SignDB s inner join s.favorites favorite where favorite = :favoriteDB")
    List<SignDB> findByFavorite(@Param("favoriteDB") FavoriteDB favoriteDB);

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b on a.id = b.last_video_id order by b.create_date desc", nativeQuery = true)
    List<Object[]> findSignsForSignsView();

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b on a.id = b.last_video_id union select 0, name, request_date, id, \"/sec/my-request-detail/\", \"/img/request.jpg\", 0 from requests where sign_id is null and user_id= :userId union select 0, name, request_date, id, \"/sec/other-request-detail/\", \"/img/request.jpg\", 0 from requests where sign_id is null and user_id != :userId order by name asc", nativeQuery = true)
    List<Object[]> findSignsAndRequestsAlphabeticalOrderAscForSignsView(@Param("userId") long userId);

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b on a.id = b.last_video_id union select 0, name, request_date, id, \"/sec/my-request-detail/\", \"/img/request.jpg\", 0 from requests where sign_id is null and user_id= :userId union select 0, name, request_date, id, \"/sec/other-request-detail/\", \"/img/request.jpg\", 0 from requests where sign_id is null and user_id != :userId order by name desc", nativeQuery = true)
    List<Object[]> findSignsAndRequestsAlphabeticalOrderDescForSignsView(@Param("userId") long userId);

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri from videos a inner join signs b on a.id = b.last_video_id and lower(b.name) like lower(concat('%', :searchTerm,'%')) order by b.create_date desc", nativeQuery = true)
    List<Object[]> findSignsForSignsViewBySearchTerm(@Param("searchTerm") String searchTerm);

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b inner join favorites_signs c on a.id = b.last_video_id and c.signs_id = b.id and c.favorites_id = :favoriteId order by b.create_date desc", nativeQuery = true)
    List<Object[]> findSignsForFavoriteView(@Param("favoriteId") long favoriteId);

    @Query(value="select a.signs_id from favorites_signs a inner join favorites b on a.favorites_id = b.id and b.user_id = :userId", nativeQuery = true)
    Long[] findSignsForAllFavoriteByUser(@Param("userId") long userId);

    @Query(value="select a.rating  from ratings a inner join signs b on a.video_id = b.last_video_id and b.id = :signId and user_id = :userId", nativeQuery = true)
    Object[] findRatingForSignByUser(@Param("signId") long signId, @Param("userId") long userId );

    @Query(value="select a.text, a.comment_date, c.first_name, c.last_name  from comments a inner join signs b inner join userdb c on a.video_id = b.last_video_id and b.id = :signId and a.user_id=c.id order by comment_date desc", nativeQuery = true)
    List<Object[]> findAllCommentsForSign(@Param("signId") long signId);

    @Query(value="select a.sign_id, b.name, a.create_date, a.id, a.url, a.picture_uri, a.nb_view, a.id_for_name, b.nb_video from videos a inner join signs b on b.id = a.sign_id order by b.name", nativeQuery = true)
    List<Object[]> findAllVideosForAllSigns();

    @Query(value="select a.create_date, b.first_name, b.last_name from videos a inner join userdb b on a.sign_id = :signId and a.user_id = b.id order by a.create_date desc", nativeQuery = true)
    List<Object[]> findAllVideosHistoryForSign(@Param("signId") long signId);

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b inner join associate_sign c on a.id = b.last_video_id and c.associate_sign_id = b.id and c.sign_id = ? union select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b inner join associate_sign c on a.id = b.last_video_id and c.sign_id = b.id and c.associate_sign_id = ?;", nativeQuery = true)
    List<Object[]> findAssociateSigns(long signId, long associateSignId);

    @Query(value="select a.sign_id, b.name, a.create_date, a.id, a.url, a.picture_uri, a.nb_view, a.id_for_name, b.nb_video from videos a inner join signs b on b.id = a.sign_id and a.sign_id = :signId order by a.create_date desc", nativeQuery = true)
    List<Object[]> findAllVideosForSign(@Param("signId") long signId);

    @Query(value="select  b.id, count(a.text) as nbr from comments a inner join videos b on a.video_id = b.id  and b.sign_id = :signId group by b.id order by nbr asc", nativeQuery = true)
    Long[] findNbCommentForAllVideoBySign(@Param("signId") long signId);

    @Query(value=" select  b.id, count(a.rating) as nbr from ratings a inner join videos b on a.video_id = b.id  and a.rating='Positive' and b.sign_id = :signId group by b.id order by nbr asc", nativeQuery = true)
    Long[] findNbPositiveRateForAllVideoBySign(@Param("signId") long signId);

    @Query(value="select  a.sign_id, sum(a.nb_view) as nbr from videos a where a.nb_view != 0 group by a.sign_id order by nbr desc", nativeQuery = true)
    Long[] findMostViewed();

    @Query(value="select  a.sign_id, sum(a.nb_view) as nbr from videos a where a.nb_view != 0 group by a.sign_id order by nbr asc", nativeQuery = true)
    Long[] findLowViewed();

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b on a.id = b.last_video_id where b.create_date > :lastDeconnectionDate order by b.create_date desc", nativeQuery = true)
    List<Object[]> findMostRecent(@Param("lastDeconnectionDate") Date lastDeconnectionDate);

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b on a.id = b.last_video_id where b.create_date > :lastDeconnectionDate order by b.create_date asc", nativeQuery = true)
    List<Object[]> findLowRecent(@Param("lastDeconnectionDate") Date lastDeconnectionDate);

}
