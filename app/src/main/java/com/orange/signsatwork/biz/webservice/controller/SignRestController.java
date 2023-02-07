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
import com.orange.signsatwork.biz.persistence.model.*;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.persistence.service.SignService;
import com.orange.signsatwork.biz.persistence.service.UserService;
import com.orange.signsatwork.biz.storage.StorageService;
import com.orange.signsatwork.biz.view.controller.CommentOrderComparator;
import com.orange.signsatwork.biz.view.model.*;
import com.orange.signsatwork.biz.webservice.model.*;
import com.orange.signsatwork.biz.webservice.model.SignView;
import com.orange.signsatwork.biz.webservice.model.VideoView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SignRestController {

  @Autowired
  Services services;
  @Autowired
  DalymotionToken dalymotionToken;
  @Autowired
  private SpringRestClient springRestClient;
  @Autowired
  MessageByLocaleService messageByLocaleService;
  @Autowired
  private StorageService storageService;
  @Autowired
  private org.springframework.core.env.Environment environment;

  String VIDEO_THUMBNAIL_FIELDS = "thumbnail_url,thumbnail_60_url,thumbnail_120_url,thumbnail_180_url,thumbnail_240_url,thumbnail_360_url,thumbnail_480_url,thumbnail_720_url,";
  String VIDEO_EMBED_FIELD = "embed_url";
  String VIDEO_STATUS = ",status";

  @RequestMapping(value = RestApi.WS_OPEN_SIGN + "/{id}")
  public SignView sign(@PathVariable long id) {
    Sign sign = services.sign().withId(id);
    return new SignView(sign);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_VIDEO_DELETE, method = RequestMethod.POST)
  public String deleteVideo(@PathVariable long signId, @PathVariable long videoId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    String dailymotionId;
    String title, bodyMail, messageType;
    User user = services.user().withUserName(principal.getName());
    User admin = services.user().getAdmin();
    Sign sign = services.sign().withId(signId);
    Video video = services.video().withId(videoId);
    if (sign.videos.list().size() == 1) {
      Request request = services.sign().requestForSign(sign);
      if (request != null) {
        if (request.requestVideoDescription != null && sign.videoDefinition != null) {
          if (!request.requestVideoDescription.equals(sign.videoDefinition)) {
            String dailymotionIdForSignDefinition;
            if (sign.videoDefinition != null) {
              if (sign.videoDefinition.contains("http")) {
                dailymotionIdForSignDefinition = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
                try {
                  DeleteVideoOnDailyMotion(dailymotionIdForSignDefinition);
                } catch (Exception errorDailymotionDeleteVideo) {
                  response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                  return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
                }
              } else {
                DeleteFilesOnServer(sign.videoDefinition, null);
              }
            }
          }
        } else {
          String dailymotionIdForSignDefinition;
          if (sign.videoDefinition != null) {
            if (sign.videoDefinition.contains("http")) {
              dailymotionIdForSignDefinition = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
              try {
                DeleteVideoOnDailyMotion(dailymotionIdForSignDefinition);
              } catch (Exception errorDailymotionDeleteVideo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
              }
            } else {
              DeleteFilesOnServer(sign.videoDefinition, null);
            }
          }
        }
      } else {
        String dailymotionIdForSignDefinition;
        if (sign.videoDefinition != null) {
          if (sign.videoDefinition.contains("http")) {
            dailymotionIdForSignDefinition = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
            try {
              DeleteVideoOnDailyMotion(dailymotionIdForSignDefinition);
            } catch (Exception errorDailymotionDeleteVideo) {
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
            }
          } else {
            DeleteFilesOnServer(sign.videoDefinition, null);
          }
        }
      }
      String thumbnail = services.video().withId(sign.lastVideoId).pictureUri;
      services.sign().delete(sign);
      if (user.username == admin.username) {
        title = messageByLocaleService.getMessage("delete_sign_title", new Object[]{sign.name});
        bodyMail = messageByLocaleService.getMessage("delete_sign_body", new Object[]{sign.name});
        messageType = "DeleteSignSendEmailMessage";
        List<String> emails = new ArrayList<String>();
        emails.add(video.user.username);
        if (emails.size() != 0) {
          String finalMessageType = messageType;
          Runnable task = () -> {
            log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
            services.emailService().sendVideoMessage(emails.toArray(new String[emails.size()]), title, bodyMail, sign.name, finalMessageType, requestHttp.getLocale());
          };
          new Thread(task).start();
        } else {
          messageType = "DeleteSignMessage";
          String values = admin.username + ';' + sign.name;
          MessageServer messageServer = new MessageServer(new Date(), messageType, values, ActionType.NO);
          services.messageServerService().addMessageServer(messageServer);
        }
      }
      if (sign.url.contains("http")) {
        dailymotionId = sign.url.substring(sign.url.lastIndexOf('/') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionId);
        } catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
        }
      } else {
        DeleteFilesOnServer(sign.url, thumbnail);
      }
      response.setStatus(HttpServletResponse.SC_OK);
      return "/signs/mostrecent?isMostRecent=false&isSearch=false";

    } else {
      services.video().delete(video);
      if (user.username == admin.username) {
        String videoName = sign.name + "_" + video.idForName;
        title = messageByLocaleService.getMessage("delete_video_title", new Object[]{videoName});
        bodyMail = messageByLocaleService.getMessage("delete_video_body", new Object[]{videoName});
        messageType = "DeleteVideoSendEmailMessage";
        Videos videos = services.video().forSign(signId);
        List<String> emails = videos.stream().filter(v -> v.user.email != null).map(v -> v.user.email).collect(Collectors.toList());
        emails = emails.stream().distinct().collect(Collectors.toList());
        if (emails.size() != 0) {
          List<String> finalEmails = emails;
          String finalMessageType1 = messageType;
          Runnable task = () -> {
            log.info("send mail email = {} / title = {} / body = {}", finalEmails.toString(), title, bodyMail);
            services.emailService().sendVideoMessage(finalEmails.toArray(new String[finalEmails.size()]), title, bodyMail, videoName, finalMessageType1, requestHttp.getLocale());
          };
          new Thread(task).start();
        } else {
          messageType = "DeleteVideoMessage";
          String values = admin.username + ';' + videoName;
          MessageServer messageServer = new MessageServer(new Date(), messageType, values, ActionType.NO);
          services.messageServerService().addMessageServer(messageServer);
        }
      }
      if (video.url.contains("http")) {
        dailymotionId = video.url.substring(video.url.lastIndexOf('/') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionId);
        } catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
        }
      } else {
        DeleteFilesOnServer(video.url, video.pictureUri);
      }
      response.setStatus(HttpServletResponse.SC_OK);
      return "/sign/" + signId;
    }
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_SEC_DELETE_VIDEO_FILE_FOR_SIGN_DEFINITION, method = RequestMethod.POST)
  public String deleteVideoSignDefinition(@PathVariable long signId, HttpServletResponse response, HttpServletRequest requestHttp) {
    Sign sign = services.sign().withId(signId);
    if (sign.videoDefinition != null) {
      if (sign.videoDefinition.contains("http")) {
        String dailymotionIdForSignDefinition = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionIdForSignDefinition);
        } catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
        }
      } else {
        DeleteFilesOnServer(sign.videoDefinition, null);
      }
    }
    services.sign().deleteSignVideoDefinition(signId);
    String title = messageByLocaleService.getMessage("delete_sign_definition_title", new Object[]{sign.name});
    String bodyMail = messageByLocaleService.getMessage("delete_sign_definition_body", new Object[]{sign.name});
    String messageType = "DeleteSignDefinitionMessage";
    Videos videos = services.video().forSign(signId);
    List<String> emails = videos.stream().filter(v-> v.user.email != null).map(v -> v.user.email).collect(Collectors.toList());
    emails = emails.stream().distinct().collect(Collectors.toList());
    if (emails.size() != 0) {
      messageType = "DeleteSignDefinitionSendEmailMessage";
      final String finalTitle = title;
      final String finalBodyMail = bodyMail;
      final String finalMessageType = messageType;
      final String finalSignName = sign.name;
      List<String> finalEmails = emails;
      Runnable task = () -> {
        log.info("send mail email = {} / title = {} / body = {}", finalEmails.toString(), finalTitle, finalBodyMail);
        services.emailService().sendSignDefinitionMessage(finalEmails.toArray(new String[finalEmails.size()]), finalTitle, finalBodyMail, finalSignName, finalMessageType, requestHttp.getLocale());
      };
      new Thread(task).start();
    } else {
      User admin = services.user().getAdmin();
      String values = admin.username + ';' + sign.name;
      MessageServer messageServer = new MessageServer(new Date(), messageType, values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);
    }

    response.setStatus(HttpServletResponse.SC_OK);
    return "";
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
    if (url!= null) {
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

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_VIDEO_ASSOCIATE, method = RequestMethod.POST)
  public String associateVideo(@RequestBody List<Long> associateVideosIds, @PathVariable long signId, @PathVariable long videoId) {
    services.video().changeVideoAssociates(videoId, associateVideosIds);

    log.info("Change video (id={}) associates, ids={}", videoId, associateVideosIds);
    return "/sign/" + signId + "/" + videoId;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SIGN_CREATE, method = RequestMethod.POST)
  public SignId createSign(@RequestBody SignCreationView signCreationView, Principal principal) {
    User user = services.user().withUserName(principal.getName());
    signCreationView.clearXss();
    Sign sign = services.sign().create(user.id, signCreationView.getSignName(), signCreationView.getVideoUrl(), "");

    log.info("createSign: username = {} / sign name = {} / video url = {}", user.username, signCreationView.getSignName(), signCreationView.getVideoUrl());

    return new SignId(sign.id);
  }

  /** API REST For Android and IOS **/

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SIGNS_VIDEOS)
  public ResponseEntity<?> videosForSign(@PathVariable long signId, Principal principal) {
    final User user = AuthentModel.isAuthenticated(principal) ? services.user().withUserName(principal.getName()) : null;
    List<Object[]> querySigns = services.sign().AllVideosForSign(signId);
    List<VideoViewData> videoViewsData = querySigns.stream()
      .map(objectArray -> new VideoViewData(objectArray))
      .collect(Collectors.toList());
    List<VideoView2> videoViews;
    List<Long> videoInFavorite = new ArrayList<>();
    if (user != null) {
      videoInFavorite = Arrays.asList(services.video().VideosForAllFavoriteByUser(user.id));
      List<Long> finalVideoInFavorite = videoInFavorite;
      videoViews = videoViewsData.stream()
        .map(videoViewData -> buildVideoView(videoViewData, finalVideoInFavorite, user))
        .collect(Collectors.toList());
    } else {
      List<Long> finalVideoInFavorite1 = videoInFavorite;
      videoViews = videoViewsData.stream()
        .map(videoViewData -> buildVideoView(videoViewData, finalVideoInFavorite1, user))
        .collect(Collectors.toList());
    }


    return new ResponseEntity<>(videoViews, HttpStatus.OK);

  }

  @RequestMapping(value = RestApi.WS_ROOT_SIGNS_VIDEOS)
  public ResponseEntity<?> videosForSignWithoutUser(@PathVariable long signId, Principal principal) {
    final User user = AuthentModel.isAuthenticated(principal) ? services.user().withUserName(principal.getName()) : null;
    List<Object[]> querySigns = services.sign().AllVideosForSign(signId);
    List<VideoViewData> videoViewsData = querySigns.stream()
      .map(objectArray -> new VideoViewData(objectArray))
      .collect(Collectors.toList());
    List<VideoView2> videoViews;
    List<Long> videoInFavorite = new ArrayList<>();
    if (user != null) {
      videoInFavorite = Arrays.asList(services.video().VideosForAllFavoriteByUser(user.id));
      List<Long> finalVideoInFavorite = videoInFavorite;
      videoViews = videoViewsData.stream()
        .map(videoViewData -> buildVideoView(videoViewData, finalVideoInFavorite, user))
        .collect(Collectors.toList());
    } else {
      List<Long> finalVideoInFavorite1 = videoInFavorite;
      videoViews = videoViewsData.stream()
        .map(videoViewData -> buildVideoView(videoViewData, finalVideoInFavorite1, user))
        .collect(Collectors.toList());
    }


    return new ResponseEntity<>(videoViews, HttpStatus.OK);

  }


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_VIDEOS)
  public ResponseEntity<?> videos() {
    List<Object[]> querySigns = services.sign().AllVideosForAllSigns();
    List<VideoViewData> videoViewsData = querySigns.stream()
      .map(objectArray -> new VideoViewData(objectArray))
      .collect(Collectors.toList());

    return new ResponseEntity<>(videoViewsData, HttpStatus.OK);

  }


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_VIDEO)
  public ResponseEntity<?>  video(@PathVariable long videoId, Principal principal) {
    Boolean isVideoCreatedByMe = false;
    Boolean isVideoBelowToFavorite = false;
    Boolean isVideoHasAveragePositiveRate = false;

    final User user = AuthentModel.isAuthenticated(principal) ? services.user().withUserName(principal.getName()) : null;



    Video video = services.video().withId(videoId);
    List<Object[]> querySigns = services.video().SignForVideo(videoId);
    SignData signData = new SignData(querySigns.get(0));

    String signTextDefinition = signData.textDefinition;
    String signVideoDefinition = signData.videoDefinition;

    String videoName;
    if ((video.idForName == 0) || (signData.nbVideo == 1 )){
      videoName = signData.name;
    } else {
      videoName = signData.name + "_" + video.idForName;
    }

    Object[] queryRating = services.video().RatingForVideoByUser(videoId, user.id);
    RatingData ratingData = new RatingData(queryRating);

    List<Object[]> queryAllComments = services.video().AllCommentsForVideo(videoId);
    List<CommentData> commentDatas = queryAllComments.stream()
      .map(objectArray -> new CommentData(objectArray))
      .collect(Collectors.toList());
    if (video.user.id == user.id) {
      isVideoCreatedByMe = true;
    }
    Long nbFavorite = services.video().NbFavoriteBelowVideoForUser(videoId, user.id);
    if (nbFavorite >=1) {
      isVideoBelowToFavorite = true;
    }

    if (video.averageRate > 0) {
      isVideoHasAveragePositiveRate = true;
    }

    Long nbRating = services.sign().NbRatingForSign(signData.id);

    List<Object[]> queryAllVideosHistory = services.sign().AllVideosHistoryForSign(signData.id);
    List<VideoHistoryData> videoHistoryDatas = queryAllVideosHistory.stream()
      .map(objectArray -> new VideoHistoryData(objectArray))
      .collect(Collectors.toList());

    return new ResponseEntity<>(new VideoView(signData.id, signTextDefinition, signVideoDefinition, videoId, videoName, video.url, isVideoCreatedByMe, isVideoHasAveragePositiveRate, isVideoBelowToFavorite, ratingData, commentDatas, nbRating, videoHistoryDatas), HttpStatus.OK);
  }

  private VideoView2 buildVideoView(VideoViewData videoViewData, List<Long> videoBelowToFavorite, User user) {
    return new VideoView2(
      videoViewData,
      videoViewData.nbComment > 0,
      VideoView2.createdAfterLastDeconnection(videoViewData.createDate, user == null ? null : user.lastDeconnectionDate),
      videoViewData.nbView > 0,
      videoViewData.averageRate > 0,
      videoBelowToFavorite.contains(videoViewData.videoId));
  }

  @CrossOrigin
  @RequestMapping(value = RestApi.WS_ROOT_SIGNS)
  public ResponseEntity<?> signsWithoutUser(@RequestParam("sort") Optional<String> sort, @RequestParam("name") Optional<String> name,  Principal principal) {
    final User user = AuthentModel.isAuthenticated(principal) ? services.user().withUserName(principal.getName()) : null;
    List<Object[]> querySigns;
    List<Long> signWithRatingList = new ArrayList<>();
    String messageError;

    if (sort.isPresent()) {
      if (sort.get().equals("date")) {
        querySigns = services.sign().mostRecentWithoutDate();
      } else if (sort.get().equals("-date")) {
        querySigns = services.sign().lowRecentWithoutDate();
      } else if (sort.get().equals("name")) {
        querySigns = services.sign().SignsAlphabeticalOrderAscSignsView();
      } else if (sort.get().equals("-name")) {
        querySigns = services.sign().SignsAlphabeticalOrderDescSignsView();
      } else if (sort.get().equals("averageRating")) {
        querySigns = services.sign().SignsForSignsView();
        signWithRatingList = Arrays.asList(services.sign().mostRating());
      } else if (sort.get().equals("-averageRating")) {
        querySigns = services.sign().SignsForSignsView();
        signWithRatingList = Arrays.asList(services.sign().lowRating());
      } else {
        messageError = messageByLocaleService.getMessage("filter_not_exits", new Object[]{sort.get()});
        return new ResponseEntity<>(messageError, HttpStatus.BAD_REQUEST);
      }
    } else {
      querySigns = services.sign().SignsForSignsView();
    }

    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());


    List<Long> signWithCommentList = Arrays.asList(services.sign().mostCommented());

    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<Long> signWithPositiveRate = Arrays.asList(services.sign().mostRating());

    List<SignView2> signViews = new ArrayList<>();
    List<Long> signInFavorite = new ArrayList<>();

    if (user != null) {
      signInFavorite = Arrays.asList(services.sign().SignsBellowToFavoriteByUser(user.id));
    }


    if (sort.isPresent()) {
      if (sort.get().equals("averageRating") || sort.get().equals("-averageRating")) {
        List<Long> finalSignWithRatingList = signWithRatingList;
        List<SignViewData> rating = signViewsData.stream()
          .filter(signViewData -> finalSignWithRatingList.contains(signViewData.id))
          .sorted(new CommentOrderComparator(signWithRatingList))
          .collect(Collectors.toList());
        if (user != null) {
          List<Long> finalSignInFavorite1 = signInFavorite;
          signViews = rating.stream()
            .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, finalSignInFavorite1, user))
            .collect(Collectors.toList());
        } else {
          signViews = rating.stream()
            .map(signViewData -> buildSignViewWithOutFavorite(signViewData, signWithCommentList, signWithView, signWithPositiveRate))
            .collect(Collectors.toList());
        }

      } else if (sort.get().equals("date") || sort.get().equals("-date") || sort.get().equals("name") || sort.get().equals("-name")) {
        if (user != null) {
          List<Long> finalSignInFavorite = signInFavorite;
          signViews = signViewsData.stream()
            .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, finalSignInFavorite, user))
            .collect(Collectors.toList());
        } else {
          signViews = signViewsData.stream()
            .map(signViewData -> buildSignViewWithOutFavorite(signViewData, signWithCommentList, signWithView, signWithPositiveRate))
            .collect(Collectors.toList());
        }
      }
    } else {
      if (name.isPresent()) {
        List<Object[]> querySignsByName = services.sign().searchBis(name.get().trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE").toUpperCase());
        List<SignViewData> signViewsDataByName = querySignsByName.stream()
          .map(objectArray -> new SignViewData(objectArray))
          .collect(Collectors.toList());
        if (user != null) {
          List<Long> finalSignInFavorite3 = signInFavorite;
          signViews = signViewsDataByName.stream()
            .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, finalSignInFavorite3, user))
            .collect(Collectors.toList());
        } else {
          signViews = signViewsDataByName.stream()
            .map(signViewData -> buildSignViewWithOutFavorite(signViewData, signWithCommentList, signWithView, signWithPositiveRate))
            .collect(Collectors.toList());
        }

      } else {
        if (user != null) {
          List<Long> finalSignInFavorite2 = signInFavorite;
          signViews = signViewsData.stream()
            .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, finalSignInFavorite2, user))
            .collect(Collectors.toList());
        } else {
          signViews = signViewsData.stream()
            .map(signViewData -> buildSignViewWithOutFavorite(signViewData, signWithCommentList, signWithView, signWithPositiveRate))
            .collect(Collectors.toList());
        }

      }
    }



    if (!sort.isPresent()) {
      SignsViewSort2 signsViewSort2 = new SignsViewSort2();
      signViews = signsViewSort2.sort(signViews, false);
    }

    return  new ResponseEntity<>(signViews, HttpStatus.OK);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SIGNS)
  public ResponseEntity<?> signs(@RequestParam("sort") Optional<String> sort, @RequestParam("name") Optional<String> name, @RequestParam("fullname") Optional<String> fullname,  Principal principal) {
    final User user = AuthentModel.isAuthenticated(principal) ? services.user().withUserName(principal.getName()) : null;
    List<Object[]> querySigns;
    List<Long> signWithRatingList = new ArrayList<>();
    String messageError;

    if (sort.isPresent()) {
      if (sort.get().equals("date")) {
        querySigns = services.sign().mostRecentWithoutDate();
      } else if (sort.get().equals("-date")) {
        querySigns = services.sign().lowRecentWithoutDate();
      } else if (sort.get().equals("name")) {
        querySigns = services.sign().SignsAlphabeticalOrderAscSignsView();
      } else if (sort.get().equals("-name")) {
        querySigns = services.sign().SignsAlphabeticalOrderDescSignsView();
      } else if (sort.get().equals("averageRating")) {
        querySigns = services.sign().SignsForSignsView();
        signWithRatingList = Arrays.asList(services.sign().mostRating());
      } else if (sort.get().equals("-averageRating")) {
        querySigns = services.sign().SignsForSignsView();
        signWithRatingList = Arrays.asList(services.sign().lowRating());
      } else {
        messageError = messageByLocaleService.getMessage("filter_not_exits", new Object[]{sort.get()});
        return new ResponseEntity<>(messageError, HttpStatus.BAD_REQUEST);
      }
    } else {
      querySigns = services.sign().SignsForSignsView();
    }

    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());


    List<Long> signWithCommentList = Arrays.asList(services.sign().mostCommented());

    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<Long> signWithPositiveRate = Arrays.asList(services.sign().mostRating());

    List<SignView2> signViews = new ArrayList<>();
    List<Long> signInFavorite = new ArrayList<>();

    if (user != null) {
      signInFavorite = Arrays.asList(services.sign().SignsBellowToFavoriteByUser(user.id));
    }


    if (sort.isPresent()) {
      if (sort.get().equals("averageRating") || sort.get().equals("-averageRating")) {
        List<Long> finalSignWithRatingList = signWithRatingList;
        List<SignViewData> rating = signViewsData.stream()
          .filter(signViewData -> finalSignWithRatingList.contains(signViewData.id))
          .sorted(new CommentOrderComparator(signWithRatingList))
          .collect(Collectors.toList());
        if (user != null) {
          List<Long> finalSignInFavorite1 = signInFavorite;
          signViews = rating.stream()
            .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, finalSignInFavorite1, user))
            .collect(Collectors.toList());
        } else {
          signViews = rating.stream()
            .map(signViewData -> buildSignViewWithOutFavorite(signViewData, signWithCommentList, signWithView, signWithPositiveRate))
            .collect(Collectors.toList());
        }

      } else if (sort.get().equals("date") || sort.get().equals("-date") || sort.get().equals("name") || sort.get().equals("-name")) {
        if (user != null) {
          List<Long> finalSignInFavorite = signInFavorite;
          signViews = signViewsData.stream()
            .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, finalSignInFavorite, user))
            .collect(Collectors.toList());
        } else {
          signViews = signViewsData.stream()
            .map(signViewData -> buildSignViewWithOutFavorite(signViewData, signWithCommentList, signWithView, signWithPositiveRate))
            .collect(Collectors.toList());
        }
      }
    } else {
      if (name.isPresent()) {
        List<Object[]> querySignsByName = services.sign().searchBis(name.get().trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE").toUpperCase());
        List<SignViewData> signViewsDataByName = querySignsByName.stream()
          .map(objectArray -> new SignViewData(objectArray))
          .collect(Collectors.toList());
        if (user != null) {
          List<Long> finalSignInFavorite3 = signInFavorite;
          signViews = signViewsDataByName.stream()
            .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, finalSignInFavorite3, user))
            .collect(Collectors.toList());
        } else {
          signViews = signViewsDataByName.stream()
            .map(signViewData -> buildSignViewWithOutFavorite(signViewData, signWithCommentList, signWithView, signWithPositiveRate))
            .collect(Collectors.toList());
        }

      } else if (fullname.isPresent()) {
        List<Object[]> querySignsByName  = services.sign().searchFull(fullname.get());
        List<SignViewData> signViewsDataByName = querySignsByName.stream()
          .map(objectArray -> new SignViewData(objectArray))
          .collect(Collectors.toList());
        if (user != null) {
          List<Long> finalSignInFavorite3 = signInFavorite;
          signViews = signViewsDataByName.stream()
            .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, finalSignInFavorite3, user))
            .collect(Collectors.toList());
        } else {
          signViews = signViewsDataByName.stream()
            .map(signViewData -> buildSignViewWithOutFavorite(signViewData, signWithCommentList, signWithView, signWithPositiveRate))
            .collect(Collectors.toList());
        }
      } else {
        if (user != null) {
          List<Long> finalSignInFavorite2 = signInFavorite;
          signViews = signViewsData.stream()
            .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, finalSignInFavorite2, user))
            .collect(Collectors.toList());
        } else {
          signViews = signViewsData.stream()
            .map(signViewData -> buildSignViewWithOutFavorite(signViewData, signWithCommentList, signWithView, signWithPositiveRate))
            .collect(Collectors.toList());
        }

      }
    }



    if (!sort.isPresent()) {
      SignsViewSort2 signsViewSort2 = new SignsViewSort2();
      signViews = signsViewSort2.sort(signViews, false);
    }

    return  new ResponseEntity<>(signViews, HttpStatus.OK);
  }

  private SignView2 buildSignView(SignViewData signViewData, List<Long> signWithCommentList, List<Long> signWithView, List<Long> signWithPositiveRate, List<Long> signInFavorite, User user) {
    return new SignView2(
      signViewData,
      signWithCommentList.contains(signViewData.id),
      SignView2.createdAfterLastDeconnection(signViewData.createDate, user == null ? null : user.lastDeconnectionDate),
      signWithView.contains(signViewData.id),
      signWithPositiveRate.contains(signViewData.id),
      signInFavorite.contains(signViewData.id));
  }

  private SignView2 buildSignViewWithOutFavorite(SignViewData signViewData, List<Long> signWithCommentList, List<Long> signWithView, List<Long> signWithPositiveRate) {
    return new SignView2(
      signViewData,
      signWithCommentList.contains(signViewData.id),
      signWithView.contains(signViewData.id),
      signWithPositiveRate.contains(signViewData.id));
  }

  private SignView2 buildSignView(Sign sign, List<Long> signWithCommentList, List<Long> signWithView, List<Long> signWithPositiveRate, List<Long> signInFavorite, User user) {
    return new SignView2(
      sign,
      signWithCommentList.contains(sign.id),
      SignView2.createdAfterLastDeconnection(sign.createDate, user == null ? null : user.lastDeconnectionDate),
      signWithView.contains(sign.id),
      signWithPositiveRate.contains(sign.id),
      signInFavorite.contains(sign.id));
  }



  @Secured("ROLE_USER_A")
  @RequestMapping(value = RestApi.WS_SEC_VIDEO, method = RequestMethod.DELETE)
  public VideoResponseApi deleteApiVideo(@PathVariable long videoId, HttpServletResponse response, Principal principal) {
    VideoResponseApi videoResponseApi = new VideoResponseApi();
    String dailymotionId;

    if (!AuthentModel.hasRole("ROLE_USER_A")) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      videoResponseApi.errorMessage = messageByLocaleService.getMessage("forbidden_action");
      return videoResponseApi;
    }

    User user = services.user().withUserName(principal.getName());
    Videos videos = services.video().forUser(user.id);

    boolean isVideoBellowToMe = videos.stream().anyMatch(video -> video.id == videoId);
    if (!isVideoBellowToMe) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      videoResponseApi.errorMessage = messageByLocaleService.getMessage("video_not_below_to_you");
      return videoResponseApi;
    }

    Video video = services.video().withId(videoId);
    List<Object[]> querySigns = services.video().SignForVideo(videoId);
    SignData signData = new SignData(querySigns.get(0));

    Sign sign = services.sign().withId(signData.id);
    if (sign.videos.list().size() == 1) {
      Request request = services.sign().requestForSign(sign);
      if (request != null) {
        if (request.requestVideoDescription != null && sign.videoDefinition != null) {
          if (!request.requestVideoDescription.equals(sign.videoDefinition)) {
            String dailymotionIdForSignDefinition;
            if (sign.videoDefinition != null) {
              if (sign.videoDefinition.contains("http")) {
                dailymotionIdForSignDefinition = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
                try {
                  DeleteVideoOnDailyMotion(dailymotionIdForSignDefinition);
                } catch (Exception errorDailymotionDeleteVideo) {
                  response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                  videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
                  return videoResponseApi;
                }
              } else {
                DeleteFilesOnServer(sign.videoDefinition, null);
              }
            }
          }
        } else {
          String dailymotionIdForSignDefinition;
          if (sign.videoDefinition != null) {
            if (sign.videoDefinition.contains("http")) {
              dailymotionIdForSignDefinition = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
              try {
                DeleteVideoOnDailyMotion(dailymotionIdForSignDefinition);
              } catch (Exception errorDailymotionDeleteVideo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
                return videoResponseApi;
              }
            } else {
              DeleteFilesOnServer(sign.videoDefinition, null);
            }
          }
        }
      } else {
        String dailymotionIdForSignDefinition;
        if (sign.videoDefinition != null) {
          if (sign.videoDefinition.contains("http")) {
            dailymotionIdForSignDefinition = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
            try {
              DeleteVideoOnDailyMotion(dailymotionIdForSignDefinition);
            } catch (Exception errorDailymotionDeleteVideo) {
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
              return videoResponseApi;
            }
          } else {
            DeleteFilesOnServer(sign.videoDefinition, null);
          }
        }
      }
      String thumbnail = services.video().withId(sign.lastVideoId).pictureUri;
      services.sign().delete(sign);
      if (sign.url.contains("http")) {
        dailymotionId = sign.url.substring(sign.url.lastIndexOf('/') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionId);
        } catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
          return videoResponseApi;
        }
      } else {
        DeleteFilesOnServer(sign.url, thumbnail);
      }
      response.setStatus(HttpServletResponse.SC_OK);
      videoResponseApi.videoId = videoId;
      return videoResponseApi;

    } else {
      services.video().delete(video);
      if (video.url.contains("http")) {
        dailymotionId = video.url.substring(video.url.lastIndexOf('/') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionId);
        } catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
          return videoResponseApi;
        }
      } else {
        DeleteFilesOnServer(video.url, video.pictureUri);
      }
      response.setStatus(HttpServletResponse.SC_OK);
      videoResponseApi.videoId = videoId;
      return videoResponseApi;
    }
  }


    @Secured("ROLE_USER_A")
    @RequestMapping(value = RestApi.WS_SEC_SIGNS, method = RequestMethod.POST, headers = {"content-type=multipart/mixed", "content-type=multipart/form-data"})
    public VideoResponseApi createVideo(@RequestPart("file") Optional<MultipartFile> file,
    @RequestPart("data") SignCreationViewApi signCreationViewApi, HttpServletResponse response, Principal principal) throws
    InterruptedException {
      VideoResponseApi videoResponseApi = new VideoResponseApi();

      if (!AuthentModel.hasRole("ROLE_USER_A")) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        videoResponseApi.errorMessage = messageByLocaleService.getMessage("forbidden_action");
        return videoResponseApi;
      }

      signCreationViewApi.clearXss();
      if (!services.sign().withName(signCreationViewApi.getName()).list().isEmpty()) {
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        videoResponseApi.errorMessage = messageByLocaleService.getMessage("sign.already_exists");
        videoResponseApi.signId = services.sign().withName(signCreationViewApi.getName()).list().get(0).id;
        return videoResponseApi;
      }

      if (environment.getProperty("app.dailymotion_url").isEmpty()) {
        return handleSelectedVideoFileUploadOnServer(file.get(), OptionalLong.empty(), OptionalLong.empty(), OptionalLong.empty(), signCreationViewApi, principal, response);
      } else {
        return handleSelectedVideoFileUpload(file.get(), OptionalLong.empty(), OptionalLong.empty(), OptionalLong.empty(), signCreationViewApi, principal, response);
      }
    }

  @Secured("ROLE_USER_A")
  @RequestMapping(value = RestApi.WS_SEC_SIGN_VIDEO, method = RequestMethod.PUT, headers = {"content-type=multipart/mixed", "content-type=multipart/form-data"})
  public VideoResponseApi updateVideo(@PathVariable Long signId, @PathVariable Long videoId, @RequestPart("file") Optional<MultipartFile> file, @RequestPart("data") SignCreationViewApi signCreationViewApi, HttpServletResponse response, Principal principal) throws
    InterruptedException {
    VideoResponseApi videoResponseApi = new VideoResponseApi();

    if (!AuthentModel.hasRole("ROLE_USER_A")) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      videoResponseApi.errorMessage = messageByLocaleService.getMessage("forbidden_action");
      return videoResponseApi;
    }

    User user = services.user().withUserName(principal.getName());
    Videos videos = services.video().forUser(user.id);

    boolean isVideoBellowToMe = videos.stream().anyMatch(video -> video.id == videoId);
    if (!isVideoBellowToMe) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      videoResponseApi.errorMessage = messageByLocaleService.getMessage("video_not_below_to_you");
      return videoResponseApi;
    }
    signCreationViewApi.clearXss();
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleSelectedVideoFileUploadOnServer(file.get(), OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.of(videoId), signCreationViewApi, principal, response);
    } else {
      return handleSelectedVideoFileUpload(file.get(), OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.of(videoId), signCreationViewApi, principal, response);
    }
  }

  @Secured("ROLE_USER_A")
  @RequestMapping(value = RestApi.WS_SEC_SIGN_VIDEO_RENAME, method = RequestMethod.PUT)
  public SignResponseApi renameSign(@RequestBody SignCreationViewApi signCreationViewApi, @PathVariable Long signId, @PathVariable Long videoId, @RequestParam("force") Boolean force, HttpServletRequest requestHttp,  HttpServletResponse response, Principal principal) throws
    InterruptedException {
    Boolean isAdmin = false;
    Boolean isVideoBellowToMe = false;
    SignResponseApi signResponseApi = new SignResponseApi();

    if (!AuthentModel.hasRole("ROLE_USER_A") && !AuthentModel.hasRole("ROLE_ADMIN")) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      signResponseApi.errorMessage = messageByLocaleService.getMessage("forbidden_action");
      return signResponseApi;
    }

    User user = services.user().withUserName(principal.getName());
    User admin = services.user().getAdmin();
    Video video = services.video().withId(videoId);
    /*Videos videos = services.video().forUser(user.id);*/
    if (video.user.id == user.id) {
      isVideoBellowToMe = true;
    }

    signCreationViewApi.clearXss();
    if (user.username != admin.username) {
      if (!isVideoBellowToMe) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        signResponseApi.errorMessage = messageByLocaleService.getMessage("video_not_below_to_you");
        return signResponseApi;
      }
    } else {
      isAdmin = true;
    }

    Sign sign = services.sign().withId(signId);
    if (!sign.name.equals(signCreationViewApi.getName())) {
      Signs signsWithSameNameIgnoreCase = services.sign().withNameIgnoreCase(signCreationViewApi.getName().trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE"));
      List<Sign> signsWithNameIgnoreCase = signsWithSameNameIgnoreCase.stream().filter(s -> s.id != sign.id).collect(Collectors.toList());
      if (signsWithNameIgnoreCase.isEmpty()) {
        Requests requestsMatches = services.request().withName(signCreationViewApi.getName());
        if (!requestsMatches.list().isEmpty()) {
          Request request = services.sign().requestForSign(sign);
          if (request != null) {
            signResponseApi.errorMessage = messageByLocaleService.getMessage("request.name_already_exist_and_sign_are_associated_to_a_request");
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return signResponseApi;
          } else {
            renameSignAndAssociateToRequest(signId, requestsMatches.list().get(0).id, videoId, signCreationViewApi.getName(), sign.name, isAdmin, requestHttp);
            response.setStatus(HttpServletResponse.SC_OK);
            return signResponseApi;
          }
        } else {
          List<Object[]> querySigns = services.sign().searchBis(signCreationViewApi.getName().trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE").toUpperCase());
          List<SignViewData> signViewData = querySigns.stream()
            .map(objectArray -> new SignViewData(objectArray))
            .filter(o -> o.id != signId)
            .collect(Collectors.toList());
          List<SignViewData> signsWithSameName = new ArrayList<>();
          for (SignViewData s : signViewData) {
            if (!s.name.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE").equalsIgnoreCase(signCreationViewApi.getName().trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE"))) {
              signsWithSameName.add(s);
            }
          }
          List<Object[]> queryRequestsWithNoAssociateSign = services.request().requestsByNameWithNoAssociateSign(signCreationViewApi.getName().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE").toUpperCase(), user.id);
          List<RequestViewData> requestViewDatasWithNoAssociateSign = queryRequestsWithNoAssociateSign.stream()
            .map(objectArray -> new RequestViewData(objectArray))
            .collect(Collectors.toList());
          List<RequestViewData> requestsWithNoAssociateSignWithSameName = new ArrayList<>();
          for (RequestViewData requestViewData : requestViewDatasWithNoAssociateSign) {
            if (!requestViewData.requestName.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE").equalsIgnoreCase(signCreationViewApi.getName().trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE"))) {
              requestsWithNoAssociateSignWithSameName.add(requestViewData);
            }
          }
          String sign_requestWithSameName = null;
          if (!signsWithSameName.isEmpty()) {
            sign_requestWithSameName = signsWithSameName.stream().map(s -> s.name).collect(Collectors.joining(","));
          }
          if (!requestsWithNoAssociateSignWithSameName.isEmpty()) {
            if (sign_requestWithSameName != null) {
              sign_requestWithSameName = sign_requestWithSameName + requestsWithNoAssociateSignWithSameName.stream().map(r -> r.requestName).collect(Collectors.joining(","));
            } else {
              sign_requestWithSameName = requestsWithNoAssociateSignWithSameName.stream().map(r -> r.requestName).collect(Collectors.joining(","));
            }
          }
          if (sign_requestWithSameName != null) {
            if (force) {
              renameSignAndAssociatedRequest(signId, signCreationViewApi.getName(), sign.name, isAdmin, requestHttp);
              response.setStatus(HttpServletResponse.SC_OK);
              return signResponseApi;
            } else {
              signResponseApi.warningMessage = messageByLocaleService.getMessage("same_name_exist", new Object[]{sign_requestWithSameName});
              response.setStatus(HttpServletResponse.SC_CONFLICT);
              return signResponseApi;
            }
          } else {
            renameSignAndAssociatedRequest(signId, signCreationViewApi.getName(), sign.name, isAdmin, requestHttp);
            response.setStatus(HttpServletResponse.SC_OK);
            return signResponseApi;
          }
        }
      } else {
        signResponseApi.errorMessage = messageByLocaleService.getMessage("sign.name_already_exist");
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        return signResponseApi;
      }
    }

    response.setStatus(HttpServletResponse.SC_OK);

    return signResponseApi;
  }

  private void renameSignAndAssociateToRequest(Long signId, long requestId, long videoId, String name, String oldName, Boolean isAdmin, HttpServletRequest requestHttp) {
    services.sign().renameSignAndAssociateToRequest(signId, requestId, name);
  /*  Video video = services.video().withId(videoId);
    if (video.url.contains("http")) {
      ChangeVideoNameOnDailyMotion(video.url.substring(video.url.lastIndexOf('/') + 1), name);
    }*/
    ChangeSignNamesOnDailyMotion(signId, name, oldName, isAdmin, requestHttp);
  }

  private void ChangeSignNamesOnDailyMotion(Long signId, String name, String oldName, Boolean isAdmin, HttpServletRequest requestHttp) {
    String title, bodyMail, messageType;
    Sign sign = services.sign().withId(signId);
    if (isAdmin) {
      sendEmailAndInsertMessage(name, oldName, requestHttp, sign);
    }
    sign.videos.stream().forEach(v -> {
      if (v.url.contains("http")) {
        ChangeVideoNameOnDailyMotion(v.url.substring(v.url.lastIndexOf('/') + 1), name);
      }});
    if (sign.videoDefinition != null) {

      if (!sign.videoDefinition.isEmpty()) {
        String signDefinitionTitle =  messageByLocaleService.getMessage("sign.title_description_LSF", new Object[]{name});
        if (sign.videoDefinition.contains("http")) {
          ChangeVideoNameOnDailyMotion(sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1), signDefinitionTitle);
        }
      }
    }
  }

  private void sendEmailAndInsertMessage(String name, String oldName, HttpServletRequest requestHttp, Sign sign) {
    String bodyMail;
    String title;
    String messageType;
    title = messageByLocaleService.getMessage("rename_sign_title", new Object[]{oldName, name});
    bodyMail = messageByLocaleService.getMessage("rename_sign_body", new Object[]{oldName, name});
    messageType = "RenameSignSendEmailMessage";
    List<String> emails = sign.videos.stream().filter(v -> v.user.email != null).map(v -> v.user.email).collect(Collectors.toList());
    emails = emails.stream().distinct().collect(Collectors.toList());
    if (emails.size() != 0) {
      List<String> finalEmails = emails;
      String finalMessageType = messageType;
      Runnable task = () -> {
        log.info("send mail email = {} / title = {} / body = {}", finalEmails.toString(), title, bodyMail);
        services.emailService().sendRenameSignMessage(finalEmails.toArray(new String[finalEmails.size()]), title, bodyMail, name, oldName, finalMessageType, requestHttp.getLocale());
      };
      new Thread(task).start();
    } else {
      User admin = services.user().getAdmin();
      messageType = "RenameSignMessage";
      String values = admin.username + ';' + oldName + ';' + name;
      MessageServer messageServer = new MessageServer(new Date(), messageType, values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);
    }
  }

  private void renameSignAndAssociatedRequest(Long signId, String name, String oldName, Boolean isAdmin, HttpServletRequest requestHttp) {
    services.sign().renameSign(signId, name);
/*    Video video = services.video().withId(videoId);
    if (video.url.contains("http")) {
      ChangeVideoNameOnDailyMotion(video.url.substring(video.url.lastIndexOf('/') + 1), name);
    }*/
    ChangeAllNamesOnDailyMotion(signId, name, oldName, isAdmin, requestHttp);
  }

  private void ChangeAllNamesOnDailyMotion(Long signId, String name, String oldName, Boolean isAdmin, HttpServletRequest requestHttp) {
    Sign sign = services.sign().withId(signId);
    if (isAdmin) {
      sendEmailAndInsertMessage(name, oldName, requestHttp, sign);
    }
    Request request = services.sign().requestForSign(sign);
    sign.videos.stream().forEach(v -> {
      if (v.url.contains("http")) {
        ChangeVideoNameOnDailyMotion(v.url.substring(v.url.lastIndexOf('/') + 1), name);
      }});
    if (sign.videoDefinition != null) {
      if (!sign.videoDefinition.isEmpty()) {
        String signDefinitionTitle = messageByLocaleService.getMessage("sign.title_description_LSF", new Object[]{name});
        if (sign.videoDefinition.contains("http")) {
          ChangeVideoNameOnDailyMotion(sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1), signDefinitionTitle);
        }
      }
    }
    if (request != null) {
      if (request.requestVideoDescription != null && sign.videoDefinition != null) {
        if (!request.requestVideoDescription.isEmpty() && !sign.videoDefinition.isEmpty()) {
          if (!request.requestVideoDescription.equals(sign.videoDefinition)) {
            String requestDescrioptionTitle = messageByLocaleService.getMessage("request.title_description_LSF", new Object[]{name});
            if (request.requestVideoDescription.contains("http")) {
              ChangeVideoNameOnDailyMotion(request.requestVideoDescription.substring(request.requestVideoDescription.lastIndexOf('/') + 1), requestDescrioptionTitle);
            }
          }
        }
      }
    }
  }

  private void ChangeVideoNameOnDailyMotion(String dailymotionId, String name) {

    AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
    if (authTokenInfo.isExpired()) {
      dalymotionToken.retrieveToken();
      authTokenInfo = dalymotionToken.getAuthTokenInfo();
    }

    final String uri = environment.getProperty("app.dailymotion_url") + "/video/"+dailymotionId;
    RestTemplate restTemplate = springRestClient.buildRestTemplate();

    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add("Authorization", "Bearer " + authTokenInfo.getAccess_token());

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
    body.add("title", name);

    HttpEntity<?> request = new HttpEntity<Object>(body, headers);

    ResponseEntity<VideoDailyMotion> response = restTemplate.exchange(uri, HttpMethod.POST, request, VideoDailyMotion.class );
    VideoDailyMotion videoDailyMotion = response.getBody();

    return;
  }

  @Secured("ROLE_USER_A")
  @RequestMapping(value = RestApi.WS_SEC_SIGNS_VIDEOS, method = RequestMethod.POST, headers = {"content-type=multipart/mixed", "content-type=multipart/form-data"})
  public VideoResponseApi addVideo(@PathVariable long signId, @RequestPart("file") Optional<MultipartFile> file, @RequestPart("data") SignCreationViewApi signCreationViewApi, HttpServletResponse response, Principal principal) throws
    InterruptedException {

    VideoResponseApi videoResponseApi = new VideoResponseApi();

    signCreationViewApi.clearXss();
    if (!AuthentModel.hasRole("ROLE_USER_A")) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      videoResponseApi.errorMessage = messageByLocaleService.getMessage("forbidden_action");
      return videoResponseApi;
    }
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleSelectedVideoFileUploadOnServer(file.get(), OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.empty(), signCreationViewApi, principal, response);
    } else {
      return handleSelectedVideoFileUpload(file.get(), OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.empty(), signCreationViewApi, principal, response);
    }
  }

  private void GenerateThumbnail(String thumbnailFile, String fileOutput, String logFile) {
    String cmdGenerateThumbnail;

    cmdGenerateThumbnail = String.format("input=\"%s\"&&dur=$(ffprobe -loglevel error -show_entries format=duration -of default=nk=1:nw=1 \"$input\")&&ffmpeg -y -ss \"$(echo \"$dur / 2\" | bc -l | sed -e 's/^-\\./-0./' -e 's/^\\./0./')\" -i  \"$input\" -vframes 1 -s 360x360 -vf \"scale=(iw*sar)*max(360.1/(iw*sar)\\,360.1/ih):ih*max(360.1/(iw*sar)\\,360.1/ih), crop=360:360\" \"%s\"", fileOutput, thumbnailFile);
    NativeInterface.launch(cmdGenerateThumbnail, null, logFile);
  }

  private VideoResponseApi handleSelectedVideoFileUpload(@RequestParam("file") MultipartFile file, OptionalLong requestId, OptionalLong signId, OptionalLong videoId, @ModelAttribute SignCreationViewApi signCreationViewApi, Principal principal, HttpServletResponse response) throws InterruptedException {
    String videoUrl = null;
    String fileName = environment.getProperty("app.file") + "/" + file.getOriginalFilename();
    String thumbnailFile = environment.getProperty("app.file") + "/thumbnail/" + file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf('.')) + ".png";
    String logFile = "/tmp/" + file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf('.')) + ".log";
    File inputFile;

    VideoResponseApi videoResponseApi = new VideoResponseApi();

    try {
      storageService.store(file);
      inputFile = storageService.load(file.getOriginalFilename()).toFile();
    } catch (Exception errorLoadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorUploadFile");
      return videoResponseApi;
    }

    try {
      GenerateThumbnail(thumbnailFile, inputFile.getAbsolutePath(), logFile);
    } catch (Exception errorEncondingFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorThumbnailFile");
      return videoResponseApi;
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
      if (signId.isPresent()) {
        body.add("title", services.sign().withId(signId.getAsLong()).name);
      } else {
        body.add("title", signCreationViewApi.getName());
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

      Sign sign;
      Video video;
      if (signId.isPresent() && (videoId.isPresent())) {
        video = services.video().withId(videoId.getAsLong());
        if (video.url.contains("http")) {
          dailymotionId = video.url.substring(video.url.lastIndexOf('/') + 1);
          try {
            DeleteVideoOnDailyMotion(dailymotionId);
          } catch (Exception errorDailymotionDeleteVideo) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
            return videoResponseApi;
          }
        } else {
          DeleteFilesOnServer(video.url, video.pictureUri);
        }
        sign = services.sign().replace(signId.getAsLong(), videoId.getAsLong(), videoUrl, pictureUri);
      } else if (signId.isPresent() && !(videoId.isPresent())) {
        sign = services.sign().addNewVideo(user.id, signId.getAsLong(), videoUrl, pictureUri);
      } else {
        sign = services.sign().create(user.id, signCreationViewApi.getName(), videoUrl, pictureUri);
      }

      log.info("handleSelectedVideoFileUpload : username = {} / sign name = {} / video url = {}", user.username, signCreationViewApi.getName(), videoUrl);

      if (requestId.isPresent()) {
        services.request().changeSignRequest(requestId.getAsLong(), sign.id);
      }

      if (sign.lastVideoId != 0 ) {
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
          services.sign().updateWithDailymotionInfo(sign.id, sign.lastVideoId, dailyMotion.thumbnail_360_url, dailyMotion.embed_url);
        };

        new Thread(task).start();
      }

      response.setStatus(HttpServletResponse.SC_OK);
      videoResponseApi.signId = sign.id;
      videoResponseApi.videoId = sign.lastVideoId;
      return videoResponseApi;

    } catch (Exception errorDailymotionUploadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionUploadFile");
      return videoResponseApi;
    }
  }

  private VideoResponseApi handleSelectedVideoFileUploadOnServer(@RequestParam("file") MultipartFile file, OptionalLong requestId, OptionalLong signId, OptionalLong videoId, @ModelAttribute SignCreationViewApi signCreationViewApi, Principal principal, HttpServletResponse response) throws InterruptedException {
    String videoUrl = null;
    String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
    String newFileName = UUID.randomUUID().toString() + "." + fileExtension;
    String newAbsoluteFileName = environment.getProperty("app.file") +"/" + newFileName;
    String thumbnailFile = environment.getProperty("app.file") + "/thumbnail/" + newFileName.substring(0, newFileName.lastIndexOf('.')) + ".png";
    String logFile = "/tmp/" + newFileName.substring(0, newFileName.lastIndexOf('.')) + ".log";
    File inputFile;
    Streams streamInfo;
    String newAbsoluteFileNameWithExtensionMp4 = newAbsoluteFileName.substring(0, newAbsoluteFileName.lastIndexOf('.')) + ".mp4";

    VideoResponseApi videoResponseApi = new VideoResponseApi();

    try {
      storageService.store(file);
      inputFile = storageService.load(file.getOriginalFilename()).toFile();
      File newName = new File(newAbsoluteFileName);
      inputFile.renameTo(newName);
    } catch (Exception errorLoadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorUploadFile");
      return videoResponseApi;
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

    try {
      GenerateThumbnail(thumbnailFile, newAbsoluteFileName, logFile);
    } catch (Exception errorEncondingFile) {
      DeleteFilesOnServer(newAbsoluteFileName, null);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorThumbnailFile");
      return videoResponseApi;
    }


    User user = services.user().withUserName(principal.getName());


    String pictureUri = thumbnailFile;
    videoUrl= newAbsoluteFileName;

    Sign sign;
    Video video;
    if (signId.isPresent() && (videoId.isPresent())) {
      video = services.video().withId(videoId.getAsLong());
      DeleteFilesOnServer(video.url, video.pictureUri);
      sign = services.sign().replace(signId.getAsLong(), videoId.getAsLong(), videoUrl, pictureUri);
    } else if (signId.isPresent() && !(videoId.isPresent())) {
      sign = services.sign().addNewVideo(user.id, signId.getAsLong(), videoUrl, pictureUri);
    } else {
      sign = services.sign().create(user.id, signCreationViewApi.getName(), videoUrl, pictureUri);
    }

    log.info("handleSelectedVideoFileUploadOnServer : username = {} / sign name = {} / video url = {}", user.username, signCreationViewApi.getName(), videoUrl);

    if (requestId.isPresent()) {
      services.request().changeSignRequest(requestId.getAsLong(), sign.id);
    }

    response.setStatus(HttpServletResponse.SC_OK);
    videoResponseApi.signId = sign.id;
    videoResponseApi.videoId = sign.lastVideoId;
    return videoResponseApi;

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

  @Secured("ROLE_USER_A")
  @RequestMapping(value = RestApi.WS_SEC_SIGN, method = RequestMethod.PUT, headers = {"content-type=multipart/mixed", "content-type=multipart/form-data"})
  public VideoResponseApi updateVideo(@PathVariable Long signId, @RequestPart("file") Optional<MultipartFile> file, @RequestPart("data") Optional<SignCreationViewApi> signCreationViewApi, HttpServletResponse response, Principal principal) throws
    InterruptedException {
    VideoResponseApi videoResponseApi = new VideoResponseApi();

    if (signCreationViewApi.isPresent()) {
      signCreationViewApi.get().clearXss();
      if (!signCreationViewApi.get().getTextDefinition().isEmpty()) {
        services.sign().changeSignTextDefinition(signId, signCreationViewApi.get().getTextDefinition());
      }
    }

    if (file.isPresent()) {
      if (environment.getProperty("app.dailymotion_url").isEmpty()) {
        return handleSelectedVideoFileUploadForSignDefinitionOnServer(file.get(), signId, principal, response);
      } else {
        return handleSelectedVideoFileUploadForSignDefinition(file.get(), signId, principal, response);
      }
    }


    response.setStatus(HttpServletResponse.SC_OK);
    return videoResponseApi;
  }

  private VideoResponseApi handleSelectedVideoFileUploadForSignDefinition(@RequestParam("file") MultipartFile file, @PathVariable long signId, Principal principal, HttpServletResponse response) throws InterruptedException {

    VideoResponseApi videoResponseApi = new VideoResponseApi();
    Sign sign = null;
    sign = services.sign().withId(signId);
    String videoUrl = null;
    String fileName = environment.getProperty("app.file") + "/" + file.getOriginalFilename();
    File inputFile;


    try {
      storageService.store(file);
      inputFile = storageService.load(file.getOriginalFilename()).toFile();
    } catch (Exception errorLoadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorUploadFile");
      return videoResponseApi;
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
      body.add("title", messageByLocaleService.getMessage("sign.title_description_LSF", new Object[]{sign.name}));
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

      if (changeSignDefinition(signId, response, videoUrl, sign)) {
        videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
        return videoResponseApi;
      }



    /*  Request request = services.sign().requestForSign(sign);
      if (request != null) {
        if (request.requestVideoDescription != null && sign.videoDefinition != null) {
          if (!request.requestVideoDescription.equals(sign.videoDefinition)) {
            if (sign.videoDefinition != null) {
              if (sign.videoDefinition.contains("http")) {
                dailymotionId = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
                try {
                  DeleteVideoOnDailyMotion(dailymotionId);
                } catch (Exception errorDailymotionDeleteVideo) {
                  response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                  videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
                  return videoResponseApi;
                }
              } else {
                DeleteFilesOnServer(sign.videoDefinition, null);
              }
            }
          }
        }
      }
      services.sign().changeSignVideoDefinition(signId, videoUrl);*/

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
          services.sign().changeSignVideoDefinition(signId, dailyMotion.embed_url);
        }
      };

      new Thread(task).start();

      response.setStatus(HttpServletResponse.SC_OK);
      videoResponseApi.signId = sign.id;
      videoResponseApi.videoId = sign.lastVideoId;
      return videoResponseApi;

    } catch (Exception errorDailymotionUploadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionUploadFile");
      return videoResponseApi;
    }
  }

  private boolean changeSignDefinition(long signId, HttpServletResponse response, String videoUrl, Sign sign) {
    String dailymotionId;
    Request request = services.sign().requestForSign(sign);
    if (request != null) {
      if (request.requestVideoDescription != null && sign.videoDefinition != null) {
        if (!request.requestVideoDescription.equals(sign.videoDefinition)) {
          if (sign.videoDefinition != null) {
            if (sign.videoDefinition.contains("http")) {
              dailymotionId = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
              try {
                DeleteVideoOnDailyMotion(dailymotionId);
              } catch (Exception errorDailymotionDeleteVideo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return true;
              }
            } else {
              DeleteFilesOnServer(sign.videoDefinition, null);
            }
          }
        }
      }
    } else {
      if (sign.videoDefinition != null) {
        if (sign.videoDefinition.contains("http")) {
          dailymotionId = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
          try {
            DeleteVideoOnDailyMotion(dailymotionId);
          } catch (Exception errorDailymotionDeleteVideo) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return true;
          }
        } else {
          DeleteFilesOnServer(sign.videoDefinition, null);
        }
      }
    }
    services.sign().changeSignVideoDefinition(signId, videoUrl);
    return false;
  }

  private void changeSignDefinitionOnServer(long signId, String videoUrl, Sign sign) {

    Request request = services.sign().requestForSign(sign);
    if (request != null) {
      if (request.requestVideoDescription != null && sign.videoDefinition != null) {
        if (!request.requestVideoDescription.equals(sign.videoDefinition)) {
          if (sign.videoDefinition != null) {
              DeleteFilesOnServer(sign.videoDefinition, null);
            }
          }
        }
    } else {
      if (sign.videoDefinition != null) {
          DeleteFilesOnServer(sign.videoDefinition, null);
      }
    }
    services.sign().changeSignVideoDefinition(signId, videoUrl);
    return;
  }

  private VideoResponseApi handleSelectedVideoFileUploadForSignDefinitionOnServer(@RequestParam("file") MultipartFile file, @PathVariable long signId, Principal principal, HttpServletResponse response) throws InterruptedException {

    VideoResponseApi videoResponseApi = new VideoResponseApi();
    Sign sign = services.sign().withId(signId);
    String videoUrl = null;
    String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
    String newFileName = UUID.randomUUID().toString() + "." + fileExtension;
    String newAbsoluteFileName = environment.getProperty("app.file") +"/" + newFileName;
    File inputFile;
    Streams streamInfo;
    String newAbsoluteFileNameWithExtensionMp4 = newAbsoluteFileName.substring(0, newAbsoluteFileName.lastIndexOf('.')) + ".mp4";


    try {
      storageService.store(file);
      inputFile = storageService.load(file.getOriginalFilename()).toFile();
      File newName = new File(newAbsoluteFileName);
      inputFile.renameTo(newName);
    } catch (Exception errorLoadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorUploadFile");
      return videoResponseApi;
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
          DeleteFilesOnServer(newAbsoluteFileName, null);DeleteFilesOnServer(newAbsoluteFileName, null);
          newAbsoluteFileName = newAbsoluteFileNameWithExtensionMp4;
        }
      }
    }

    videoUrl= newAbsoluteFileName;

    changeSignDefinitionOnServer(signId, videoUrl, sign);

    response.setStatus(HttpServletResponse.SC_OK);
    videoResponseApi.signId = sign.id;
    videoResponseApi.videoId = sign.lastVideoId;
    return videoResponseApi;

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

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_MY_VIDEOS)
  public ResponseEntity<?> myVideos(Principal principal) {
    final User user = AuthentModel.isAuthenticated(principal) ? services.user().withUserName(principal.getName()) : null;

    List<Object[]> queryVideos = services.video().AllVideosCreateByUser(user.id);
    List<VideoViewData> videoViewsData = queryVideos.stream()
      .map(objectArray -> new VideoViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> videoInFavorite = Arrays.asList(services.video().VideosForAllFavoriteByUser(user.id));

    List<VideoView2> videoViews = videoViewsData.stream()
      .map(videoViewData -> buildVideoView(videoViewData, videoInFavorite, user))
      .collect(Collectors.toList());

    VideosViewSort videosViewSort = new VideosViewSort();
    videoViews = videosViewSort.sort(videoViews);


    return new ResponseEntity<>(videoViews, HttpStatus.OK);

  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_USER_VIDEOS)
  public ResponseEntity<?> userVideos(@PathVariable long userId, Principal principal) {

    User connectedUser = services.user().withUserName(principal.getName());

    User user = services.user().withId(userId);

    List<Object[]> queryVideos = services.video().AllVideosCreateByUser(user.id);
    List<VideoViewData> videoViewsData = queryVideos.stream()
      .map(objectArray -> new VideoViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> videoInFavorite = Arrays.asList(services.video().VideosForAllFavoriteByUser(user.id));

    List<VideoView2> videoViews = videoViewsData.stream()
      .map(videoViewData -> buildVideoView(videoViewData, videoInFavorite, connectedUser))
      .collect(Collectors.toList());

    VideosViewSort videosViewSort = new VideosViewSort();
    videoViews = videosViewSort.sort(videoViews);


    return new ResponseEntity<>(videoViews, HttpStatus.OK);

  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_INCREASE_NB_VIEW, method = RequestMethod.POST)
  public void increaseNbView(@PathVariable long videoId, HttpServletResponse response, Principal principal) {
    User user = services.user().withUserName(principal.getName());
    Videos videos = services.video().forUser(user.id);

    boolean isVideoBellowToMe = videos.stream().anyMatch(video -> video.id == videoId);
    if (isVideoBellowToMe) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    } else {
      services.video().increaseNbView(videoId);
      response.setStatus(HttpServletResponse.SC_OK);
      return;
    }
  }

  }
