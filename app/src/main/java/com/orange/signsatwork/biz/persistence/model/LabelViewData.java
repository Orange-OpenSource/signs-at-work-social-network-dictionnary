package com.orange.signsatwork.biz.persistence.model;
import com.orange.signsatwork.biz.domain.Label;
import java.math.BigInteger;

public class LabelViewData {
  public final Long id;
  public final String name;
  public final String type;

  public LabelViewData(Object[] queryResultItem) {
    id = toLong(queryResultItem[0]);
    name = toString(queryResultItem[1]);
    type = toString(queryResultItem[2]);
  }

  public LabelViewData(Label label) {
    this.id = label.id;
    this.name = label.name;
    this.type = label.type.toString();
  }

  private String toString(Object o) {
    return (String) o;
  }

  private long toLong(Object o) {
    return ((BigInteger)o).longValue();
  }

}