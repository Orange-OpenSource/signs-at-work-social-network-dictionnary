package com.orange.signsatwork.biz.sandboxremoveafteruse;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SignListSortTest {

  @Test
  public void signs_created_since_last_connexion_appears_first_then_signs_modified() {
    // Given
    List<ComparableSign> signs = buildTestSigns();
    SignsSort signsSort = new SignsSort();

    // When
    List<ComparableSign> signsSorted = signsSort.sort(signs);

    // Then
    Assertions.assertThat(signsSorted.get(0).id()).isEqualTo(3);
    Assertions.assertThat(signsSorted.get(1).id()).isEqualTo(4);
    Assertions.assertThat(signsSorted.get(2).id()).isEqualTo(1);
    Assertions.assertThat(signsSorted.get(3).id()).isEqualTo(0);
    Assertions.assertThat(signsSorted.get(4).id()).isEqualTo(2);
  }

  private List<ComparableSign> buildTestSigns() {
    List<ComparableSign> signs = new ArrayList<>();
    signs.add(buildComparableSignWith(0, false, false));
    signs.add(buildComparableSignWith(1, false, true));
    signs.add(buildComparableSignWith(2, false, false));
    signs.add(buildComparableSignWith(3, true, false));
    signs.add(buildComparableSignWith(4, true, true));
    return signs;
  }

  private ComparableSign buildComparableSignWith(
    int id, boolean createdSinceLastConnexion, boolean modifiedSinceLastConnexion) {

    return new ComparableSign() {
      @Override
      public int id() {
        return id;
      }
      @Override
      public boolean createdSinceLastConnexion() {
        return createdSinceLastConnexion;
      }
      @Override
      public boolean modifiedSinceLastConnexion() {
        return modifiedSinceLastConnexion;
      }
    };
  }
}
