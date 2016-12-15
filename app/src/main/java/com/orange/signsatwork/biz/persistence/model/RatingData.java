package com.orange.signsatwork.biz.persistence.model;

import com.orange.signsatwork.biz.domain.Rating;

public class RatingData {
  public String ratingString;
  public boolean ratePositive;
  public boolean rateNoRate = true;
  public boolean rateNeutral;
  public boolean rateNegative;

  public RatingData(Object[] queryResultItem) {
    if (queryResultItem.length > 0) {
      ratingString = toString(queryResultItem[0]);
      Rating rating = Rating.valueOf(ratingString);
      rateNoRate = rating == Rating.NoRate;
      ratePositive = rating == Rating.Positive;
      rateNeutral = rating == Rating.Neutral;
      rateNegative = rating == Rating.Negative;
    }
  }

  private String toString(Object o) {
    return (String) o;
  }

}
