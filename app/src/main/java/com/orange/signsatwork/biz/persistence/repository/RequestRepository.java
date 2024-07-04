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

import com.orange.signsatwork.biz.persistence.model.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RequestRepository extends CrudRepository<RequestDB, Long> {
    List<RequestDB> findByName(String name);

    List<RequestDB> findByOrderByRequestDateDesc();

    RequestDB findBySign(SignDB signDB);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user = :userDB")
    List<RequestDB> findByUser(@Param("userDB") UserDB userDB);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user = :userDB order by c.requestDate desc")
    List<RequestDB> findMyRequestMostRecent(@Param("userDB") UserDB userDB);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user = :userDB order by c.requestDate asc")
    List<RequestDB> findMyRequestLowRecent(@Param("userDB") UserDB userDB);

    @Query(value = "select * FROM requests where user_id = :userId order by lower(name) collate utf8_unicode_ci desc", nativeQuery = true)
    List<RequestDB> findMyRequestAlphabeticalOrderDesc(@Param("userId") long userId);

    @Query(value = "select * FROM requests where user_id = :userId order by lower(name) collate utf8_unicode_ci asc", nativeQuery = true)
    List<RequestDB> findMyRequestAlphabeticalOrderAsc(@Param("userId") long userId);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user = :userDB and c.sign is null order by c.requestDate desc ")
    List<RequestDB> findByUserWithoutSignAssociate(@Param("userDB") UserDB userDB);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user = :userDB and c.sign is not null")
    List<RequestDB> findByUserWithSignAssociate(@Param("userDB") UserDB userDB);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user <> :userDB and c.sign is null")
    List<RequestDB> findByOtherUserWithoutSignAssociate(@Param("userDB") UserDB userDB);

    @Query(value="select a.name, concat(\"/sec/my-request-detail/\", a.id), a.sign_id, a.name as sign_name from requests a where replace(replace(upper(a.name),'Œ','OE'),'Æ','AE') collate utf8_unicode_ci like concat('%',:name,'%') and a.user_id = :userId and a.sign_id is null union select b.name, concat(\"/sec/other-request-detail/\", b.id), b.sign_id, b.name as sign_name  from requests b where replace(replace(upper(b.name),'Œ','OE'),'Æ','AE') collate utf8_unicode_ci like concat('%',:name,'%') and b.user_id != :userId and b.sign_id is null", nativeQuery = true)
    List<Object[]> findRequestsByNameWithNoAssociateSign(@Param("name") String name,@Param("userId") long userId);

    @Query(value="select a.name, concat(\"/sec/my-request-detail/\", a.id), a.sign_id, c.name as sign_name from requests a join signs c on c.id = a.sign_id and c.name != a.name and replace(replace(upper(a.name),'Œ','OE'),'Æ','AE') collate utf8_unicode_ci like concat('%',:name,'%') and a.user_id = :userId and a.sign_id is not null union select b.name, concat(\"/sec/other-request-detail/\", b.id), b.sign_id, d.name as sign_name from requests b join signs d on d.id = b.sign_id and d.name != b.name and replace(replace(upper(b.name),'Œ','OE'),'Æ','AE') collate utf8_unicode_ci like concat('%',:name,'%') and b.user_id != :userId and b.sign_id is not null", nativeQuery = true)
    List<Object[]> findRequestsByNameWithAssociateSign(@Param("name") String name,@Param("userId") long userId);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user <> :userDB and c.sign is null order by c.requestDate desc")
    List<RequestDB> findOtherRequestWithNoSignMostRecent(@Param("userDB") UserDB userDB);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user <> :userDB and c.sign is null order by c.requestDate asc")
    List<RequestDB> findOtherRequestWithNoSignLowRecent(@Param("userDB") UserDB userDB);

    @Query(value = "select * FROM requests where user_id != :userId and sign_id is null order by lower(name) collate utf8_unicode_ci desc", nativeQuery = true)
    List<RequestDB> findOtherRequestWithNoSignAlphabeticalOrderDesc(@Param("userId") long userId);

    @Query(value = "select * FROM requests where user_id != :userId and sign_id is null order by lower(name) collate utf8_unicode_ci asc", nativeQuery = true)
    List<RequestDB> findOtherRequestWithNoSignAlphabeticalOrderAsc(@Param("userId") long userId);

    @Query(value = "select * FROM requests order by lower(name) collate utf8_unicode_ci asc", nativeQuery = true)
    List<RequestDB> findAllRequestsAlphabeticalOrderAsc();

  public default RequestDB findOne(long id) {
      return findById(id).orElse(null);
    }
  }
