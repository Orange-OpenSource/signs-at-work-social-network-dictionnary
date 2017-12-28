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

import com.orange.signsatwork.biz.persistence.model.RequestDB;
import com.orange.signsatwork.biz.persistence.repository.RequestRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class RequestRepositoryIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private RequestRepository requestRepository;

  @MockBean
  public JavaMailSender emailSender;


  private String request1Name = "chat";
  private String request2Name = "cloud";
  Date requestDate = new Date();


  @Test
  public void returnAllPersisted() throws IOException {
    // given
    entityManager.persist(new RequestDB(request1Name, requestDate));
    entityManager.persist(new RequestDB(request2Name, requestDate));

    // do
    Iterable<RequestDB> requests = requestRepository.findAll();
    RequestDB request1 = requestRepository.findByName(request1Name).get(0);
    RequestDB request2 = requestRepository.findByName(request2Name).get(0);

    // then
    assertThat(requests).hasSize(2);
    assertThat(requests).contains(request1);
    assertThat(requests).contains(request2);

    assertThat(request1.getName()).isEqualTo(request1Name);
    assertThat(request1.getRequestDate()).isEqualTo(requestDate);
    assertThat(request2.getName()).isEqualTo(request2Name);
    assertThat(request2.getRequestDate()).isEqualTo(requestDate);
  }

  @Test
  public void createRequest() {
    // given
    // do
    entityManager.persist(new RequestDB(request1Name, requestDate));
    RequestDB request1 = requestRepository.findByName(request1Name).get(0);
    // then
    assertThat(request1.getName()).isEqualTo(request1Name);
    assertThat(request1.getRequestDate()).isEqualTo(requestDate);
  }
}
