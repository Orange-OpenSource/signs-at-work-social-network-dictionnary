package com.orange.signsatwork.biz.persistence.model;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;

public class SignViewData {

  public final long id;
  public final String name;
  public final Date createDate;
  public final long lastVideoId;
  public final String url;
  public final String pictureUri;


  public SignViewData(Object[] queryResultItem) {
    id = toLong(queryResultItem[0]);
    name = toString(queryResultItem[1]);
    createDate = toDate(queryResultItem[2]);
    lastVideoId = toLong(queryResultItem[3]);
    url = toString(queryResultItem[4]);
    pictureUri = toString(queryResultItem[5]);
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
