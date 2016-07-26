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
import com.orange.signsatwork.biz.persistence.model.UserDB;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RequestRepository extends CrudRepository<RequestDB, Long> {
    List<RequestDB> findByName(String name);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user = :userDB")
    List<RequestDB> findByUser(@Param("userDB") UserDB userDB);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user = :userDB and c.sign is null")
    List<RequestDB> findByUserWithoutSignAssociate(@Param("userDB") UserDB userDB);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user = :userDB and c.sign is not null")
    List<RequestDB> findByUserWithSignAssociate(@Param("userDB") UserDB userDB);

    @Query("select distinct c FROM RequestDB c inner join c.user user where user <> :userDB and c.sign is null")
    List<RequestDB> findByOtherUserWithoutSignAssociate(@Param("userDB") UserDB userDB);

}
