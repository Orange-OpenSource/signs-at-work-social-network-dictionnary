package com.orange.signsatwork.biz.view.model;

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
import com.orange.signsatwork.biz.domain.RatingDat;
import com.orange.signsatwork.biz.domain.RatingId;
import com.orange.signsatwork.biz.domain.Ratings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RatingView {
  private RatingId primaryKey;
  private Date ratingDate;
  private Rating rating;


  public RatingDat toRating() {
    return new RatingDat(primaryKey, ratingDate, rating);
  }

  public static RatingView from(RatingDat ratingDat) {
    return new RatingView(ratingDat.primaryKey, ratingDat.ratingDate, ratingDat.rating);
  }

  public static List<RatingView> from(Ratings ratings) {
    return ratings.stream()
            .map(RatingView::from)
            .collect(Collectors.toList());
  }
}
