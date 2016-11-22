package com.orange.signsatwork.biz.sandboxremoveafteruse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SignsSort {

  /**
   * Sort criteria:
   *  - created since last connexion first, then
   *  - modified (comment added or video changed) since last connexion, then
   *  - the rest
   * @param signs to sort
   * @return sorted signs
   */
  public List<ComparableSign> sort(List<ComparableSign> signs) {
    List<ComparableSign> createdSinceLastConnexion = signs.stream()
      .filter(ComparableSign::createdSinceLastConnexion)
      .collect(Collectors.toList());

    signs.removeAll(createdSinceLastConnexion);

    List<ComparableSign> commentAddedSinceLastConnexion = signs.stream()
      .filter(ComparableSign::modifiedSinceLastConnexion)
      .collect(Collectors.toList());

    signs.removeAll(commentAddedSinceLastConnexion);

    List<ComparableSign> sortedSigns = new ArrayList<>();
    sortedSigns.addAll(createdSinceLastConnexion);
    sortedSigns.addAll(commentAddedSinceLastConnexion);
    sortedSigns.addAll(signs);

    return sortedSigns;
  }
}
