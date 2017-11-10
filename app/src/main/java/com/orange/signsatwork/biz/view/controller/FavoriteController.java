package com.orange.signsatwork.biz.view.controller;

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

import com.orange.signsatwork.biz.domain.Favorite;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.persistence.model.SignViewData;
import com.orange.signsatwork.biz.persistence.model.VideoViewData;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.view.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class FavoriteController {

  private static final String HOME_URL = "/";
  private static final String REQUEST_URL = "/sec/favorites";

  @Autowired
  private Services services;

  @Autowired
  MessageByLocaleService messageByLocaleService;

  @RequestMapping(value = REQUEST_URL)
  public String favorite(Principal principal, Model model) {

    return "favorites";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorite/create", method = RequestMethod.POST)
  public String createFavorite(@ModelAttribute FavoriteCreationView favoriteCreationView, Principal principal) {
    User user = services.user().withUserName(principal.getName());
    Favorite favorite = services.favorite().create(user.id, favoriteCreationView.getFavoriteName());

    log.info("createFavorite: username = {} / favorite name = {}", user.username, favoriteCreationView.getFavoriteName());

    return showFavorite(favorite.id);

  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorite/{favoriteId}")
  public String favorite(@PathVariable long favoriteId, Principal principal, Model model)  {
    User user = services.user().withUserName(principal.getName());

    Favorite favorite = services.favorite().withId(favoriteId);
    if (favorite == null) {
      return("redirect:/");
    }

    model.addAttribute("title", favorite.name);
    model.addAttribute("backUrl", "/");
    model.addAttribute("favoriteManageView", favorite);

    List<Object[]> queryVideos = services.video().VideosForFavoriteView(favoriteId);
    List<VideoViewData> videoViewsData = queryVideos.stream()
      .map(objectArray -> new VideoViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> videoWithCommentList = Arrays.asList(services.favorite().NbCommentForAllVideoByFavorite(favoriteId));


    List<VideoView2> videoViews = videoViewsData.stream()
      .map(videoViewData -> new VideoView2(
        videoViewData,
        videoWithCommentList.contains(videoViewData.videoId),
        VideoView2.createdAfterLastDeconnection(videoViewData.createDate, user == null ? null : user.lastDeconnectionDate),
        videoViewData.nbView > 0,
        videoViewData.averageRate > 0,
        true))
      .collect(Collectors.toList());


    VideosViewSort videosViewSort = new VideosViewSort();
    videoViews = videosViewSort.sort(videoViews);

    model.addAttribute("videosView", videoViews);

    return "favorite";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorite/{favoriteId}/rename")
  public String renameFavorite(@PathVariable long favoriteId, @ModelAttribute FavoriteCreationView favoriteCreationView)  {
    Favorite favorite = services.favorite().updateName(favoriteId, favoriteCreationView.getFavoriteName());

    return showFavorite(favorite.id);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorite/{favoriteId}/delete")
  public String deleteFavorite(@PathVariable long favoriteId)  {
    Favorite favorite = services.favorite().withId(favoriteId);
    services.favorite().delete(favorite);

    return "redirect:/";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorite/{favoriteId}/duplicate")
  public String duplicateFavorite(@PathVariable long favoriteId, Principal principal)  {
    User user = services.user().withUserName(principal.getName());
    Favorite favorite = services.favorite().withId(favoriteId);
    Favorite duplicateFavorite = services.favorite().create(user.id, favorite.name);
    favorite = favorite.loadVideos();
    if (favorite.videos != null) {
      services.favorite().changeFavoriteVideos(duplicateFavorite.id, favorite.videosIds());
    }

    return showFavorite(duplicateFavorite.id);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorite/{favoriteId}/manage-favorite")
  public String manageFavorite(@PathVariable long favoriteId, Model model)  {

    Favorite favorite = services.favorite().withId(favoriteId);
    if (favorite == null) {
      return("redirect:/");
    }
    model.addAttribute("title", favorite.name);
    model.addAttribute("backUrl", "/sec/favorite/" + favoriteId);

    model.addAttribute("favoriteManageView", favorite);
    model.addAttribute("favoriteCreationView", new FavoriteCreationView());


    return "manage-favorite";
  }

  private String showFavorite(long favoriteId) {
    return "redirect:/sec/favorite/" + favoriteId;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorite/{favoriteId}/associate-sign")
  public String associateSign(@PathVariable long favoriteId, Model model)  {
    Favorite favorite = services.favorite().withId(favoriteId);
    if (favorite == null) {
      return("redirect:/");
    }
    model.addAttribute("title", favorite.name);
    model.addAttribute("backUrl", "/sec/favorite/" + favoriteId);
    FavoriteProfileView favoriteProfileView = new FavoriteProfileView(favorite);
    model.addAttribute("favoriteProfileView", favoriteProfileView);

    List<Object[]> querySigns = services.sign().AllVideosForAllSigns();
    List<VideoViewData> videoViewsData = querySigns.stream()
      .map(objectArray -> new VideoViewData(objectArray))
      .collect(Collectors.toList());

    List<VideoViewData> videoInFavorite = videoViewsData.stream()
      .filter(v -> favoriteProfileView.getFavoriteVideosIds().contains(v.videoId))
      .collect(Collectors.toList());

    videoViewsData.removeAll(videoInFavorite);

    List<VideoViewData> sortedVideos = new ArrayList<>();
    sortedVideos.addAll(videoInFavorite);
    sortedVideos.addAll(videoViewsData);



    model.addAttribute("videosView", sortedVideos);

    return "favorite-associate-sign";
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorite/{favoriteId}/add/video/{videoId}")
  public String addVideon(@PathVariable long favoriteId, @PathVariable long videoId,  Model model)  {
    Favorite favorite = services.favorite().withId(favoriteId);
    favorite = favorite.loadVideos();
    List<Long> videosIds = favorite.videosIds();
    videosIds.add(videoId);
    services.favorite().changeFavoriteVideos(favorite.id, videosIds);

    model.addAttribute("title", favorite.name);
    model.addAttribute("backUrl", "/sec/favorite/" + favoriteId);
    FavoriteProfileView favoriteProfileView = new FavoriteProfileView(favorite);
    model.addAttribute("favoriteProfileView", favoriteProfileView);

    return showFavorite(favoriteId);
  }



  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorite/create_favorite_add_video/{videoId}")
  public String createAndAddVideo(@ModelAttribute FavoriteCreationView favoriteCreationView, Principal principal, @PathVariable long videoId,  Model model)  {
    User user = services.user().withUserName(principal.getName());
    Favorite favorite = services.favorite().create(user.id, favoriteCreationView.getFavoriteName());

    favorite = favorite.loadVideos();
    List<Long> videosIds = favorite.videosIds();
    videosIds.add(videoId);
    services.favorite().changeFavoriteVideos(favorite.id, videosIds);

    model.addAttribute("title", favorite.name);
    model.addAttribute("backUrl", "/sec/favorite/" + favorite.id);
    FavoriteProfileView favoriteProfileView = new FavoriteProfileView(favorite);
    model.addAttribute("favoriteProfileView", favoriteProfileView);

    return showFavorite(favorite.id);
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorite/{favoriteId}/add/videos", method = RequestMethod.POST)
  public String changeFavoriteVideos(
    HttpServletRequest req, @PathVariable long favoriteId) {

    List<Long> videosIds =
      transformVideosIdsToLong(req.getParameterMap().get("favoriteVideosIds"));

    services.favorite().changeFavoriteVideos(favoriteId, videosIds);

    return showFavorite(favoriteId);
  }

  private List<Long> transformVideosIdsToLong(String[] favoriteVideosIds) {
    if (favoriteVideosIds == null) {
      return new ArrayList<>();
    }
    return Arrays.asList(favoriteVideosIds).stream()
      .map(Long::parseLong)
      .collect(Collectors.toList());
  }
}
