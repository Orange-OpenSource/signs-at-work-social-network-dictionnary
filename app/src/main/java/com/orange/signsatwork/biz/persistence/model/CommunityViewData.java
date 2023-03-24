package com.orange.signsatwork.biz.persistence.model;

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

import com.orange.signsatwork.biz.domain.Communities;
import com.orange.signsatwork.biz.domain.Community;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;

public class CommunityViewData {
  public final Long id;
  public final String type;
  public final String name;
  public Long ownerId = 0L;
  public String descriptionText;
  public String descriptionVideo;


  public CommunityViewData(Object[] queryResultItem) {
    id = toLong(queryResultItem[0]);
    type = toString(queryResultItem[1]);
    name = toString(queryResultItem[2]);
    if (queryResultItem[3] != null) {
      ownerId = toLong(queryResultItem[3]);
    }
  }

  public CommunityViewData(Community community) {
    this.id = community.id;
    this.type = community.type.toString();
    this.name = community.name;
    this.ownerId = community.user.id;
  }

  private String toString(Object o) {
    return (String) o;
  }

  private long toLong(Object o) {
    return ((BigInteger)o).longValue();
  }

}
