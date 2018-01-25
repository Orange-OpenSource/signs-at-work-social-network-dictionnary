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
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Slf4j
@RestController
public class RatingRestController {

  @Autowired
  private Services services;


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
}
