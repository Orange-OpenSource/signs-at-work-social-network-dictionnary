package com.orange.signsatwork.biz.webservice.controller;

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

import com.orange.signsatwork.biz.domain.Communities;
import com.orange.signsatwork.biz.domain.Community;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.view.model.CommunityView;
import com.orange.signsatwork.biz.webservice.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class CommunityRestController {

  @Autowired
  private Services services;
  @Autowired
  MessageByLocaleService messageByLocaleService;

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi. WS_SEC_MY_COMMUNITIES)
  public ResponseEntity<?> myCommunities(Principal principal) {
    User user = services.user().withUserName(principal.getName());
    Communities communities = services.community().forUser(user.id);

    List<CommunityViewApi> communitiesViewApi = communities.stream()
      .map(community -> new CommunityViewApi(community))
      .collect(Collectors.toList());

    return new ResponseEntity<>(communitiesViewApi, HttpStatus.OK);

  }


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi. WS_SEC_COMMUNITY_USERS)
  public ResponseEntity<?> usersInMycommunities(@PathVariable long communityId, Principal principal)  {

    String messageError;
    User user = services.user().withUserName(principal.getName());
    Communities communities = services.community().forUser(user.id);

    boolean isMyCommunities = communities.stream().anyMatch(community -> community.id == communityId);
    if (!isMyCommunities) {
      messageError = messageByLocaleService.getMessage("do_not_below_to_this-community");
      return new ResponseEntity<>(messageError, HttpStatus.FORBIDDEN);
    }

    Community community = services.community().withId(communityId);

    List<UserCommunityViewApi> usersCommunityViewApi = community.users.stream()
    .map(userCommunity -> new UserCommunityViewApi(userCommunity))
    .collect(Collectors.toList());

    return new ResponseEntity<>(usersCommunityViewApi, HttpStatus.OK);
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_ADMIN_COMMUNITIES, method = RequestMethod.POST, headers = {"content-type=application/json"})
  public CommunityResponseApi community(@RequestBody CommunityCreationViewApi communityCreationViewApi, HttpServletResponse response) {
    CommunityResponseApi communityResponseApi = new CommunityResponseApi();
    if (services.community().withCommunityName(communityCreationViewApi.getName()) != null) {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      communityResponseApi.errorMessage = messageByLocaleService.getMessage("community_already_exist");
      return communityResponseApi;
    }

    Community community = services.community().create(communityCreationViewApi.toCommunity());
    communityResponseApi.communityId = community.id;
    response.setStatus(HttpServletResponse.SC_OK);
    return communityResponseApi;
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_ADMIN_COMMUNITY_USERS, method = RequestMethod.PUT, headers = {"content-type=application/json"})
  public CommunityResponseApi addUserToCommunity(@RequestBody CommunityCreationViewApi communityCreationViewApi, @PathVariable long communityId, HttpServletResponse response) {
    CommunityResponseApi communityResponseApi = new CommunityResponseApi();

    List<Long> communitiesId = new ArrayList<>();
    communitiesId.add(communityId);
    User user = services.user().withUserName(communityCreationViewApi.getUsername());
    if (user != null) {
     /* services.user().changeUserCommunities(user.id, communitiesId);*/
    }
    response.setStatus(HttpServletResponse.SC_OK);
    return communityResponseApi;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_CREATE_COMMUNITY, method = RequestMethod.POST)
  public CommunityResponseApi createCommunity(@RequestBody CommunityCreationApi communityCreationApi, Principal principal, HttpServletResponse response) {
    User user = services.user().withUserName(principal.getName());
    List<Long> usersIds = communityCreationApi.getCommunityUsersIds();
    usersIds.add(user.id);

    Community community = services.community().create(communityCreationApi.toCommunity());
    if (community != null) {
      community = services.community().changeCommunityUsers(community.id, usersIds);
    }

    CommunityResponseApi communityResponseApi = new CommunityResponseApi();

    response.setStatus(HttpServletResponse.SC_OK);
    List<String> name = community.users.stream().map(c -> c.name()).collect(Collectors.toList());

    communityResponseApi.communityId = community.id;
    communityResponseApi.errorMessage = messageByLocaleService.getMessage("community.members", new Object[]{name.toString()});
    return communityResponseApi;
  }
}
