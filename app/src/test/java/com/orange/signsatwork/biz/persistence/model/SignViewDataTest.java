package com.orange.signsatwork.biz.persistence.model;

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

    Object[] queryResultItem = buildQueryResult(id, name, createDate, lastVideoId, url, pictureUri);

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

  private Object[] buildQueryResult(long id, String name, Date createDate, long lastVideoId, String url, String pictureUri) {
    Object[] queryItem = {
      BigInteger.valueOf(id),
      name,
      new Timestamp(createDate.getTime()),
      BigInteger.valueOf(lastVideoId),
      url,
      pictureUri
    };
    return queryItem;
  }

}
