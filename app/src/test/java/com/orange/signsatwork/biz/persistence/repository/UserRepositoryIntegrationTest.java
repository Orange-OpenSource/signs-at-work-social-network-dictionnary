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

import com.orange.signsatwork.biz.persistence.model.UserDB;
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

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryIntegrationTest {

  @MockBean
  public JavaMailSender emailSender;

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private UserRepository userRepository;
  @Autowired
  Services services;

  private String username1 = "Duchess";
  private String password1 = "1234";
  private String firstName1 = "Duchess";
  private String lastName1 = "Aristocats";
  private String nameVideo1 = "name1.mp4";
  private String email1 = "duchess@cats.com";
  private String entity1 = "CATS";
  private String job1 = "Chatte au foyer";
  private String jobTextDescription1 = "bla bla bla";
  private String jobVideoDescription1 = "job1.mp4";


  private String username2 = "Thomas";
  private String password2 = "4321";
  private String firstName2 = "Thomas";
  private String lastName2 = "O'Malley";
  private String nameVideo2 = "name2.mp4";
  private String email2 = "gangster@cats.com";
  private String entity2 = "MOUSE";
  private String job2 = "Gangster";
  private String jobTextDescription2 = "blu blu blu";
  private String jobVideoDescription2 = "job2.mp4";


  @Before
  public void setup() {
    services.clearPersistence();

  }

  @Test
  public void returnAllPersisted() throws IOException {
    // given
    entityManager.persist(new UserDB(username1, password1, firstName1, lastName1, nameVideo1, email1, entity1, job1, jobTextDescription1, jobVideoDescription1));
    entityManager.persist(new UserDB(username2, password2, firstName2, lastName2, nameVideo2, email2, entity2, job2, jobTextDescription2, jobVideoDescription2));
    // do
    Iterable<UserDB> users = userRepository.findAll();
    UserDB admin = userRepository.findByUsername("admin").get(0);
    UserDB user1 = userRepository.findByUsername(username1).get(0);
    UserDB user2 = userRepository.findByUsername(username2).get(0);
    // then
    assertThat(users).hasSize(3);
    assertThat(users).contains(admin);
    assertThat(users).contains(user1);
    assertThat(users).contains(user2);

    assertThat(admin.getUsername()).isEqualTo("admin");

    assertThat(user1.getUsername()).isEqualTo(username1);
    assertThat(user1.getPasswordHash()).isEqualTo(password1);
    assertThat(user1.getFirstName()).isEqualTo(firstName1);
    assertThat(user1.getLastName()).isEqualTo(lastName1);
    assertThat(user1.getNameVideo()).isEqualTo(nameVideo1);
    assertThat(user1.getEmail()).isEqualTo(email1);
    assertThat(user1.getEntity()).isEqualTo(entity1);
    assertThat(user1.getJob()).isEqualTo(job1);
    assertThat(user1.getJobTextDescription()).isEqualTo(jobTextDescription1);
    assertThat(user1.getJobVideoDescription()).isEqualTo(jobVideoDescription1);

    assertThat(user2.getUsername()).isEqualTo(username2);
    assertThat(user2.getPasswordHash()).isEqualTo(password2);
    assertThat(user2.getFirstName()).isEqualTo(firstName2);
    assertThat(user2.getLastName()).isEqualTo(lastName2);
    assertThat(user2.getNameVideo()).isEqualTo(nameVideo2);
    assertThat(user2.getEmail()).isEqualTo(email2);
    assertThat(user2.getEntity()).isEqualTo(entity2);
    assertThat(user2.getJob()).isEqualTo(job2);
    assertThat(user2.getJobTextDescription()).isEqualTo(jobTextDescription2);
    assertThat(user2.getJobVideoDescription()).isEqualTo(jobVideoDescription2);
  }

  @Test
  public void createUser() {
    // given
    // do
    entityManager.persist(new UserDB(username1, password1, firstName1, lastName1, nameVideo1, email1, entity1, job1, jobTextDescription1, jobVideoDescription1));
    UserDB user1 = userRepository.findByUsername(username1).get(0);
    // then
    assertThat(user1.getUsername()).isEqualTo(username1);
    assertThat(user1.getPasswordHash()).isEqualTo(password1);
    assertThat(user1.getFirstName()).isEqualTo(firstName1);
    assertThat(user1.getLastName()).isEqualTo(lastName1);
    assertThat(user1.getNameVideo()).isEqualTo(nameVideo1);
    assertThat(user1.getEmail()).isEqualTo(email1);
    assertThat(user1.getEntity()).isEqualTo(entity1);
    assertThat(user1.getJob()).isEqualTo(job1);
    assertThat(user1.getJobTextDescription()).isEqualTo(jobTextDescription1);
    assertThat(user1.getJobVideoDescription()).isEqualTo(jobVideoDescription1);
  }
}
