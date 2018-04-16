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

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b on a.id = b.last_video_id order by b.create_date desc", nativeQuery = true)
    List<Object[]> findSignsForSignsView();

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b on a.id = b.last_video_id and length(b.name) != 0 union select 0, name, request_date, id, \"/sec/my-request-detail/\", \"/img/my_request.png\", 0 from requests where sign_id is null and user_id= :userId and length(name) != 0 union select 0, name, request_date, id, \"/sec/other-request-detail/\", \"/img/request.jpg\", 0 from requests where sign_id is null and user_id != :userId and length(name) != 0 order by name asc", nativeQuery = true)
    List<Object[]> findSignsAndRequestsAlphabeticalOrderAscForSignsView(@Param("userId") long userId);

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b on a.id = b.last_video_id and length(b.name) != 0 union select 0, name, request_date, id, \"/sec/my-request-detail/\", \"/img/my_request.png\", 0 from requests where sign_id is null and user_id= :userId and length(name) != 0 union select 0, name, request_date, id, \"/sec/other-request-detail/\", \"/img/request.jpg\", 0 from requests where sign_id is null and user_id != :userId and length(name) != 0 order by name desc", nativeQuery = true)
    List<Object[]> findSignsAndRequestsAlphabeticalOrderDescForSignsView(@Param("userId") long userId);

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b on a.id = b.last_video_id order by name asc", nativeQuery = true)
    List<Object[]> findSignsAlphabeticalOrderAscForSignsView();

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b on a.id = b.last_video_id order by name desc", nativeQuery = true)
    List<Object[]> findSignsAlphabeticalOrderDescForSignsView();


  @Query(value="select distinct(c.sign_id) from favorites_videos a inner join favorites b inner join videos c on c.id = a.videos_id and a.favorites_id = b.id and b.user_id = :userId", nativeQuery = true)
    Long[] findSignsBellowToFavoriteByUser(@Param("userId") long userId);

    @Query(value="select a.sign_id, b.name, a.create_date, a.id, a.url, a.picture_uri, a.nb_view, a.average_rate, a.nb_comment, a.id_for_name, b.nb_video from videos a inner join signs b on b.id = a.sign_id order by b.name", nativeQuery = true)
    List<Object[]> findAllVideosForAllSigns();

    @Query(value="select a.create_date, b.username, b.first_name, b.last_name from videos a inner join userdb b on a.sign_id = :signId and a.user_id = b.id order by a.create_date desc", nativeQuery = true)
    List<Object[]> findAllVideosHistoryForSign(@Param("signId") long signId);

    @Query(value="select a.sign_id, b.name, a.create_date, a.id, a.url, a.picture_uri, a.nb_view, a.average_rate, a.nb_comment, a.id_for_name, b.nb_video from videos a inner join signs b on b.id = a.sign_id and a.sign_id = :signId order by a.create_date desc", nativeQuery = true)
    List<Object[]> findAllVideosForSign(@Param("signId") long signId);


    @Query(value="select  a.sign_id, sum(a.nb_view) as nbr from videos a where a.nb_view != 0 group by a.sign_id order by nbr desc", nativeQuery = true)
    Long[] findMostViewed();

    @Query(value="select  a.sign_id, sum(a.nb_view) as nbr from videos a where a.nb_view != 0 group by a.sign_id order by nbr asc", nativeQuery = true)
    Long[] findLowViewed();

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b on a.id = b.last_video_id where b.create_date > :lastDeconnectionDate order by b.create_date desc", nativeQuery = true)
    List<Object[]> findMostRecent(@Param("lastDeconnectionDate") Date lastDeconnectionDate);

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b on a.id = b.last_video_id where b.create_date > :lastDeconnectionDate order by b.create_date asc", nativeQuery = true)
    List<Object[]> findLowRecent(@Param("lastDeconnectionDate") Date lastDeconnectionDate);

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b on a.id = b.last_video_id order by b.create_date desc", nativeQuery = true)
    List<Object[]> findMostRecentWithoutDate();

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b on a.id = b.last_video_id order by b.create_date asc", nativeQuery = true)
    List<Object[]> findLowRecentWithoutDate();


  @Query(value="select  sign_id, sum(average_rate) as nbr from videos where average_rate != 0 group by sign_id having nbr > 0 order by nbr desc", nativeQuery = true)
    Long[] findMostRating();

    @Query(value="select  sign_id, sum(average_rate) as nbr from videos where average_rate != 0 group by sign_id having nbr > 0 order by nbr asc", nativeQuery = true)
    Long[] findLowRating();

    @Query(value="select  sign_id, sum(nb_comment) as nbr from videos where nb_comment != 0 group by sign_id having nbr != 0 order by nbr desc", nativeQuery = true)
    Long[] findMostCommented();

    @Query(value="select  sign_id, sum(nb_comment) as nbr from videos where nb_comment != 0 group by sign_id having nbr != 0 order by nbr asc", nativeQuery = true)
    Long[] findLowCommented();

    @Query(value="select count(*) from ratings r, videos v where r.video_id = v.id and v.sign_id =  :signId", nativeQuery = true)
    Long findNbRatingForSign(@Param("signId") long signId);

}
