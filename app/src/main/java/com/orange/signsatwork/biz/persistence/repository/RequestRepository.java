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
import com.orange.signsatwork.biz.persistence.model.UserDB;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RequestRepository extends CrudRepository<RequestDB, Long> {
    List<RequestDB> findByName(String name);

    RequestDB findBySign(SignDB signDB);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user = :userDB")
    List<RequestDB> findByUser(@Param("userDB") UserDB userDB);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user = :userDB order by c.requestDate desc")
    List<RequestDB> findMyRequestMostRecent(@Param("userDB") UserDB userDB);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user = :userDB order by c.requestDate asc")
    List<RequestDB> findMyRequestLowRecent(@Param("userDB") UserDB userDB);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user = :userDB order by c.name desc")
    List<RequestDB> findMyRequestAlphabeticalOrderDesc(@Param("userDB") UserDB userDB);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user = :userDB order by c.name asc")
    List<RequestDB> findMyRequestAlphabeticalOrderAsc(@Param("userDB") UserDB userDB);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user = :userDB and c.sign is null order by c.requestDate desc ")
    List<RequestDB> findByUserWithoutSignAssociate(@Param("userDB") UserDB userDB);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user = :userDB and c.sign is not null")
    List<RequestDB> findByUserWithSignAssociate(@Param("userDB") UserDB userDB);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user <> :userDB and c.sign is null")
    List<RequestDB> findByOtherUserWithoutSignAssociate(@Param("userDB") UserDB userDB);

    @Query(value="select a.name, concat(\"/sec/my-request-detail/\", a.id), a.sign_id, a.name as sign_name from requests a where a.name like concat(:name,'%') and a.user_id = :userId and a.sign_id is null union select b.name, concat(\"/sec/other-request-detail/\", b.id), b.sign_id, b.name as sign_name  from requests b where b.name like concat(:name,'%') and b.user_id != :userId and b.sign_id is null", nativeQuery = true)
    List<Object[]> findRequestsByNameWithNoAssociateSign(@Param("name") String name,@Param("userId") long userId);

    @Query(value="select a.name, concat(\"/sec/my-request-detail/\", a.id), a.sign_id, c.name as sign_name from requests a join signs c on c.id = a.sign_id and c.name != a.name and a.name like concat(:name,'%') and a.user_id = :userId and a.sign_id is not null union select b.name, concat(\"/sec/other-request-detail/\", b.id), b.sign_id, d.name as sign_name from requests b join signs d on d.id = b.sign_id and d.name != b.name and b.name like concat(:name,'%') and b.user_id != :userId and b.sign_id is not null", nativeQuery = true)
    List<Object[]> findRequestsByNameWithAssociateSign(@Param("name") String name,@Param("userId") long userId);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user <> :userDB and c.sign is null order by c.requestDate desc")
    List<RequestDB> findOtherRequestWithNoSignMostRecent(@Param("userDB") UserDB userDB);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user <> :userDB and c.sign is null order by c.requestDate asc")
    List<RequestDB> findOtherRequestWithNoSignLowRecent(@Param("userDB") UserDB userDB);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user <> :userDB and c.sign is null order by c.name desc")
    List<RequestDB> findOtherRequestWithNoSignAlphabeticalOrderDesc(@Param("userDB") UserDB userDB);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user <> :userDB and c.sign is null order by c.name asc")
    List<RequestDB> findOtherRequestWithNoSignAlphabeticalOrderAsc(@Param("userDB") UserDB userDB);
  }
