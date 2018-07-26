package com.orange.signsatwork.biz.persistence.service;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
class ServicesLinker {
  @Autowired
  private CommentService commentService;
  @Autowired
  private CommunityService communityService;
  @Autowired
  private FavoriteService favoriteService;
  @Autowired
  private RatingService ratingService;
  @Autowired
  private RequestService requestService;
  @Autowired
  private SignService signService;
  @Autowired
  private UserService userService;
  @Autowired
  private VideoService videoService;
  @Autowired
  private EmailService emailService;

  @Autowired
  private Services services;

  @PostConstruct
  private void init() {
    services.community(communityService);
    services.comment(commentService);
    services.favorite(favoriteService);
    services.rating(ratingService);
    services.request(requestService);
    services.sign(signService);
    services.user(userService);
    services.video(videoService);
    services.emailService(emailService);
  }
}
