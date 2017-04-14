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

public class VideoViewData {

  public final long signId;
  public final String signName;
  public final Date createDate;
  public final long videoId;
  public final String url;
  public final String pictureUri;
  public final long nbView;
  public final long averageRate;
  public final long nbComment;
  public final long idForName;
  public final long nbVideo;


  public VideoViewData(Object[] queryResultItem) {
    signId = toLong(queryResultItem[0]);
    signName = toString(queryResultItem[1]);
    createDate = toDate(queryResultItem[2]);
    videoId = toLong(queryResultItem[3]);
    url = toString(queryResultItem[4]);
    pictureUri = toString(queryResultItem[5]);
    nbView = toLong(queryResultItem[6]);
    averageRate = toLong(queryResultItem[7]);
    nbComment = toLong(queryResultItem[8]);
    idForName = toLong(queryResultItem[9]);
    nbVideo = toLong(queryResultItem[10]);
  }

  private String toString(Object o) {
    return (String) o;
  }

  private long toLong(Object o) {
    return ((BigInteger)o).longValue();
  }

  private Date toDate(Object o) {
    Timestamp timestamp = ((Timestamp)o);
    return new Date(timestamp.getTime());
  }
}
