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
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.storage.StorageService;
import com.orange.signsatwork.biz.view.model.*;
import com.orange.signsatwork.biz.webservice.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Types that carry this annotation are treated as controllers where @RequestMapping
 * methods assume @ResponseBody semantics by default, ie return json body.
 */
@Slf4j
@RestController
/** Rest controller: returns a json body */
public class UserRestController {

  @Autowired
  private StorageService storageService;

  @Autowired
  Services services;
  @Autowired
  DalymotionToken dalymotionToken;
  @Autowired
  private SpringRestClient springRestClient;
  @Autowired
  MessageByLocaleService messageByLocaleService;
  @Autowired
  private Environment environment;
  @Autowired
  PasswordEncoder passwordEncoder;

  @Value("${app.admin.username}")
  String adminUsername;

  @Value("${app.name}")
  String appName;

  @Value("${app.admin.password}")
  String adminPassword;

  String VIDEO_THUMBNAIL_FIELDS = "thumbnail_url,thumbnail_60_url,thumbnail_120_url,thumbnail_180_url,thumbnail_240_url,thumbnail_360_url,thumbnail_480_url,thumbnail_720_url,";
  String VIDEO_EMBED_FIELD = "embed_url";
  String VIDEO_STATUS = ",status";


  @Secured("ROLE_USER")
  @RequestMapping(RestApi.WS_SEC_GET_USERS)
  public List<UserCommunityViewApi> users() {
    return services.user().all().stream().filter(u -> u.id != 1 && u.isEnabled).map(u -> new UserCommunityViewApi(u)).collect(Collectors.toList());
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_ADMIN_USERS, method = RequestMethod.POST, headers = {"content-type=application/json"})
  public UserResponseApi user(@RequestBody UserCreationView userCreationView, HttpServletRequest request, HttpServletResponse response) {
    String title, bodyMail;
    UserResponseApi userResponseApi = new UserResponseApi();
    if (userCreationView.getUsername() == null) {
      if (userCreationView.getMessageServerId() != 0) {
        MessagesServer queryMessagesServer = services.messageServerService().messagesServerCreateUserChangeEmailWithId(userCreationView.getMessageServerId());
        if (queryMessagesServer.list().size() == 1) {
          Date date = queryMessagesServer.list().get(0).date;
          String values = queryMessagesServer.list().get(0).values;
          if (queryMessagesServer.list().get(0).type.equals("RequestCreateUserMessage")) {
            Object[] valuesToArray = Arrays.stream(values.split(";")).toArray();
            String firstName = valuesToArray[0].toString();
            String lastName = valuesToArray[1].toString();
            String email = valuesToArray[2].toString();
            services.messageServerService().updateMessageServerAction(userCreationView.getMessageServerId(), ActionType.NOTDONE);
            title = messageByLocaleService.getMessage("canceled_create_user_title");
            bodyMail = messageByLocaleService.getMessage("canceled_create_user_body", new Object[]{date});

            Runnable task = () -> {
              log.info("send mail email = {} / title = {} / body = {}", email, title, bodyMail);
              services.emailService().sendCanceledCreateUserChangeEmailMessage(email, title, bodyMail, request.getLocale());
            };

            new Thread(task).start();
            userResponseApi.errorMessage = messageByLocaleService.getMessage("confirm_cancel_create_user_text", new Object[]{firstName, lastName});
          } else if (queryMessagesServer.list().get(0).type.equals("RequestChangeEmailMessage")) {
            Object[] valuesToArray = Arrays.stream(values.split(";")).toArray();
            String name = valuesToArray[0].toString();
            String email = valuesToArray[1].toString();
            String newEmail = valuesToArray[2].toString();
            if (userCreationView.getEmail() == null) {
              services.messageServerService().updateMessageServerAction(userCreationView.getMessageServerId(), ActionType.NOTDONE);
              title = messageByLocaleService.getMessage("canceled_change_email_title");
              bodyMail = messageByLocaleService.getMessage("canceled_change_email_body", new Object[]{date});

              Runnable task = () -> {
                log.info("send mail email = {} / title = {} / body = {}", email, title, bodyMail);
                services.emailService().sendCanceledCreateUserChangeEmailMessage(email, title, bodyMail, request.getLocale());
              };

              new Thread(task).start();
              userResponseApi.errorMessage = messageByLocaleService.getMessage("confirm_cancel_change_email_text", new Object[]{name});
            } else {
              final String token = UUID.randomUUID().toString();
              User user = services.user().changeUserLogin(email, newEmail, token);
              services.messageServerService().updateMessageServerAction(userCreationView.getMessageServerId(), ActionType.DONE);

              final String url = getAppUrl() + "/user/createPassword?id=" + user.id + "&token=" + token;
              title = messageByLocaleService.getMessage("ask_to_change_email_title", new Object[]{appName});
              bodyMail = messageByLocaleService.getMessage("email_change_body", new Object[]{url});

              Runnable task = () -> {
                log.info("send mail email = {} / title = {} / body = {}", newEmail, title, bodyMail);
                services.emailService().sendCreatePasswordMessageAfterChangeEmail(newEmail, title,  newEmail, url, request.getLocale());
              };

              new Thread(task).start();
              userResponseApi.errorMessage = messageByLocaleService.getMessage("confirm_change_email_text", new Object[]{name});
            }
          }
        }
      }
    } else {
      if (services.user().withUserName(userCreationView.getUsername()) != null) {
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        userResponseApi.errorMessage = messageByLocaleService.getMessage("user_already_exist");
        return userResponseApi;
      }
      if (userCreationView.getRole().equals("Nourricier")) {
        userCreationView.setRole("USER_A");
      } else if (userCreationView.getRole().equals("Consultatif")) {
        userCreationView.setRole("USER");
      }
      User user = services.user().create(userCreationView.toUser(), passwordEncoder.encode(adminPassword), userCreationView.getRole(), userCreationView.getUsername());
      if (user != null) {
        services.user().createUserFavorite(user.id, messageByLocaleService.getMessage("default_favorite"));
        final String token = UUID.randomUUID().toString();
        services.user().createPasswordResetTokenForUser(user, token);
        if (userCreationView.getMessageServerId() != null && userCreationView.getMessageServerId() != 0)  {
          services.messageServerService().updateMessageServerAction(userCreationView.getMessageServerId(), ActionType.DONE);
        }
        final String url = getAppUrl() + "/user/createPassword?id=" + user.id + "&token=" + token;
        title = messageByLocaleService.getMessage("password_create_title", new Object[]{appName});
        bodyMail = messageByLocaleService.getMessage("password_create_body", new Object[]{appName, userCreationView.getUsername(), url});

        Runnable task = () -> {
          log.info("send mail email = {} / title = {} / body = {}", userCreationView.getUsername(), title, bodyMail);
          services.emailService().sendCreatePasswordMessage(userCreationView.getUsername(), title, userCreationView.getUsername(), url, request.getLocale());
        };

        new Thread(task).start();
      }
      userResponseApi.errorMessage = messageByLocaleService.getMessage("confirm_create_user_text", new Object[]{userCreationView.getFirstName(), userCreationView.getLastName()});
    }
    response.setStatus(HttpServletResponse.SC_OK);
    return userResponseApi;
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_ADMIN_USER, method = RequestMethod.DELETE)
  public UserResponseApi deleteUser(@PathVariable long userId, HttpServletResponse response, HttpServletRequest request) {
    String dailymotionId;
    UserResponseApi userResponseApi = new UserResponseApi();
    User user = services.user().withId(userId);
    if (user.username == adminUsername) {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      userResponseApi.errorMessage = messageByLocaleService.getMessage("user_admin");
      return userResponseApi;
    }


    if (user.nameVideo != null) {
      if (user.nameVideo.contains("http")) {
        dailymotionId = user.nameVideo.substring(user.nameVideo.lastIndexOf('=') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionId);
        } catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          userResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
          return userResponseApi;
        }
      } else {
        DeleteFilesOnServer(user.nameVideo, user.namePicture);
      }
    }

    if (user.jobDescriptionVideo != null) {
      if (user.jobDescriptionVideo.contains("http")) {
        dailymotionId = user.jobDescriptionVideo.substring(user.jobDescriptionVideo.lastIndexOf('=') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionId);
        } catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          userResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
          return userResponseApi;
        }
      } else {
        DeleteFilesOnServer(user.jobDescriptionVideo, user.jobDescriptionPicture);
      }
    }
    services.user().delete(user);

    Runnable task = () -> {
      String title, bodyMail;
      title = messageByLocaleService.getMessage("user_deleted_title", new Object[]{appName});
      bodyMail = messageByLocaleService.getMessage("user_deleted_body");
      log.info("send mail email = {} / title = {} / body = {}", user.username.toString(), title, bodyMail);
      services.emailService().sendDeleteLockUnLockUserMessage(user.username, title, user.name(), user.username, "delete_user_body_1", "delete_user_body_2", "UserDeleteMessage", request.getLocale());
    };

    new Thread(task).start();

    return userResponseApi;
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_ADMIN_USER, method = RequestMethod.PUT)
  public UserResponseApi enableDisableUser(@PathVariable long userId, @RequestParam("unlock") Boolean unlock, HttpServletResponse response, HttpServletRequest request) {
    UserResponseApi userResponseApi = new UserResponseApi();
    User user = services.user().withId(userId);
    if (unlock) {
      if (user.isNonLocked) {
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        userResponseApi.errorMessage = messageByLocaleService.getMessage("user_already_unlock");
        return userResponseApi;
      } else if (!user.isNonLocked){
        services.user().unlock(user);
        Runnable task = () -> {
          String title, bodyMail;
          title = messageByLocaleService.getMessage("user_unlock_title", new Object[]{appName});
          bodyMail = messageByLocaleService.getMessage("user_unlock_body");
          log.info("send mail email = {} / title = {} / body = {}", user.username.toString(), title, bodyMail);
          services.emailService().sendDeleteLockUnLockUserMessage(user.username, title, user.name(), user.username, "unlock_user_body_1", "unlock_user_body_2", "UserUnLockMessage", request.getLocale());
        };

        new Thread(task).start();
      }
    } else if (!unlock){
      if (!user.isNonLocked) {
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        userResponseApi.errorMessage = messageByLocaleService.getMessage("user_already_lock");
        return userResponseApi;
      } else if (user.isNonLocked){
        services.user().lock(user);
        Runnable task = () -> {
          String title, bodyMail;
          title = messageByLocaleService.getMessage("user_lock_title", new Object[]{appName});
          bodyMail = messageByLocaleService.getMessage("user_lock_body");
          log.info("send mail email = {} / title = {} / body = {}", user.username.toString(), title, bodyMail);
          services.emailService().sendDeleteLockUnLockUserMessage(user.username, title, user.name(), user.username, "lock_user_body_1", "lock_user_body_2", "UserLockMessage", request.getLocale());
        };

        new Thread(task).start();
      }

    }
    response.setStatus(HttpServletResponse.SC_OK);
    return userResponseApi;
  }

  @RequestMapping(value = RestApi.FORGET_PASSWORD)
  public UserResponseApi forgotPassword(@RequestBody UserCreationView userCreationView, HttpServletResponse response, HttpServletRequest request) {
    String title, bodyMail;
    UserResponseApi userResponseApi = new UserResponseApi();
    userCreationView.clearXss();
    User user = services.user().withUserName(userCreationView.getUsername());
    if ( user == null) {
      response.setStatus(HttpServletResponse.SC_OK);
      /*userResponseApi.errorMessage = messageByLocaleService.getMessage("user_not_exist");*/
      return userResponseApi;
    }

    if (!userCreationView.getUsername().isEmpty()) {
        final String token = UUID.randomUUID().toString();
        services.user().createPasswordResetTokenForUser(user, token);
        final String url = getAppUrl() + "/user/changePassword?id=" + user.id + "&token=" + token;
        title = messageByLocaleService.getMessage("password_reset_title", new Object[]{appName});
        bodyMail = messageByLocaleService.getMessage("password_reset_body", new Object[]{url});

        Runnable task = () -> {
          log.info("send mail email = {} / title = {} / body = {}", userCreationView.getUsername(), title, bodyMail);
          services.emailService().sendResetPasswordMessage(userCreationView.getUsername(), title, user.name(), url, request.getLocale());
        };

        new Thread(task).start();
    }


    response.setStatus(HttpServletResponse.SC_OK);
    return userResponseApi;
  }

  private String getAppUrl() {
    return environment.getProperty("app.url");
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_CLOSE, method = RequestMethod.POST)
  public void close( Principal principal) {
    services.user().changeLastDeconnectionDate(principal.getName());
    return;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_USER_ME, method = RequestMethod.GET)
  public ResponseEntity<?> userMe(Principal principal) {

    final User user = AuthentModel.isAuthenticated(principal) ? services.user().withUserName(principal.getName()) : null;
    String role = null;

    if (AuthentModel.hasRole("ROLE_ADMIN")) {
      role = "ROLE_ADMIN";
    } else if (AuthentModel.hasRole("ROLE_USER_A")) {
      role = "ROLE_USER_A";
    } else if (AuthentModel.hasRole("ROLE_USER")) {
      role = "ROLE_USER";
    }

    UserMeViewApi userMeViewApi = new UserMeViewApi(user, role);

    return new ResponseEntity<>(userMeViewApi, HttpStatus.OK);

  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_USER, method = RequestMethod.GET)
  public ResponseEntity<?> user(@PathVariable long userId, Principal principal) {

    User userConnected = services.user().withUserName(principal.getName());
    if (userConnected.id == userId) {
      UserMeViewApi userMeViewApi = new UserMeViewApi(userConnected);
      return new ResponseEntity<>(userMeViewApi, HttpStatus.OK);
    } else {
      User user = services.user().withId(userId);
      UserViewApi userViewApi = new UserViewApi(user);
      return new ResponseEntity<>(userViewApi, HttpStatus.OK);
    }

  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_USER_ME, method = RequestMethod.PUT, headers = {"content-type=multipart/mixed", "content-type=multipart/form-data", "content-type=application/json"})
  public UserResponseApi updateProfil(@RequestPart("fileVideoName") Optional<MultipartFile> fileVideoName, @RequestPart("fileJobVideoDescription") Optional<MultipartFile> fileJobVideoDescription, @RequestPart("data") Optional<UserCreationViewApi> userCreationViewApi, HttpServletResponse response, HttpServletRequest request, Principal principal) throws
    InterruptedException {
    UserResponseApi userResponseApi = new UserResponseApi();

    User user = services.user().withUserName(principal.getName());

    if (userCreationViewApi.isPresent()) {
      userCreationViewApi.get().clearXss();
      if (userCreationViewApi.get().getFirstName() != null) {
        if ((!userCreationViewApi.get().getFirstName().isEmpty()) && (userCreationViewApi.get().getFirstName() != user.firstName)) {
          services.user().changeFirstName(user, userCreationViewApi.get().getFirstName());
        }
      }

      if (userCreationViewApi.get().getLastName() != null) {
        if ((!userCreationViewApi.get().getLastName().isEmpty()) && (userCreationViewApi.get().getLastName() != user.lastName)) {
          services.user().changeLastName(user, userCreationViewApi.get().getLastName());
        }
      }

      if (userCreationViewApi.get().getEmail() != null) {
        String title, body;
        User admin = services.user().getAdmin();

        User userSearch = services.user().withUserName(userCreationViewApi.get().getEmail());
        if (userSearch == null) {
          MessagesServer queryMessagesServer = services.messageServerService().messagesServerChangeEmailWithUserName(userCreationViewApi.get().getEmail());
          if (queryMessagesServer.list().size() >= 1) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            userResponseApi.errorMessage = messageByLocaleService.getMessage("request_for_change_email_user_already_exist");
            return  userResponseApi;
          }
          Date date = new Date();
          String values = user.name() + ";" + user.username + ";" + userCreationViewApi.get().getEmail();
          MessageServer messageServer = new MessageServer(new Date(), "RequestChangeEmailMessage", values, ActionType.TODO);
          long idMessage = services.messageServerService().addMessageServer(messageServer);
          String url = getAppUrl() + "/sec/admin/create-users?id=" + idMessage;
          body = messageByLocaleService.getMessage("ask_to_change_email_text", new Object[]{date, user.name(), user.username, url});
          title = messageByLocaleService.getMessage("ask_to_change_email_title", new Object[]{user.name()});
          User finalUser = user;
          Runnable task = () -> {
            log.info("send mail email = {} / title = {} / body = {}", admin.email, title, body);
            services.emailService().sendChangeEmailMessage(admin.email, title, date, finalUser.name(), finalUser.username, url, request.getLocale());
          };

          new Thread(task).start();
          response.setStatus(HttpServletResponse.SC_OK);
        } else {
          response.setStatus(HttpServletResponse.SC_CONFLICT);
          userResponseApi.errorMessage = messageByLocaleService.getMessage("user_already_exist");
        }
      }

      if (userCreationViewApi.get().getEntity() != null) {
        if ((!userCreationViewApi.get().getEntity().isEmpty()) && (userCreationViewApi.get().getEntity() != user.entity)) {
          services.user().changeEntity(user, userCreationViewApi.get().getEntity());
        }
      }

      if (userCreationViewApi.get().getJob() != null) {
        if ((!userCreationViewApi.get().getJob().isEmpty()) && (userCreationViewApi.get().getJob() != user.job)) {
          services.user().changeJob(user, userCreationViewApi.get().getJob());
          user = user.loadCommunities();
          List<Community> oldCommunitiesJob = user.communities.stream().filter(c -> c.type == CommunityType.Job).collect(Collectors.toList());
          for(Community community:oldCommunitiesJob) {
            Community community1 = services.community().withCommunityName(community.name);
            List<Long> listUsersIds = community1.usersIds();
            listUsersIds.remove(user.id);
            services.community().changeCommunityUsers(community1.id, listUsersIds);
          }

          Community community = services.community().withCommunityName(userCreationViewApi.get().getJob());
          List<Long> usersIds = community.usersIds();
          usersIds.add(user.id);
          services.community().changeCommunityUsers(community.id, usersIds);
        }
      }

      if (userCreationViewApi.get().getJobDescriptionText() != null) {
        if ((!userCreationViewApi.get().getJobDescriptionText().isEmpty()) && (userCreationViewApi.get().getJobDescriptionText() != user.jobDescriptionText)) {
          services.user().changeDescription(user, userCreationViewApi.get().getJobDescriptionText());
        }
      }
    }

    if (fileVideoName.isPresent()) {
      if (environment.getProperty("app.dailymotion_url").isEmpty()) {
        return handleSelectedVideoFileUploadForProfilOnServer(fileVideoName.get(), principal, "Name", response);
      } else {
        return handleSelectedVideoFileUploadForProfil(fileVideoName.get(), principal, "Name", response);
      }
    }

    if (fileJobVideoDescription.isPresent()) {
      if (environment.getProperty("app.dailymotion_url").isEmpty()) {
        return handleSelectedVideoFileUploadForProfilOnServer(fileJobVideoDescription.get(), principal, "JobDescription", response);
      } else {
        return handleSelectedVideoFileUploadForProfil(fileJobVideoDescription.get(), principal, "JobDescription", response);
      }
    }

    response.setStatus(HttpServletResponse.SC_OK);
    return userResponseApi;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_USER_ME_DATAS, method = RequestMethod.PUT, headers = {"content-type=application/json"})
  public UserResponseApi updateDataProfil(@RequestBody UserCreationViewApi userCreationViewApi, HttpServletResponse response, Principal principal) throws
    InterruptedException {
    UserResponseApi userResponseApi = new UserResponseApi();

    User user = services.user().withUserName(principal.getName());


    if (userCreationViewApi != null) {
      userCreationViewApi.clearXss();
      if (userCreationViewApi.getFirstName() != null) {
        if ((!userCreationViewApi.getFirstName().isEmpty()) && (userCreationViewApi.getFirstName() != user.firstName)) {
          services.user().changeFirstName(user, userCreationViewApi.getFirstName());
        }
      }

      if (userCreationViewApi.getLastName() != null) {
        if ((!userCreationViewApi.getLastName().isEmpty()) && (userCreationViewApi.getLastName() != user.lastName)) {
          services.user().changeLastName(user, userCreationViewApi.getLastName());
        }
      }

      if (userCreationViewApi.getEmail() != null) {
        if ((!userCreationViewApi.getEmail().isEmpty()) && (userCreationViewApi.getEmail() != user.email)) {
          services.user().changeEmail(user, userCreationViewApi.getEmail());
        }
      }

      if (userCreationViewApi.getEntity() != null) {
        if ((!userCreationViewApi.getEntity().isEmpty()) && (userCreationViewApi.getEntity() != user.entity)) {
          services.user().changeEntity(user, userCreationViewApi.getEntity());
        }
      }

      if (userCreationViewApi.getJob() != null) {
        if ((!userCreationViewApi.getJob().isEmpty()) && (userCreationViewApi.getJob() != user.job)) {
          services.user().changeJob(user, userCreationViewApi.getJob());
          user = user.loadCommunities();
          List<Community> oldCommunitiesJob = user.communities.stream().filter(c -> c.type == CommunityType.Job).collect(Collectors.toList());
          for(Community community:oldCommunitiesJob) {
            Community community1 = services.community().withCommunityName(community.name);
            List<Long> listUsersIds = community1.usersIds();
            listUsersIds.remove(user.id);
            services.community().changeCommunityUsers(community1.id, listUsersIds);
          }

          Community community = services.community().withCommunityName(userCreationViewApi.getJob());
          List<Long> usersIds = community.usersIds();
          usersIds.add(user.id);
          services.community().changeCommunityUsers(community.id, usersIds);
        }
      }

      if (userCreationViewApi.getJobDescriptionText() != null) {
        if ((userCreationViewApi.getJobDescriptionText() != user.jobDescriptionText)) {
          services.user().changeDescription(user, userCreationViewApi.getJobDescriptionText());
        }
      }
    }

    response.setStatus(HttpServletResponse.SC_OK);
    return userResponseApi;
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_SEC_USER_DATAS, method = RequestMethod.PUT, headers = {"content-type=application/json"})
  public UserResponseApi updateDataProfilByAdmin(@PathVariable long userId, @RequestBody UserCreationViewApi userCreationViewApi, HttpServletResponse response, HttpServletRequest request, Principal principal) throws
    InterruptedException {
    String title, body = null, messageServer = null;
    UserResponseApi userResponseApi = new UserResponseApi();

    User user = services.user().withId(userId);
    User admin = services.user().getAdmin();
    title = messageByLocaleService.getMessage("admin_change_profil");

    if (userCreationViewApi != null) {
      userCreationViewApi.clearXss();
      if ((userCreationViewApi.getFirstName() != null) && (userCreationViewApi.getLastName() != null)) {
        if ((!userCreationViewApi.getFirstName().isEmpty()) && !(userCreationViewApi.getFirstName().equals(user.firstName)) && ((!userCreationViewApi.getLastName().isEmpty()) && !(userCreationViewApi.getLastName().equals(user.lastName)))) {
          services.user().changeFirstName(user, userCreationViewApi.getFirstName());
          services.user().changeLastName(user, userCreationViewApi.getLastName());
          body = messageByLocaleService.getMessage("admin_change_your_first_and_last_name", new Object[]{admin.username});
          messageServer = "UserChangeFirstLastNameMessage";
        } else if  ((!userCreationViewApi.getFirstName().isEmpty()) && !(userCreationViewApi.getFirstName().equals(user.firstName))) {
          services.user().changeFirstName(user, userCreationViewApi.getFirstName());
          body = messageByLocaleService.getMessage("admin_change_your_first_name", new Object[]{admin.username});
          messageServer = "UserChangeFirstNameMessage";
        } else if ((!userCreationViewApi.getLastName().isEmpty()) && !(userCreationViewApi.getLastName().equals(user.lastName))) {
          services.user().changeLastName(user, userCreationViewApi.getLastName());
          body = messageByLocaleService.getMessage("admin_change_your_last_name", new Object[]{admin.username});
          messageServer = "UserChangeLastNameMessage";
        }
      }


      if (userCreationViewApi.getEntity() != null) {
        if ((!userCreationViewApi.getEntity().isEmpty()) && (userCreationViewApi.getEntity() != user.entity)) {
          services.user().changeEntity(user, userCreationViewApi.getEntity());
          body = messageByLocaleService.getMessage("admin_change_your_entity", new Object[]{admin.username});
          messageServer = "UserChangeEntityMessage";
        }
      }

      if (userCreationViewApi.getJob() != null) {
        if ((!userCreationViewApi.getJob().isEmpty()) && (userCreationViewApi.getJob() != user.job)) {
          services.user().changeJob(user, userCreationViewApi.getJob());
          user = user.loadCommunities();
          List<Community> oldCommunitiesJob = user.communities.stream().filter(c -> c.type == CommunityType.Job).collect(Collectors.toList());
          for(Community community:oldCommunitiesJob) {
            Community community1 = services.community().withCommunityName(community.name);
            List<Long> listUsersIds = community1.usersIds();
            listUsersIds.remove(user.id);
            services.community().changeCommunityUsers(community1.id, listUsersIds);
          }

          Community community = services.community().withCommunityName(userCreationViewApi.getJob());
          List<Long> usersIds = community.usersIds();
          usersIds.add(user.id);
          services.community().changeCommunityUsers(community.id, usersIds);
          body = messageByLocaleService.getMessage("admin_change_your_job", new Object[]{admin.username});
          messageServer = "UserChangeJobMessage";
        }
      }

      if (userCreationViewApi.getJobDescriptionText() != null) {
        if ((userCreationViewApi.getJobDescriptionText() != user.jobDescriptionText)) {
          services.user().changeDescription(user, userCreationViewApi.getJobDescriptionText());
          body = messageByLocaleService.getMessage("admin_change_your_job_description_text", new Object[]{admin.username});
          messageServer = "UserChangeJobDescriptionTextMessage";
        }
      }
      if (body != null) {
        String finalBody = body;
        String finalMessageServer = messageServer;
        User finalUser = user;
        Runnable task = () -> {
          log.info("send mail email = {} / title = {} / body = {}", finalUser.username, title, finalBody);
          services.emailService().sendUpdateProfilUserByAdminMessage(finalUser.username, title, finalUser.username, finalBody, finalMessageServer, request.getLocale());
        };

        new Thread(task).start();
      }
    }

    response.setStatus(HttpServletResponse.SC_OK);
    return userResponseApi;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_USER_ME_DELETE_VIDEO_NAME, method = RequestMethod.PUT)
  public UserResponseApi deleteNameVideo(@PathVariable long userId, HttpServletResponse response, Principal principal) throws
    InterruptedException {
    UserResponseApi userResponseApi = new UserResponseApi();

    User user = services.user().withId(userId);
    User userConnected = services.user().withUserName(principal.getName());

    if (user.id == userConnected.id) {
      if (user.nameVideo != null) {
        if (user.nameVideo.contains("http")) {
          String dailymotionId = user.nameVideo.substring(user.nameVideo.lastIndexOf('=') + 1);
          try {
            DeleteVideoOnDailyMotion(dailymotionId);
          } catch (Exception errorDailymotionDeleteVideo) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            userResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
            return userResponseApi;
          }
        } else {
          DeleteFilesOnServer(user.nameVideo, user.namePicture);
        }
        services.user().changeNameVideoUrl(user, null, null);
      }
    } else {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      userResponseApi.errorMessage = messageByLocaleService.getMessage("forbidden_action");
      return userResponseApi;
    }

    response.setStatus(HttpServletResponse.SC_OK);
    return userResponseApi;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_USER_ME_DELETE_VIDEO_JOB, method = RequestMethod.PUT)
  public UserResponseApi deleteJobVideo(@PathVariable long userId, HttpServletResponse response, Principal principal) throws
    InterruptedException {
    UserResponseApi userResponseApi = new UserResponseApi();

    User user = services.user().withId(userId);
    User userConnected = services.user().withUserName(principal.getName());

    if (user.id == userConnected.id) {
      if (user.jobDescriptionVideo != null) {
        if (user.jobDescriptionVideo.contains("http")) {
          String dailymotionId = user.jobDescriptionVideo.substring(user.jobDescriptionVideo.lastIndexOf('=') + 1);
          try {
            DeleteVideoOnDailyMotion(dailymotionId);
          } catch (Exception errorDailymotionDeleteVideo) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            userResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
            return userResponseApi;
          }
        } else {
          DeleteFilesOnServer(user.jobDescriptionVideo, user.jobDescriptionPicture);
        }
        services.user().changeDescriptionVideoUrl(user, null, null);
      }
    } else {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      userResponseApi.errorMessage = messageByLocaleService.getMessage("forbidden_action");
      return userResponseApi;
    }

    response.setStatus(HttpServletResponse.SC_OK);
    return userResponseApi;
  }

  private void GenerateThumbnail(String thumbnailFile, String fileOutput, String logFile) {
    String cmdGenerateThumbnail;

    cmdGenerateThumbnail = String.format("input=\"%s\"&&dur=$(ffprobe -loglevel error -show_entries format=duration -of default=nk=1:nw=1 \"$input\")&&ffmpeg -y -ss \"$(echo \"$dur / 2\" | bc -l | sed -e 's/^-\\./-0./' -e 's/^\\./0./')\" -i  \"$input\" -vframes 1 -s 360x360 -vf \"scale=(iw*sar)*max(360.1/(iw*sar)\\,360.1/ih):ih*max(360.1/(iw*sar)\\,360.1/ih), crop=360:360\" \"%s\"", fileOutput, thumbnailFile);
    NativeInterface.launch(cmdGenerateThumbnail, null, logFile);
  }

  private UserResponseApi handleSelectedVideoFileUploadForProfil(@RequestParam("file") MultipartFile file, Principal principal, String inputType, HttpServletResponse response) throws InterruptedException {
    {
      String videoUrl = null;
      String fileName = environment.getProperty("app.file") + "/" + file.getOriginalFilename();
      String thumbnailFile = environment.getProperty("app.file") + "thumbnail/" + file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf('.')) + ".png";
      String logFile = "/tmp/" + file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf('.')) + ".log";
      File inputFile;
      UserResponseApi userResponseApi = new UserResponseApi();

      try {
        storageService.store(file);
        inputFile = storageService.load(file.getOriginalFilename()).toFile();
      } catch (Exception errorLoadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        userResponseApi.errorMessage = messageByLocaleService.getMessage("errorUploadFile");
        return userResponseApi;
      }

      try {
        GenerateThumbnail(thumbnailFile, inputFile.getAbsolutePath(), logFile);
      } catch (Exception errorEncondingFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        userResponseApi.errorMessage = messageByLocaleService.getMessage("errorThumbnailFile");
        return userResponseApi;
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
        if (inputType.equals("JobDescription")) {
          body.add("title", messageByLocaleService.getMessage("user.job_description"));
        } else {
          body.add("title", messageByLocaleService.getMessage("user.name_LSF"));
        }
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
        String pictureUri = null;
        String id = videoDailyMotion.id;
          pictureUri = thumbnailFile;
          videoUrl= fileName;

        if (inputType.equals("JobDescription")) {
          if (user.jobDescriptionVideo != null) {
            if (user.jobDescriptionVideo.contains("http")) {
              dailymotionId = user.jobDescriptionVideo.substring(user.jobDescriptionVideo.lastIndexOf('=') + 1);
              try {
                DeleteVideoOnDailyMotion(dailymotionId);
              } catch (Exception errorDailymotionDeleteVideo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                userResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
                return userResponseApi;
              }
            }
          }
          services.user().changeDescriptionVideoUrl(user, videoUrl, pictureUri);
        } else {
          if (user.nameVideo != null) {
            if (user.nameVideo.contains("http")) {
              dailymotionId = user.nameVideo.substring(user.nameVideo.lastIndexOf('=') + 1);
              try {
                DeleteVideoOnDailyMotion(dailymotionId);
              } catch (Exception errorDailymotionDeleteVideo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                userResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
              }
            }
          }
          services.user().changeNameVideoUrl(user, videoUrl, pictureUri);
        }

        log.warn("handleSelectedVideoFileUploadForProfil : embed_url = {}", videoDailyMotion.embed_url);

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
              if (inputType.equals("JobDescription")) {
                services.user().changeDescriptionVideoUrl(user, dailyMotion.embed_url, dailyMotion.thumbnail_360_url);
              } else {
                services.user().changeNameVideoUrl(user, dailyMotion.embed_url, dailyMotion.thumbnail_360_url);
              }
            }

          };

          new Thread(task).start();

        response.setStatus(HttpServletResponse.SC_OK);
        return  userResponseApi;

      } catch (Exception errorDailymotionUploadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        userResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionUploadFile");
        return userResponseApi;
      }
    }
  }

  private UserResponseApi handleSelectedVideoFileUploadForProfilOnServer(@RequestParam("file") MultipartFile file, Principal principal, String inputType, HttpServletResponse response) throws InterruptedException {
    {
      String videoUrl = null;
      String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
      String newFileName = UUID.randomUUID().toString() + "." + fileExtension;
      String newAbsoluteFileName = environment.getProperty("app.file") +"/" + newFileName;
      String thumbnailFile = environment.getProperty("app.file") + "/thumbnail/" + newFileName.substring(0, newFileName.lastIndexOf('.')) + ".png";
      String logFile = "/tmp/" + newFileName.substring(0, newFileName.lastIndexOf('.')) + ".log";
      File inputFile;
      Streams streamInfo;
      String newAbsoluteFileNameWithExtensionMp4 = newAbsoluteFileName.substring(0, newAbsoluteFileName.lastIndexOf('.')) + ".mp4";

      UserResponseApi userResponseApi = new UserResponseApi();

      try {
        storageService.store(file);
        inputFile = storageService.load(file.getOriginalFilename()).toFile();
        File newName = new File(newAbsoluteFileName);
        inputFile.renameTo(newName);
      } catch (Exception errorLoadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        userResponseApi.errorMessage = messageByLocaleService.getMessage("errorUploadFile");
        return userResponseApi;
      }

      try {
        GenerateThumbnail(thumbnailFile, newAbsoluteFileName, logFile);
      } catch (Exception errorEncondingFile) {
        DeleteFilesOnServer(newAbsoluteFileName, null);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        userResponseApi.errorMessage = messageByLocaleService.getMessage("errorThumbnailFile");
        return userResponseApi;
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
            DeleteFilesOnServer(newAbsoluteFileName, null);
            newAbsoluteFileName = newAbsoluteFileNameWithExtensionMp4;
          }
        }
      }

      User user = services.user().withUserName(principal.getName());

      String pictureUri = thumbnailFile;
      videoUrl= newAbsoluteFileName;

      if (inputType.equals("JobDescription")) {
        if (user.jobDescriptionVideo != null) {
          DeleteFilesOnServer(user.jobDescriptionVideo, user.jobDescriptionPicture);
        }
        services.user().changeDescriptionVideoUrl(user, videoUrl, pictureUri);
      } else {
        if (user.nameVideo != null) {
          DeleteFilesOnServer(user.nameVideo, user.namePicture);
        }
        services.user().changeNameVideoUrl(user, videoUrl, pictureUri);
      }

      response.setStatus(HttpServletResponse.SC_OK);
      return  userResponseApi;

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
    if (file.equals(fileOutput)) {
      String fileTmp =  fileOutput.substring(0, fileOutput.lastIndexOf('.')) + ".tmp.mp4";
      cmd = String.format("ffmpeg -i %s -filter:v \"scale='min(1280,iw)':min'(720,ih)':force_original_aspect_ratio=decrease,pad=1280:720:(ow-iw)/2:(oh-ih)/2,crop=1280:720\" %s", file, fileTmp);
      NativeInterface.launch(cmd, null, null);
      Path source = Paths.get(fileTmp);
      Path target = Paths.get(file);
      try{
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      cmd = String.format("ffmpeg -i %s -filter:v \"scale='min(1280,iw)':min'(720,ih)':force_original_aspect_ratio=decrease,pad=1280:720:(ow-iw)/2:(oh-ih)/2,crop=1280:720\" %s", file, fileOutput);
      NativeInterface.launch(cmd, null, null);
    }

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

  private void DeleteFilesOnServer(String url, String pictureUri) {
    if (url != null) {
      File video = new File(url);
      if (video.exists()) {
        video.delete();
      }
    }
    if (pictureUri != null) {
      File thumbnail = new File(pictureUri);
      if (thumbnail.exists()) {
        thumbnail.delete();
      }
    }
  }

  @RequestMapping(value = RestApi.SAVE_PASSWORD)
  public UserResponseApi saveUserPassword(@RequestBody UserCreationView userCreationView, @PathVariable long userId, HttpServletResponse response) {
    UserResponseApi userResponseApi = new UserResponseApi();

    User user = services.user().withId(userId);
    userCreationView.clearXss();
    PasswordResetToken passToken = services.user().getPasswordResetToken(userCreationView.getToken());
    if ((passToken == null) || (user.id != passToken.user.id)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      userResponseApi.errorMessage = messageByLocaleService.getMessage("token_not_ok");
      return userResponseApi;
    }

    Calendar cal = Calendar.getInstance();
    if ((passToken.expiryDate.getTime() - cal.getTime().getTime()) <= 0) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      userResponseApi.errorMessage = messageByLocaleService.getMessage("token_not_ok");
      return userResponseApi;
    }

    services.user().changeUserPassword(user, userCreationView.getPassword());

    String values = user.name();
    MessageServer messageServer = new MessageServer(new Date(), "SavePasswordMessage", values, ActionType.NO);
    services.messageServerService().addMessageServer(messageServer);

    response.setStatus(HttpServletResponse.SC_OK);
    return  userResponseApi;
  }

  @RequestMapping(value = RestApi.SEND_MAIL)
  public UserResponseApi sendMail(@RequestBody UserCreationView userCreationView, HttpServletRequest request, HttpServletResponse response) {
    String title, body;
    UserResponseApi userResponseApi = new UserResponseApi();
    User admin = services.user().getAdmin();
    userCreationView.clearXss();
    String userToCreateEmail = userCreationView.getEmail().trim();
    User user = services.user().withUserName(userToCreateEmail);
    if (user == null) {
      MessagesServer queryMessagesServer = services.messageServerService().messagesServerCreateUserWithUserName(userToCreateEmail);
      if (queryMessagesServer.list().size() >= 1) {
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        userResponseApi.errorMessage = messageByLocaleService.getMessage("request_for_create_user_already_exist");
        return  userResponseApi;
      }
      Date date = new Date();
      String values = userCreationView.getFirstName() + ";" + userCreationView.getLastName() + ";" + userToCreateEmail;
      MessageServer messageServer = new MessageServer(new Date(), "RequestCreateUserMessage", values, ActionType.TODO);
      long idMessage = services.messageServerService().addMessageServer(messageServer);
      String url = getAppUrl() + "/sec/admin/create-users?id=" + idMessage;
      body = messageByLocaleService.getMessage("ask_to_create_user_text", new Object[]{date, userToCreateEmail, url});
      title = messageByLocaleService.getMessage("ask_to_create_user_title", new Object[]{userCreationView.getFirstName(), userCreationView.getLastName()});
      Runnable task = () -> {
        log.info("send mail email = {} / title = {} / body = {}", admin.email, title, body);
        services.emailService().sendCreateUserMessage(admin.email, title, date, userToCreateEmail, url, request.getLocale());
      };

      new Thread(task).start();

      response.setStatus(HttpServletResponse.SC_OK);
    } else {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      userResponseApi.errorMessage = messageByLocaleService.getMessage("user_already_exist");
    }
    return  userResponseApi;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.SEND_MAIL_FOR_CHANGE_EMAIL)
  public UserResponseApi sendMailForChangeEmail(@RequestBody UserCreationView userCreationView, HttpServletResponse response, HttpServletRequest request, Principal principal) {
    String title, body;
    UserResponseApi userResponseApi = new UserResponseApi();
    User admin = services.user().getAdmin();
    User user = services.user().withUserName(principal.getName());
    userCreationView.clearXss();
    String userToCreateEmail = userCreationView.getEmail().trim();
    User userSearch = services.user().withUserName(userToCreateEmail);
    if (userSearch == null) {
      MessagesServer queryMessagesServer = services.messageServerService().messagesServerChangeEmailWithUserName(userToCreateEmail);
      if (queryMessagesServer.list().size() >= 1) {
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        userResponseApi.errorMessage = messageByLocaleService.getMessage("request_for_change_email_user_already_exist");
        return  userResponseApi;
      }
      Date date = new Date();
      String values = user.name() + ";" + user.username + ";" + userToCreateEmail;
      MessageServer messageServer = new MessageServer(new Date(), "RequestChangeEmailMessage", values, ActionType.TODO);
      long idMessage = services.messageServerService().addMessageServer(messageServer);
      String url = getAppUrl() + "/sec/admin/create-users?id=" + idMessage;
      body = messageByLocaleService.getMessage("ask_to_change_email_text", new Object[]{date, user.name(), user.username, url});
      title = messageByLocaleService.getMessage("ask_to_change_email_title", new Object[]{user.name()});
      Runnable task = () -> {
        log.info("send mail email = {} / title = {} / body = {}", admin.email, title, body);
        services.emailService().sendChangeEmailMessage(admin.email, title, date, user.name(),user.username, url, request.getLocale());
      };

      new Thread(task).start();

      response.setStatus(HttpServletResponse.SC_OK);
    } else {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      userResponseApi.errorMessage = messageByLocaleService.getMessage("user_already_exist");
    }
    return  userResponseApi;
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.ARTICLES)
  public ResponseEntity<?> articles(@RequestParam("language") String language, @RequestParam("type") ArticleType type,  Principal principal) {

    Articles articlesCgu = services.article().findByLanguageAndType(language, type);
    List<ArticleView> articleViews = articlesCgu.list().stream().map(article -> new ArticleView(article.name, article.descriptionText, article.descriptionPicture, article.descriptionVideo)).collect(Collectors.toList());

    return  new ResponseEntity<>(articleViews, HttpStatus.OK);
  }
}