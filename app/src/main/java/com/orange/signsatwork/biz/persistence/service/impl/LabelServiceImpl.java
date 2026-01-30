package com.orange.signsatwork.biz.persistence.service.impl;

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

import com.orange.signsatwork.biz.domain.*;
import com.orange.signsatwork.biz.persistence.model.*;
import com.orange.signsatwork.biz.persistence.repository.*;
import com.orange.signsatwork.biz.persistence.service.FavoriteService;
import com.orange.signsatwork.biz.persistence.service.LabelService;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LabelServiceImpl implements LabelService {
  private final LabelRepository labelRepository;
  private final CommunityRepository communityRepository;
  private final Services services;

  @Override
  public Labels findLabelsOrderByNameAsc() {
    return labelsFrom(labelRepository.findByOrderByNameAsc());
  }
  @Override
  public Labels labelsByType(LabelType type) {
    return labelsFrom(labelRepository.findLabelsByType(type));
  }

  @Override
  public List<Object[]> searchBis(String labelName) {
    List<Object[]>  labelsMatches = labelRepository.findStartByNameIgnoreCase(labelName);

    return labelsMatches;
  }

  @Override
  public Label withLabelName(String labelName) {
    List<LabelDB> labelDBList = labelRepository.findByName(labelName);
    if (labelDBList.size() == 1) {
      return labelFrom(labelDBList.get(0));
    } else if (labelDBList.size() > 1){
      String err = "Error while retrieving label with labelName = '" + labelName + "' (list size = " + labelDBList.size() + ")";
      RuntimeException e = new IllegalStateException(err);
      log.error(err, e);
      throw e;
    } else {
      return null;
    }
  }

  @Override
  public Label create(Label label) {
    LabelDB labelDB;

    labelDB = new LabelDB(label.name, label.type);
    labelRepository.save(labelDB);


    return labelFrom(labelDB);
  }

  @Override
  public Label withId(long id) {
    return labelFrom(labelRepository.findOne(id));
  }

  @Override
  public Labels withNameIgnoreCase(String name) {
    return labelsFrom(labelRepository.findByNameIgnoreCase(name));
  }

  @Override
  public void delete(Label label) {
    LabelDB labelDB = labelRepository.findOne(label.id);
    labelRepository.delete(labelDB);
  }

  @Override
  public long findNbSignForLabel(long id) {
    return labelRepository.findNbSignForLabel(id);
  }

  @Override
  public void renameLabel(Long labelId, String name) {
    LabelDB labelDB = labelRepository.findOne(labelId);

    labelDB.setName(name);
    labelRepository.save(labelDB);
  }


  private Labels labelsFrom(Iterable<LabelDB> labelsDB) {
    List<Label>labels = new ArrayList<>();
    labelsDB.forEach(lavelDB -> labels.add(labelFrom(lavelDB)));
    return new Labels(labels);
  }
  static Label labelFrom(LabelDB labelDB) {
    return labelDB == null ? null :
      new Label(labelDB.getId(), labelDB.getName(), labelDB.getType());
  }

}
