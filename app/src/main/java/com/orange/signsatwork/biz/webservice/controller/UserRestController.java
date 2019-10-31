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

import com.orange.signsatwork.DalymotionToken;
import com.orange.signsatwork.SpringRestClient;
import com.orange.signsatwork.biz.domain.*;
import com.orange.signsatwork.biz.persistence.model.UserDB;
import com.orange.signsatwork.biz.persistence.model.VideoViewData;
import com.orange.signsatwork.biz.persistence.repository.UserRepository;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.persistence.service.UserService;
import com.orange.signsatwork.biz.storage.StorageService;
import com.orange.signsatwork.biz.view.model.AuthentModel;
import com.orange.signsatwork.biz.view.model.UserCreationView;
import com.orange.signsatwork.biz.view.model.UserView;
import com.orange.signsatwork.biz.view.model.VideoView2;
import com.orange.signsatwork.biz.webservice.model.UserCreationViewApi;
import com.orange.signsatwork.biz.webservice.model.UserResponseApi;
import com.orange.signsatwork.biz.webservice.model.UserViewApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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


  String VIDEO_THUMBNAIL_FIELDS = "thumbnail_url,thumbnail_60_url,thumbnail_120_url,thumbnail_180_url,thumbnail_240_url,thumbnail_360_url,thumbnail_480_url,thumbnail_720_url,";
  String VIDEO_EMBED_FIELD = "embed_url";


  @Secured("ROLE_USER")
  @RequestMapping(RestApi.WS_SEC_GET_USERS)
  public List<UserView> users() {
    return UserView.from(services.user().all());
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_ADMIN_USERS, method = RequestMethod.POST, headers = {"content-type=application/json"})
  public UserResponseApi user(@RequestBody UserCreationView userCreationView, HttpServletResponse response) {
    UserResponseApi userResponseApi = new UserResponseApi();
    if (services.user().withUserName(userCreationView.getUsername()) != null) {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      userResponseApi.errorMessage = messageByLocaleService.getMessage("user_already_exist");
      return userResponseApi;
    }
    User user = services.user().create(userCreationView.toUser(), userCreationView.getPassword(), userCreationView.getRole());
    services.user().createUserFavorite(user.id, messageByLocaleService.getMessage("default_favorite"));
    response.setStatus(HttpServletResponse.SC_OK);
    return userResponseApi;
  }

  @RequestMapping(value = RestApi.FORGET_PASSWORD)
  public UserResponseApi forgotPassword(@RequestBody UserCreationView userCreationView, HttpServletResponse response, HttpServletRequest request) {
    String title, bodyMail;
    UserResponseApi userResponseApi = new UserResponseApi();
    User user = services.user().withUserName(userCreationView.getUsername());
    if ( user == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      userResponseApi.errorMessage = messageByLocaleService.getMessage("user_not_exist");
      return userResponseApi;
    }

    if (!userCreationView.getUsername().isEmpty()) {
        final String token = UUID.randomUUID().toString();
        services.user().createPasswordResetTokenForUser(user, token);
        final String url = getAppUrl(request) + "/user/changePassword?id=" + user.id + "&token=" + token;
        title = messageByLocaleService.getMessage("password_reset_title");
        bodyMail = messageByLocaleService.getMessage("password_reset_body", new Object[]{url});

        Runnable task = () -> {
          log.info("send mail email = {} / title = {} / body = {}", userCreationView.getUsername(), title, bodyMail);
          services.emailService().sendResetPasswordMessage(userCreationView.getUsername(), title, url);
        };

        new Thread(task).start();
    }


    /*User user = services.user().create(userCreationView.toUser(), userCreationView.getPassword(), userCreationView.getRole());
    services.user().createUserFavorite(user.id, messageByLocaleService.getMessage("default_favorite"));*/
    response.setStatus(HttpServletResponse.SC_OK);
    return userResponseApi;
  }

  private String getAppUrl(HttpServletRequest request) {
    return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_CLOSE, method = RequestMethod.POST)
  public void close( Principal principal) {
    services.user().changeLastDeconnectionDate(principal.getName());
    return;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi. WS_SEC_USER)
  public ResponseEntity<?> user(Principal principal) {

    final User user = AuthentModel.isAuthenticated(principal) ? services.user().withUserName(principal.getName()) : null;

    UserViewApi userViewApi = new UserViewApi(user);

    return new ResponseEntity<>(userViewApi, HttpStatus.OK);

  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_USER, method = RequestMethod.PUT, headers = {"content-type=multipart/mixed", "content-type=multipart/form-data"})
  public UserResponseApi updateVideo(@RequestPart("fileVideoName") Optional<MultipartFile> fileVideoName, @RequestPart("fileJobVideoDescription") Optional<MultipartFile> fileJobVideoDescription, @RequestPart("data") Optional<UserCreationViewApi> userCreationViewApi, HttpServletResponse response, Principal principal) throws
    InterruptedException {
    UserResponseApi userResponseApi = new UserResponseApi();

    User user = services.user().withUserName(principal.getName());

    if (userCreationViewApi.isPresent()) {

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
        if ((!userCreationViewApi.get().getEmail().isEmpty()) && (userCreationViewApi.get().getEmail() != user.email)) {
          services.user().changeEmail(user, userCreationViewApi.get().getEmail());
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
        }
      }

      if (userCreationViewApi.get().getJobTextDescription() != null) {
        if ((!userCreationViewApi.get().getJobTextDescription().isEmpty()) && (userCreationViewApi.get().getJobTextDescription() != user.jobTextDescription)) {
          services.user().changeDescription(user, userCreationViewApi.get().getJobTextDescription());
        }
      }
    }

    if (fileVideoName.isPresent()) {
      return handleSelectedVideoFileUploadForProfil(fileVideoName.get(), principal, "Name", response);
    }

    if (fileJobVideoDescription.isPresent()) {
      return handleSelectedVideoFileUploadForProfil(fileJobVideoDescription.get(), principal, "JobDescription", response);
    }

    response.setStatus(HttpServletResponse.SC_OK);
    return userResponseApi;
  }

  private UserResponseApi handleSelectedVideoFileUploadForProfil(@RequestParam("file") MultipartFile file, Principal principal, String inputType, HttpServletResponse response) throws InterruptedException {
    {
      UserResponseApi userResponseApi = new UserResponseApi();
      try {
        String dailymotionId;
        String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");
        AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
        if (authTokenInfo.isExpired()) {
          dalymotionToken.retrieveToken();
          authTokenInfo = dalymotionToken.getAuthTokenInfo();
        }

        User user = services.user().withUserName(principal.getName());
        storageService.store(file);
        File inputFile = storageService.load(file.getOriginalFilename()).toFile();

        UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


        Resource resource = new FileSystemResource(inputFile.getAbsolutePath());
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        parts.add("file", resource);

        RestTemplate restTemplate = springRestClient.buildRestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);


        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

        ResponseEntity<FileUploadDailymotion> responseDailyMotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
          HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
        FileUploadDailymotion fileUploadDailyMotion = responseDailyMotion.getBody();


        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("url", fileUploadDailyMotion.url);
        if (inputType.equals("JobDescription")) {
          body.add("title", "Description du métier de " + user.name());
        } else {
          body.add("title", user.name());
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


        String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD;
        int i=0;
        do {
          videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
          Thread.sleep(2 * 1000);
          if (i > 30) {
            break;
          }
          i++;
        }
        while ((videoDailyMotion.thumbnail_360_url == null) || (videoDailyMotion.embed_url == null) || (videoDailyMotion.thumbnail_360_url.contains("no-such-asset")));


        if (!videoDailyMotion.embed_url.isEmpty()) {
          if (inputType.equals("JobDescription")) {
            if (user.jobVideoDescription != null) {
              dailymotionId = user.jobVideoDescription.substring(user.jobVideoDescription.lastIndexOf('/') + 1);
              try {
                DeleteVideoOnDailyMotion(dailymotionId);
              }
              catch (Exception errorDailymotionDeleteVideo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                userResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
                return  userResponseApi;
              }
            }
            services.user().changeDescriptionVideoUrl(user, videoDailyMotion.embed_url);
          } else {
            if (user.nameVideo != null) {
              dailymotionId = user.nameVideo.substring(user.nameVideo.lastIndexOf('/') + 1);
              try {
                DeleteVideoOnDailyMotion(dailymotionId);
              }
              catch (Exception errorDailymotionDeleteVideo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                userResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
              }
            }
            services.user().changeNameVideoUrl(user, videoDailyMotion.embed_url);
          }

          log.warn("handleSelectedVideoFileUploadForProfil : embed_url = {}", videoDailyMotion.embed_url);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        return  userResponseApi;

      } catch (Exception errorDailymotionUploadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        userResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionUploadFile");
        return userResponseApi;
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

  @RequestMapping(value = RestApi.SAVE_PASSWORD)
  public UserResponseApi saveUserPassword(@RequestBody UserCreationView userCreationView, @PathVariable long userId, HttpServletResponse response) {
    UserResponseApi userResponseApi = new UserResponseApi();

    services.user().changeUserPassword(services.user().withId(userId), userCreationView.getPassword());

    response.setStatus(HttpServletResponse.SC_OK);
    return  userResponseApi;
  }

  @RequestMapping(value = RestApi.SEND_MAIL)
  public UserResponseApi sendMail(@ModelAttribute UserCreationView userCreationView, HttpServletResponse response) {
    UserResponseApi userResponseApi = new UserResponseApi();
    User admin = services.user().getAdmin();

    String body = messageByLocaleService.getMessage("ask_to_create_user_text", new Object[]{userCreationView.getFirstName(), userCreationView.getLastName(), userCreationView.getEmail()});

    Runnable task = () -> {
      services.emailService().sendSimpleMessage(admin.email.split(""), messageByLocaleService.getMessage("ask_to_create_user_title"), body );
    };

    new Thread(task).start();


    response.setStatus(HttpServletResponse.SC_OK);
    return  userResponseApi;
  }
}
