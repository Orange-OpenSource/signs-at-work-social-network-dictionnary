package com.orange.signsatwork.biz.controller;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.signsatwork.biz.TestUser;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.webservice.controller.RestApi;
import com.orange.signsatwork.biz.view.model.SignCreationView;
import com.orange.signsatwork.biz.webservice.model.SignId;
import com.orange.signsatwork.biz.webservice.model.SignView;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SignRestControllerIntegrationTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  Services services;

  @Autowired
  TestUser testUser;

  private MockMvc mockMvc;

  private String username = "user";

  private String signName = "sign";
  private String videoUrl = "//video";

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .alwaysDo(print())
            .build();

    services.clearPersistence();
    testUser.get(username);
  }

  @Test
  public void anonymousCanNotPost() throws Exception {
    mockMvc
            // do
            .perform(
                    post(RestApi.WS_SEC_SIGN_CREATE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bodyForSignCreation())
            )
            // then
            .andExpect(status().isUnauthorized());
  }

  @Test
  public void userCanPost() throws Exception {
    mockMvc
            // do
            .perform(
                    post(RestApi.WS_SEC_SIGN_CREATE).with(httpBasic(username, TestUser.PASSWORD))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bodyForSignCreation())
            )
            // then
            .andExpect(status().isOk());
  }

  @Test
  public void getSignAfterPost() throws Exception {
    // given
    MvcResult result = mockMvc.perform(
            post(RestApi.WS_SEC_SIGN_CREATE).with(httpBasic(username, TestUser.PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(bodyForSignCreation())
    )
            .andExpect(status().isOk())
            .andReturn();

    SignId signId = bodyToSignId(result.getResponse().getContentAsString());

    // do
    result = mockMvc.perform(get(RestApi.WS_OPEN_SIGN + signId.signId))
            // then
            .andExpect(status().isOk())
            .andReturn();

    SignView signView = bodyToSignCreationView(result.getResponse().getContentAsString());

    // then
    Assertions.assertThat(signView.getSignName()).isEqualTo(signName);
    Assertions.assertThat(signView.getVideoUrl()).isEqualTo(videoUrl);
  }

  private String bodyForSignCreation() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(
            new SignCreationView(signName, videoUrl));
  }

  private SignView bodyToSignCreationView(String content) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(content, SignView.class);
  }

  private SignId bodyToSignId(String content) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(content, SignId.class);
  }
}
