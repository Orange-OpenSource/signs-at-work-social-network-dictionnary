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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SignsViewSort2 {

  /**
   * Sort criteria:
   *  - created since last connection first, then
   *  - modified (comment added or video changed) since last connection, then
   *  - the rest
   * @param signs to sort
   * @return sorted signs
   */
  public List<SignView2> sort(List<SignView2> signs, boolean onlyActiveSign) {
    List<SignView2> createdSinceLastDeconnection = signs.stream()
      .filter(SignView2::createdSinceLastDeconnection)
      .collect(Collectors.toList());

    signs.removeAll(createdSinceLastDeconnection);

    List<SignView2> commentAdded = signs.stream()
      .filter(SignView2::hasComment)
      .collect(Collectors.toList());

    signs.removeAll(commentAdded);

    List<SignView2> viewAdded = signs.stream()
      .filter(SignView2::hasView)
      .collect(Collectors.toList());

    signs.removeAll(viewAdded);

    List<SignView2> positiveRateAdded = signs.stream()
      .filter(SignView2::hasPositiveRate)
      .collect(Collectors.toList());

    signs.removeAll(positiveRateAdded);

    List<SignView2> favoriteAdded = signs.stream()
      .filter(SignView2::belowToFavorite)
      .collect(Collectors.toList());

    signs.removeAll(favoriteAdded);

    List<SignView2> sortedSigns = new ArrayList<>();
    sortedSigns.addAll(createdSinceLastDeconnection);
    sortedSigns.addAll(commentAdded);
    sortedSigns.addAll(viewAdded);
    sortedSigns.addAll(positiveRateAdded);
    sortedSigns.addAll(favoriteAdded);
    if (!onlyActiveSign) {
      sortedSigns.addAll(signs);
    }

    return sortedSigns;
  }
}
