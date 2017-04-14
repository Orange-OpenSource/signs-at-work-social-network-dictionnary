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

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;

public class RequestViewData {

  public final String requestName;
  public final String urlForAccessToRequestDetail;
  public final long signId;
  public final String signName;

  public RequestViewData(Object[] queryResultItem) {
    requestName = toString(queryResultItem[0]);
    urlForAccessToRequestDetail = toString(queryResultItem[1]);
    signId = toLong(queryResultItem[2]);
    signName = toString(queryResultItem[3]);
  }

  private String toString(Object o) {
    return (String) o;
  }

  private long toLong(Object o) {
    if (o == null) {
      return 0;
    };
    return ((BigInteger)o).longValue();
  }


}
