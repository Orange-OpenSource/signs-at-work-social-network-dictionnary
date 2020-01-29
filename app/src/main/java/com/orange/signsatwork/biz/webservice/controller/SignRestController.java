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
import org.omg.CORBA.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

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

  @RequestMapping(value = RestApi.WS_OPEN_SIGN + "/{id}")
  public SignView sign(@PathVariable long id) {
    Sign sign = services.sign().withId(id);
    return new SignView(sign);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_VIDEO_DELETE, method = RequestMethod.POST)
  public String deleteVideo(@PathVariable long signId, @PathVariable long videoId, HttpServletResponse response) {
    String dailymotionId;
    Sign sign = services.sign().withId(signId);
    if (sign.videos.list().size() == 1) {
      Request request = services.sign().requestForSign(sign);
      if (request != null) {
        if (request.requestVideoDescription != sign.videoDefinition) {
          String dailymotionIdForSignDefinition;
          if (sign.videoDefinition != null) {
            dailymotionIdForSignDefinition = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
            try {
              DeleteVideoOnDailyMotion(dailymotionIdForSignDefinition);
            } catch (Exception errorDailymotionDeleteVideo) {
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
            }
          }
        }
      } else {
        String dailymotionIdForSignDefinition;
        if (sign.videoDefinition != null) {
          dailymotionIdForSignDefinition = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
          try {
            DeleteVideoOnDailyMotion(dailymotionIdForSignDefinition);
          } catch (Exception errorDailymotionDeleteVideo) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
          }
        }
      }

      services.sign().delete(sign);
      dailymotionId = sign.url.substring(sign.url.lastIndexOf('/') + 1);
      try {
        DeleteVideoOnDailyMotion(dailymotionId);
      }
      catch (Exception errorDailymotionDeleteVideo) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
      }
      response.setStatus(HttpServletResponse.SC_OK);
      return "/sec/signs/mostrecent?isMostRecent=false&isSearch=false";

    } else {
      Video video = services.video().withId(videoId);
      services.video().delete(video);
      dailymotionId = video.url.substring(video.url.lastIndexOf('/') + 1);
      try {
        DeleteVideoOnDailyMotion(dailymotionId);
      }
      catch (Exception errorDailymotionDeleteVideo) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
      }
      response.setStatus(HttpServletResponse.SC_OK);
      return "/sign/" + signId;
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


    VideosViewSort videosViewSort = new VideosViewSort();
    videoViews = videosViewSort.sort(videoViews);

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

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SIGNS)
  public ResponseEntity<?> signs(@RequestParam("sort") Optional<String> sort, @RequestParam("name") Optional<String> name,  Principal principal) {
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

    signInFavorite = Arrays.asList(services.sign().SignsBellowToFavoriteByUser(user.id));

    if (sort.isPresent()) {
      if (sort.get().equals("averageRating") || sort.get().equals("-averageRating")) {
        List<Long> finalSignWithRatingList = signWithRatingList;
        List<SignViewData> rating = signViewsData.stream()
          .filter(signViewData -> finalSignWithRatingList.contains(signViewData.id))
          .sorted(new CommentOrderComparator(signWithRatingList))
          .collect(Collectors.toList());
        List<Long> finalSignInFavorite1 = signInFavorite;
        signViews = rating.stream()
          .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, finalSignInFavorite1, user))
          .collect(Collectors.toList());
      } else if (sort.get().equals("date") || sort.get().equals("-date") || sort.get().equals("name") || sort.get().equals("-name")) {
        List<Long> finalSignInFavorite = signInFavorite;
        signViews = signViewsData.stream()
          .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, finalSignInFavorite, user))
          .collect(Collectors.toList());
      }
    } else {
      if (name.isPresent()) {
        Signs signs = services.sign().search(name.get());
        List<Long> finalSignInFavorite3 = signInFavorite;
        signViews = signs.stream()
          .map(sign -> buildSignView(sign, signWithCommentList, signWithView, signWithPositiveRate, finalSignInFavorite3, user))
          .collect(Collectors.toList());

      } else {
        List<Long> finalSignInFavorite2 = signInFavorite;
        signViews = signViewsData.stream()
          .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, finalSignInFavorite2, user))
          .collect(Collectors.toList());
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
        if (request.requestVideoDescription != sign.videoDefinition) {
          String dailymotionIdForSignDefinition;
          if (sign.videoDefinition != null) {
            dailymotionIdForSignDefinition = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
            try {
              DeleteVideoOnDailyMotion(dailymotionIdForSignDefinition);
            } catch (Exception errorDailymotionDeleteVideo) {
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
              return videoResponseApi;
            }
          }
        }
      } else {
        String dailymotionIdForSignDefinition;
        if (sign.videoDefinition != null) {
          dailymotionIdForSignDefinition = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
          try {
            DeleteVideoOnDailyMotion(dailymotionIdForSignDefinition);
          } catch (Exception errorDailymotionDeleteVideo) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
            return videoResponseApi;
          }
        }
      }

      services.sign().delete(sign);
      dailymotionId = sign.url.substring(sign.url.lastIndexOf('/') + 1);
      try {
        DeleteVideoOnDailyMotion(dailymotionId);
      } catch (Exception errorDailymotionDeleteVideo) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
        return videoResponseApi;
      }
      response.setStatus(HttpServletResponse.SC_OK);
      return videoResponseApi;

    } else {
      services.video().delete(video);
      dailymotionId = video.url.substring(video.url.lastIndexOf('/') + 1);
      try {
        DeleteVideoOnDailyMotion(dailymotionId);
      } catch (Exception errorDailymotionDeleteVideo) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
        return videoResponseApi;
      }
      response.setStatus(HttpServletResponse.SC_OK);
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

      if (!services.sign().withName(signCreationViewApi.getName()).list().isEmpty()) {
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        videoResponseApi.errorMessage = messageByLocaleService.getMessage("sign.already_exists");
        videoResponseApi.signId = services.sign().withName(signCreationViewApi.getName()).list().get(0).id;
        return videoResponseApi;
      }

      return handleSelectedVideoFileUpload(file.get(), OptionalLong.empty(), OptionalLong.empty(), OptionalLong.empty(), signCreationViewApi, principal, response);
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

    return handleSelectedVideoFileUpload(file.get(), OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.of(videoId), signCreationViewApi, principal, response);
  }

  @Secured("ROLE_USER_A")
  @RequestMapping(value = RestApi.WS_SEC_SIGNS_VIDEOS, method = RequestMethod.POST, headers = {"content-type=multipart/mixed", "content-type=multipart/form-data"})
  public VideoResponseApi addVideo(@PathVariable long signId, @RequestPart("file") Optional<MultipartFile> file, @RequestPart("data") SignCreationViewApi signCreationViewApi, HttpServletResponse response, Principal principal) throws
    InterruptedException {

    VideoResponseApi videoResponseApi = new VideoResponseApi();

    if (!AuthentModel.hasRole("ROLE_USER_A")) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      videoResponseApi.errorMessage = messageByLocaleService.getMessage("forbidden_action");
      return videoResponseApi;
    }

    Long nbRating = services.sign().NbRatingForSign(signId);
    if (nbRating == 0) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      videoResponseApi.errorMessage = messageByLocaleService.getMessage("sign_with_no_rate");
      return videoResponseApi;
    }

    return handleSelectedVideoFileUpload(file.get(), OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.empty(), signCreationViewApi, principal, response);
  }



  private VideoResponseApi handleSelectedVideoFileUpload(@RequestParam("file") MultipartFile file, OptionalLong requestId, OptionalLong signId, OptionalLong videoId, @ModelAttribute SignCreationViewApi signCreationViewApi, Principal principal, HttpServletResponse response) throws InterruptedException {

    String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");
    VideoResponseApi videoResponseApi = new VideoResponseApi();
    try {
      String dailymotionId;
      String videoUrl = null;

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


      String pictureUri = null;
      if (!videoDailyMotion.thumbnail_360_url.isEmpty()) {
        pictureUri = videoDailyMotion.thumbnail_360_url;
        log.warn("handleSelectedVideoFileUpload : thumbnail_360_url = {}", videoDailyMotion.thumbnail_360_url);
      }

      if (!videoDailyMotion.embed_url.isEmpty()) {
        videoUrl =videoDailyMotion.embed_url;
        log.warn("handleSelectedVideoFileUpload : embed_url = {}", videoDailyMotion.embed_url);
      }

      Sign sign;
      if (signId.isPresent() && (videoId.isPresent())) {
        sign = services.sign().withId(signId.getAsLong());
        dailymotionId = sign.url.substring(sign.url.lastIndexOf('/') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionId);
        }
        catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
          return videoResponseApi;
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

      response.setStatus(HttpServletResponse.SC_OK);
      videoResponseApi.signId = sign.id;
      return videoResponseApi;

    } catch (Exception errorDailymotionUploadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionUploadFile");
      return videoResponseApi;
    }
  }

  @Secured("ROLE_USER_A")
  @RequestMapping(value = RestApi.WS_SEC_SIGN, method = RequestMethod.PUT, headers = {"content-type=multipart/mixed", "content-type=multipart/form-data"})
  public VideoResponseApi updateVideo(@PathVariable Long signId, @RequestPart("file") Optional<MultipartFile> file, @RequestPart("data") Optional<SignCreationViewApi> signCreationViewApi, HttpServletResponse response, Principal principal) throws
    InterruptedException {
    VideoResponseApi videoResponseApi = new VideoResponseApi();

    if (signCreationViewApi.isPresent()) {
      if (!signCreationViewApi.get().getTextDefinition().isEmpty()) {
        services.sign().changeSignTextDefinition(signId, signCreationViewApi.get().getTextDefinition());
      }
    }

    if (file.isPresent()) {
      return handleSelectedVideoFileUploadForSignDefinition(file.get(), signId, principal, response);
    }


    response.setStatus(HttpServletResponse.SC_OK);
    return videoResponseApi;
  }

  private VideoResponseApi handleSelectedVideoFileUploadForSignDefinition(@RequestParam("file") MultipartFile file, @PathVariable long signId, Principal principal, HttpServletResponse response) throws InterruptedException {
    Sign sign = null;
    VideoResponseApi videoResponseApi = new VideoResponseApi();
    String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");
    sign = services.sign().withId(signId);

    try {
      String dailymotionId;
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
      body.add("title", "Définition LSF du signe " + sign.name);
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
      int i = 0;
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
        if (sign.videoDefinition != null) {
          dailymotionId = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
          try {
            DeleteVideoOnDailyMotion(dailymotionId);
          } catch (Exception errorDailymotionDeleteVideo) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
            return videoResponseApi;
          }
        }
        services.sign().changeSignVideoDefinition(signId, videoDailyMotion.embed_url);

      }

      response.setStatus(HttpServletResponse.SC_OK);
      videoResponseApi.signId = sign.id;
      return videoResponseApi;

    } catch (Exception errorDailymotionUploadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      videoResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionUploadFile");
      return videoResponseApi;
    }
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
  public ResponseEntity<?> userVideos(@PathVariable long userId) {

    User user = services.user().withId(userId);

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


  }
