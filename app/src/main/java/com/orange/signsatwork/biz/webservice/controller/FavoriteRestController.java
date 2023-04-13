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
import com.orange.signsatwork.biz.persistence.model.CommunityViewData;
import com.orange.signsatwork.biz.persistence.model.VideoViewData;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.view.model.*;
import com.orange.signsatwork.biz.webservice.model.*;
import com.orange.signsatwork.biz.webservice.model.SignView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Types that carry this annotation are treated as controllers where @RequestMapping
 * methods assume @ResponseBody semantics by default, ie return json body.
 */
@Slf4j
@RestController
/** Rest controller: returns a json body */
public class FavoriteRestController {

  @Autowired
  Services services;

  @Autowired
  MessageByLocaleService messageByLocaleService;

  @Autowired
  private Environment environment;

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_FAVORITE_VIDEO_ASSOCIATE, method = RequestMethod.POST)
  public String favoriteAssociateVideo(@RequestBody List<Long> favoriteVideosIds, @PathVariable long favoriteId, HttpServletResponse response) {

    services.favorite().changeFavoriteVideos(favoriteId, favoriteVideosIds);

    response.setStatus(HttpServletResponse.SC_OK);
    return "/sec/favorite/" + favoriteId;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_FAVORITE_COMMUNITY_ASSOCIATE, method = RequestMethod.POST)
  public String favoriteAssociateCommunity(@RequestBody List<Long> favoriteCommunitiesIds, @PathVariable long favoriteId, Principal principal, HttpServletResponse response, HttpServletRequest request) {

    User user = services.user().withUserName(principal.getName());
    Favorite favorite = services.favorite().changeFavoriteCommunities(favoriteId, favoriteCommunitiesIds, user.name(), getAppUrl(), request.getLocale());
    favorite = favorite.loadCommunities();
    response.setStatus(HttpServletResponse.SC_OK);
    List<String> communitiesName = favorite.communities.stream().map(c -> c.name).collect(Collectors.toList());

    return messageByLocaleService.getMessage("favorite.confirm_share_to_community",  new Object[]{favorite.favoriteName(), communitiesName.toString()});
  }

  private String getAppUrl() {
    return environment.getProperty("app.url");
  }

  /** API REST For Android and IOS **/
  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_MY_FAVORITES)
  public ResponseEntity<?> myFavorites(@RequestParam("name") Optional<String> name, Principal principal) {

    User user = services.user().withUserName(principal.getName());

    List<FavoriteViewApi> myFavorites = FavoriteViewApi.from(services.favorite().favoritesforUser(user.id));
    List<FavoriteViewApi> myFavoritesFilter = myFavorites;

    if (name.isPresent()) {
      myFavoritesFilter = myFavorites.stream().filter(m -> m.getName().equals(name.get())).collect(Collectors.toList());
    }

    return  new ResponseEntity<>(myFavoritesFilter, HttpStatus.OK);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_FAVORITES)
  public ResponseEntity<?> favorites(Principal principal) {

    User user = services.user().withUserName(principal.getName());

    List<FavoriteViewApi> myFavorites = fillModelWithFavorites(user);

    return  new ResponseEntity<>(myFavorites, HttpStatus.OK);
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi. WS_SEC_VIDEO_FAVORITES)
  public ResponseEntity<?> favoritesHaveVideo(@PathVariable long videoId, Principal principal)  {

    User user = services.user().withUserName(principal.getName());
    List<FavoriteViewApi> myFavorites = fillModelWithFavorites(user, videoId);

    return  new ResponseEntity<>(myFavorites, HttpStatus.OK);
  }

  private List<FavoriteViewApi> fillModelWithFavorites(User user) {
    List<FavoriteViewApi> favorites = new ArrayList<>();
    List<FavoriteViewApi> newFavoritesShareToMe = FavoriteViewApi.fromNewShare(services.favorite().newFavoritesShareToUser(user.id));
    favorites.addAll(newFavoritesShareToMe);

    List<FavoriteViewApi> favoritesAlpha = new ArrayList<>();
    List<FavoriteViewApi> oldFavoritesShareToMe = FavoriteViewApi.from(services.favorite().oldFavoritesShareToUser(user.id));
    favoritesAlpha.addAll(oldFavoritesShareToMe);
    List<FavoriteViewApi> myFavorites = FavoriteViewApi.from(services.favorite().favoritesforUser(user.id));
    favoritesAlpha.addAll(myFavorites);
    favoritesAlpha = favoritesAlpha.stream().sorted((f1, f2) -> f1.getName().compareTo(f2.getName())).collect(Collectors.toList());
    favorites.addAll(favoritesAlpha);

    return favorites;
  }

  private List<FavoriteViewApi> fillModelWithFavorites(User user, Long videoId) {
    List<Long> favoritesIdBelowVideo = new ArrayList<>();
    List<FavoriteViewApi> favoritesBelowVideo = new ArrayList<>();
    if (user != null) {
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

      favoritesIdBelowVideo = Arrays.asList(services.favorite().FavoriteIdsBelowVideoId(videoId, favorites.stream().map(f -> f.getId()).collect(Collectors.toList())));

      List<Long> finalFavoritesIdBelowVideo = favoritesIdBelowVideo;
      favoritesBelowVideo = favorites.stream().filter(f -> finalFavoritesIdBelowVideo.contains(f.getId())).map(f ->FavoriteViewApi.fromFavoriteModalView(f)).collect(Collectors.toList());

    }
    return favoritesBelowVideo;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_FAVORITES_FOR_FILTER)
  public ResponseEntity<?> favoritesForFilter(Principal principal) {

    User user = services.user().withUserName(principal.getName());

    List<FavoriteViewApi> myFavorites = fillModelWithFavoritesForSignFilter(user);

    return  new ResponseEntity<>(myFavorites, HttpStatus.OK);
  }

  private List<FavoriteViewApi> fillModelWithFavoritesForSignFilter(User user) {
    List<FavoriteViewApi> favorites = new ArrayList<>();

    if (user != null) {
      List<FavoriteViewApi> newFavoritesShareToMe = FavoriteViewApi.fromNewShare(services.favorite().newFavoritesShareToUserForSignFilter(user.id));
      favorites.addAll(newFavoritesShareToMe);

      List<FavoriteViewApi> favoritesAlpha = new ArrayList<>();
      List<FavoriteViewApi> oldFavoritesShareToMe = FavoriteViewApi.from(services.favorite().oldFavoritesShareToUserForSignFilter(user.id));
      favoritesAlpha.addAll(oldFavoritesShareToMe);
      List<FavoriteViewApi> myFavorites = FavoriteViewApi.from(services.favorite().favoritesforUserForSignFilter(user.id));
      favoritesAlpha.addAll(myFavorites);
      favoritesAlpha = favoritesAlpha.stream().sorted((f1, f2) -> f1.getName().compareTo(f2.getName())).collect(Collectors.toList());
      favorites.addAll(favoritesAlpha);
    }
    return favorites;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_FAVORITE)
  public ResponseEntity<?> favorite(@PathVariable long favoriteId, Principal principal) {

    String messageError;
    User user = services.user().withUserName(principal.getName());
    Favorite favorite = services.favorite().withId(favoriteId);

    if (favorite.type == FavoriteType.Individual) {
      List<FavoriteViewApi> myFavorites = FavoriteViewApi.from(services.favorite().favoritesforUser(user.id));
      boolean isFavoriteBelowToMe = myFavorites.stream().anyMatch(favoriteModalView -> favoriteModalView.getId() == favoriteId);
      if (!isFavoriteBelowToMe) {
        messageError = messageByLocaleService.getMessage("favorite_not_below_to_you");
        return new ResponseEntity<>(messageError, HttpStatus.FORBIDDEN);
      }
    }
    favorite = favorite.loadCommunities();

    FavoriteViewApi myFavorite = FavoriteViewApi.fromShare(favorite);

    if (favorite.user.id != user.id) {
      favorite = favorite.loadUsers();
      if (!favorite.users.list().contains(user.id)) {
        services.favorite().addUserOpenFavoritePage(favoriteId, user.id);
      }
    }

    return  new ResponseEntity<>(myFavorite, HttpStatus.OK);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_FAVORITES_VIDEOS)
  public ResponseEntity<?> videosFavorite(@PathVariable long favoriteId, Principal principal) {

    String messageError;
    User user = services.user().withUserName(principal.getName());
    Favorite favorite = services.favorite().withId(favoriteId);
    if (favorite.type == FavoriteType.Individual) {
      List<FavoriteViewApi> myFavorites = FavoriteViewApi.from(services.favorite().favoritesforUser(user.id));
      boolean isFavoriteBelowToMe = myFavorites.stream().anyMatch(favoriteModalView -> favoriteModalView.getId() == favoriteId);
      if (!isFavoriteBelowToMe) {
        messageError = messageByLocaleService.getMessage("favorite_not_below_to_you");
        return new ResponseEntity<>(messageError, HttpStatus.FORBIDDEN);
      }
    }

    List<Object[]> queryVideos = services.video().VideosForFavoriteView(favoriteId, user.id);
    List<VideoViewData> videoViewsData = queryVideos.stream()
      .map(objectArray -> new VideoViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> videoWithCommentList = Arrays.asList(services.favorite().NbCommentForAllVideoByFavorite(favoriteId));


    List<VideoView2>  videoViews = videoViewsData.stream()
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


    return  new ResponseEntity<>(videoViews, HttpStatus.OK);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_FAVORITES_COMMUNITIES)
  public ResponseEntity<?> communitiesFavorite(@PathVariable long favoriteId, Principal principal) {

    String messageError;
    User user = services.user().withUserName(principal.getName());
    Favorite favorite = services.favorite().withId(favoriteId);
    if (favorite.type == FavoriteType.Individual) {
      List<FavoriteViewApi> myFavorites = FavoriteViewApi.from(services.favorite().favoritesforUser(user.id));
      boolean isFavoriteBelowToMe = myFavorites.stream().anyMatch(favoriteModalView -> favoriteModalView.getId() == favoriteId);
      if (!isFavoriteBelowToMe) {
        messageError = messageByLocaleService.getMessage("favorite_not_below_to_you");
        return new ResponseEntity<>(messageError, HttpStatus.FORBIDDEN);
      }
    }

    favorite = favorite.loadCommunities();
    List<Object[]> queryCommunities = services.community().allForFavorite(user.id);
    List<CommunityViewData> communitiesViewData = queryCommunities.stream()
      .map(objectArray -> new CommunityViewData(objectArray))
      .collect(Collectors.toList());
    Favorite finalFavorite = favorite;
    communitiesViewData = communitiesViewData.stream()
      .filter(c -> finalFavorite.communitiesIds().contains(c.id))
      .sorted((c1, c2) -> c1.name.compareTo(c2.name)).collect(Collectors.toList());

    return  new ResponseEntity<>(communitiesViewData, HttpStatus.OK);
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_FAVORITE, method = RequestMethod.DELETE)
  public FavoriteResponseApi deleteFavorite(@PathVariable long favoriteId, HttpServletResponse response, Principal principal)  {

    FavoriteResponseApi favoriteResponseApi = new FavoriteResponseApi();
    User user = services.user().withUserName(principal.getName());
    List<FavoriteViewApi> myFavorites = FavoriteViewApi.from(services.favorite().favoritesforUser(user.id));

    boolean isFavoriteBelowToMe = myFavorites.stream().anyMatch(favoriteModalView -> favoriteModalView.getId() == favoriteId);
    if (!isFavoriteBelowToMe) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      favoriteResponseApi.errorMessage = messageByLocaleService.getMessage("favorite_not_below_to_you");
      return favoriteResponseApi;
    }

    Favorite favorite = services.favorite().withId(favoriteId);
    services.favorite().delete(favorite);

    response.setStatus(HttpServletResponse.SC_OK);
    return favoriteResponseApi;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_FAVORITES, method = RequestMethod.POST)
  public FavoriteResponseApi createFavorite(@RequestBody FavoriteCreationViewApi favoriteCreationViewApi, HttpServletResponse response, Principal principal) {
    FavoriteResponseApi favoriteResponseApi = new FavoriteResponseApi();
    User user = services.user().withUserName(principal.getName());

    favoriteCreationViewApi.clearXss();
    Favorite favorite = services.favorite().create(user.id, favoriteCreationViewApi.getName());

    if (favoriteCreationViewApi.getVideoIdToAdd() != null) {
      services.favorite().changeFavoriteVideos(favorite.id, java.util.Arrays.asList(favoriteCreationViewApi.getVideoIdToAdd()));
    }

    response.setStatus(HttpServletResponse.SC_OK);
    favoriteResponseApi.favoriteId = favorite.id;
    return favoriteResponseApi;

  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_FAVORITE, method = RequestMethod.PUT)
  public FavoriteResponseApi updateFavorite(@RequestBody FavoriteCreationViewApi favoriteCreationViewApi, @PathVariable long favoriteId, HttpServletResponse response, HttpServletRequest request, Principal principal) {

    FavoriteResponseApi favoriteResponseApi = new FavoriteResponseApi();
    favoriteResponseApi.favoriteId = favoriteId;
    User user = services.user().withUserName(principal.getName());


    Favorite favorite = services.favorite().withId(favoriteId);
    favoriteCreationViewApi.clearXss();
    if (favoriteCreationViewApi.getName() != null) {
      List<FavoriteViewApi> myFavorites = FavoriteViewApi.from(services.favorite().favoritesforUser(user.id));

      boolean isFavoriteBelowToMe = myFavorites.stream().anyMatch(favoriteModalView -> favoriteModalView.getId() == favoriteId);
      if (!isFavoriteBelowToMe) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        favoriteResponseApi.errorMessage = messageByLocaleService.getMessage("favorite_not_below_to_you");
        return favoriteResponseApi;
      }

      if (!favorite.name.equals(favoriteCreationViewApi.getName())) {
        Long maxIdForName = services.favorite().maxIdForName(favoriteCreationViewApi.getName().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE"), favoriteId);
        if (maxIdForName != null) {
            favoriteResponseApi.errorMessage = messageByLocaleService.getMessage("favorite.name_already_exist");
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return favoriteResponseApi;
        }
        services.favorite().updateName(favoriteId, favoriteCreationViewApi.getName());
        favoriteResponseApi.errorMessage = messageByLocaleService.getMessage("favorite.renamed", new Object[]{favorite.name, favoriteCreationViewApi.getName()});
      } else {
        favoriteResponseApi.errorMessage = messageByLocaleService.getMessage("favorite.name_already_exist");
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        return favoriteResponseApi;
      }
    }

    if (favoriteCreationViewApi.getVideosIds() != null) {
      services.favorite().changeFavoriteVideos(favorite.id, favoriteCreationViewApi.getVideosIds());
    } else {
      if (favoriteCreationViewApi.getVideoIdToAdd() != null) {
        Long videoIdToAdd = favoriteCreationViewApi.getVideoIdToAdd();
        favorite = favorite.loadVideos();
        List<Long> videosIds = favorite.videosIds();
        if (!videosIds.contains(videoIdToAdd)) {
          videosIds.add(videoIdToAdd);
          services.favorite().changeFavoriteVideos(favorite.id, videosIds);
        }
      } else {
        if (favoriteCreationViewApi.getCommunitiesIds() != null) {
          services.favorite().changeFavoriteCommunities(favorite.id, favoriteCreationViewApi.getCommunitiesIds(), user.name(), getAppUrl(), request.getLocale());
        }
      }
    }

    response.setStatus(HttpServletResponse.SC_OK);
    return favoriteResponseApi;

  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_FAVORITE_DUPLICATE, method = RequestMethod.POST)
  public FavoriteResponseApi duplicateFavorite(@RequestBody FavoriteCreationViewApi favoriteCreationViewApi, @PathVariable long favoriteId, HttpServletResponse response, Principal principal) {

    FavoriteResponseApi favoriteResponseApi = new FavoriteResponseApi();
    User user = services.user().withUserName(principal.getName());

    favoriteCreationViewApi.clearXss();
    Favorite favorite = services.favorite().withId(favoriteId);
    if (favorite.type == FavoriteType.Individual) {
      List<FavoriteViewApi> myFavorites = FavoriteViewApi.from(services.favorite().favoritesforUser(user.id));
      boolean isFavoriteBelowToMe = myFavorites.stream().anyMatch(favoriteModalView -> favoriteModalView.getId() == favoriteId);
      if (!isFavoriteBelowToMe) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        favoriteResponseApi.errorMessage = messageByLocaleService.getMessage("favorite_not_below_to_you");
        return favoriteResponseApi;
      }
    }

    Long maxIdForName = services.favorite().maxIdForName(favoriteCreationViewApi.getName().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE"), favoriteId);
    if (maxIdForName != null) {
      favoriteResponseApi.errorMessage = messageByLocaleService.getMessage("favorite.name_already_exist");
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      return favoriteResponseApi;
    }


    favorite = favorite.loadVideos();
    Favorite duplicateFavorite = services.favorite().create(user.id, favoriteCreationViewApi.getName());
    if (favorite.videos != null) {
      services.favorite().changeFavoriteVideos(duplicateFavorite.id, favorite.videosIds());
    }
    favoriteResponseApi.favoriteId = duplicateFavorite.id;
    favoriteResponseApi.errorMessage = messageByLocaleService.getMessage("favorite.duplicated", new Object[]{favorite.name, favoriteCreationViewApi.getName()});

    response.setStatus(HttpServletResponse.SC_OK);
    return favoriteResponseApi;

  }

}
