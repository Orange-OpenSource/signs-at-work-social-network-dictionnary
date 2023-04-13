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

import com.orange.signsatwork.biz.domain.Communities;
import com.orange.signsatwork.biz.domain.Favorite;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.domain.Users;
import com.orange.signsatwork.biz.persistence.model.CommunityViewData;
import com.orange.signsatwork.biz.persistence.model.SignViewData;
import com.orange.signsatwork.biz.persistence.model.VideoViewData;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.security.ClearXss;
import com.orange.signsatwork.biz.view.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
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

  @Autowired
  private Environment environment;

  @Value("${app.name}")
  String appName;

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorite/create", method = RequestMethod.POST)
  public String createFavorite(@ModelAttribute FavoriteCreationView favoriteCreationView, Principal principal) {
    User user = services.user().withUserName(principal.getName());
    favoriteCreationView.clearXss();
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
      return("redirect:/sec/favorites");
    }

    model.addAttribute("title", favorite.favoriteName());
    model.addAttribute("backUrl", "/");
    model.addAttribute("favoriteManageView", favorite);

    List<Object[]> queryVideos = services.video().VideosForFavoriteView(favoriteId, user.id);
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
    model.addAttribute("shareNumber", services.community().forFavorite(favorite.id).stream().count());

    if (favorite.user.id != user.id) {
      favorite = favorite.loadUsers();
      if (!favorite.users.list().contains(user.id)) {
        services.favorite().addUserOpenFavoritePage(favoriteId, user.id);
      }
    }
    model.addAttribute("appName", appName);

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
  public String manageFavorite(@PathVariable long favoriteId, Model model, Principal principal)  {
    Boolean isFavoriteBelowToMe = true;
    String name = null;
    User user = services.user().withUserName(principal.getName());

    Favorite favorite = services.favorite().withId(favoriteId);
    if (favorite == null) {
      return("redirect:/");
    }

    model.addAttribute("title", favorite.favoriteName());
    model.addAttribute("backUrl", "/sec/favorite/" + favoriteId);

    model.addAttribute("favoriteManageView", favorite);
    model.addAttribute("favoriteCreationView", new FavoriteCreationView());
    if (favorite.user.id != user.id) {
      name = favorite.user.name();
      isFavoriteBelowToMe = false;
    }
    model.addAttribute("userName", name);
    model.addAttribute("isFavoriteBelowToMe", isFavoriteBelowToMe);
    model.addAttribute("appName", appName);

    return "manage-favorite";
  }

  private String showFavorite(long favoriteId) {
    return "redirect:/sec/favorite/" + favoriteId;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorite/{favoriteId}/associate-sign")
  public String associateSign(@PathVariable long favoriteId, Principal principal, Model model)  {
    User user = services.user().withUserName(principal.getName());
    Favorite favorite = services.favorite().withId(favoriteId);
    if (favorite == null) {
      return("redirect:/");
    }


    favorite = favorite.loadVideos();

    if (favorite.videos.list().size() > 0) {
      model.addAttribute("title", messageByLocaleService.getMessage("favorite.choose_sign"));
      model.addAttribute("subtitle", messageByLocaleService.getMessage("favorite.modified"));
    } else {
      model.addAttribute("title", messageByLocaleService.getMessage("favorite.add_sign"));
      model.addAttribute("subtitle", messageByLocaleService.getMessage("favorite.confirm_sign_add_to_list", new Object[]{favorite.favoriteName()}));
    }

    model.addAttribute("backUrl", "/sec/favorite/" + favoriteId);
    FavoriteProfileView favoriteProfileView = new FavoriteProfileView(favorite);
    model.addAttribute("favoriteProfileView", favoriteProfileView);

    List<Object[]> querySigns = services.sign().AllVideosForAllSigns(user.id);
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
    model.addAttribute("appName", appName);

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

    model.addAttribute("title", favorite.favoriteName());
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

    model.addAttribute("title", favorite.favoriteName());
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

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorite/share")
  public String shareFavorite(@RequestParam("id")  long favoriteId, @RequestParam("communityId") long communityId, Principal principal, Model model)  {
    User user = services.user().withUserName(principal.getName());
    Favorite favorite = services.favorite().withId(favoriteId);
    if (favorite == null) {
      return("redirect:/");
    }
    favorite = favorite.loadCommunities();
    if (communityId != 0) {
      favorite = favorite.addCommunity(communityId);
    }
    model.addAttribute("title", messageByLocaleService.getMessage("favorite.share_with"));
    model.addAttribute("backUrl", "/sec/favorite/" + favoriteId);
    FavoriteProfileView favoriteProfileView = new FavoriteProfileView(favorite);
    model.addAttribute("favoriteProfileView", favoriteProfileView);

    List<Object[]> queryCommunities = services.community().allForFavorite(user.id);
    List<CommunityViewData> communitiesViewData = queryCommunities.stream()
      .map(objectArray -> new CommunityViewData(objectArray))
      .collect(Collectors.toList());
    communitiesViewData = communitiesViewData.stream().sorted((c1, c2) -> c1.name.compareTo(c2.name)).collect(Collectors.toList());
    model.addAttribute("communities", communitiesViewData);
    model.addAttribute("communityCreationView", new CommunityCreationView());
    model.addAttribute("communityId", communityId);
    model.addAttribute("appName", appName);

    return "favorite-share";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorite/{favoriteId}/add/communities", method = RequestMethod.POST)
  public String changeFavoriteCommunities(
    HttpServletRequest req, @PathVariable long favoriteId, Model model, Principal principal) {

    User user = services.user().withUserName(principal.getName());
    List<Long> communitiesIds =
      transformCommunitiesIdsToLong(req.getParameterMap().get("favoriteCommunitiesIds"));

    services.favorite().changeFavoriteCommunities(favoriteId, communitiesIds, user.name(), getAppUrl(), req.getLocale());

    return showFavorite(favoriteId);
  }

  private String getAppUrl() {
    return environment.getProperty("app.url");
  }

  private List<Long> transformCommunitiesIdsToLong(String[] favoriteCommunitiesIds) {
    if (favoriteCommunitiesIds == null) {
      return new ArrayList<>();
    }
    return Arrays.asList(favoriteCommunitiesIds).stream()
      .map(Long::parseLong)
      .collect(Collectors.toList());
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorite/create_community")
  public String createCommunity(@RequestParam("name") String name, @RequestParam("id") long favoriteId, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());
    String decodeName = URLDecoder.decode(name);
    model.addAttribute("backUrl", "/sec/favorite/share/?id=" + favoriteId +"&communityId=0");
    model.addAttribute("communityName", decodeName.trim());
    model.addAttribute("communityProfileView", new CommunityProfileView());
    Users users = services.user().allForCreateCommunity();
    List<User> usersWithoutMeAndWithoutAdmin = users.stream().filter(u -> u.id != user.id).filter(u-> u.id != 1).collect(Collectors.toList());
    model.addAttribute("users", usersWithoutMeAndWithoutAdmin);
    model.addAttribute("favoriteId", favoriteId);
    model.addAttribute("appName", appName);

    return "favorite-create-community";
    }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorites")
  public String favorites(Principal principal, Model model)  {
    User user = services.user().withUserName(principal.getName());
    fillModelWithFavorites(model, user);
    model.addAttribute("title", messageByLocaleService.getMessage("favorites"));
    model.addAttribute("favoriteCreationView", new FavoriteCreationView());
    model.addAttribute("appName", appName);
    return "favorites";
  }

  private void fillModelWithFavorites(Model model, User user) {
    List<FavoriteModalView> favorites = new ArrayList<>();
    List<FavoriteModalView> newFavoritesShareToMe = FavoriteModalView.fromNewShare(services.favorite().newFavoritesShareToUser(user.id));
    favorites.addAll(newFavoritesShareToMe);

    List<FavoriteModalView> favoritesAlpha = new ArrayList<>();
    List<FavoriteModalView> oldFavoritesShareToMe = FavoriteModalView.from(services.favorite().oldFavoritesShareToUser(user.id));
    favoritesAlpha.addAll(oldFavoritesShareToMe);
    List<FavoriteModalView> myFavorites = FavoriteModalView.from(services.favorite().favoritesforUser(user.id));
    favoritesAlpha.addAll(myFavorites);
    favoritesAlpha = favoritesAlpha.stream().sorted((f1, f2) -> f1.getName().compareTo(f2.getName())).collect(Collectors.toList());
    favorites.addAll(favoritesAlpha);

    model.addAttribute("myFavorites", favorites);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorite/{favoriteId}/communities")
  public String favoritesCommunities(@PathVariable long favoriteId, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());
    Favorite favorite = services.favorite().withId(favoriteId);
    if (favorite == null) {
      return("redirect:/");
    }
    favorite = favorite.loadCommunities();
    FavoriteProfileView favoriteProfileView = new FavoriteProfileView(favorite);
    model.addAttribute("favoriteProfileView", favoriteProfileView);
    List<Object[]> queryCommunities = services.community().allForFavorite(user.id);
    Favorite finalFavorite = favorite;
    List<CommunityViewData> communitiesViewData = queryCommunities.stream()
      .map(objectArray -> new CommunityViewData(objectArray))
      .filter(c -> finalFavorite.communities.ids().contains(c.id))
      .sorted((c1, c2) -> c1.name.compareTo(c2.name))
      .collect(Collectors.toList());

    model.addAttribute("title", messageByLocaleService.getMessage("favorite.see_share_communities"));
    model.addAttribute("communities", communitiesViewData);
    model.addAttribute("appName", appName);
    return "favorite-communities";
  }
}
