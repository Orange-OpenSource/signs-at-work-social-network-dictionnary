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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.signsatwork.DalymotionToken;
import com.orange.signsatwork.SpringRestClient;
import com.orange.signsatwork.biz.domain.*;
import com.orange.signsatwork.biz.nativeinterface.NativeInterface;
import com.orange.signsatwork.biz.persistence.model.CommunityViewData;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.storage.StorageService;
import com.orange.signsatwork.biz.view.model.CommunityView;
import com.orange.signsatwork.biz.webservice.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class CommunityRestController {

  @Autowired
  private StorageService storageService;
  @Autowired
  private Services services;
  @Autowired
  DalymotionToken dalymotionToken;
  @Autowired
  private SpringRestClient springRestClient;
  @Autowired
  MessageByLocaleService messageByLocaleService;
  @Autowired
  private Environment environment;

  String VIDEO_THUMBNAIL_FIELDS = "thumbnail_url,thumbnail_60_url,thumbnail_120_url,thumbnail_180_url,thumbnail_240_url,thumbnail_360_url,thumbnail_480_url,thumbnail_720_url,";
  String VIDEO_EMBED_FIELD = "embed_url";
  String VIDEO_STATUS = ",status";


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi. WS_SEC_MY_COMMUNITIES)
  public ResponseEntity<?> myCommunities(Principal principal) {
    User user = services.user().withUserName(principal.getName());
    Communities communities = services.community().forUser(user.id);

    List<CommunityViewApi> communitiesViewApi = communities.stream()
      .map(community -> CommunityViewApi.fromMe(community))
      .collect(Collectors.toList());

    return new ResponseEntity<>(communitiesViewApi, HttpStatus.OK);

  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi. WS_SEC_COMMUNITY)
  public ResponseEntity<?> community(@PathVariable long communityId, Principal principal) {
    User user = services.user().withUserName(principal.getName());
    Community community = services.community().withId(communityId);

    CommunityViewApi communitiesViewApi = CommunityViewApi.fromMe(community);

    return new ResponseEntity<>(communitiesViewApi, HttpStatus.OK);

  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi. WS_SEC_COMMUNITIES, method = RequestMethod.GET)
  public ResponseEntity<?> communities(@RequestParam("type") Optional<String> type, @RequestParam("name") Optional<String> name, Principal principal) {
    User user = services.user().withUserName(principal.getName());
    List<Object[]> queryCommunities = new ArrayList<>();
    List<CommunityViewData> communitiesViewData;
    List<CommunityViewData> communitiesSearchViewData = new ArrayList<>();
    if (type.isPresent()) {
      if (type.get().equals("Job")) {
        queryCommunities = services.community().allForJob(user.id);
        communitiesViewData = queryCommunities.stream()
          .map(objectArray -> new CommunityViewData(objectArray))
          .collect(Collectors.toList());
      } else {
        queryCommunities = services.community().allForFavorite(user.id);
        communitiesViewData = queryCommunities.stream()
          .map(objectArray -> new CommunityViewData(objectArray))
          .collect(Collectors.toList());
      }
    } else {
      if (name.isPresent()) {
        List<Object[]> querySearchCommunity = services.community().searchBis(name.get().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE").toUpperCase());
        communitiesSearchViewData = querySearchCommunity.stream()
          .map(objectArray -> new CommunityViewData(objectArray))
          .collect(Collectors.toList());
        queryCommunities = services.community().allForFavorite(user.id);
        List<CommunityViewData> finalCommunitiesSearchViewData1 = communitiesSearchViewData;
        communitiesViewData = queryCommunities.stream()
          .map(objectArray -> new CommunityViewData(objectArray))
          .filter(c-> finalCommunitiesSearchViewData1.stream().map(co -> co.id).collect(Collectors.toList()).contains(c.id))
          .collect(Collectors.toList());
      } else {
        queryCommunities = services.community().allForFavorite(user.id);
        communitiesViewData = queryCommunities.stream()
          .map(objectArray -> new CommunityViewData(objectArray))
          .collect(Collectors.toList());
      }
    }

    return new ResponseEntity<>(communitiesViewData, HttpStatus.OK);

  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi. WS_SEC_COMMUNITY_USERS)
  public ResponseEntity<?> usersInMycommunities(@PathVariable long communityId, Principal principal)  {

    String messageError;
    User user = services.user().withUserName(principal.getName());
    Communities communities = services.community().forUser(user.id);

    Community community = services.community().withId(communityId);

    List<UserCommunityViewApi> usersCommunityViewApi = community.users.stream()
    .map(userCommunity -> new UserCommunityViewApi(userCommunity))
    .collect(Collectors.toList());

    return new ResponseEntity<>(usersCommunityViewApi, HttpStatus.OK);
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_ADMIN_COMMUNITIES, method = RequestMethod.POST, headers = {"content-type=application/json"})
  public CommunityResponseApi community(@RequestBody CommunityCreationViewApi communityCreationViewApi, HttpServletResponse response, Principal principal) {
    CommunityResponseApi communityResponseApi = new CommunityResponseApi();
    if (services.community().withCommunityName(communityCreationViewApi.getName()) != null) {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      communityResponseApi.errorMessage = messageByLocaleService.getMessage("community_already_exist");
      return communityResponseApi;
    }

    User user = services.user().withUserName(principal.getName());

    Community community = services.community().create(user.id, communityCreationViewApi.toCommunity());
    communityResponseApi.communityId = community.id;
    response.setStatus(HttpServletResponse.SC_OK);
    return communityResponseApi;
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_ADMIN_COMMUNITY_USERS, method = RequestMethod.PUT, headers = {"content-type=application/json"})
  public CommunityResponseApi addUserToCommunity(@RequestBody CommunityCreationViewApi communityCreationViewApi, @PathVariable long communityId, HttpServletResponse response) {
    CommunityResponseApi communityResponseApi = new CommunityResponseApi();

    Community community = services.community().withId(communityId);
    User user = services.user().withUserName(communityCreationViewApi.getUsername());
    List<Long> usersIds = community.usersIds();
    usersIds.add(user.id);
    if (user != null) {
      services.community().changeCommunityUsers(communityId, usersIds);
     /* services.user().changeUserCommunities(user.id, communitiesId);*/
    }
    response.setStatus(HttpServletResponse.SC_OK);
    return communityResponseApi;
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_COMMUNITIES, method = RequestMethod.POST)
  public CommunityResponseApi createCommunity(@RequestBody CommunityCreationApi communityCreationApi, Principal principal, HttpServletResponse response, HttpServletRequest request) {
    List<String> emails;
    User user = services.user().withUserName(principal.getName());
    CommunityResponseApi communityResponseApi = new CommunityResponseApi();
    communityCreationApi.clearXss();
    if (services.community().withCommunityName(communityCreationApi.getName()) == null) {
      List<Long> usersIds = communityCreationApi.getCommunityUsersIds();
      usersIds.add(user.id);


      Community community = services.community().create(user.id, communityCreationApi.toCommunity());
      if (community != null) {
        community = services.community().changeCommunityUsers(community.id, usersIds);
      }


      response.setStatus(HttpServletResponse.SC_OK);
      List<String> name = community.users.stream().map(c -> c.name()).collect(Collectors.toList());

      emails = community.users.stream().filter(u -> u.email != null).map(u -> u.email).collect(Collectors.toList());
      emails = emails.stream().distinct().collect(Collectors.toList());
      if (emails.size() != 0) {
        Community finalCommunity = community;
        List<String> finalEmails = emails;
        Runnable task = () -> {
          String title, bodyMail;
          final String url = getAppUrl() + "/sec/community/" + finalCommunity.id;
          title = messageByLocaleService.getMessage("community_created_by_user_title");
          bodyMail = messageByLocaleService.getMessage("community_created_by_user_body", new Object[]{user.name(), finalCommunity.name, url});
          log.info("send mail email = {} / title = {} / body = {}", finalEmails.toString(), title, bodyMail);
          services.emailService().sendCommunityCreateMessage(finalEmails.toArray(new String[finalEmails.size()]), title, user.name(), finalCommunity.name, url, request.getLocale());
        };

        new Thread(task).start();
      }


      communityResponseApi.communityId = community.id;
      communityResponseApi.errorMessage = messageByLocaleService.getMessage("community.members", new Object[]{name.toString()});
      return communityResponseApi;
    } else {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      communityResponseApi.errorMessage = messageByLocaleService.getMessage("community.already_exists");
      return communityResponseApi;
    }
  }

  private String getAppUrl() {
    return environment.getProperty("app.url");
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_COMMUNITY, method = RequestMethod.DELETE)
  public CommunityResponseApi deleteCommunity(@PathVariable long communityId, HttpServletResponse response, HttpServletRequest request, Principal principal)  {
    List<String> emails;
    CommunityResponseApi communityResponseApi = new CommunityResponseApi();
    Community community = services.community().withId(communityId);
    User user = services.user().withUserName(principal.getName());

    if (community.user.id != user.id) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      communityResponseApi.errorMessage = messageByLocaleService.getMessage("community_not_below_to_you");
      return communityResponseApi;
    }

    if (community.descriptionVideo != null) {
      if (community.descriptionVideo.contains("http")) {
        String dailymotionId = community.descriptionVideo.substring(community.descriptionVideo.lastIndexOf('/') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionId);
        } catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          communityResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
          return communityResponseApi;
        }
      } else {
        DeleteFileOnServer(community.descriptionVideo);
      }
    }
    services.community().delete(community);

    emails = community.users.stream().filter(u-> u.email != null).map(u -> u.email).collect(Collectors.toList());
    emails = emails.stream().distinct().collect(Collectors.toList());
    if (emails.size() != 0) {
      Community finalCommunity = community;
      List<String> finalEmails = emails;
      Runnable task = () -> {
        String title, bodyMail;
        title = messageByLocaleService.getMessage("community_deleted_by_user_title");
        bodyMail = messageByLocaleService.getMessage("community_deleted_by_user_body", new Object[]{user.name(), finalCommunity.name});
        log.info("send mail email = {} / title = {} / body = {}", finalEmails.toString(), title, bodyMail);
        services.emailService().sendCommunityDeleteMessage(finalEmails.toArray(new String[finalEmails.size()]), title, user.name(), finalCommunity.name, request.getLocale());
      };

      new Thread(task).start();
    }

    response.setStatus(HttpServletResponse.SC_OK);
    return communityResponseApi;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_COMMUNITY_DATAS, method = RequestMethod.PUT)
  public CommunityResponseApi updateCommunityDatas(@RequestBody CommunityCreationViewApi communityCreationViewApi, @PathVariable long communityId, HttpServletResponse response, HttpServletRequest request, Principal principal) {
    List<String> emails, emailsUsersAdded, emailsUsersRemoved;
    CommunityResponseApi communityResponseApi = new CommunityResponseApi();
    communityResponseApi.communityId = communityId;
    User user = services.user().withUserName(principal.getName());


    Community community = services.community().withId(communityId);
    communityCreationViewApi.clearXss();
    if (communityCreationViewApi.getName() != null) {
      if (community.user.id != user.id) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        communityResponseApi.errorMessage = messageByLocaleService.getMessage("community_not_below_to_you");
        return communityResponseApi;
      }

      if (!community.name.equals(communityCreationViewApi.getName())) {
        if (services.community().withCommunityName(communityCreationViewApi.getName()) == null) {
          services.community().updateName(communityId, communityCreationViewApi.getName());
          communityResponseApi.errorMessage = messageByLocaleService.getMessage("community.renamed", new Object[]{community.name, communityCreationViewApi.getName()});
          emails = community.users.stream().filter(u-> u.email != null).map(u -> u.email).collect(Collectors.toList());
          emails = emails.stream().distinct().collect(Collectors.toList());
          if (emails.size() != 0) {
            List<String> finalEmails = emails;
            Runnable task = () -> {
              String title, bodyMail;
              final String url = getAppUrl() + "/sec/community/" + community.id;
              title = messageByLocaleService.getMessage("community_renamed_by_user_title");
              bodyMail = messageByLocaleService.getMessage("community_renamed_by_user_body", new Object[]{community.name, communityCreationViewApi.getName(), url});
              log.info("send mail email = {} / title = {} / body = {}", finalEmails.toString(), title, bodyMail);
              services.emailService().sendCommunityRenameMessage(finalEmails.toArray(new String[finalEmails.size()]), title, user.name(), community.name, communityCreationViewApi.getName(), url, request.getLocale());
            };

            new Thread(task).start();
          }
        } else {
          communityResponseApi.errorMessage = messageByLocaleService.getMessage("community.name_already_exist");
          response.setStatus(HttpServletResponse.SC_CONFLICT);
          return communityResponseApi;
        }
      }
    } else if (communityCreationViewApi.getUserIdToRemove() != 0) {
        User userToRemove = services.user().withId(communityCreationViewApi.getUserIdToRemove());
        if (userToRemove != null && userToRemove.id != user.id) {
          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
          communityResponseApi.errorMessage = messageByLocaleService.getMessage("community.remove_user_forbidden");
          return communityResponseApi;
        } else {
          services.community().removeMeFromCommunity(communityId, userToRemove.id);
          String values = userToRemove.name() + ';' + community.name;
          MessageServer messageServer = new MessageServer(new Date(), "CommunityRemoveMeMessage", values, ActionType.NO);
          services.messageServerService().addMessageServer(messageServer);
        }
    } else if (communityCreationViewApi.getDescriptionText() != null && !communityCreationViewApi.getDescriptionText().isEmpty()) {
        services.community().updateDescriptionText(communityId, communityCreationViewApi.getDescriptionText());
        emails = community.users.stream().filter(u-> u.email != null).map(u -> u.email).collect(Collectors.toList());
        emails = emails.stream().distinct().collect(Collectors.toList());
        if (emails.size() != 0) {
          List<String> finalEmails1 = emails;
          Runnable task = () -> {
            String title, bodyMail;
            final String url = getAppUrl() + "/sec/descriptionCommunity/" + community.id;
            title = messageByLocaleService.getMessage("community_description_changed_by_user_title");
            bodyMail = messageByLocaleService.getMessage("community_description_changed_by_user_body", new Object[]{user.name(), community.name, url});
            log.info("send mail email = {} / title = {} / body = {}", finalEmails1.toString(), title, bodyMail);
            services.emailService().sendCommunityAddDescriptionMessage(finalEmails1.toArray(new String[finalEmails1.size()]), title, user.name(), community.name, url, request.getLocale());
          };

          new Thread(task).start();
        }
    }
    else {
      if (community.user.id != user.id) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        communityResponseApi.errorMessage = messageByLocaleService.getMessage("community_not_below_to_you");
        return communityResponseApi;
      }
      List<Long> usersIds = communityCreationViewApi.getCommunityUsersIds();
      usersIds.add(user.id);
      List<Long> usersIdsToAdd = usersIds.stream().filter(u -> !community.usersIds().contains(u)).collect(Collectors.toList());
      List<Long> usersIdsToRemove = community.usersIds().stream().filter(u -> !usersIds.contains(u)).collect(Collectors.toList());
      if (!usersIds.isEmpty()) {
        Community newCommunity = services.community().changeCommunityUsers(community.id, usersIds);
        List<String> name = newCommunity.users.stream().map(c -> c.name()).collect(Collectors.toList());
        communityResponseApi.errorMessage = messageByLocaleService.getMessage("community.members", new Object[]{name.toString()});
        List<User> usersAdded = usersIdsToAdd.stream().map(id -> services.user().withId(id)).collect(Collectors.toList());
        List<User> usersRemoved = usersIdsToRemove.stream().map(id -> services.user().withId(id)).collect(Collectors.toList());
        emailsUsersAdded = usersAdded.stream().filter(u-> u.email != null).map(u -> u.email).collect(Collectors.toList());
        if (emailsUsersAdded.size() != 0) {
          Community finalCommunity = community;
          Runnable task = () -> {
            String title, bodyMail;
            final String url = getAppUrl() + "/sec/community/" + finalCommunity.id;
            title = messageByLocaleService.getMessage("community_created_by_user_title");
            bodyMail = messageByLocaleService.getMessage("community_created_by_user_body", new Object[]{user.name(), finalCommunity.name, url});
            log.info("send mail email = {} / title = {} / body = {}", emailsUsersAdded.toString(), title, bodyMail);
            services.emailService().sendCommunityAddMessage(emailsUsersAdded.toArray(new String[emailsUsersAdded.size()]), title, user.name(), finalCommunity.name, url, request.getLocale());
          };

          new Thread(task).start();
        }
        emailsUsersRemoved = usersRemoved.stream().filter(u-> u.email != null).map(u -> u.email).collect(Collectors.toList());
        if (emailsUsersRemoved.size() != 0) {
          Community finalCommunity = community;
          Runnable task = () -> {
            String title, bodyMail;
            title = messageByLocaleService.getMessage("community_removed_user_title");
            bodyMail = messageByLocaleService.getMessage("community_removed_user_body", new Object[]{finalCommunity.name});
            log.info("send mail email = {} / title = {} / body = {}", emailsUsersRemoved.toString(), title, bodyMail);
            services.emailService().sendCommunityRemoveMessage(emailsUsersRemoved.toArray(new String[emailsUsersRemoved.size()]), title, user.name(), finalCommunity.name, request.getLocale());
          };

          new Thread(task).start();
        }
      }
    }

      response.setStatus(HttpServletResponse.SC_OK);
      return communityResponseApi;

    }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_COMMUNITY, method = RequestMethod.PUT, headers = {"content-type=multipart/mixed", "content-type=multipart/form-data", "content-type=application/json"})
  public CommunityResponseApi updateCommunity(@RequestPart("file") Optional<MultipartFile> fileCommunityDescriptionVideo, @RequestPart("data") Optional<CommunityCreationViewApi> communityCreationViewApi, @PathVariable long communityId, HttpServletResponse response, HttpServletRequest request, Principal principal) throws InterruptedException {
    List<String> emails, emailsUsersAdded, emailsUsersRemoved;
    CommunityResponseApi communityResponseApi = new CommunityResponseApi();
    communityResponseApi.communityId = communityId;
    User user = services.user().withUserName(principal.getName());


    Community community = services.community().withId(communityId);
    if (communityCreationViewApi.isPresent()) {
      communityCreationViewApi.get().clearXss();
      if (communityCreationViewApi.get().getName() != null) {
        if (community.user.id != user.id) {
          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
          communityResponseApi.errorMessage = messageByLocaleService.getMessage("community_not_below_to_you");
          return communityResponseApi;
        }

        if (!community.name.equals(communityCreationViewApi.get().getName())) {
          if (services.community().withCommunityName(communityCreationViewApi.get().getName()) == null) {
            services.community().updateName(communityId, communityCreationViewApi.get().getName());
            communityResponseApi.errorMessage = messageByLocaleService.getMessage("community.renamed", new Object[]{community.name, communityCreationViewApi.get().getName()});
            emails = community.users.stream().filter(u -> u.email != null).map(u -> u.email).collect(Collectors.toList());
            emails = emails.stream().distinct().collect(Collectors.toList());
            if (emails.size() != 0) {
              List<String> finalEmails = emails;
              Runnable task = () -> {
                String title, bodyMail;
                final String url = getAppUrl() + "/sec/community/" + community.id;
                title = messageByLocaleService.getMessage("community_renamed_by_user_title");
                bodyMail = messageByLocaleService.getMessage("community_renamed_by_user_body", new Object[]{community.name, communityCreationViewApi.get().getName(), url});
                log.info("send mail email = {} / title = {} / body = {}", finalEmails.toString(), title, bodyMail);
                services.emailService().sendCommunityRenameMessage(finalEmails.toArray(new String[finalEmails.size()]), title, user.name(), community.name, communityCreationViewApi.get().getName(), url, request.getLocale());
              };

              new Thread(task).start();
            }
          } else {
            communityResponseApi.errorMessage = messageByLocaleService.getMessage("community.name_already_exist");
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return communityResponseApi;
          }
        }
      } else if (communityCreationViewApi.get().getUserIdToRemove() != 0) {
        User userToRemove = services.user().withId(communityCreationViewApi.get().getUserIdToRemove());
        if (userToRemove != null && userToRemove.id != user.id) {
          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
          communityResponseApi.errorMessage = messageByLocaleService.getMessage("community.remove_user_forbidden");
          return communityResponseApi;
        } else {
          services.community().removeMeFromCommunity(communityId, userToRemove.id);
          String values = userToRemove.name() + ';' + community.name;
          MessageServer messageServer = new MessageServer(new Date(), "CommunityRemoveMeMessage", values, ActionType.NO);
          services.messageServerService().addMessageServer(messageServer);
        }
      } else if (communityCreationViewApi.get().getDescriptionText() != null && !communityCreationViewApi.get().getDescriptionText().isEmpty()) {
          services.community().updateDescriptionText(communityId, communityCreationViewApi.get().getDescriptionText());
          emails = community.users.stream().filter(u-> u.email != null).map(u -> u.email).collect(Collectors.toList());
        emails = emails.stream().distinct().collect(Collectors.toList());
          if (emails.size() != 0) {
            List<String> finalEmails1 = emails;
            Runnable task = () -> {
              String title, bodyMail;
              final String url = getAppUrl() + "/sec/descriptionCommunity/" + community.id;
              title = messageByLocaleService.getMessage("community_description_changed_by_user_title");
              bodyMail = messageByLocaleService.getMessage("community_description_changed_by_user_body", new Object[]{user.name(), community.name, url});
              log.info("send mail email = {} / title = {} / body = {}", finalEmails1.toString(), title, bodyMail);
              services.emailService().sendCommunityAddDescriptionMessage(finalEmails1.toArray(new String[finalEmails1.size()]), title, user.name(), community.name, url, request.getLocale());
            };

            new Thread(task).start();
          }
      } else {
        if (community.user.id != user.id) {
          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
          communityResponseApi.errorMessage = messageByLocaleService.getMessage("community_not_below_to_you");
          return communityResponseApi;
        }

        List<Long> usersIds = communityCreationViewApi.get().getCommunityUsersIds();
        usersIds.add(user.id);
        List<Long> usersIdsToAdd = usersIds.stream().filter(u -> !community.usersIds().contains(u)).collect(Collectors.toList());
        List<Long> usersIdsToRemove = community.usersIds().stream().filter(u -> !usersIds.contains(u)).collect(Collectors.toList());
        if (!usersIds.isEmpty()) {
          Community newCommunity = services.community().changeCommunityUsers(community.id, usersIds);
          List<String> name = newCommunity.users.stream().map(c -> c.name()).collect(Collectors.toList());
          communityResponseApi.errorMessage = messageByLocaleService.getMessage("community.members", new Object[]{name.toString()});
          List<User> usersAdded = usersIdsToAdd.stream().map(id -> services.user().withId(id)).collect(Collectors.toList());
          List<User> usersRemoved = usersIdsToRemove.stream().map(id -> services.user().withId(id)).collect(Collectors.toList());
          emailsUsersAdded = usersAdded.stream().filter(u -> u.email != null).map(u -> u.email).collect(Collectors.toList());
          if (emailsUsersAdded.size() != 0) {
            Community finalCommunity = community;
            Runnable task = () -> {
              String title, bodyMail;
              final String url = getAppUrl() + "/sec/community/" + finalCommunity.id;
              title = messageByLocaleService.getMessage("community_created_by_user_title");
              bodyMail = messageByLocaleService.getMessage("community_created_by_user_body", new Object[]{user.name(), finalCommunity.name, url});
              log.info("send mail email = {} / title = {} / body = {}", emailsUsersAdded.toString(), title, bodyMail);
              services.emailService().sendCommunityAddMessage(emailsUsersAdded.toArray(new String[emailsUsersAdded.size()]), title, user.name(), finalCommunity.name, url, request.getLocale());
            };

            new Thread(task).start();
          }
          emailsUsersRemoved = usersRemoved.stream().filter(u -> u.email != null).map(u -> u.email).collect(Collectors.toList());
          if (emailsUsersRemoved.size() != 0) {
            Community finalCommunity = community;
            Runnable task = () -> {
              String title, bodyMail;
              title = messageByLocaleService.getMessage("community_removed_user_title");
              bodyMail = messageByLocaleService.getMessage("community_removed_user_body", new Object[]{finalCommunity.name});
              log.info("send mail email = {} / title = {} / body = {}", emailsUsersRemoved.toString(), title, bodyMail);
              services.emailService().sendCommunityRemoveMessage(emailsUsersRemoved.toArray(new String[emailsUsersRemoved.size()]), title, user.name(), finalCommunity.name, request.getLocale());
            };

            new Thread(task).start();
          }
        }
      }
    }

    if (fileCommunityDescriptionVideo.isPresent()) {
      if (environment.getProperty("app.dailymotion_url").isEmpty()) {
        return handleSelectedVideoFileUploadForCommunityDescriptionOnServer(fileCommunityDescriptionVideo.get(), communityId, principal, response, request);
      } else {
        return handleSelectedVideoFileUploadForCommunityDescription(fileCommunityDescriptionVideo.get(), communityId, principal, response, request);
      }
    }

    response.setStatus(HttpServletResponse.SC_OK);
    return communityResponseApi;

  }

  private CommunityResponseApi handleSelectedVideoFileUploadForCommunityDescription(@RequestParam("file") MultipartFile file, @PathVariable long communityId, Principal principal, HttpServletResponse response, HttpServletRequest request) throws InterruptedException {
    {
      String videoUrl = null;
      String fileName = environment.getProperty("app.file") + "/" + file.getOriginalFilename();
      File inputFile;

      CommunityResponseApi communityResponseApi = new CommunityResponseApi();
      Community community = null;
      community = services.community().withId(communityId);

      try {
        storageService.store(file);
        inputFile = storageService.load(file.getOriginalFilename()).toFile();
      } catch (Exception errorLoadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        communityResponseApi.errorMessage = messageByLocaleService.getMessage("errorUploadFile");
        return communityResponseApi;
      }

      try {
        String dailymotionId;
        String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");
        AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
        if (authTokenInfo.isExpired()) {
          dalymotionToken.retrieveToken();
          authTokenInfo = dalymotionToken.getAuthTokenInfo();
        }

        User user = services.user().withUserName(principal.getName());

        UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


        Resource resource = new FileSystemResource(inputFile.getAbsolutePath());
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        parts.add("file", resource);

        RestTemplate restTemplate = springRestClient.buildRestTemplate();
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));


        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

        ResponseEntity<FileUploadDailymotion> responseDailyMotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
          HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
        FileUploadDailymotion fileUploadDailyMotion = responseDailyMotion.getBody();


        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("url", fileUploadDailyMotion.url);
        body.add("title", messageByLocaleService.getMessage("community.title_description_LSF", new Object[]{community.name}));
        body.add("channel", "tech");
        body.add("published", true);
        body.add("private", true);


        RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
        HttpHeaders headers1 = new HttpHeaders();
        headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
        headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
        String videosUrl = REST_SERVICE_URI + "/videos";
        ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange(videosUrl,
          HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
        VideoDailyMotion videoDailyMotion = response1.getBody();


        String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD + VIDEO_STATUS;
        String id = videoDailyMotion.id;
        videoUrl= fileName;

        if (community.descriptionVideo != null) {
          if (community.descriptionVideo.contains("http")) {
            dailymotionId = community.descriptionVideo.substring(community.descriptionVideo.lastIndexOf('/') + 1);
            try {
              DeleteVideoOnDailyMotion(dailymotionId);
            } catch (Exception errorDailymotionDeleteVideo) {
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              communityResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
              return communityResponseApi;
            }
          }
        }
        services.community().changeDescriptionVideo(communityId, videoUrl);
        List<String> emails = community.users.stream().filter(u-> u.email != null).map(u -> u.email).collect(Collectors.toList());
        emails = emails.stream().distinct().collect(Collectors.toList());
        if (emails.size() != 0) {
          Community finalCommunity = community;
          List<String> finalEmails = emails;
          Runnable task = () -> {
            String title, bodyMail;
            final String urlDescriptionCommunity = getAppUrl() + "/sec/descriptionCommunity/" + finalCommunity.id;
            title = messageByLocaleService.getMessage("community_description_changed_by_user_title");
            bodyMail = messageByLocaleService.getMessage("community_description_changed_by_user_body", new Object[]{user.name(), finalCommunity.name, urlDescriptionCommunity});
            log.info("send mail email = {} / title = {} / body = {}", finalEmails.toString(), title, bodyMail);
            services.emailService().sendCommunityAddDescriptionMessage(finalEmails.toArray(new String[finalEmails.size()]), title, user.name(), finalCommunity.name, urlDescriptionCommunity, request.getLocale());
          };

          new Thread(task).start();
        }

        Runnable task = () -> {
          int i = 0;
          VideoDailyMotion dailyMotion;
          do {
            dailyMotion = services.sign().getVideoDailyMotionDetails(id, url);
            try {
              Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            if (i > 60) {
              break;
            }
            i++;
            log.info("status " + dailyMotion.status);
          }
          while (!dailyMotion.status.equals("published"));
          if (!dailyMotion.embed_url.isEmpty()) {
            services.community().changeDescriptionVideo(communityId, dailyMotion.embed_url);
          }
        };

        new Thread(task).start();
        response.setStatus(HttpServletResponse.SC_OK);
        communityResponseApi.communityId = community.id;
        return communityResponseApi;

      } catch (Exception errorDailymotionUploadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        communityResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionUploadFile");
        return communityResponseApi;
      }
    }
  }

  private CommunityResponseApi handleSelectedVideoFileUploadForCommunityDescriptionOnServer(@RequestParam("file") MultipartFile file, @PathVariable long communityId, Principal principal, HttpServletResponse response, HttpServletRequest request) throws InterruptedException {
    {
      String videoUrl = null;
      String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
      String newFileName = UUID.randomUUID().toString() + "." + fileExtension;
      String newAbsoluteFileName = environment.getProperty("app.file") +"/" + newFileName;
      File inputFile;
      Streams streamInfo;
      String newAbsoluteFileNameWithExtensionMp4 = newAbsoluteFileName.substring(0, newAbsoluteFileName.lastIndexOf('.')) + ".mp4";

      CommunityResponseApi communityResponseApi = new CommunityResponseApi();
      Community community = services.community().withId(communityId);

      try {
        storageService.store(file);
        inputFile = storageService.load(file.getOriginalFilename()).toFile();
        File newName = new File(newAbsoluteFileName);
        inputFile.renameTo(newName);
      } catch (Exception errorLoadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        communityResponseApi.errorMessage = messageByLocaleService.getMessage("errorUploadFile");
        return communityResponseApi;
      }

      try {
        streamInfo = SearchFileInfo(newAbsoluteFileName);
      } catch (Exception errorSearchFileInfo) {
        streamInfo = new Streams(new ArrayList<>());
      }

      if (streamInfo.getStreams().stream().findFirst().get().getCodec_name().equals("hevc")) {
        EncodeFileInH264(newAbsoluteFileName, newAbsoluteFileNameWithExtensionMp4);
        newAbsoluteFileName = newAbsoluteFileNameWithExtensionMp4;
      } else {
        if ((streamInfo.getStreams().stream().findFirst().get().getWidth() == 1920) &&
          (file.getSize() >= parseSize(environment.getProperty("file-size-max-to-reduce")))) {
          ReduceFileSizeInChangingResolution(newAbsoluteFileName, newAbsoluteFileNameWithExtensionMp4);
          newAbsoluteFileName = newAbsoluteFileNameWithExtensionMp4;
        } else {
          if (fileExtension.equals("webm")) {
            TransformWebmInMp4(newAbsoluteFileName, newAbsoluteFileNameWithExtensionMp4);
            DeleteFileOnServer(newAbsoluteFileName);
            newAbsoluteFileName = newAbsoluteFileNameWithExtensionMp4;
          }
        }
      }

      User user = services.user().withUserName(principal.getName());

      videoUrl= newAbsoluteFileName;

      if (community.descriptionVideo != null) {
        DeleteFileOnServer(community.descriptionVideo);
      }
      services.community().changeDescriptionVideo(communityId, videoUrl);
      List<String> emails = community.users.stream().filter(u-> u.email != null).map(u -> u.email).collect(Collectors.toList());
      emails = emails.stream().distinct().collect(Collectors.toList());
      if (emails.size() != 0) {
        Community finalCommunity = community;
        List<String> finalEmails = emails;
        Runnable task = () -> {
          String title, bodyMail;
          final String urlDescriptionCommunity = getAppUrl() + "/sec/descriptionCommunity/" + finalCommunity.id;
          title = messageByLocaleService.getMessage("community_description_changed_by_user_title");
          bodyMail = messageByLocaleService.getMessage("community_description_changed_by_user_body", new Object[]{user.name(), finalCommunity.name, urlDescriptionCommunity});
          log.info("send mail email = {} / title = {} / body = {}", finalEmails.toString(), title, bodyMail);
          services.emailService().sendCommunityAddDescriptionMessage(finalEmails.toArray(new String[finalEmails.size()]), title, user.name(), finalCommunity.name, urlDescriptionCommunity, request.getLocale());
        };

        new Thread(task).start();
      }

      response.setStatus(HttpServletResponse.SC_OK);
      communityResponseApi.communityId = community.id;
      return communityResponseApi;

    }
  }

  private Streams SearchFileInfo(String file) throws JsonProcessingException {
    String cmdFileInfo = String.format("ffprobe -v quiet -print_format json -show_streams -select_streams v:0 %s", file);
    String fileInfo = NativeInterface.launchAndGetOutput(cmdFileInfo, null, null);
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    Streams streams = objectMapper.readValue(fileInfo, Streams.class);
    return streams;
  }

  private void ReduceFileSizeInChangingResolution(String file, String fileOutput) {
    String cmd;
    cmd = String.format("ffmpeg -i %s -filter:v \"scale='min(1280,iw)':min'(720,ih)':force_original_aspect_ratio=decrease,pad=1280:720:(ow-iw)/2:(oh-ih)/2,crop=1280:720\" %s", file, fileOutput);

    NativeInterface.launch(cmd, null, null);
  }

  public static long parseSize(String text) {
    double d = Double.parseDouble(text.replaceAll("[GMK]B$", ""));
    long l = Math.round(d * 1024 * 1024 * 1024L);
    switch (text.charAt(Math.max(0, text.length() - 2))) {
      default:  l /= 1024;
      case 'K': l /= 1024;
      case 'M': l /= 1024;
      case 'G': return l;
    }
  }

  private String SearchFileCodec(String file) {
    String cmdFileCodec = String.format("ffprobe -v error -select_streams v:0 -show_entries stream=codec_name -of default=noprint_wrappers=1:nokey=1 %s", file);
    String fileCodec = NativeInterface.launchAndGetOutput(cmdFileCodec, null, null);
    return fileCodec;
  }

  private void EncodeFileInH264(String file, String fileOutput) {
    String cmd;

    cmd = String.format("ffmpeg -i %s -c:v libx264 -crf 20 -c:a copy %s", file, fileOutput);

    NativeInterface.launch(cmd, null, null);
  }

  private void TransformWebmInMp4(String file, String fileOutput) {
    String cmd;

    cmd = String.format("ffmpeg -fflags +genpts -i %s -r 25 %s", file, fileOutput);

    NativeInterface.launch(cmd, null, null);
  }

  private void DeleteFileOnServer(String url) {
    if (url!= null) {
      File video = new File(url);
      if (video.exists()) {
        video.delete();
      }
    }
  }

  private void DeleteVideoOnDailyMotion(String dailymotionId) {

    AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
    if (authTokenInfo.isExpired()) {
      dalymotionToken.retrieveToken();
      authTokenInfo = dalymotionToken.getAuthTokenInfo();
    }

    final String uri = environment.getProperty("app.dailymotion_url") + "/video/"+dailymotionId;
    RestTemplate restTemplate = springRestClient.buildRestTemplate();

    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add("Authorization", "Bearer " + authTokenInfo.getAccess_token());

    HttpEntity<?> request = new HttpEntity<Object>(headers);

    restTemplate.exchange(uri, HttpMethod.DELETE, request, String.class );

    return;
  }

}
