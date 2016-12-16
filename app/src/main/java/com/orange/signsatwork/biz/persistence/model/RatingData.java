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
