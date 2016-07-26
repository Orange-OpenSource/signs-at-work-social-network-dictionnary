package com.orange.signsatwork.biz.view.controller.testUI;

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

import com.orange.signsatwork.biz.domain.Comments;
import com.orange.signsatwork.biz.domain.Rating;
import com.orange.signsatwork.biz.domain.Video;
import com.orange.signsatwork.biz.persistence.service.CommentService;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.UserService;
import com.orange.signsatwork.biz.persistence.service.VideoService;
import com.orange.signsatwork.biz.view.model.AuthentModel;
import com.orange.signsatwork.biz.view.model.CommentView;
import com.orange.signsatwork.biz.view.model.RatingView;
import com.orange.signsatwork.biz.view.model.VideoView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Controller
public class VideoTestUIController {

  @Autowired
  private UserService userService;
  @Autowired
  private VideoService videoService;
  @Autowired
  private CommentService commentService;
  @Autowired
  MessageByLocaleService messageByLocaleService;

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/testUI/video/{id}")
  public String videoDetails(@PathVariable long id, Model model) {
    Video video = videoService.withId(id);

    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("video_details"));

    VideoView videoView = VideoView.from(video);
    model.addAttribute("videoView", videoView);
    model.addAttribute("commentView", new CommentView());
    model.addAttribute("ratingView", new RatingView());

    Comments comments = commentService.forVideo(video.id);
    model.addAttribute("allCommentView", comments.list());

    return "testUI/video";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/testUI/video/{videoId}/add/comment", method = RequestMethod.POST)
  public String createVideoComment(
          HttpServletRequest req, @PathVariable long videoId, Model model, Principal principal) {

    String commentText = req.getParameter("text");
    long userId = userService.withUserName(principal.getName()).id;
    videoService.createVideoComment(videoId, userId, commentText);


    return videoDetails(videoId, model);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/testUI/video/{videoId}/add/rating", method = RequestMethod.POST)
  public String createVideoRating(
          HttpServletRequest req, @PathVariable long videoId, Model model, Principal principal) {

    Rating rating = Rating.valueOf(req.getParameter("rating"));
    long userId = userService.withUserName(principal.getName()).id;
    videoService.createVideoRating(videoId, userId, rating);

    return videoDetails(videoId, model);
  }
}
