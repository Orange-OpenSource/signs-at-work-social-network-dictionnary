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

public class CommentData {

  public final Long id;
  public final String text;
  public final Date commentDate;
  public final String userName;
  public final String firstName;
  public final String lastName;
  public final Long userId;


  public CommentData(Object[] queryResultItem) {
    id = toLong(queryResultItem[0]);
    text = toString(queryResultItem[1]);
    commentDate = toDate(queryResultItem[2]);
    userName = toString(queryResultItem[3]);
    firstName = toString(queryResultItem[4]);
    lastName = toString(queryResultItem[5]);
    userId = toLong(queryResultItem[6]);
  }

  public String name() {
    if ((lastName == null || lastName.length() == 0) && (firstName == null || firstName.length() == 0)) {
      return userName;
    } else if (lastName == null || lastName.length() == 0) {
      return firstName;
    } else if (firstName == null || firstName.length() == 0) {
      return lastName;
    } else {
      return firstName + " " + lastName;
    }
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
