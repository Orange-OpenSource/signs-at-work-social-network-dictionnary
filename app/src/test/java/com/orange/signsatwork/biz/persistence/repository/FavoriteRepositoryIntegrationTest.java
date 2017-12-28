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

import com.orange.signsatwork.biz.persistence.model.FavoriteDB;
import com.orange.signsatwork.biz.persistence.repository.FavoriteRepository;
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
public class FavoriteRepositoryIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private FavoriteRepository favoriteRepository;

  @MockBean
  public JavaMailSender emailSender;


  private String favorite1Name = "Favoris";
  private String favorite2Name = "Interfaces";

  @Test
  public void returnAllPersisted() throws IOException {
    // given
    entityManager.persist(new FavoriteDB(favorite1Name));
    entityManager.persist(new FavoriteDB(favorite2Name));

    // do
    Iterable<FavoriteDB> favorites = favoriteRepository.findAll();
    FavoriteDB favorite1 = favoriteRepository.findByName(favorite1Name).get(0);
    FavoriteDB favorite2 = favoriteRepository.findByName(favorite2Name).get(0);

    // then
    assertThat(favorites).hasSize(2);
    assertThat(favorites).contains(favorite1);
    assertThat(favorites).contains(favorite2);

    assertThat(favorite1.getName()).isEqualTo(favorite1Name);
    assertThat(favorite2.getName()).isEqualTo(favorite2Name);
  }

  @Test
  public void createFavorite() {
    // given
    // do
    entityManager.persist(new FavoriteDB(favorite1Name));
    FavoriteDB favorite1 = favoriteRepository.findByName(favorite1Name).get(0);
    // then
    assertThat(favorite1.getName()).isEqualTo(favorite1Name);
  }
}
