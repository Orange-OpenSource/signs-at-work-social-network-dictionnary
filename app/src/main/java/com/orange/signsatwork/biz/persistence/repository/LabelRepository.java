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

import com.orange.signsatwork.biz.domain.LabelType;
import com.orange.signsatwork.biz.persistence.model.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LabelRepository extends CrudRepository<LabelDB, Long> {

    List<LabelDB> findByName(String name);

    List<LabelDB> findByOrderByNameAsc();

    @Query(value="select distinct id, name, type from labels where type = :type order by lower(name) collate utf8_unicode_ci asc", nativeQuery = true)
    List<LabelDB> findLabelsByType(@Param("type") LabelType type);

    @Query(value="select id, name, type from labels  where replace(replace(upper(name),'Œ','OE'),'Æ','AE') collate utf8_unicode_ci like concat('%',:name,'%')", nativeQuery = true)
    List<Object[]> findStartByNameIgnoreCase(@Param("name") String name);

  public default LabelDB findOne(long id) {
    return findById(id).orElse(null);
  }

}
