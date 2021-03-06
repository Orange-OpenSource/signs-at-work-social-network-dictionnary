package com.orange.signsatwork.biz.webservice.model;

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

import com.orange.signsatwork.biz.domain.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;

@Getter
@AllArgsConstructor

public class CommentViewApi {
  private String text;
  private Date date;
  private String userName;


  public CommentViewApi(Object[] queryResultItem) {
    text = toString(queryResultItem[0]);
    date = toDate(queryResultItem[1]);
    userName = name(toString(queryResultItem[2]),toString(queryResultItem[3]), toString(queryResultItem[4]));
  }

  private String toString(Object o) {
    return (String) o;
  }

  private Date toDate(Object o) {
    Timestamp timestamp = ((Timestamp)o);
    return new Date(timestamp.getTime());
  }
  public String name(String userName,String  firstName, String lastName) {
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
}
