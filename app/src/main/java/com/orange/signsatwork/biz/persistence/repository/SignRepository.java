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

import com.orange.signsatwork.biz.persistence.model.RequestDB;
import com.orange.signsatwork.biz.persistence.model.SignDB;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface SignRepository extends CrudRepository<SignDB, Long> {
    List<SignDB> findByName(String name);

    List<SignDB> findByNameIgnoreCaseStartingWith(String name);

    List<SignDB> findByNameIgnoreCase(String name);

    @Query(value="select id, name, create_date, last_video_id, url, 'picture_uri', nb_video from signs  where replace(replace(upper(name),'Œ','OE'),'Æ','AE') collate utf8_unicode_ci like concat('%',:name,'%')", nativeQuery = true)
    List<Object[]> findContainsNameIgnoreCase(@Param("name") String name);

    @Query(value="select id, name, create_date, last_video_id, url, 'picture_uri', nb_video from signs  where name = :name", nativeQuery = true)
    List<Object[]> findFullName(@Param("name") String name);

    @Query(value="select distinct(c.sign_id) from favorites_videos a inner join favorites b inner join videos c on c.id = a.videos_id and a.favorites_id = b.id and b.user_id = :userId", nativeQuery = true)
    Long[] findSignsBellowToFavoriteByUser(@Param("userId") long userId);


    @Query(value="select a.create_date, b.username, b.first_name, b.last_name from videos a inner join userdb b on a.sign_id = :signId and a.user_id = b.id order by a.create_date desc", nativeQuery = true)
    List<Object[]> findAllVideosHistoryForSign(@Param("signId") long signId);


    @Query(value="select count(*) from ratings r, videos v where r.video_id = v.id and v.sign_id =  :signId", nativeQuery = true)
    Long findNbRatingForSign(@Param("signId") long signId);

    public default SignDB findOne(long id) {
      return findById(id).orElse(null);
    }

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b inner join communities_videos c inner join communities_users d on a.id = b.last_video_id and b.id = a.sign_id and a.id = c.videos_id and c.communities_id = d.communities_id and d.users_id = :userId union  select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b on a.id = b.last_video_id and b.id = a.sign_id and a.user_id = :userId and a.id not in (select videos_id from communities_videos) order by create_date desc", nativeQuery = true)
    List<Object[]> findMostRecentWithoutDate(@Param("userId") long userId);

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b inner join communities_videos c inner join communities_users d on a.id = b.last_video_id and b.id = a.sign_id and a.id = c.videos_id and c.communities_id = d.communities_id and d.users_id = :userId union  select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b on a.id = b.last_video_id and b.id = a.sign_id and a.user_id = :userId and a.id not in (select videos_id from communities_videos) order by create_date asc", nativeQuery = true)
    List<Object[]> findLowRecentWithoutDate(@Param("userId") long userId);


    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b inner join communities_videos c inner join communities d on a.id = b.last_video_id and b.id = a.sign_id and a.id = c.videos_id and c.communities_id = d.id and d.type = \"Public\" order by create_date desc", nativeQuery = true)
    List<Object[]> findMostRecentWithoutDate();

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b inner join communities_videos c inner join communities d on a.id = b.last_video_id and b.id = a.sign_id and a.id = c.videos_id and c.communities_id = d.id and d.type = \"Public\" order by create_date asc", nativeQuery = true)
    List<Object[]> findLowRecentWithoutDate();
    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b inner join communities_videos c inner join communities_users d on a.id = b.last_video_id and b.id = a.sign_id and a.id = c.videos_id and c.communities_id = d.communities_id and d.users_id = :userId union  select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b on a.id = b.last_video_id and b.id = a.sign_id and a.user_id = :userId and a.id not in (select videos_id from communities_videos) order by lower(name) collate utf8_unicode_ci asc", nativeQuery = true)
    List<Object[]> findSignsAlphabeticalOrderAscForSignsView(@Param("userId") long userId);

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b inner join communities_videos c inner join communities_users d on a.id = b.last_video_id and b.id = a.sign_id and a.id = c.videos_id and c.communities_id = d.communities_id and d.users_id = :userId union  select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b on a.id = b.last_video_id and b.id = a.sign_id and a.user_id = :userId and a.id not in (select videos_id from communities_videos) order by lower(name) collate utf8_unicode_ci desc", nativeQuery = true)
    List<Object[]> findSignsAlphabeticalOrderDescForSignsView(@Param("userId") long userId);

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b inner join communities_videos c inner join communities d on a.id = b.last_video_id and b.id = a.sign_id and a.id = c.videos_id and c.communities_id = d.id and d.type = \"Public\" order by lower(b.name) collate utf8_unicode_ci asc", nativeQuery = true)
    List<Object[]> findSignsAlphabeticalOrderAscForSignsView();

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b inner join communities_videos c inner join communities d on a.id = b.last_video_id and b.id = a.sign_id and a.id = c.videos_id and c.communities_id = d.id and d.type = \"Public\" order by lower(b.name) collate utf8_unicode_ci desc", nativeQuery = true)
    List<Object[]> findSignsAlphabeticalOrderDescForSignsView();

    @Query(value="select  a.sign_id, sum(a.average_rate) as nbr from videos a inner join communities_videos b inner join communities_users c on a.id = b.videos_id and b.communities_id = c.communities_id and c.users_id = :userId and a.average_rate != 0 group by sign_id having nbr > 0 union select a.sign_id, sum(a.average_rate) as nbr from videos a where a.user_id = :userId and a.id not in (select videos_id from communities_videos) group by sign_id having nbr > 0 order by nbr desc", nativeQuery = true)
    Long[] findMostRating(@Param("userId") long userId);

  @Query(value="select  a.sign_id, sum(a.average_rate) as nbr from videos a inner join signs b inner join communities_videos c inner join communities d on a.id = b.last_video_id and b.id = a.sign_id and a.id = c.videos_id and c.communities_id = d.id and d.type = 'Public' and a.average_rate != 0 group by sign_id having nbr > 0 order by nbr desc", nativeQuery = true)
  Long[] findMostRating();

    @Query(value="select  a.sign_id, sum(a.average_rate) as nbr from videos a inner join communities_videos b inner join communities_users c on a.id = b.videos_id and b.communities_id = c.communities_id and c.users_id = :userId and a.average_rate != 0 group by sign_id having nbr > 0 union select a.sign_id, sum(a.average_rate) as nbr from videos a where a.user_id = :userId and a.id not in (select videos_id from communities_videos) group by sign_id having nbr > 0 order by nbr asc", nativeQuery = true)
    Long[] findLowRating(@Param("userId") long userId);

    @Query(value="select  a.sign_id, sum(a.average_rate) as nbr from videos a inner join signs b inner join communities_videos c inner join communities d on a.id = b.last_video_id and b.id = a.sign_id and a.id = c.videos_id and c.communities_id = d.id and d.type = 'Public' and a.average_rate != 0 group by sign_id having nbr > 0 order by nbr asc", nativeQuery = true)
    Long[] findLowRating();

    @Query(value="select  a.sign_id, sum(a.average_rate) as nbr from videos a inner join signs b inner join communities_videos c inner join communities_users d on a.id = b.last_video_id and b.id = a.sign_id and a.id = c.videos_id and c.communities_id = d.communities_id and d.users_id = :userId and a.average_rate != 0 union select a.sign_id, sum(a.average_rate) as nbr from videos a inner join signs b on b.id = a.sign_id and a.user_id = :userId and a.id not in (select videos_id from communities_videos) group by sign_id having nbr > 0 order by nbr desc", nativeQuery = true)
    Long[] findMostViewed(@Param("userId") long userId);

  @Query(value="select  a.sign_id, sum(a.nb_view) as nbr from videos a where a.nb_view != 0 group by a.sign_id order by nbr desc", nativeQuery = true)
  Long[] findMostViewed();
    @Query(value="select  a.sign_id, sum(a.average_rate) as nbr from videos a inner join signs b inner join communities_videos c inner join communities_users d on a.id = b.last_video_id and b.id = a.sign_id and a.id = c.videos_id and c.communities_id = d.communities_id and d.users_id = :userId and a.average_rate != 0 union select a.sign_id, sum(a.average_rate) as nbr from videos a inner join signs b on b.id = a.sign_id and a.user_id = :userId and a.id not in (select videos_id from communities_videos) group by sign_id having nbr > 0 order by nbr asc", nativeQuery = true)
    Long[] findLowViewed(@Param("userId") long userId);

    @Query(value="select  a.sign_id, sum(a.nb_comment) as nbr from videos a inner join signs b inner join communities_videos c inner join communities_users d on a.id = b.last_video_id and b.id = a.sign_id and a.id = c.videos_id and c.communities_id = d.communities_id and d.users_id = :userId and a.average_rate != 0 union select a.sign_id, sum(a.average_rate) as nbr from videos a inner join signs b on b.id = a.sign_id and a.user_id = :userId and a.id not in (select videos_id from communities_videos) group by sign_id having nbr > 0 order by nbr desc", nativeQuery = true)
    Long[] findMostCommented(@Param("userId") long userId);

  @Query(value="select  sign_id, sum(nb_comment) as nbr from videos where nb_comment != 0 group by sign_id having nbr != 0 order by nbr desc", nativeQuery = true)
  Long[] findMostCommented();

    @Query(value="select  a.sign_id, sum(a.nb_comment) as nbr from videos a inner join signs b inner join communities_videos c inner join communities_users d on a.id = b.last_video_id and b.id = a.sign_id and a.id = c.videos_id and c.communities_id = d.communities_id and d.users_id = :userId and a.average_rate != 0 union select a.sign_id, sum(a.average_rate) as nbr from videos a inner join signs b on b.id = a.sign_id and a.user_id = :userId and a.id not in (select videos_id from communities_videos) group by sign_id having nbr > 0 order by nbr asc", nativeQuery = true)
    Long[] findLowCommented(@Param("userId") long userId);

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b inner join communities_videos c inner join communities_users d on a.id = b.last_video_id and b.id = a.sign_id and a.id = c.videos_id and c.communities_id = d.communities_id and d.users_id = :userId union  select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b on a.id = b.last_video_id and b.id = a.sign_id and a.user_id = :userId and a.id not in (select videos_id from communities_videos) order by create_date desc", nativeQuery = true)
    List<Object[]> findSignsForSignsView(@Param("userId") long userId);

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri, b.nb_video from videos a inner join signs b inner join communities_videos c inner join communities d on a.id = b.last_video_id and b.id = a.sign_id and a.id = c.videos_id and c.communities_id = d.id and d.type = 'Public' order by create_date desc", nativeQuery = true)
    List<Object[]> findSignsForSignsView();

    @Query(value="select a.sign_id, b.name, a.create_date, a.id, a.url, a.picture_uri, a.nb_view, a.average_rate, a.nb_comment, a.id_for_name, b.nb_video from videos a inner join signs b  inner join communities_videos c inner join  communities_users d  on b.id = a.sign_id and a.sign_id = :signId and a.id = c.videos_id and c.communities_id = d.communities_id and d.users_id = :userId union select a.sign_id, b.name, a.create_date, a.id, a.url, a.picture_uri, a.nb_view, a.average_rate, a.nb_comment, a.id_for_name, b.nb_video from videos a inner join signs b on b.id = a.sign_id and a.sign_id = :signId and a.user_id = :userId and a.id not in (select videos_id from communities_videos) order by create_date desc", nativeQuery = true)
    List<Object[]> findAllVideosForSign(@Param("userId") long userId, @Param("signId") long signId);


  @Query(value="select a.sign_id, b.name, a.create_date, a.id, a.url, a.picture_uri, a.nb_view, a.average_rate, a.nb_comment, a.id_for_name, b.nb_video from videos a inner join signs b  inner join communities_videos c inner join  communities_users d  on b.id = a.sign_id and a.id = c.videos_id and c.communities_id = d.communities_id and d.users_id = :userId union select a.sign_id, b.name, a.create_date, a.id, a.url, a.picture_uri, a.nb_view, a.average_rate, a.nb_comment, a.id_for_name, b.nb_video from videos a inner join signs b on b.id = a.sign_id and a.user_id = :userId and a.id not in (select videos_id from communities_videos) order by lower(name)", nativeQuery = true)
  List<Object[]> findAllVideosForAllSigns(@Param("userId") long userId);
}
