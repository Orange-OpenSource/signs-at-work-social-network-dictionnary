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

import com.orange.signsatwork.biz.persistence.model.SignDB;
import com.orange.signsatwork.biz.persistence.model.FavoriteDB;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface SignRepository extends CrudRepository<SignDB, Long> {
    List<SignDB> findByName(String name);

    List<SignDB> findByOrderByCreateDateDesc();

    @Query("select distinct s FROM SignDB s where lower(s.name) like lower(concat('%',:searchTerm,'%')) order by s.createDate desc")
    List<SignDB> findAllBySearchTermOrderByCreateDateDesc(@Param("searchTerm") String searchTerm);

    @Query("select distinct s FROM SignDB s inner join s.favorites favorite where favorite = :favoriteDB")
    List<SignDB> findByFavorite(@Param("favoriteDB") FavoriteDB favoriteDB);

    @Query("select distinct s FROM SignDB s where s.createDate >= :lastConnectionDate")
    List<SignDB> findSignCreateAfterLastDateConnection(@Param("lastConnectionDate") Date lastConnectionDate);

    @Query("select distinct s FROM SignDB s where s.createDate < :lastConnectionDate")
    List<SignDB> findSignCreateBeforeLastDateConnection(@Param("lastConnectionDate") Date lastConnectionDate);

    @Query("select distinct s FROM SignDB s where lower(s.name) like lower(concat('%',:searchTerm,'%'))")
    List<SignDB> findAllBySearchTerm(@Param("searchTerm") String searchTerm);

    @Query("select distinct s FROM SignDB s where s.createDate >= :lastConnectionDate and lower(s.name) like lower(concat('%',:searchTerm,'%'))")
    List<SignDB> findSignCreateAfterLastDateConnectionBySearchTerm(@Param("lastConnectionDate") Date lastConnectionDate, @Param("searchTerm") String searchTerm);

    @Query("select distinct s FROM SignDB s where s.createDate < :lastConnectionDate and lower(s.name) like lower(concat('%',:searchTerm,'%'))")
    List<SignDB> findSignCreateBeforeLastDateConnectionBySearchTerm(@Param("lastConnectionDate") Date lastConnectionDate, @Param("searchTerm") String searchTerm);

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri from videos a inner join signs b on a.id = b.last_video_id order by b.create_date desc", nativeQuery = true)
    List<Object[]> findSignsForSignsView();

    @Query(value="select b.id, b.name, b.create_date, b.last_video_id, a.url, a.picture_uri from videos a inner join signs b on a.id = b.last_video_id and lower(b.name) like lower(concat('%', :searchTerm,'%')) order by b.create_date desc", nativeQuery = true)
    List<Object[]> findSignsForSignsViewBySearchTerm(@Param("searchTerm") String searchTerm);

}
