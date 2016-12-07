package com.orange.signsatwork.biz.persistence.model;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;

public class CommentData {

  public final String text;
  public final Date commentDate;
  public final String firstName;
  public final String lastName;


  public CommentData(Object[] queryResultItem) {
    text = toString(queryResultItem[0]);
    commentDate = toDate(queryResultItem[1]);
    firstName = toString(queryResultItem[2]);
    lastName = toString(queryResultItem[3]);
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
