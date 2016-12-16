package com.orange.signsatwork.biz.view.controller;

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


import com.orange.signsatwork.biz.persistence.model.SignViewData;

import java.util.Comparator;
import java.util.List;

public class CommentOrderComparator implements Comparator<SignViewData> {
  private final List<Long> signWithCommentList;

  public CommentOrderComparator(List<Long> signWithCommentList) {
    this.signWithCommentList = signWithCommentList;
  }

  @Override
  public int compare(SignViewData o1, SignViewData o2) {
    SignViewData signViewData1 = (SignViewData) o1;
    int indexSign1 = signWithCommentList.indexOf(signViewData1.id);
    SignViewData signViewData2 = (SignViewData) o2;
    int indexSign2 = signWithCommentList.indexOf(signViewData2.id);

    return Integer.compare(indexSign1, indexSign2);
  }
}
