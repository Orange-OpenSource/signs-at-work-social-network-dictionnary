package com.orange.spring.demo.biz.persistence.repository;

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

import com.orange.spring.demo.biz.persistence.model.CommunityDB;
import com.orange.spring.demo.biz.persistence.model.UserDB;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class CommunityRepositoryIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private CommunityRepository communityRepository;

  private String name1 = "aristochat";

  private String name2 = "gangster";


  @Test
  public void returnAllPersisted() throws IOException {
    // given
    entityManager.persist(new CommunityDB(name1));
    entityManager.persist(new CommunityDB(name2));

    // do
    Iterable<CommunityDB> communities = communityRepository.findAll();
    CommunityDB community1 = communityRepository.findByName(name1).get(0);
    CommunityDB community2 = communityRepository.findByName(name2).get(0);

    // then
    assertThat(communities).hasSize(2);
    assertThat(communities).contains(community1);
    assertThat(communities).contains(community2);

    assertThat(community1.getName()).isEqualTo(name1);

    assertThat(community2.getName()).isEqualTo(name2);

  }

  @Test
  public void createCommunity() {
    // given
    // do
    entityManager.persist(new CommunityDB(name1));
    CommunityDB community1 = communityRepository.findByName(name1).get(0);
    // then
    assertThat(community1.getName()).isEqualTo(name1);

  }
}
