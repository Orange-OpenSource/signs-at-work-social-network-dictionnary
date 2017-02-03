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
import com.orange.signsatwork.biz.domain.Sign;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.domain.Video;
import com.orange.signsatwork.biz.persistence.model.*;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.persistence.service.SignService;
import com.orange.signsatwork.biz.view.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

  @RequestMapping(value = SIGNS_URL)
  public String signs(Principal principal, Model model) {
    fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE, HOME_URL);
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
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("all"));

    return "signs";
  }

  @RequestMapping(value = "/sec/signs/{favoriteId}")
  public String signsInFavorite(@PathVariable long favoriteId, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE, HOME_URL);
    Favorite favorite = services.favorite().withId(favoriteId);
    List<Object[]> querySigns = services.sign().SignsForFavoriteView(favoriteId);
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithCommentList = Arrays.asList(services.sign().lowCommented());

    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<SignView2> signViews = signViewsData.stream()
      .map(signViewData -> new SignView2(
        signViewData,
        signWithCommentList.contains(signViewData.id),
        SignView2.createdAfterLastDeconnection(signViewData.createDate, user == null ? null : user.lastDeconnectionDate),
        signWithView.contains((signViewData.id)))
      )
      .collect(Collectors.toList());

    SignsViewSort2 signsViewSort2 = new SignsViewSort2();
    signViews = signsViewSort2.sort(signViews, false);

    model.addAttribute("signsView", signViews);
    fillModelWithFavorites(model, user);
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
    model.addAttribute("favoriteId", favoriteId);
    model.addAttribute("dropdownTitle", favorite.name);

    return "signs";
  }


  @RequestMapping(value = "/sec/signs/mostcommented")
  public String signsMostCommented(@RequestParam("isMostCommented") boolean isMostCommented, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE, HOME_URL);

    List<Object[]> querySigns = services.sign().SignsForSignsView();
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithCommentList;
    if (isMostCommented == true) {
      signWithCommentList = Arrays.asList(services.sign().lowCommented());
      model.addAttribute("isLowCommented", true);
      model.addAttribute("isMostCommented", false);
    } else {
      signWithCommentList = Arrays.asList(services.sign().mostCommented());
      model.addAttribute("isMostCommented", true);
      model.addAttribute("isLowCommented", false);
    }

    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<SignViewData> notCommented = signViewsData.stream()
      .filter(signViewData -> !signWithCommentList.contains(signViewData.id))
      .collect(Collectors.toList());

    List<SignViewData> commented = signViewsData.stream()
      .filter(signViewData -> signWithCommentList.contains(signViewData.id))
      .sorted(new CommentOrderComparator(signWithCommentList))
      .collect(Collectors.toList());

    List<SignView2> signViews = commented.stream()
      .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, user))
      .collect(Collectors.toList());


    model.addAttribute("signsView", signViews);
    fillModelWithFavorites(model, user);
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("isAll", false);
    model.addAttribute("isMostRating", false);
    model.addAttribute("isLowRating", false);
    model.addAttribute("isMostViewed", false);
    model.addAttribute("isLowViewed", false);
    model.addAttribute("isMostRecent", false);
    model.addAttribute("isLowRecent", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("most_commented"));

    return "signs";
  }

  @RequestMapping(value = "/sec/signs/mostrating")
  public String signsMostRating(@RequestParam("isMostRating") boolean isMostRating,Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE, HOME_URL);

    List<Object[]> querySigns = services.sign().SignsForSignsView();
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithRatingList;
    if (isMostRating == true) {
      signWithRatingList = Arrays.asList(services.sign().lowRating());
      model.addAttribute("isLowRating", true);
      model.addAttribute("isMostRating", false);
    } else {
      signWithRatingList = Arrays.asList(services.sign().mostRating());
      model.addAttribute("isMostRating", true);
      model.addAttribute("isLowRating", false);
    }

    List<SignViewData> rating = signViewsData.stream()
      .filter(signViewData -> signWithRatingList.contains(signViewData.id))
      .sorted(new CommentOrderComparator(signWithRatingList))
      .collect(Collectors.toList());

    List<Long> signWithCommentList = Arrays.asList(services.sign().lowCommented());

    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<SignView2> signViews = rating.stream()
      .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, user))
      .collect(Collectors.toList());


    model.addAttribute("signsView", signViews);


    fillModelWithFavorites(model, user);
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("isAll", false);
    model.addAttribute("isMostCommented", false);
    model.addAttribute("isLowCommented", false);
    model.addAttribute("isMostViewed", false);
    model.addAttribute("isLowViewed", false);
    model.addAttribute("isMostRecent", false);
    model.addAttribute("isLowRecent", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("most_rating"));

    return "signs";
  }

  @RequestMapping(value = "/sec/signs/mostviewed")
  public String signsMostViewed(@RequestParam("isMostViewed") boolean isMostViewed, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE, HOME_URL);

    List<Object[]> querySigns = services.sign().SignsForSignsView();
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithViewedList;
    if (isMostViewed == true) {
      signWithViewedList = Arrays.asList(services.sign().lowViewed());
      model.addAttribute("isLowViewed", true);
      model.addAttribute("isMostViewed", false);
    } else {
      signWithViewedList = Arrays.asList(services.sign().mostViewed());
      model.addAttribute("isMostViewed", true);
      model.addAttribute("isLowViewed", false);
    }

    List<SignViewData> viewed = signViewsData.stream()
      .filter(signViewData -> signWithViewedList.contains(signViewData.id))
      .sorted(new CommentOrderComparator(signWithViewedList))
      .collect(Collectors.toList());

    List<Long> signWithCommentList = Arrays.asList(services.sign().lowCommented());

    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<SignView2> signViews = viewed.stream()
      .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, user))
      .collect(Collectors.toList());


    model.addAttribute("signsView", signViews);


    fillModelWithFavorites(model, user);
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("isAll", false);
    model.addAttribute("isMostCommented", false);
    model.addAttribute("isLowCommented", false);
    model.addAttribute("isMostRating", false);
    model.addAttribute("isLowRating", false);
    model.addAttribute("isMostRecent", false);
    model.addAttribute("isLowRecent", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("most_viewed"));

    return "signs";
  }

  @RequestMapping(value = "/sec/signs/mostrecent")
  public String signsMostRecent(@RequestParam("isMostRecent") boolean isMostRecent, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE, HOME_URL);

    List<Object[]> querySigns;
    if (isMostRecent == true) {
      querySigns = services.sign().lowRecent(user.lastDeconnectionDate);
      model.addAttribute("isLowRecent", true);
      model.addAttribute("isMostRecent", false);
    } else {
     querySigns = services.sign().mostRecent(user.lastDeconnectionDate);
      model.addAttribute("isMostRecent", true);
      model.addAttribute("isLowRecent", false);
    }
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithCommentList = Arrays.asList(services.sign().lowCommented());

    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<SignView2> signViews = signViewsData.stream()
      .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, user))
      .collect(Collectors.toList());


    model.addAttribute("signsView", signViews);


    fillModelWithFavorites(model, user);
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("isAll", false);
    model.addAttribute("isMostCommented", false);
    model.addAttribute("isLowCommented", false);
    model.addAttribute("isMostRating", false);
    model.addAttribute("isLowRating", false);
    model.addAttribute("isMostViewed", false);
    model.addAttribute("isLowViewed", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("most_recent"));

    return "signs";
  }

  @RequestMapping(value = "/sign/{signId}")
  public String sign(HttpServletRequest req, @PathVariable long signId, Principal principal, Model model) {
    final User user = AuthentModel.isAuthenticated(principal) ? services.user().withUserName(principal.getName()) : null;

    String referer = req.getHeader("Referer");
    String backUrl = referer != null && referer.contains(SIGNS_URL) ? SIGNS_URL : HOME_URL;
    fillModelWithContext(model, "sign.info", principal, SHOW_ADD_FAVORITE, backUrl);

    List<Object[]> querySigns = services.sign().AllVideosForSign(signId);
    List<VideoViewData> videoViewsData = querySigns.stream()
      .map(objectArray -> new VideoViewData(objectArray))
      .collect(Collectors.toList());
    if (videoViewsData.size() == 1) {
      return showVideo(signId, videoViewsData.get(0).videoId);
    } else {
      model.addAttribute("title", videoViewsData.get(0).signName);


      List<Long> videoWithCommentList = Arrays.asList(services.sign().NbCommentForAllVideoBySign(signId));

      List<VideoView2> videoViews = videoViewsData.stream()
        .map(videoViewData -> buildVideoView(videoViewData, videoWithCommentList, user))
        .collect(Collectors.toList());


      VideosViewSort videosViewSort = new VideosViewSort();
      videoViews = videosViewSort.sort(videoViews);

      model.addAttribute("videosView", videoViews);
      return "videos";
    }

  }

  @RequestMapping(value = "/sign/{signId}/{videoId}")
  public String video(HttpServletRequest req, @PathVariable long signId, @PathVariable long videoId, Principal principal, Model model) {

    Boolean isVideoCreatedByMe = false;
    String referer = req.getHeader("Referer");
    String backUrl;


    AuthentModel.addAuthenticatedModel(model, AuthentModel.isAuthenticated(principal));
    model.addAttribute("showAddFavorite", SHOW_ADD_FAVORITE && AuthentModel.isAuthenticated(principal));
    model.addAttribute("commentCreationView", new CommentCreationView());
    model.addAttribute("favoriteCreationView", new FavoriteCreationView());


    Sign sign = services.sign().withIdSignsView(signId);
    if (referer != null ) {
      if (referer.contains(SIGNS_URL)) {
        backUrl = SIGNS_URL;
      }  else if (referer.contains(SIGN_URL)) {

        if (sign.nbVideo == 1) {
          backUrl = HOME_URL;
        } else {
          backUrl = signUrl(signId);
        }
      } else {
        backUrl = HOME_URL;
      }
    } else {
      backUrl = HOME_URL;
    }
    model.addAttribute("backUrl", backUrl);

    Video video = services.video().withId(videoId);
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
      fillModelWithFavorites(model, user);
      if (video.user.id == user.id) {
        isVideoCreatedByMe = true;
      }
    }

    if (referer != null) {
      if (!isVideoCreatedByMe && !referer.contains("detail")) {
        services.video().increaseNbView(videoId);
      }
    }

    if ((video.idForName == 0) || (sign.nbVideo == 1 )){
      model.addAttribute("title", sign.name + " / " + messageByLocaleService.getMessage("info"));
      model.addAttribute("videoName", sign.name);
    } else {
      model.addAttribute("title", sign.name + " (" + video.idForName + ")" + " / " + messageByLocaleService.getMessage("info"));
      model.addAttribute("videoName", sign.name + " (" + video.idForName + ")");
    }


    model.addAttribute("signView", sign);
    model.addAttribute("videoView", video);
    model.addAttribute("isVideoCreatedByMe", isVideoCreatedByMe);

    return "sign";
  }



  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/sign/{signId}/{videoId}/detail")
  public String videoDetail(@PathVariable long signId, @PathVariable long videoId, Principal principal, Model model)  {
    Boolean isVideoCreatedByMe = false;

    //fillModelWithContext(model, "sign.detail", principal, SHOW_ADD_FAVORITE, videoUrl(signId, videoId));

    model.addAttribute("backUrl", videoUrl(signId, videoId));
    AuthentModel.addAuthenticatedModel(model, AuthentModel.isAuthenticated(principal));
    model.addAttribute("showAddFavorite", SHOW_ADD_FAVORITE && AuthentModel.isAuthenticated(principal));

    model.addAttribute("favoriteCreationView", new FavoriteCreationView());
    Sign sign = services.sign().withIdSignsView(signId);
    Video video = services.video().withId(videoId);
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
      fillModelWithFavorites(model, user);
      if (video.user.id == user.id) {
        isVideoCreatedByMe = true;
      }
    }
    List<Object[]> queryAllVideosHistory = services.sign().AllVideosHistoryForSign(signId);
    List<VideoHistoryData> videoHistoryDatas = queryAllVideosHistory.stream()
      .map(objectArray -> new VideoHistoryData(objectArray))
      .collect(Collectors.toList());
    model.addAttribute("videoHistoryDatas", videoHistoryDatas);

    if ((video.idForName == 0) || (sign.nbVideo == 1 )){
      model.addAttribute("title", sign.name + " / " + messageByLocaleService.getMessage("détail"));
      model.addAttribute("videoName", sign.name);
    } else {
      model.addAttribute("title", sign.name + " (" + video.idForName + ")" + " / " + messageByLocaleService.getMessage("détail"));
      model.addAttribute("videoName", sign.name + " (" + video.idForName + ")");
    }

    model.addAttribute("signView", sign);
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("videoView", video);
    model.addAttribute("isVideoCreatedByMe", isVideoCreatedByMe);

    return "sign-detail";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sign/{signId}/associates")
  public String associates(@PathVariable long signId, Principal principal, Model model)  {
    fillModelWithContext(model, "sign.associated", principal, HIDE_ADD_FAVORITE, signUrl(signId));

    List<Object[]> querySigns = services.sign().AssociateSigns(signId, signId);
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithCommentList = Arrays.asList(services.sign().lowCommented());

    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<SignView2> signViews = signViewsData.stream()
      .map(signViewData -> new SignView2(
        signViewData,
        signWithCommentList.contains(signViewData.id),
        SignView2.createdAfterLastDeconnection(signViewData.createDate, services.user().withUserName(principal.getName()) == null ? null : services.user().withUserName(principal.getName()).lastDeconnectionDate),
        signWithView.contains(signViewData.id))
      )
      .collect(Collectors.toList());

    SignsViewSort2 signsViewSort2 = new SignsViewSort2();
    signViews = signsViewSort2.sort(signViews, false);

    model.addAttribute("signsView", signViews);

    return "sign-associates";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/sign/{signId}/associate-form")
  public String associate(@PathVariable long signId, Principal principal, Model model)  {
    fillModelWithContext(model, "sign.associate-form", principal, HIDE_ADD_FAVORITE, signUrl(signId));
    fillModelWithSign(model, signId, principal);

    List<Object[]> querySigns = services.sign().SignsForSignsView();
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .filter(s -> s.id != signId)
      .collect(Collectors.toList());
    model.addAttribute("signsView", signViewsData);

    return "sign-associate-form";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/sign/{signId}/associate", method = RequestMethod.POST)
  public String changeAssociates(HttpServletRequest req, @PathVariable long signId, Principal principal)  {
    List<Long> associateSignsIds =
            transformAssociateSignsIdsToLong(req.getParameterMap().get("associateSignsIds"));

    services.sign().changeSignAssociates(signId, associateSignsIds);

    log.info("Change sign (id={}) associates, ids={}", signId, associateSignsIds);

    return showSign(signId);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/sign/create", method = RequestMethod.POST)
  public String createSign(@ModelAttribute SignCreationView signCreationView, Principal principal) {
    User user = services.user().withUserName(principal.getName());
    Sign sign = services.sign().create(user.id, signCreationView.getSignName(), signCreationView.getVideoUrl(), "");

    log.info("createSign: username = {} / sign name = {} / video url = {}", user.username, signCreationView.getSignName(), signCreationView.getVideoUrl());

    return showSign(sign.id);
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

  private String showVideo(long signId, long videoId) {
    return "redirect:/sign/" + signId + "/" + videoId;
  }

  private void fillModelWithContext(Model model, String messageEntry, Principal principal, boolean showAddFavorite, String backUrl) {
    model.addAttribute("title", messageByLocaleService.getMessage(messageEntry));
    model.addAttribute("backUrl", backUrl);
    AuthentModel.addAuthenticatedModel(model, AuthentModel.isAuthenticated(principal));
    model.addAttribute("showAddFavorite", showAddFavorite && AuthentModel.isAuthenticated(principal));
  }

  private void fillModelWithSigns(Model model, Principal principal) {
    final User user = AuthentModel.isAuthenticated(principal) ? services.user().withUserName(principal.getName()) : null;

    List<Object[]> querySigns = services.sign().SignsForSignsView();
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithCommentList = Arrays.asList(services.sign().lowCommented());

    List<Long> signWithView = Arrays.asList(services.sign().mostViewed());

    List<SignView2> signViews = signViewsData.stream()
      .map(signViewData -> buildSignView(signViewData, signWithCommentList, signWithView, user))
      .collect(Collectors.toList());

    SignsViewSort2 signsViewSort2 = new SignsViewSort2();
    signViews = signsViewSort2.sort(signViews, false);

    fillModelWithFavorites(model, user);
    model.addAttribute("signsView", signViews);
    model.addAttribute("signCreationView", new SignCreationView());
  }

  private SignView2 buildSignView(SignViewData signViewData, List<Long> signWithCommentList, List<Long> signWithView, User user) {
    return new SignView2(
      signViewData,
      signWithCommentList.contains(signViewData.id),
      SignView2.createdAfterLastDeconnection(signViewData.createDate, user == null ? null : user.lastDeconnectionDate),
      signWithView.contains(signViewData.id));
  }

  private VideoView2 buildVideoView(VideoViewData videoViewData, List<Long> videoWithCommentList, User user) {
    return new VideoView2(
      videoViewData,
      videoWithCommentList.contains(videoViewData.videoId),
      VideoView2.createdAfterLastDeconnection(videoViewData.createDate, user == null ? null : user.lastDeconnectionDate),
      videoViewData.nbView > 0);
  }

  private void fillModelWithSign(Model model, long signId, Principal principal) {
    SignService signService = services.sign();
    Sign sign = signService.withIdLoadAssociates(signId);

    SignProfileView signProfileView = new SignProfileView(sign);
    model.addAttribute("signProfileView", signProfileView);
    model.addAttribute("signCreationView", new SignCreationView());
  }

  private List<Long> transformAssociateSignsIdsToLong(String[] associateSignsIds) {
    return associateSignsIds == null ? new ArrayList<>() :
      Arrays.asList(associateSignsIds).stream()
            .map(Long::parseLong)
            .collect(Collectors.toList());
  }

  private void fillModelWithFavorites(Model model, User user) {
    if (user != null) {
      List<FavoriteModalView> myFavorites = FavoriteModalView.from(services.favorite().favoritesforUser(user.id));
      model.addAttribute("myFavorites", myFavorites);
    }
  }

}
