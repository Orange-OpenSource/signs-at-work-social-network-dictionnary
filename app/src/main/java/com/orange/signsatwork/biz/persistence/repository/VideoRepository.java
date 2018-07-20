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
import com.orange.signsatwork.biz.persistence.model.VideoDB;
import com.orange.signsatwork.biz.persistence.model.UserDB;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VideoRepository extends CrudRepository<VideoDB, Long> {

    @Query("select distinct c FROM VideoDB c inner join c.sign sign where sign = :signDB order by c.id")
    List<VideoDB> findBySign(@Param("signDB") SignDB signDB);

    @Query("select distinct c FROM VideoDB c inner join c.user user where user = :userDB")
    List<VideoDB> findByUser(@Param("userDB") UserDB userDB);

    @Query(value="select a.rating  from ratings a where a.video_id = :videoId and a.user_id = :userId", nativeQuery = true)
    Object[] findRatingForVideoByUser(@Param("videoId") long signId, @Param("userId") long userId );

    @Query(value="select a.text, a.comment_date, c.username, c.first_name, c.last_name  from comments a inner join userdb c on a.video_id = :videoId and a.user_id=c.id order by comment_date desc", nativeQuery = true)
    List<Object[]> findAllCommentsForVideo(@Param("videoId") long videoId);

    @Query(value="select a.sign_id, b.name, a.create_date, a.id, a.url, a.picture_uri, a.nb_view, a.average_rate, a.nb_comment, a.id_for_name, b.nb_video from videos a inner join signs b inner join associate_video c on a.sign_id = b.id and c.associate_video_id = a.id and c.video_id = ? union select a.sign_id, b.name, a.create_date, a.id, a.url, a.picture_uri, a.nb_view, a.average_rate, a.nb_comment, a.id_for_name, b.nb_video from videos a inner join signs b inner join associate_video c on a.sign_id=b.id and c.video_id = a.id and c.associate_video_id = ?" , nativeQuery = true)
    List<Object[]> findAssociateVideos(long videoId, long associateVideoId);

    @Query("select distinct s FROM VideoDB s inner join s.favorites favorite where favorite = :favoriteDB")
    List<VideoDB> findByFavorite(@Param("favoriteDB") FavoriteDB favoriteDB);

    @Query(value="select a.sign_id, b.name, a.create_date, a.id, a.url, a.picture_uri, a.nb_view, a.average_rate, a.nb_comment, a.id_for_name, b.nb_video from videos a inner join signs b inner join favorites_videos c on a.sign_id = b.id and c.videos_id = a.id and c.favorites_id = :favoriteId order by b.create_date desc", nativeQuery = true)
    List<Object[]> findVideosForFavoriteView(@Param("favoriteId") long favoriteId);

    @Query(value="select a.videos_id from favorites_videos a inner join favorites b on a.favorites_id = b.id and b.user_id = :userId", nativeQuery = true)
    Long[] findVideosForAllFavoriteByUser(@Param("userId") long userId);

    @Query(value="select count(a.favorites_id) from favorites_videos a inner join favorites b on a.favorites_id = b.id and a.videos_id = :videoId and b.user_id = :userId", nativeQuery = true)
    Long findNbFavoriteBelowVideoForUser(@Param("videoId") long videoId, @Param("userId") long userId);

    @Query(value="select a.sign_id, b.name, a.create_date, a.id, a.url, a.picture_uri, a.nb_view, a.average_rate, a.nb_comment, a.id_for_name, b.nb_video from videos a inner join signs b on a.sign_id = b.id and a.user_id = :userId order by b.create_date desc", nativeQuery = true)
    List<Object[]> findAllVideosCreateByUser(@Param("userId") long userId);

}
