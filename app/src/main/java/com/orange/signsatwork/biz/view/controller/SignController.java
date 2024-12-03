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

import com.orange.signsatwork.biz.domain.*;
import com.orange.signsatwork.biz.persistence.model.*;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.persistence.service.SignService;
import com.orange.signsatwork.biz.persistence.service.VideoService;
import com.orange.signsatwork.biz.security.AppSecurityAdmin;
import com.orange.signsatwork.biz.view.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Principal;
import java.text.Collator;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@Controller
public class SignController {
  private static final boolean SHOW_ADD_FAVORITE = true;
  private static final boolean HIDE_ADD_FAVORITE = false;

  private static final String HOME_URL = "/";
  private static final String SIGNS_URL = "/signs";
  private static final String SIGN_URL = "/sign";


  @Autowired
  private Services services;

  @Autowired
  MessageByLocaleService messageByLocaleService;

  @Autowired
  private AppSecurityAdmin appSecurityAdmin;

  @Value("${app.name}")
  String appName;

  @Value("${app.admin.username}")
  String adminUsername;

  @RequestMapping(value = SIGNS_URL)
  public String signs(@RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {
    fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE);
    fillModelWithSigns(model, principal);
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("isAll", true);
    model.addAttribute("isMostCommented", false);
    model.addAttribute("isLowCommented", false);
    model.addAttribute("isMostRating", false);
    model.addAttribute("isLowRating", false);
    model.addAttribute("isMostViewed", false);
    model.addAttribute("isLowViewed", false);
    model.addAttribute("isMostRecent", false);
    model.addAttribute("isLowRecent", false);
    model.addAttribute("isAlphabeticAsc", false);
    model.addAttribute("isAlphabeticDesc", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("all"));
    model.addAttribute("classDropdownTitle", " all-signs_blue pull-left");
    model.addAttribute("classDropdownSize", "adjust_size btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);

    model.addAttribute("isAuthenticated", AuthentModel.isAuthenticated(principal));

    return "signs";
  }

  @RequestMapping(value = "/signs/frame")
  public String signsFrame(@RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {
    fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE);
    fillModelWithSigns(model, principal);
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("isAll", true);
    model.addAttribute("isMostCommented", false);
    model.addAttribute("isLowCommented", false);
    model.addAttribute("isMostRating", false);
    model.addAttribute("isLowRating", false);
    model.addAttribute("isMostViewed", false);
    model.addAttribute("isLowViewed", false);
    model.addAttribute("isMostRecent", false);
    model.addAttribute("isLowRecent", false);
    model.addAttribute("isAlphabeticAsc", false);
    model.addAttribute("isAlphabeticDesc", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("all"));
    model.addAttribute("classDropdownTitle", " all-signs_blue pull-left");
    model.addAttribute("classDropdownSize", "adjust_size btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);
    return "fragments/frame-signs";
  }


  @RequestMapping(value = "/signs/alphabetic")
  public String signsAndRequestInAlphabeticalOrder(@RequestParam("isAlphabeticAsc") boolean isAlphabeticAsc, @RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {

    final User user = AuthentModel.isAuthenticated(principal) ? services.user().withUserName(principal.getName()) : null;
    if (user == null && (appName.equals("Signs@Form") || appName.equals("Signs@ADIS") || appName.equals("Signs@LMB") || appName.equals("Signs@ANVOL"))) {
      return "redirect:/login";
    }

    if (isSearch) {
      fillModelWithContext(model, "sign.search", principal, SHOW_ADD_FAVORITE);
    } else {
      fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE);
    }

    List<Object[]> querySigns;

    if (isAlphabeticAsc == true) {
      querySigns = services.sign().SignsAlphabeticalOrderDescSignsView();
      model.addAttribute("isAlphabeticDesc", true);
      model.addAttribute("isAlphabeticAsc", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");

    } else {
      querySigns = services.sign().SignsAlphabeticalOrderAscSignsView();
      model.addAttribute("isAlphabeticAsc", true);
      model.addAttribute("isAlphabeticDesc", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }


    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithCommentList = Arrays.asList(services.sign().mostCommented());

    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<Long> signWithPositiveRate = Arrays.asList(services.sign().mostRating());

    List<SignView2> signViews;
    if (user != null) {
      List<Long> signInFavorite = Arrays.asList(services.sign().SignsBellowToFavoriteByUser(user.id));

      signViews = signViewsData.stream()
        .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, signInFavorite, user))
        .collect(Collectors.toList());
    } else {
      signViews = signViewsData.stream()
        .map(signViewData -> buildSignViewWithOutFavorite(signViewData, signWithCommentList, signWithView, signWithPositiveRate))
        .collect(Collectors.toList());
    }
   // List<Long> signInFavorite = Arrays.asList(services.sign().SignsBellowToFavoriteByUser(user.id));

    //List<SignView2> signViews = signViewsData.stream()
    //  .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, signInFavorite, user))
    //  .collect(Collectors.toList());


    fillModelWithFavoritesForSignFilter(model, user);
    model.addAttribute("signsView", signViews);
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("isAll", false);
    model.addAttribute("isMostCommented", false);
    model.addAttribute("isLowCommented", false);
    model.addAttribute("isMostRating", false);
    model.addAttribute("isLowRating", false);
    model.addAttribute("isMostViewed", false);
    model.addAttribute("isLowViewed", false);
    model.addAttribute("isMostRecent", false);
    model.addAttribute("isLowRecent", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("alphabetic"));
    model.addAttribute("classDropdownTitle", " sort_alpha_blue pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);
    return "signs";
  }

  @RequestMapping(value = "/signs/alphabetic/frame")
  public String signsAndRequestInAlphabeticalOrderFrame(@RequestParam("isAlphabeticAsc") boolean isAlphabeticAsc, @RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {
    fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE);
    final User user = AuthentModel.isAuthenticated(principal) ? services.user().withUserName(principal.getName()) : null;
    List<Object[]> querySigns;

    if (isAlphabeticAsc == true) {
      querySigns = services.sign().SignsAlphabeticalOrderDescSignsView();
      model.addAttribute("isAlphabeticDesc", true);
      model.addAttribute("isAlphabeticAsc", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");

    } else {
      querySigns = services.sign().SignsAlphabeticalOrderAscSignsView();
      model.addAttribute("isAlphabeticAsc", true);
      model.addAttribute("isAlphabeticDesc", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }


    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithCommentList = Arrays.asList(services.sign().mostCommented());

    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<Long> signWithPositiveRate = Arrays.asList(services.sign().mostRating());
    List<SignView2> signViews;
    if (user != null) {
      List<Long> signInFavorite = Arrays.asList(services.sign().SignsBellowToFavoriteByUser(user.id));

      signViews = signViewsData.stream()
        .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, signInFavorite, user))
        .collect(Collectors.toList());
    } else {
      signViews = signViewsData.stream()
        .map(signViewData -> buildSignViewWithOutFavorite(signViewData, signWithCommentList, signWithView, signWithPositiveRate))
        .collect(Collectors.toList());
    }


    fillModelWithFavoritesForSignFilter(model, user);
    model.addAttribute("signsView", signViews);
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("isAll", false);
    model.addAttribute("isMostCommented", false);
    model.addAttribute("isLowCommented", false);
    model.addAttribute("isMostRating", false);
    model.addAttribute("isLowRating", false);
    model.addAttribute("isMostViewed", false);
    model.addAttribute("isLowViewed", false);
    model.addAttribute("isMostRecent", false);
    model.addAttribute("isLowRecent", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("alphabetic"));
    model.addAttribute("classDropdownTitle", " sort_alpha_blue pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);
    return "fragments/frame-signs";
  }

  @RequestMapping(value = "/sec/signs/{favoriteId}")
  public String signsInFavorite(@PathVariable long favoriteId, @RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE);
    Favorite favorite = services.favorite().withId(favoriteId);
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

    fillModelWithFavoritesForSignFilter(model, user);
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("isAll", false);
    model.addAttribute("isMostCommented", false);
    model.addAttribute("isLowCommented", false);
    model.addAttribute("isMostRating", false);
    model.addAttribute("isLowRating", false);
    model.addAttribute("isMostViewed", false);
    model.addAttribute("isLowViewed", false);
    model.addAttribute("isMostRecent", false);
    model.addAttribute("isLowRecent", false);
    model.addAttribute("isAlphabeticAsc", false);
    model.addAttribute("isAlphabeticDesc", false);
    model.addAttribute("favoriteId", favoriteId);
    model.addAttribute("dropdownTitle", favorite.favoriteName());
    if (favorite.type.equals(FavoriteType.NewShare)) {
      model.addAttribute("classDropdownTitle", "pinlist_shared_new pull-left");
    } else if (favorite.type.equals(FavoriteType.Share)) {
      model.addAttribute("classDropdownTitle", "pinlist_shared pull-left");
    } else if (favorite.type.equals(FavoriteType.Individual)) {
      model.addAttribute("classDropdownTitle", "pinlist_blue pull-left");
    }
    model.addAttribute("classDropdownSize", "adjust_size btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);

    return "signs";
  }


  @RequestMapping(value = "/sec/signs/frame/{favoriteId}")
  public String signsInFavoritFrame(@PathVariable long favoriteId, @RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE);
    Favorite favorite = services.favorite().withId(favoriteId);
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

    fillModelWithFavoritesForSignFilter(model, user);
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("isAll", false);
    model.addAttribute("isMostCommented", false);
    model.addAttribute("isLowCommented", false);
    model.addAttribute("isMostRating", false);
    model.addAttribute("isLowRating", false);
    model.addAttribute("isMostViewed", false);
    model.addAttribute("isLowViewed", false);
    model.addAttribute("isMostRecent", false);
    model.addAttribute("isLowRecent", false);
    model.addAttribute("isAlphabeticAsc", false);
    model.addAttribute("isAlphabeticDesc", false);
    model.addAttribute("favoriteId", favoriteId);
    model.addAttribute("dropdownTitle", favorite.favoriteName());
    favorite = favorite.loadUsers();
    if (favorite.type.equals(FavoriteType.Share) && !favorite.users.ids().contains(user.id) && favorite.user.id != user.id) {
      model.addAttribute("classDropdownTitle", "pinlist_shared_new pull-left");
    } else if (favorite.type.equals(FavoriteType.Share)) {
      model.addAttribute("classDropdownTitle", "pinlist_shared pull-left");
    } else if (favorite.type.equals(FavoriteType.Individual)) {
      model.addAttribute("classDropdownTitle", "pinlist_blue pull-left");
    }
    model.addAttribute("classDropdownSize", "adjust_size btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);

    return "fragments/frame-signs";
  }

  //fix me !!!!! kanban 473311 suite retour tests utilisateurs
//    item supprime ihm
  @RequestMapping(value = "/sec/signs/mostcommented")
  public String signsMostCommented(@RequestParam("isMostCommented") boolean isMostCommented, @RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE);

    List<Object[]> querySigns = services.sign().SignsForSignsView();
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithCommentList;
    if (isMostCommented == true) {
      signWithCommentList = Arrays.asList(services.sign().lowCommented());
      model.addAttribute("isLowCommented", true);
      model.addAttribute("isMostCommented", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");


    } else {
      signWithCommentList = Arrays.asList(services.sign().mostCommented());
      model.addAttribute("isMostCommented", true);
      model.addAttribute("isLowCommented", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }

    List<SignViewData> commented = signViewsData.stream()
      .filter(signViewData -> signWithCommentList.contains(signViewData.id))
      .sorted(new CommentOrderComparator(signWithCommentList))
      .collect(Collectors.toList());


    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<Long> signWithPositiveRate = Arrays.asList(services.sign().mostRating());

    List<Long> signInFavorite = Arrays.asList(services.sign().SignsBellowToFavoriteByUser(user.id));


    List<SignView2> signViews = commented.stream()
      .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, signInFavorite, user))
      .collect(Collectors.toList());


    model.addAttribute("signsView", signViews);
    fillModelWithFavoritesForSignFilter(model, user);
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("isAll", false);
    model.addAttribute("isMostRating", false);
    model.addAttribute("isLowRating", false);
    model.addAttribute("isMostViewed", false);
    model.addAttribute("isLowViewed", false);
    model.addAttribute("isMostRecent", false);
    model.addAttribute("isLowRecent", false);
    model.addAttribute("isAlphabeticAsc", false);
    model.addAttribute("isAlphabeticDesc", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("most_commented"));
    model.addAttribute("classDropdownTitle", " most_active pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);
    return "signs";
  }

  @RequestMapping(value = "/sec/signs/mostrating")
  public String signsMostRating(@RequestParam("isMostRating") boolean isMostRating, @RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE);

    List<Object[]> querySigns = services.sign().SignsForSignsView();
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithRatingList;
    if (isMostRating == true) {
      signWithRatingList = Arrays.asList(services.sign().lowRating());
      model.addAttribute("isLowRating", true);
      model.addAttribute("isMostRating", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");

    } else {
      signWithRatingList = Arrays.asList(services.sign().mostRating());
      model.addAttribute("isMostRating", true);
      model.addAttribute("isLowRating", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");

    }

    List<SignViewData> rating = signViewsData.stream()
      .filter(signViewData -> signWithRatingList.contains(signViewData.id))
      .sorted(new CommentOrderComparator(signWithRatingList))
      .collect(Collectors.toList());

    List<Long> signWithCommentList = Arrays.asList(services.sign().mostCommented());

    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<Long> signWithPositiveRate = Arrays.asList(services.sign().mostRating());

    List<Long> signInFavorite = Arrays.asList(services.sign().SignsBellowToFavoriteByUser(user.id));

    List<SignView2> signViews = rating.stream()
      .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, signInFavorite, user))
      .collect(Collectors.toList());


    model.addAttribute("signsView", signViews);


    fillModelWithFavoritesForSignFilter(model, user);
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("isAll", false);
    model.addAttribute("isMostCommented", false);
    model.addAttribute("isLowCommented", false);
    model.addAttribute("isMostViewed", false);
    model.addAttribute("isLowViewed", false);
    model.addAttribute("isMostRecent", false);
    model.addAttribute("isLowRecent", false);
    model.addAttribute("isAlphabeticAsc", false);
    model.addAttribute("isAlphabeticDesc", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("most_rating"));
    model.addAttribute("classDropdownTitle", " smiley_happy_blue pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);
    return "signs";
  }

  @RequestMapping(value = "/signs/mostrating/frame")
  public String signsMostRatingFrame(@RequestParam("isMostRating") boolean isMostRating, @RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {
    final User user = AuthentModel.isAuthenticated(principal) ? services.user().withUserName(principal.getName()) : null;

    fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE);

    List<Object[]> querySigns = services.sign().SignsForSignsView();
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithRatingList;
    if (isMostRating == true) {
      signWithRatingList = Arrays.asList(services.sign().lowRating());
      model.addAttribute("isLowRating", true);
      model.addAttribute("isMostRating", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");

    } else {
      signWithRatingList = Arrays.asList(services.sign().mostRating());
      model.addAttribute("isMostRating", true);
      model.addAttribute("isLowRating", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");

    }

    List<SignViewData> rating = signViewsData.stream()
      .filter(signViewData -> signWithRatingList.contains(signViewData.id))
      .sorted(new CommentOrderComparator(signWithRatingList))
      .collect(Collectors.toList());

    List<Long> signWithCommentList = Arrays.asList(services.sign().mostCommented());

    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<Long> signWithPositiveRate = Arrays.asList(services.sign().mostRating());
    List<SignView2> signViews;
    if (user != null) {
      List<Long> signInFavorite = Arrays.asList(services.sign().SignsBellowToFavoriteByUser(user.id));

      signViews = rating.stream()
        .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, signInFavorite, user))
        .collect(Collectors.toList());
    } else {
      signViews = rating.stream()
        .map(signViewData -> buildSignViewWithOutFavorite(signViewData, signWithCommentList, signWithView, signWithPositiveRate))
        .collect(Collectors.toList());
    }


    model.addAttribute("signsView", signViews);


    fillModelWithFavoritesForSignFilter(model, user);
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("isAll", false);
    model.addAttribute("isMostCommented", false);
    model.addAttribute("isLowCommented", false);
    model.addAttribute("isMostViewed", false);
    model.addAttribute("isLowViewed", false);
    model.addAttribute("isMostRecent", false);
    model.addAttribute("isLowRecent", false);
    model.addAttribute("isAlphabeticAsc", false);
    model.addAttribute("isAlphabeticDesc", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("most_rating"));
    model.addAttribute("classDropdownTitle", " smiley_happy_blue pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);
    return "fragments/frame-signs";
  }

  //fix me !!!!! kanban 473311 suite retour tests utilisateurs
//    item supprime ihm
  @RequestMapping(value = "/sec/signs/mostviewed")
  public String signsMostViewed(@RequestParam("isMostViewed") boolean isMostViewed, @RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE);

    List<Object[]> querySigns = services.sign().SignsForSignsView();
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithViewedList;
    if (isMostViewed == true) {
      signWithViewedList = Arrays.asList(services.sign().lowViewed());
      model.addAttribute("isLowViewed", true);
      model.addAttribute("isMostViewed", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");
    } else {
      signWithViewedList = Arrays.asList(services.sign().mostViewed());
      model.addAttribute("isMostViewed", true);
      model.addAttribute("isLowViewed", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }

    List<SignViewData> viewed = signViewsData.stream()
      .filter(signViewData -> signWithViewedList.contains(signViewData.id))
      .sorted(new CommentOrderComparator(signWithViewedList))
      .collect(Collectors.toList());

    List<Long> signWithCommentList = Arrays.asList(services.sign().mostCommented());

    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<Long> signWithPositiveRate = Arrays.asList(services.sign().mostRating());

    List<Long> signInFavorite = Arrays.asList(services.sign().SignsBellowToFavoriteByUser(user.id));

    List<SignView2> signViews = viewed.stream()
      .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, signInFavorite, user))
      .collect(Collectors.toList());


    model.addAttribute("signsView", signViews);


    fillModelWithFavoritesForSignFilter(model, user);
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("isAll", false);
    model.addAttribute("isMostCommented", false);
    model.addAttribute("isLowCommented", false);
    model.addAttribute("isMostRating", false);
    model.addAttribute("isLowRating", false);
    model.addAttribute("isMostRecent", false);
    model.addAttribute("isLowRecent", false);
    model.addAttribute("isAlphabeticAsc", false);
    model.addAttribute("isAlphabeticDesc", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("most_viewed"));
    model.addAttribute("classDropdownTitle", " most_viewed pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);
    return "signs";
  }

  @RequestMapping(value = "/sec/signs/mostrecent")
  public String signsMostRecent(@RequestParam("isMostRecent") boolean isMostRecent, @RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {
    final User user = AuthentModel.isAuthenticated(principal) ? services.user().withUserName(principal.getName()) : null;
    if (user == null && (appName.equals("Signs@Form") || appName.equals("Signs@ADIS") || appName.equals("Signs@LMB") || appName.equals("Signs@ANVOL"))) {
      return "redirect:/login";
    }


    if (isSearch) {
      fillModelWithContext(model, "sign.search", principal, SHOW_ADD_FAVORITE);
    } else {
      fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE);
    }

    List<Object[]> querySigns;
    if (isMostRecent == true) {
      /*querySigns = services.sign().lowRecent(user.lastDeconnectionDate);*/
      querySigns = services.sign().lowRecentWithoutDate();
      model.addAttribute("isLowRecent", true);
      model.addAttribute("isMostRecent", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");
    } else {
     /*querySigns = services.sign().mostRecent(user.lastDeconnectionDate);*/
      querySigns = services.sign().mostRecentWithoutDate();
      model.addAttribute("isMostRecent", true);
      model.addAttribute("isLowRecent", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithCommentList = Arrays.asList(services.sign().mostCommented());

    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<Long> signWithPositiveRate = Arrays.asList(services.sign().mostRating());
    List<SignView2> signViews;
    if (user != null) {
      List<Long> signInFavorite = Arrays.asList(services.sign().SignsBellowToFavoriteByUser(user.id));

      signViews = signViewsData.stream()
        .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, signInFavorite, user))
        .collect(Collectors.toList());
    } else {
      signViews = signViewsData.stream()
        .map(signViewData -> buildSignViewWithOutFavorite(signViewData, signWithCommentList, signWithView, signWithPositiveRate))
        .collect(Collectors.toList());
    }


    model.addAttribute("signsView", signViews);


    fillModelWithFavoritesForSignFilter(model, user);
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("isAll", false);
    model.addAttribute("isMostCommented", false);
    model.addAttribute("isLowCommented", false);
    model.addAttribute("isMostRating", false);
    model.addAttribute("isLowRating", false);
    model.addAttribute("isMostViewed", false);
    model.addAttribute("isLowViewed", false);
    model.addAttribute("isAlphabeticAsc", false);
    model.addAttribute("isAlphabeticDesc", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("most_recent"));
    model.addAttribute("classDropdownTitle", "  new_blue pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);
    return "signs";
  }

  @RequestMapping(value = "/signs/mostrecent/frame")
  public String signsMostRecentFrame(@RequestParam("isMostRecent") boolean isMostRecent, @RequestParam("isSearch") boolean isSearch, Principal principal, Model model) {
    final User user = AuthentModel.isAuthenticated(principal) ? services.user().withUserName(principal.getName()) : null;

    fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE);

    List<Object[]> querySigns;
    if (isMostRecent == true) {
      /*querySigns = services.sign().lowRecent(user.lastDeconnectionDate);*/
      querySigns = services.sign().lowRecentWithoutDate();
      model.addAttribute("isLowRecent", true);
      model.addAttribute("isMostRecent", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");
    } else {
      /*querySigns = services.sign().mostRecent(user.lastDeconnectionDate);*/
      querySigns = services.sign().mostRecentWithoutDate();
      model.addAttribute("isMostRecent", true);
      model.addAttribute("isLowRecent", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithCommentList = Arrays.asList(services.sign().mostCommented());

    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<Long> signWithPositiveRate = Arrays.asList(services.sign().mostRating());
    List<SignView2> signViews;
    if (user != null) {
      List<Long> signInFavorite = Arrays.asList(services.sign().SignsBellowToFavoriteByUser(user.id));

      signViews = signViewsData.stream()
        .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, signInFavorite, user))
        .collect(Collectors.toList());
    } else {
      signViews = signViewsData.stream()
        .map(signViewData -> buildSignViewWithOutFavorite(signViewData, signWithCommentList, signWithView, signWithPositiveRate))
        .collect(Collectors.toList());
    }


    model.addAttribute("signsView", signViews);


    fillModelWithFavoritesForSignFilter(model, user);
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("isAll", false);
    model.addAttribute("isMostCommented", false);
    model.addAttribute("isLowCommented", false);
    model.addAttribute("isMostRating", false);
    model.addAttribute("isLowRating", false);
    model.addAttribute("isMostViewed", false);
    model.addAttribute("isLowViewed", false);
    model.addAttribute("isAlphabeticAsc", false);
    model.addAttribute("isAlphabeticDesc", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("most_recent"));
    model.addAttribute("classDropdownTitle", "  new_blue pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");
    model.addAttribute("isSearch", isSearch);
    model.addAttribute("appName", appName);
    return "fragments/frame-signs";
  }

  @RequestMapping(value = "/sign/{signId}")
  public String sign(@PathVariable long signId, Principal principal, Model model) {
    boolean isAdmin = appSecurityAdmin.isAdmin(principal);
    final User user = AuthentModel.isAuthenticated(principal) ? services.user().withUserName(principal.getName()) : null;


    fillModelWithContext(model, "sign.info", principal, SHOW_ADD_FAVORITE);

    List<Object[]> querySigns = services.sign().AllVideosForSign(signId);
    List<VideoViewData> videoViewsData = querySigns.stream()
      .map(objectArray -> new VideoViewData(objectArray))
      .collect(Collectors.toList());
    if (videoViewsData.size() == 0) {
      return "redirect:/";
    }

    if (videoViewsData.size() == 1) {
      return showVideo(signId, videoViewsData.get(0).videoId, isAdmin);
    } else {
      model.addAttribute("title", messageByLocaleService.getMessage("sign.all_video"));
      model.addAttribute("signName", videoViewsData.get(0).signName);


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


      model.addAttribute("videosView", videoViews);
      model.addAttribute("appName", appName);
      model.addAttribute("isAdmin", isAdmin);
      return "videos";
    }

  }

  @RequestMapping(value = "/sign/{signId}/{videoId}")
  public String video(HttpServletRequest req, @PathVariable long signId, @PathVariable long videoId, Principal principal, Model model) {
    List<Long> favoritesIdBelowVideo = new ArrayList<>();
    Boolean isVideoCreatedByMe = false;
    String referer = req.getHeader("Referer");

    model.addAttribute("videoBelowToFavorite", false);
    StringBuffer location = req.getRequestURL();

    AuthentModel.addAuthenticatedModel(model, AuthentModel.isAuthenticated(principal));
    model.addAttribute("showAddFavorite", SHOW_ADD_FAVORITE && AuthentModel.isAuthenticated(principal));
    model.addAttribute("commentCreationView", new CommentCreationView());
    model.addAttribute("favoriteCreationView", new FavoriteCreationView());


    Sign sign = services.sign().withIdSignsView(signId);
    if (sign == null) {
      return "redirect:/signs/alphabetic?isAlphabeticAsc=false&isSearch=false";
    }

    Video video = services.video().withId(videoId);
    if (video == null) {
      return "redirect:/sign/" + signId;
    }

    if (principal != null) {
      User user = services.user().withUserName(principal.getName());
      Object[] queryRating = services.video().RatingForVideoByUser(videoId, user.id);
      RatingData ratingData = new RatingData(queryRating);
      model.addAttribute("ratingData", ratingData);
      List<Object[]> queryAllComments = services.video().AllCommentsForVideo(videoId);
      List<CommentData> commentDatas = queryAllComments.stream()
        .map(objectArray -> new CommentData(objectArray))
        .collect(Collectors.toList());
      List<CommentAdminView> commentAdminViews = commentDatas.stream()
        .map(commentData ->
          new CommentAdminView(commentData.id, commentData.commentDate, commentData.text, commentData.name(), messageByLocaleService.getMessage("comment_from_user_delete_message", new Object[]{commentData.name()}), commentData.userId))
        .collect(Collectors.toList());
      model.addAttribute("commentDatas", commentAdminViews);
      model.addAttribute("title", messageByLocaleService.getMessage("card"));
      favoritesIdBelowVideo = Arrays.asList(services.video().FavoritesBelowVideoForUser(videoId, user.id));
      if (favoritesIdBelowVideo.size() >= 1) {
        model.addAttribute("videoBelowToFavorite", true);
      }
      favoritesIdBelowVideo = fillModelWithFavorites(model, user, videoId);
      if (video.user.id == user.id) {
        isVideoCreatedByMe = true;
      }
      model.addAttribute("userId", user.id);
    }

    if (video.averageRate > 0) {
      model.addAttribute("videoHasPositiveRate", true);
    } else {
      model.addAttribute("videoHasPositiveRate", false);
    }

    if (!isVideoCreatedByMe) {
      if ((referer != null) && referer.contains("detail")) {
      } else {
        services.video().increaseNbView(videoId);
      }
    }

    if ((video.idForName == 0) || (sign.nbVideo == 1)) {
      model.addAttribute("videoName", sign.name);
    } else {
      model.addAttribute("videoName", sign.name + "_" + video.idForName);
    }

    VideoProfileView2 videoProfileView = new VideoProfileView2(video, favoritesIdBelowVideo);
    model.addAttribute("signView", sign);
    model.addAttribute("videoView", videoProfileView);
    model.addAttribute("isVideoCreatedByMe", isVideoCreatedByMe);

    Long nbRating = services.sign().NbRatingForSign(signId);
    model.addAttribute("nbRating", nbRating);
    model.addAttribute("appName", appName);

    return "sign";
  }

  public static String millisToShortDHMS(long duration) {
    String res = "";    // java.util.concurrent.TimeUnit;
    long days       = TimeUnit.MILLISECONDS.toDays(duration);
    long hours      = TimeUnit.MILLISECONDS.toHours(duration) -
      TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
    long minutes    = TimeUnit.MILLISECONDS.toMinutes(duration) -
      TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration));
    long seconds    = TimeUnit.MILLISECONDS.toSeconds(duration) -
      TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
    long millis     = TimeUnit.MILLISECONDS.toMillis(duration) -
      TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(duration));

    if (days == 0)      res = String.format("%02d:%02d:%02d.%04d", hours, minutes, seconds, millis);
    else                res = String.format("%dd %02d:%02d:%02d.%04d", days, hours, minutes, seconds, millis);
    return res;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/sign/{signId}/{videoId}/detail")
  public String videoDetail(@PathVariable long signId, @PathVariable long videoId, HttpServletRequest request, Principal principal, Model model) {
    List<Long> favoritesIdBelowVideo = new ArrayList<>();
    Boolean isVideoCreatedByMe = false;
    String userAgent = request.getHeader("User-Agent");

    model.addAttribute("isIOSDevice", isIOSDevice(userAgent));

    AuthentModel.addAuthenticatedModel(model, AuthentModel.isAuthenticated(principal));
    model.addAttribute("showAddFavorite", SHOW_ADD_FAVORITE && AuthentModel.isAuthenticated(principal));
    model.addAttribute("videoBelowToFavorite", false);

    model.addAttribute("favoriteCreationView", new FavoriteCreationView());
    Sign sign = services.sign().withIdSignsView(signId);
    if (sign == null) {
      return "redirect:/";
    }
    Video video = services.video().withId(videoId);
    if (video == null) {
      return "redirect:/";
    }
    if (principal != null) {
      User user = services.user().withUserName(principal.getName());
      Object[] queryRating = services.video().RatingForVideoByUser(videoId, user.id);
      RatingData ratingData = new RatingData(queryRating);
      model.addAttribute("ratingData", ratingData);
      List<Object[]> queryAllComments = services.video().AllCommentsForVideo(videoId);
      List<CommentData> commentDatas = queryAllComments.stream()
        .map(objectArray -> new CommentData(objectArray))
        .collect(Collectors.toList());
      model.addAttribute("commentDatas", commentDatas);
      favoritesIdBelowVideo = Arrays.asList(services.video().FavoritesBelowVideoForUser(videoId, user.id));
      if (favoritesIdBelowVideo.size() >= 1) {
        model.addAttribute("videoBelowToFavorite", true);
      }
      favoritesIdBelowVideo = fillModelWithFavorites(model, user, videoId);
      if (video.user.id == user.id) {
        isVideoCreatedByMe = true;
      }
    }

    if (video.averageRate > 0) {
      model.addAttribute("videoHasPositiveRate", true);
    } else {
      model.addAttribute("videoHasPositiveRate", false);
    }

    List<Object[]> queryAllVideosHistory = services.sign().AllVideosHistoryForSign(signId);
    List<VideoHistoryData> videoHistoryDatas = queryAllVideosHistory.stream()
      .map(objectArray -> new VideoHistoryData(objectArray))
      .collect(Collectors.toList());
    model.addAttribute("videoHistoryDatas", videoHistoryDatas);
    model.addAttribute("title", messageByLocaleService.getMessage("détail"));
    if ((video.idForName == 0) || (sign.nbVideo == 1)) {
      model.addAttribute("videoName", sign.name);
    } else {
      model.addAttribute("videoName", sign.name + "_" + video.idForName);
    }

    VideoProfileView2 videoProfileView = new VideoProfileView2(video, favoritesIdBelowVideo);
    model.addAttribute("signView", sign);
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("videoView", videoProfileView);
    model.addAttribute("isVideoCreatedByMe", isVideoCreatedByMe);
    model.addAttribute("appName", appName);
    return "sign-detail";
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/sign/{signId}/{videoId}/associate", method = RequestMethod.POST)
  public String changeVideoAssociates(HttpServletRequest req, @PathVariable long signId, @PathVariable long videoId, Principal principal) {
    List<Long> associateVideosIds =
      transformAssociateVideosIdsToLong(req.getParameterMap().get("associateVideosIds"));

    services.video().changeVideoAssociates(videoId, associateVideosIds);

    log.info("Change video (id={}) associates, ids={}", videoId, associateVideosIds);

    return showSign(signId);
  }


  @Secured("ROLE_USER_A")
  @RequestMapping(value = "/sec/sign/create", method = RequestMethod.POST)
  public String createSign(@ModelAttribute SignCreationView signCreationView, Principal principal) {
    User user = services.user().withUserName(principal.getName());
    Sign sign = services.sign().create(user.id, signCreationView.getSignName(), signCreationView.getVideoUrl(), "");

    log.info("createSign: username = {} / sign name = {} / video url = {}", user.username, signCreationView.getSignName(), signCreationView.getVideoUrl());

    return showSign(sign.id);
  }

  @Secured("ROLE_USER_A")
  @RequestMapping(value = "/sec/sign/search")
  public String searchSign(@ModelAttribute SignCreationView signCreationView, @RequestParam("id") Optional<Long> requestId) {
    String name = signCreationView.getSignName();
    return "redirect:/sec/signs-suggest?name=" + URLEncoder.encode(name) + "&id=" + requestId.orElse(0L);
  }

  public static String stripAccents(String s) {
    s = Normalizer.normalize(s, Normalizer.Form.NFD);
    s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    return s;
  }

  private boolean isIOSDevice(String userAgent) {
    boolean isIOSDevice = false;
    String osType = "Unknown";
    String osVersion = "Unknown";
    String deviceType = "Unknown";

    if (userAgent.indexOf("Mac OS") >= 0) {
      osType = "Mac";
      osVersion = userAgent.substring(userAgent.indexOf("Mac OS ") + 7, userAgent.indexOf(")"));

      if (userAgent.indexOf("iPhone") >= 0) {
        deviceType = "iPhone";
        isIOSDevice = true;
      } else if (userAgent.indexOf("iPad") >= 0) {
        deviceType = "iPad";
        isIOSDevice = true;
      }
    }
    return isIOSDevice;
  }



  @Secured("ROLE_USER_A")
  @RequestMapping(value = "/sec/signs-suggest")
  public String showSignsSuggest(Model model,@RequestParam("name") String name, @RequestParam("id") Long requestId, HttpServletRequest  request, Principal principal) {
    String decodeName = URLDecoder.decode(name);
    String userAgent = request.getHeader("User-Agent");

    model.addAttribute("isIOSDevice", isIOSDevice(userAgent));
    model.addAttribute("title", messageByLocaleService.getMessage("sign.suggest"));
    AuthentModel.addAuthenticatedModel(model, AuthentModel.isAuthenticated(principal));
    User user = services.user().withUserName(principal.getName());
    List<Object[]> querySigns = services.sign().searchBis(decodeName.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE").toUpperCase());
    List<SignViewData> signViewData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    model.addAttribute("signName", decodeName.trim());
    model.addAttribute("isSignAlreadyExist", false);
    List<SignViewData> signsWithSameName = new ArrayList<>();
    for (SignViewData sign: signViewData) {
      if (sign.name.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE").equalsIgnoreCase(decodeName.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE")) ) {
        model.addAttribute("isSignAlreadyExist", true);
        model.addAttribute("signMatche", sign);
      } else {
        signsWithSameName.add(sign);
      }
    }

    model.addAttribute("signsWithSameName", signsWithSameName);

    model.addAttribute("isRequestAlreadyExist", false);
    List<Object[]> queryRequestsWithNoASsociateSign = services.request().requestsByNameWithNoAssociateSign(decodeName.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE"), user.id);
    List<RequestViewData> requestViewDatasWithNoAssociateSign =  queryRequestsWithNoASsociateSign.stream()
      .map(objectArray -> new RequestViewData(objectArray))
      .collect(Collectors.toList());
    List<RequestViewData> requestsWithNoAssociateSignWithSameName = new ArrayList<>();
    for( RequestViewData requestViewData: requestViewDatasWithNoAssociateSign) {
      if (requestViewData.requestName.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE").equalsIgnoreCase(decodeName.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE"))) {
        model.addAttribute("isRequestAlreadyExist", true);
        model.addAttribute("requestMatche", requestViewData);
      } else {
        requestsWithNoAssociateSignWithSameName.add(requestViewData);
      }
    }

    model.addAttribute("requestsWithSameName", requestsWithNoAssociateSignWithSameName);

    model.addAttribute("isRequestWithAssociateSignAlreadyExist", false);
    List<Object[]> queryRequestsWithASsociateSign = services.request().requestsByNameWithAssociateSign(decodeName.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE"), user.id);
    List<RequestViewData> requestViewDatasWithAssociateSign =  queryRequestsWithASsociateSign.stream()
      .map(objectArray -> new RequestViewData(objectArray))
      .collect(Collectors.toList());
    List<RequestViewData> requestsWithAssociateSignWithSameName = new ArrayList<>();
    for( RequestViewData requestViewData: requestViewDatasWithAssociateSign) {
      if (requestViewData.requestName.trim().replace("œ", "oe").replace("æ", "ae").equalsIgnoreCase(decodeName.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE"))) {
        model.addAttribute("isRequestWithAssociateSignAlreadyExist", true);
        model.addAttribute("requestWithAssociateSignMatche", requestViewData);
      } else {
        requestsWithAssociateSignWithSameName.add(requestViewData);
      }
    }

    model.addAttribute("requestsWithAssociateSignWithSameName", requestsWithAssociateSignWithSameName);


    SignCreationView signCreationView = new SignCreationView();
    signCreationView.setSignName(decodeName.trim());
    model.addAttribute("signCreationView", signCreationView);
    model.addAttribute("requestId", requestId);
    model.addAttribute("appName", appName);
    return "signs-suggest";
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/sign/{signId}/definition")
  public String definition(@PathVariable long signId, HttpServletRequest  request, Principal principal, Model model)  {
    Sign sign = services.sign().withId(signId);
    String userAgent = request.getHeader("User-Agent");

    model.addAttribute("isIOSDevice", isIOSDevice(userAgent));
    model.addAttribute("title", sign.name);

    AuthentModel.addAuthenticatedModel(model, AuthentModel.isAuthenticated(principal));
    model.addAttribute("showAddFavorite", HIDE_ADD_FAVORITE);

    model.addAttribute("signView", sign);
    model.addAttribute("signDefinitionCreationView", new SignDefinitionCreationView());
    model.addAttribute("appName", appName);
    model.addAttribute("modalSignDefinitionAction", "/sec/sign/" + signId + "/definitionText");

    return "my-sign-definition";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/sign/{signId}/definitionText", method = RequestMethod.POST)
  public String changeDefinitionSign(@PathVariable long signId, @ModelAttribute SignDefinitionCreationView signDefinitionCreationView) {

    signDefinitionCreationView.clearXss();
    services.sign().changeSignTextDefinition(signId, signDefinitionCreationView.getTextDefinition());

    return "redirect:/sec/sign/" + signId + "/definition";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/sign/{signId}/{videoId}/definitionText", method = RequestMethod.POST)
  public String changeDefinitionSignByAdmin(@PathVariable long signId, @PathVariable long videoId, @ModelAttribute SignDefinitionCreationView signDefinitionCreationView, HttpServletRequest requestHttp) {
    String title = null, bodyMail = null, messageType = null;
    Sign sign = services.sign().withId(signId);
    Videos videos = services.video().forSign(signId);
    signDefinitionCreationView.clearXss();
    List<String> emails = videos.stream().filter(v-> v.user.email != null).map(v -> v.user.email).collect(Collectors.toList());
    emails = emails.stream().distinct().collect(Collectors.toList());
    if (sign.textDefinition != null) {
      title = messageByLocaleService.getMessage("update_sign_definition_text_title", new Object[]{sign.name});
      bodyMail = messageByLocaleService.getMessage("update_sign_definition_text_body", new Object[]{sign.name});
      messageType = "UpdateSignDefinitionTextMessage";
    } else {
      title = messageByLocaleService.getMessage("add_sign_definition_text_title", new Object[]{sign.name});
      bodyMail = messageByLocaleService.getMessage("add_sign_definition_text_body", new Object[]{sign.name});
      messageType = "AddSignDefinitionTextMessage";
    }
    if (emails.size() != 0) {
      if (sign.textDefinition != null) {
        messageType = "UpdateSignDefinitionTextSendEmailMessage";
      } else {
        messageType = "AddSignDefinitionTextSendEmailMessage";
      }
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
      String values = adminUsername + ';' + sign.name;
      MessageServer messageServer = new MessageServer(new Date(), messageType, values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);
    }

    services.sign().changeSignTextDefinition(signId, signDefinitionCreationView.getTextDefinition());

    return "redirect:/sec/admin/sign/" + signId + "/" + videoId;
  }

  private String signUrl(long signId) {
    return "/sign/" + signId;
  }

  private String videoUrl(long signId, long videoId) {
    return "/sign/" + signId + "/" + videoId;
  }

  private String showSign(long signId) {
    return "redirect:/sign/" + signId;
  }

  private String showVideo(long signId, long videoId, boolean isAdmin) {
    if (isAdmin) {
      return "redirect:/sec/admin/sign/" + signId + "/" + videoId;
    } else {
      return "redirect:/sign/" + signId + "/" + videoId;
    }
  }

  private void fillModelWithContext(Model model, String messageEntry, Principal principal, boolean showAddFavorite) {
    model.addAttribute("title", messageByLocaleService.getMessage(messageEntry));
    AuthentModel.addAuthenticatedModel(model, AuthentModel.isAuthenticated(principal));
    model.addAttribute("showAddFavorite", showAddFavorite && AuthentModel.isAuthenticated(principal));
  }

  private void fillModelWithSigns(Model model, Principal principal) {
    final User user = AuthentModel.isAuthenticated(principal) ? services.user().withUserName(principal.getName()) : null;

    List<Object[]> querySigns = services.sign().SignsForSignsView();
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithCommentList = Arrays.asList(services.sign().mostCommented());

    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<Long> signWithPositiveRate = Arrays.asList(services.sign().mostRating());

    List<SignView2> signViews;
    List<Long> signInFavorite = null;
    if (user != null) {
      signInFavorite = Arrays.asList(services.sign().SignsBellowToFavoriteByUser(user.id));
      List<Long> finalSignInFavorite = signInFavorite;
      signViews = signViewsData.stream()
        .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, signWithPositiveRate, finalSignInFavorite, user))
        .collect(Collectors.toList());
    } else {
      signViews = signViewsData.stream()
        .map(signViewData -> new SignView2(
          signViewData,
          signWithCommentList.contains(signViewData.id),
          SignView2.createdAfterLastDeconnection(signViewData.createDate, user == null ? null : user.lastDeconnectionDate),
          signWithView.contains(signViewData.id),
          signWithPositiveRate.contains(signViewData.id),
          false)
        )
        .collect(Collectors.toList());
    }

    SignsViewSort2 signsViewSort2 = new SignsViewSort2();
    signViews = signsViewSort2.sort(signViews, false);

    fillModelWithFavoritesForSignFilter(model, user);
    model.addAttribute("signsView", signViews);
    model.addAttribute("signCreationView", new SignCreationView());
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

  private VideoView2 buildVideoView(VideoViewData videoViewData, List<Long> videoBelowToFavorite, User user) {
    return new VideoView2(
      videoViewData,
      videoViewData.nbComment > 0,
      VideoView2.createdAfterLastDeconnection(videoViewData.createDate, user == null ? null : user.lastDeconnectionDate),
      videoViewData.nbView > 0,
      videoViewData.averageRate > 0,
      videoBelowToFavorite.contains(videoViewData.videoId));
  }


  private List<Long> transformAssociateVideosIdsToLong(String[] associateVideosIds) {
    return associateVideosIds == null ? new ArrayList<>() :
      Arrays.asList(associateVideosIds).stream()
        .map(Long::parseLong)
        .collect(Collectors.toList());
  }

  private void fillModelWithFavoritesForSignFilter(Model model, User user) {
    if (user != null) {
      List<FavoriteModalView> favorites = new ArrayList<>();
      List<FavoriteModalView> newFavoritesShareToMe = FavoriteModalView.fromNewShare(services.favorite().newFavoritesShareToUserForSignFilter(user.id));
      favorites.addAll(newFavoritesShareToMe);
      List<FavoriteModalView> oldFavoritesShareToMe = FavoriteModalView.from(services.favorite().oldFavoritesShareToUserForSignFilter(user.id));
      favorites.addAll(oldFavoritesShareToMe);
      List<FavoriteModalView> myFavorites = FavoriteModalView.from(services.favorite().favoritesforUserForSignFilter(user.id));
      favorites.addAll(myFavorites);

      Collator collator = Collator.getInstance();
      collator.setStrength(0);
      favorites = favorites.stream().sorted((f1, f2) -> collator.compare(f1.getName(),f2.getName())).collect(Collectors.toList());


      model.addAttribute("myFavorites", favorites);
    }
  }

  private List<Long> fillModelWithFavorites(Model model, User user, Long videoId) {
    List<Long> favoritesIdBelowVideo;
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
      List<FavoriteModalView> favoritesOrdered = new ArrayList<>();
      Collator collator = Collator.getInstance();
      collator.setStrength(0);
      List<FavoriteModalView> favoritesBelowVideo = new ArrayList<>();
      List<Long> finalFavoritesIdBelowVideo = favoritesIdBelowVideo;
      favoritesBelowVideo = favorites.stream().filter(f -> finalFavoritesIdBelowVideo.contains(f.getId()))
        .sorted((f1, f2) -> collator.compare(f1.getName(),f2.getName())).collect(Collectors.toList());
      favoritesOrdered.addAll(favoritesBelowVideo);
      List<FavoriteModalView> favoritesNotBelowVideo = new ArrayList<>();
      favoritesNotBelowVideo = favorites.stream().filter(f -> !finalFavoritesIdBelowVideo.contains(f.getId()))
        .sorted((f1, f2) -> collator.compare(f1.getName(),f2.getName())).collect(Collectors.toList());
      favoritesOrdered.addAll(favoritesNotBelowVideo);


      model.addAttribute("myFavorites", favoritesOrdered);
    } else {
        favoritesIdBelowVideo = new ArrayList<>();
    }
      return favoritesIdBelowVideo;
  }

}
