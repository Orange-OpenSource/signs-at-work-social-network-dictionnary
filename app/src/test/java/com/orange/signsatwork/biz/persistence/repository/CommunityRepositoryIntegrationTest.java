package com.orange.signsatwork.biz.persistence.repository;

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

import com.orange.signsatwork.biz.persistence.model.CommunityDB;
import com.orange.signsatwork.biz.persistence.repository.CommunityRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class CommunityRepositoryIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;

  @MockBean
  public JavaMailSender emailSender;


  @Autowired
  private CommunityRepository communityRepository;

  private String community1Name = "aristochat";
  private String community2Name = "gangster";


  @Test
  public void returnAllPersisted() throws IOException {
    // given
    entityManager.persist(new CommunityDB(community1Name));
    entityManager.persist(new CommunityDB(community2Name));

    // do
    Iterable<CommunityDB> communities = communityRepository.findAll();
    CommunityDB community1 = communityRepository.findByName(community1Name).get(0);
    CommunityDB community2 = communityRepository.findByName(community2Name).get(0);

    // then
    assertThat(communities).hasSize(2);
    assertThat(communities).contains(community1);
    assertThat(communities).contains(community2);

    assertThat(community1.getName()).isEqualTo(community1Name);
    assertThat(community2.getName()).isEqualTo(community2Name);
  }

  @Test
  public void createCommunity() {
    // given
    // do
    entityManager.persist(new CommunityDB(community1Name));
    CommunityDB community1 = communityRepository.findByName(community1Name).get(0);
    // then
    assertThat(community1.getName()).isEqualTo(community1Name);
  }
}
