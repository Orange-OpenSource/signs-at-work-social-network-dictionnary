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

import com.orange.signsatwork.biz.domain.Rating;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.domain.Videos;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.webservice.model.RatingViewApi;
import com.orange.signsatwork.biz.webservice.model.RatingCreationViewApi;
import com.orange.signsatwork.biz.webservice.model.VideoResponseApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class RatingRestController {

  @Autowired
  private Services services;
  @Autowired
  MessageByLocaleService messageByLocaleService;


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_VIDEO_RATE_POSITIVE, method = RequestMethod.POST)
  public void videoRatePositive(@PathVariable long signId, @PathVariable long videoId, Principal principal) {
    videoDoRate(signId, videoId, principal, Rating.Positive);
    return;
  }

  // kanban 473325 suite retour test utilisateurs
  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_VIDEO_RATE_NEUTRAL, method = RequestMethod.POST)
  public void videoRateNeutral(@PathVariable long signId, @PathVariable long videoId, Principal principal) {
    videoDoRate(signId, videoId, principal, Rating.Neutral);
    return;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_VIDEO_RATE_NEGATIVE, method = RequestMethod.POST)
  public void videoRateNegative(@PathVariable long signId, @PathVariable long videoId, Principal principal) {
    videoDoRate(signId, videoId, principal, Rating.Negative);
    return;
  }

  private void videoDoRate(long signId, long videoId, Principal principal, Rating rating) {
    User user = services.user().withUserName(principal.getName());
    services.video().createVideoRating(videoId, user.id, rating);
    return;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RATINGS)
  public ResponseEntity<?> comments(@PathVariable long videoId) {

    List<Object[]> queryAllRatingsForVideo = services.video().AllRatingsForVideo(videoId);
    List<RatingViewApi> ratingViewApis = queryAllRatingsForVideo.stream()
      .map(objectArray -> new RatingViewApi(objectArray))
      .collect(Collectors.toList());

    return new ResponseEntity<>(ratingViewApis, HttpStatus.OK);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RATINGS, method = RequestMethod.POST, headers = {"content-type=application/json"})
  public VideoResponseApi updateVideo(@PathVariable long videoId, @RequestBody RatingCreationViewApi ratingCreationViewApi, HttpServletResponse response, Principal principal) {
    VideoResponseApi videoResponseApi = new VideoResponseApi();

    User user = services.user().withUserName(principal.getName());
    Videos videos = services.video().forUser(user.id);

    boolean isVideoBellowToMe = videos.stream().anyMatch(video -> video.id == videoId);
    if (isVideoBellowToMe) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      videoResponseApi.errorMessage = messageByLocaleService.getMessage("can_rate_on_video_below_to_you");
      return videoResponseApi;
    }


    if (ratingCreationViewApi.getRating() != null) {
      Rating rating = ratingCreationViewApi.getRating();
      if (rating.equals(Rating.Negative) || (rating.equals(Rating.Positive))) {
        services.video().createVideoRating(videoId, user.id, ratingCreationViewApi.getRating());
      } else {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        videoResponseApi.errorMessage = messageByLocaleService.getMessage("rate_value_not_define");
        return videoResponseApi;
      }
    }

    response.setStatus(HttpServletResponse.SC_OK);
    return videoResponseApi;
  }

}
