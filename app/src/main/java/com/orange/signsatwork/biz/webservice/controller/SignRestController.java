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
import com.orange.signsatwork.biz.view.controller.CommentOrderComparator;
import com.orange.signsatwork.biz.view.model.*;
import com.orange.signsatwork.biz.webservice.model.SignId;
import com.orange.signsatwork.biz.webservice.model.SignView;
import com.orange.signsatwork.biz.webservice.model.VideoView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
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


  @RequestMapping(value = RestApi.WS_OPEN_SIGN + "/{id}")
  public SignView sign(@PathVariable long id) {
    Sign sign = services.sign().withId(id);
    return new SignView(sign);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_VIDEOS)
  public List<VideoView2> videos(@PathVariable long signId, Principal principal) {
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

    return videoViews;

  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_VIDEO)
  public VideoView video(@PathVariable long signId, @PathVariable long videoId, Principal principal) {
    Boolean isVideoCreatedByMe = false;
    Boolean isVideoBelowToFavorite = false;
    Boolean isVideoHasAveragePositiveRate = false;

    final User user = AuthentModel.isAuthenticated(principal) ? services.user().withUserName(principal.getName()) : null;

    Sign sign = services.sign().withIdSignsView(signId);
    Video video = services.video().withId(videoId);
    String signTextDefinition = sign.textDefinition;
    String signVideoDefinition = sign.videoDefinition;

    String videoName;
    if ((video.idForName == 0) || (sign.nbVideo == 1 )){
      videoName = sign.name;
    } else {
      videoName = sign.name + " (" + video.idForName + ")";
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

    Long nbRating = services.sign().NbRatingForSign(signId);

    List<Object[]> queryAllVideosHistory = services.sign().AllVideosHistoryForSign(signId);
    List<VideoHistoryData> videoHistoryDatas = queryAllVideosHistory.stream()
      .map(objectArray -> new VideoHistoryData(objectArray))
      .collect(Collectors.toList());

    return new VideoView(signId, signTextDefinition, signVideoDefinition, videoId, videoName, video.url, isVideoCreatedByMe, isVideoHasAveragePositiveRate, isVideoBelowToFavorite, ratingData, commentDatas, nbRating, videoHistoryDatas);
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
  public ResponseEntity<?> signs(@RequestParam("sort") Optional<String> sort, Principal principal) {
    final User user = AuthentModel.isAuthenticated(principal) ? services.user().withUserName(principal.getName()) : null;
    List<Object[]> querySigns = new ArrayList<>();
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
      List<Long> finalSignInFavorite2 = signInFavorite;
      signViews = signViewsData.stream()
        .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, finalSignInFavorite2, user))
        .collect(Collectors.toList());
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


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SIGN_CREATE, method = RequestMethod.POST)
  public SignId createSign(@RequestBody SignCreationView signCreationView, Principal principal) {
    User user = services.user().withUserName(principal.getName());
    Sign sign = services.sign().create(user.id, signCreationView.getSignName(), signCreationView.getVideoUrl(), "");

    log.info("createSign: username = {} / sign name = {} / video url = {}", user.username, signCreationView.getSignName(), signCreationView.getVideoUrl());

    return new SignId(sign.id);
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
      return "/";

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

      final String uri = "https://api.dailymotion.com/video/"+dailymotionId;
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
  @RequestMapping(value = RestApi.WS_SEC_VIDEO_DELETE, method = RequestMethod.DELETE)
  public String deleteApiVideo(@PathVariable long signId, @PathVariable long videoId, HttpServletResponse response) {
    String dailymotionId;
    Sign sign = services.sign().withId(signId);
    if (sign.videos.list().size() == 1) {
      Request request = services.sign().requestForSign(sign);
      if (request != null) {
        if (request.requestVideoDescription != sign.videoDefinition) {
          String dailymotionIdForSignDefinition;
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
      return "/";

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

}
