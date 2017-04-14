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

import org.fest.assertions.Assertions;
import org.junit.Test;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;

public class SignViewDataTest {

  @Test
  public void build_signs_view_data_from_query_result() {
    // Given
    long id = 12;
    String name = "asterix";
    Date createDate = new Date(123456);
    long lastVideoId = 34;
    String url = "http://obelix";
    String pictureUri = "http://obelix.jpg";
    long nbVideo = 1;

    Object[] queryResultItem = buildQueryResult(id, name, createDate, lastVideoId, url, pictureUri, nbVideo);

    // When
    SignViewData signViewData = new SignViewData(queryResultItem);

    // Then
    Assertions.assertThat(signViewData.id).isEqualTo(id);
    Assertions.assertThat(signViewData.name).isEqualTo(name);
    Assertions.assertThat(signViewData.createDate).isEqualTo(createDate);
    Assertions.assertThat(signViewData.lastVideoId).isEqualTo(lastVideoId);
    Assertions.assertThat(signViewData.url).isEqualTo(url);
    Assertions.assertThat(signViewData.pictureUri).isEqualTo(pictureUri);
  }

  private Object[] buildQueryResult(long id, String name, Date createDate, long lastVideoId, String url, String pictureUri, long nbVideo) {
    Object[] queryItem = {
      BigInteger.valueOf(id),
      name,
      new Timestamp(createDate.getTime()),
      BigInteger.valueOf(lastVideoId),
      url,
      pictureUri,
      BigInteger.valueOf(nbVideo)
    };
    return queryItem;
  }

}
