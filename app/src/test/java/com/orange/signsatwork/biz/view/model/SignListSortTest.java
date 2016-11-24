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

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SignListSortTest {

  @Test
  public void signs_created_since_last_connexion_appears_first_then_signs_modified() {
    // Given
    List<SignsView> signs = buildTestSigns();
    SignsViewSort signsViewSort = new SignsViewSort();

    // When
    List<? extends ComparableSign> signsSorted = signsViewSort.sort(signs);

    // Then
    Assertions.assertThat(signsSorted.get(0).id()).isEqualTo(3);
    Assertions.assertThat(signsSorted.get(1).id()).isEqualTo(4);
    Assertions.assertThat(signsSorted.get(2).id()).isEqualTo(1);
    Assertions.assertThat(signsSorted.get(3).id()).isEqualTo(0);
    Assertions.assertThat(signsSorted.get(4).id()).isEqualTo(2);
  }

  private List<SignsView> buildTestSigns() {
    List<SignsView> signs = new ArrayList<>();
    signs.add(buildComparableSignWith(0, false, false));
    signs.add(buildComparableSignWith(1, false, true));
    signs.add(buildComparableSignWith(2, false, false));
    signs.add(buildComparableSignWith(3, true, false));
    signs.add(buildComparableSignWith(4, true, true));
    return signs;
  }

  private SignsView buildComparableSignWith(
    int id, boolean createdSinceLastConnexion, boolean modifiedSinceLastConnexion) {

    return new SignsView(id, null, null, null, createdSinceLastConnexion, modifiedSinceLastConnexion);
  }
}
