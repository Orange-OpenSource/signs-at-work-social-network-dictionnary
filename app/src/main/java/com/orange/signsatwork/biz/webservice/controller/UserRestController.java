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
import com.orange.signsatwork.biz.nativeinterface.NativeInterface;
import com.orange.signsatwork.biz.persistence.model.SignViewData;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.storage.StorageService;
import com.orange.signsatwork.biz.view.controller.CommentOrderComparator;
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

  @Value("${app.admin.username}")
  String adminUsername;

  @Value("${app.name}")
  String appName;

  String VIDEO_THUMBNAIL_FIELDS = "thumbnail_url,thumbnail_60_url,thumbnail_120_url,thumbnail_180_url,thumbnail_240_url,thumbnail_360_url,thumbnail_480_url,thumbnail_720_url,";
  String VIDEO_EMBED_FIELD = "embed_url";
  String VIDEO_STATUS = ",status";


  @Secured("ROLE_USER")
  @RequestMapping(RestApi.WS_SEC_GET_USERS)
  public List<UserCommunityViewApi> users() {
    return services.user().all().stream().map(u -> new UserCommunityViewApi(u)).collect(Collectors.toList());
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
    User user = services.user().create(userCreationView.toUser(), userCreationView.getPassword(), userCreationView.getRole(), userCreationView.getUsername());
    services.user().createUserFavorite(user.id, messageByLocaleService.getMessage("default_favorite"));
    response.setStatus(HttpServletResponse.SC_OK);
    return userResponseApi;
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi. WS_ADMIN_USER, method = RequestMethod.DELETE)
  public UserResponseApi deleteUser(@PathVariable long userId, HttpServletResponse response) {
    String dailymotionId;
    UserResponseApi userResponseApi = new UserResponseApi();
    User user = services.user().withId(userId);
    if (user.username == adminUsername) {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      userResponseApi.errorMessage = messageByLocaleService.getMessage("user_admin");
      return userResponseApi;
    }


    user = user.loadVideos();
    if (!user.videos.list().isEmpty()) {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      userResponseApi.errorMessage = messageByLocaleService.getMessage("user_have_videos");
      return userResponseApi;
    }

    user = user.loadCommunitiesRequestsFavorites();
    if (!user.requests.list().isEmpty()) {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      userResponseApi.errorMessage = messageByLocaleService.getMessage("user_have_requests");
      return userResponseApi;
    }

    if (!user.communities.list().isEmpty()) {
      for (Community c:user.communities.list()) {
        if (c.user.id == user.id) {
          response.setStatus(HttpServletResponse.SC_CONFLICT);
          userResponseApi.errorMessage = messageByLocaleService.getMessage("user_have_communities");
          return userResponseApi;
        }
      }
    }

    if (!user.favorites.list().isEmpty()) {
      for (Favorite f:user.favorites.list()) {
        if (f.user.id == user.id && f.type == FavoriteType.Share) {
          response.setStatus(HttpServletResponse.SC_CONFLICT);
          userResponseApi.errorMessage = messageByLocaleService.getMessage("user_have_favorites_shared");
          return userResponseApi;
        }
      }
    }

    if (user.nameVideo != null) {
      if (user.nameVideo.contains("http")) {
        dailymotionId = user.nameVideo.substring(user.nameVideo.lastIndexOf('/') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionId);
        } catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          userResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
        }
      }
    }

    if (user.jobDescriptionVideo != null) {
      if (user.jobDescriptionVideo.contains("http")) {
        dailymotionId = user.jobDescriptionVideo.substring(user.jobDescriptionVideo.lastIndexOf('/') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionId);
        } catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          userResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
          return userResponseApi;
        }
      }
    }

    Favorites favorites = services.favorite().oldFavoritesShareToUser(user.id);
    for(Favorite favorite:favorites.list()) {
      services.favorite().removeMeFromSeeFavorite(favorite.id, user.id);
    }

    for (Community community:user.communities.list()) {
      services.community().removeMeFromCommunity(community.id, user.id);
    }


    services.user().delete(user);


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
        title = messageByLocaleService.getMessage("password_reset_title", new Object[]{appName});
        bodyMail = messageByLocaleService.getMessage("password_reset_body", new Object[]{url});

        Runnable task = () -> {
          log.info("send mail email = {} / title = {} / body = {}", userCreationView.getUsername(), title, bodyMail);
          services.emailService().sendResetPasswordMessage(userCreationView.getUsername(), title, user.name(), url, request.getLocale());
        };

        new Thread(task).start();
    }


    /*User user = services.user().create(userCreationView.toUser(), userCreationView.getPassword(), userCreationView.getRole());
    services.user().createUserFavorite(user.id, messageByLocaleService.getMessage("default_favorite"));*/
    response.setStatus(HttpServletResponse.SC_OK);
    return userResponseApi;
  }

  private String getAppUrl(HttpServletRequest request) {
    return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_CLOSE, method = RequestMethod.POST)
  public void close( Principal principal) {
    services.user().changeLastDeconnectionDate(principal.getName());
    return;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi. WS_SEC_USER_ME, method = RequestMethod.GET)
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
  @RequestMapping(value = RestApi. WS_SEC_USER, method = RequestMethod.GET)
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
  public UserResponseApi updateProfil(@RequestPart("fileVideoName") Optional<MultipartFile> fileVideoName, @RequestPart("fileJobVideoDescription") Optional<MultipartFile> fileJobVideoDescription, @RequestPart("data") Optional<UserCreationViewApi> userCreationViewApi, @RequestPart("leaveBeforePublished") Optional<Boolean> leaveBeforePublished, HttpServletResponse response, Principal principal) throws
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
       /* if ((!userCreationViewApi.get().getEmail().isEmpty()) && (userCreationViewApi.get().getEmail() != user.email)) {
          services.user().changeEmail(user, userCreationViewApi.get().getEmail());
        }*/
        String title, body;
        User admin = services.user().getAdmin();

        User userSearch = services.user().withUserName(userCreationViewApi.get().getEmail());
        if (userSearch == null) {
          body = messageByLocaleService.getMessage("ask_to_change_email_text", new Object[]{user.id, user.username, userCreationViewApi.get().getEmail()});
          title = messageByLocaleService.getMessage("ask_to_change_email_title", new Object[]{appName});
          User finalUser = user;
          Runnable task = () -> {
            log.info("send mail email = {} / title = {} / body = {}", admin.email, title, body);
            String values = finalUser.name() + ";" + finalUser.username + ";" + userCreationViewApi.get().getEmail();
            services.emailService().sendSimpleMessage(admin.email, title , body, "RequestChangeEmailMessage", values);
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
      return handleSelectedVideoFileUploadForProfil(fileVideoName.get(), leaveBeforePublished, principal, "Name", response);
    }

    if (fileJobVideoDescription.isPresent()) {
      return handleSelectedVideoFileUploadForProfil(fileJobVideoDescription.get(), leaveBeforePublished, principal, "JobDescription", response);
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

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_USER_ME_DELETE_VIDEO_NAME, method = RequestMethod.PUT)
  public UserResponseApi deleteNameVideo(@PathVariable long userId, HttpServletResponse response, Principal principal) throws
    InterruptedException {
    UserResponseApi userResponseApi = new UserResponseApi();

    User user = services.user().withId(userId);
    User userConnected = services.user().withUserName(principal.getName());

    if (user.id == userConnected.id) {
      services.user().changeNameVideoUrl(user, null, null);
    } else {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      userResponseApi.errorMessage = messageByLocaleService.getMessage("forbidden_action");
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
      services.user().changeDescriptionVideoUrl(user, null, null);
    } else {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      userResponseApi.errorMessage = messageByLocaleService.getMessage("forbidden_action");
    }

    response.setStatus(HttpServletResponse.SC_OK);
    return userResponseApi;
  }

  private void GenerateThumbnail(String thumbnailFile, String fileOutput) {
    String cmdGenerateThumbnail;

    cmdGenerateThumbnail = String.format("input=\"%s\"&&dur=$(ffprobe -loglevel error -show_entries format=duration -of default=nk=1:nw=1 \"$input\")&&ffmpeg -y -ss \"$(echo \"$dur / 2\" | bc -l)\" -i  \"$input\" -vframes 1 -s 360x360 -vf crop=360:360,scale=-1:360 \"%s\"", fileOutput, thumbnailFile);
    String cmdGenerateThumbnailFilterLog = "/tmp/ffmpeg.log";
    NativeInterface.launch(cmdGenerateThumbnail, null, cmdGenerateThumbnailFilterLog);
  }

  private UserResponseApi handleSelectedVideoFileUploadForProfil(@RequestParam("file") MultipartFile file, Optional<Boolean> leaveBeforePublished, Principal principal, String inputType, HttpServletResponse response) throws InterruptedException {
    {
      String videoUrl = null;
      String fileName = environment.getProperty("app.file") + "/" + file.getOriginalFilename();
      String thumbnailFile = environment.getProperty("app.file") + "thumbnail/" + file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf('.')) + ".png";
      File inputFile;
      UserResponseApi userResponseApi = new UserResponseApi();
      Boolean leaveDailymotionAfterLoadVideo = false;
      if (leaveBeforePublished.isPresent()) {
        leaveDailymotionAfterLoadVideo = leaveBeforePublished.get();
      }

      try {
        storageService.store(file);
        inputFile = storageService.load(file.getOriginalFilename()).toFile();
      } catch (Exception errorLoadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        userResponseApi.errorMessage = messageByLocaleService.getMessage("errorUploadFile");
        return userResponseApi;
      }

      if (leaveDailymotionAfterLoadVideo) {
        try {
          GenerateThumbnail(thumbnailFile, inputFile.getAbsolutePath());
        } catch (Exception errorEncondingFile) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          userResponseApi.errorMessage = messageByLocaleService.getMessage("errorThumbnailFile");
          return userResponseApi;
        }
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
        if (leaveDailymotionAfterLoadVideo) {
          pictureUri = thumbnailFile;
          videoUrl= fileName;
        } else {
          int i = 0;
          do {
            videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
            Thread.sleep(2 * 1000);
            if (i > 30) {
              break;
            }
            i++;
            log.info("status " + videoDailyMotion.status);
          }
          while (!videoDailyMotion.status.equals("published"));

          if (!videoDailyMotion.thumbnail_360_url.isEmpty()) {
            if (videoDailyMotion.thumbnail_360_url.contains("no-such-asset")) {
              pictureUri = "/img/no-such-asset.jpg";
            } else {
              pictureUri = videoDailyMotion.thumbnail_360_url;
            }
            log.warn("handleSelectedVideoFileUploadForProfil : thumbnail_360_url = {}", videoDailyMotion.thumbnail_360_url);
          }
          if (!videoDailyMotion.embed_url.isEmpty()) {
            videoUrl = videoDailyMotion.embed_url;
            log.warn("handleSelectedVideoFileUpload : embed_url = {}", videoDailyMotion.embed_url);
          }
        }

        if (inputType.equals("JobDescription")) {
          if (user.jobDescriptionVideo != null) {
            if (user.jobDescriptionVideo.contains("http")) {
              dailymotionId = user.jobDescriptionVideo.substring(user.jobDescriptionVideo.lastIndexOf('/') + 1);
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
              dailymotionId = user.nameVideo.substring(user.nameVideo.lastIndexOf('/') + 1);
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

        if (leaveDailymotionAfterLoadVideo) {
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

    User user = services.user().withId(userId);

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
  public UserResponseApi sendMail(@RequestBody UserCreationView userCreationView, HttpServletResponse response) {
    String title, body;
    UserResponseApi userResponseApi = new UserResponseApi();
    User admin = services.user().getAdmin();

    User user = services.user().withUserName(userCreationView.getEmail());
    if (user == null) {
      body = messageByLocaleService.getMessage("ask_to_create_user_text", new Object[]{userCreationView.getFirstName(), userCreationView.getLastName(), userCreationView.getEmail()});
      title = messageByLocaleService.getMessage("ask_to_create_user_title", new Object[]{appName});
      Runnable task = () -> {
        log.info("send mail email = {} / title = {} / body = {}", admin.email, title, body);
        String values = userCreationView.getFirstName() + ";" + userCreationView.getLastName() + ";" + userCreationView.getEmail();
        services.emailService().sendSimpleMessage(admin.email, title, body, "RequestCreateUserMessage", values);
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
  public UserResponseApi sendMailForChangeEmail(@RequestBody UserCreationView userCreationView, HttpServletResponse response, Principal principal) {
    String title, body;
    UserResponseApi userResponseApi = new UserResponseApi();
    User admin = services.user().getAdmin();
    User user = services.user().withUserName(principal.getName());

    User userSearch = services.user().withUserName(userCreationView.getEmail());
    if (userSearch == null) {
      body = messageByLocaleService.getMessage("ask_to_change_email_text", new Object[]{user.id, user.username, userCreationView.getEmail()});
      title = messageByLocaleService.getMessage("ask_to_change_email_title", new Object[]{appName});
      Runnable task = () -> {
        log.info("send mail email = {} / title = {} / body = {}", admin.email, title, body);
        String values = user.name() + ";" + user.username + ";" + userCreationView.getEmail();
        services.emailService().sendSimpleMessage(admin.email, title , body, "RequestChangeEmailMessage", values);
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