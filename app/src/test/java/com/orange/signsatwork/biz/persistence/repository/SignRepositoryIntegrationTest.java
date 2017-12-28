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

import com.orange.signsatwork.biz.persistence.model.SignDB;
import com.orange.signsatwork.biz.persistence.repository.SignRepository;
import com.orange.signsatwork.biz.persistence.service.Services;
import org.junit.Before;
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
public class SignRepositoryIntegrationTest {

  @Autowired
  Services services;

  @MockBean
  public JavaMailSender emailSender;


  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private SignRepository signRepository;

  private String sign1Name = "cloud";
  private String sign1Url ="//www.dailymotion.com/embed/video/x2mnl8q";
  private String sign2Name = "chat";
  private String sign2Url ="//www.dailymotion.com/embed/video/k4h7GSlUDZQUvkaMF5s";

  @Before
  public void setup() {
    services.clearPersistence();

  }

  @Test
  public void returnAllPersisted() throws IOException {
    // given
    entityManager.persist(new SignDB(sign1Name, sign1Url, new Date()));
    entityManager.persist(new SignDB(sign2Name, sign2Url, new Date()));

    // do
    Iterable<SignDB> signs = signRepository.findAll();
    SignDB sign1 = signRepository.findByName(sign1Name).get(0);
    SignDB sign2 = signRepository.findByName(sign2Name).get(0);

    // then
    assertThat(signs).hasSize(2);
    assertThat(signs).contains(sign1);
    assertThat(signs).contains(sign2);

    assertThat(sign1.getName()).isEqualTo(sign1Name);
    assertThat(sign1.getUrl()).isEqualTo(sign1Url);
    assertThat(sign2.getName()).isEqualTo(sign2Name);
    assertThat(sign2.getUrl()).isEqualTo(sign2Url);
  }

  @Test
  public void createSign() {
    // given
    // do
    entityManager.persist(new SignDB(sign1Name, sign1Url, new Date()));
    SignDB sign1 = signRepository.findByName(sign1Name).get(0);
    // then
    assertThat(sign1.getName()).isEqualTo(sign1Name);
    assertThat(sign1.getUrl()).isEqualTo(sign1Url);
  }
}
