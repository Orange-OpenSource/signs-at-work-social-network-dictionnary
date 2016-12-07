package com.orange.signsatwork.biz.persistence.model;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;

public class VideoHistoryData {

  public final Date createDate;
  public final String firstName;
  public final String lastName;


  public VideoHistoryData(Object[] queryResultItem) {
    createDate = toDate(queryResultItem[0]);
    firstName = toString(queryResultItem[1]);
    lastName = toString(queryResultItem[2]);
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
