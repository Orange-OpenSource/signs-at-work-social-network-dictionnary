package com.orange.signsatwork.biz.domain;

/** interface which describes methods required to sort a signs' list */
public interface ComparableSign {
  /** sign unique id */
  long id();

  boolean createdSinceLastConnexion();

  /** modified means: a comment was added, or the video changed */
  boolean modifiedSinceLastConnexion();
}
