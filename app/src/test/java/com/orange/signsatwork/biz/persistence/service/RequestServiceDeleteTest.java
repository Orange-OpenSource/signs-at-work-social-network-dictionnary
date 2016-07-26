package com.orange.signsatwork.biz.persistence.service;

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

import com.orange.signsatwork.biz.TestUser;
import com.orange.signsatwork.biz.domain.Request;
import com.orange.signsatwork.biz.domain.User;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RequestServiceDeleteTest {

  @Autowired
  Services services;

  @Autowired
  TestUser testUser;

  @Test
  public void canRemoveRequest() {
    // given
    User user = testUser.get("user-canRemoveRequest");

    // do/then
    Request request = services.user().createUserRequest(user.id, "request-canRemoveRequest");
    Assertions.assertThat(services.request().withId(request.id)).isNotNull();
    Assertions.assertThat(services.user().withId(user.id).loadCommunitiesRequestsFavorites().requests.ids()).contains(request.id);

    // do/then
    services.request().delete(request);
    Assertions.assertThat(services.request().all().stream().filter(r -> r.id == request.id).count()).isEqualTo(0);
    Assertions.assertThat(services.user().withId(user.id).loadCommunitiesRequestsFavorites().requests.ids()).doesNotContain(request.id);
  }
}
