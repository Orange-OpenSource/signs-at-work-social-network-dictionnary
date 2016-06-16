package com.orange.spring.demo.biz.webservice.controller;

/*
 * #%L
 * Spring demo
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

import com.orange.spring.demo.biz.domain.Communities;
import com.orange.spring.demo.biz.domain.Community;
import com.orange.spring.demo.biz.domain.User;
import com.orange.spring.demo.biz.domain.Users;
import com.orange.spring.demo.biz.persistence.service.CommunityService;
import com.orange.spring.demo.biz.persistence.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIntegrationTest {

  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @MockBean
  private CommunityService communityService;

  private long id1 = 11;
  private String username1 = "Duchess";
  private String firstName1 = "Duchess";
  private String lastName1 = "Aristocats";
  private String email1 = "duchess@cats.com";
  private String entity1 = "CATS";
  private String activity1 = "mother";
  private Date lastConnectionDate1;


  private long id2 = 22;
  private String username2 = "Thomas";
  private String firstName2 = "Thomas";
  private String lastName2 = "O'Malley";
  private String email2 = "gangster@cats.com";
  private String entity2 = "MOUSE";
  private String activity2 = "gangster";
  private Date lastConnectionDate2;


  @Before
  public void setup() {
    mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .alwaysDo(print())
            .build();

    Users users = new Users(
            Arrays.asList(
                    new User(
                            id1, username1, firstName1, lastName1,
                            email1, entity1, activity1,
                            new Communities(new ArrayList<>()),
                            lastConnectionDate1, communityService),
                    new User(id2, username2, firstName2, lastName2,
                            email2, entity2, activity2,
                            new Communities(new ArrayList<>()),
                            lastConnectionDate2, communityService)));

    given(userService.all()).willReturn(users);
  }

  @Test
  @WithMockUser
  public void retrieveUsers() throws Exception {
    // given
    // do
    MvcResult result = mockMvc
            .perform(get(RestApi.WS_SEC_GET_USERS))
            .andExpect(status().isOk())
            .andReturn();

    String json = result.getResponse().getContentAsString();
    // then
    assertThat(json).contains(username1);
    assertThat(json).contains(username2);
  }
}
