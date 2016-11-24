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

public class SignsViewSort {

  /**
   * Sort criteria:
   *  - created since last connexion first, then
   *  - modified (comment added or video changed) since last connexion, then
   *  - the rest
   * @param signs to sort
   * @return sorted signs
   */
  public List<SignsView> sort(List<SignsView> signs) {
    List<SignsView> createdSinceLastConnexion = signs.stream()
      .filter(SignsView::createdSinceLastConnexion)
      .collect(Collectors.toList());

    signs.removeAll(createdSinceLastConnexion);

    List<SignsView> commentAddedSinceLastConnexion = signs.stream()
      .filter(SignsView::modifiedSinceLastConnexion)
      .collect(Collectors.toList());

    signs.removeAll(commentAddedSinceLastConnexion);

    List<SignsView> sortedSigns = new ArrayList<>();
    sortedSigns.addAll(createdSinceLastConnexion);
    sortedSigns.addAll(commentAddedSinceLastConnexion);
    sortedSigns.addAll(signs);

    return sortedSigns;
  }
}
