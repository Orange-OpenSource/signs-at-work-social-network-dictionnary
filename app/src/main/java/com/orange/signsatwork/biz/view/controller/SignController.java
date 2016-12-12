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
import com.orange.signsatwork.biz.persistence.model.CommentData;
import com.orange.signsatwork.biz.persistence.model.RatingData;
import com.orange.signsatwork.biz.persistence.model.SignViewData;
import com.orange.signsatwork.biz.persistence.model.VideoHistoryData;
import com.orange.signsatwork.biz.view.model.SignsViewSort2;
import com.orange.signsatwork.biz.domain.User;
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
import java.math.BigInteger;
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

  @Autowired
  private Services services;

  @Autowired
  MessageByLocaleService messageByLocaleService;

  @RequestMapping(value = SIGNS_URL)
  public String signs(Principal principal, Model model) {
    fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE, HOME_URL);
    fillModelWithSigns(model, principal);
    fillModelWithFavorites(model, principal);
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("isAll", true);
    model.addAttribute("isMostCommented", false);
    model.addAttribute("isLowCommented", false);
    model.addAttribute("isMostRating", false);
    model.addAttribute("isLowRating", false);

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

    List<SignView2> signViews = signViewsData.stream()
      .map(signViewData -> new SignView2(
        signViewData,
        signWithCommentList.contains(signViewData.id),
        SignView2.createdAfterLastConnection(signViewData.createDate, user == null ? null : user.lastConnectionDate))
      )
      .collect(Collectors.toList());

    SignsViewSort2 signsViewSort2 = new SignsViewSort2();
    signViews = signsViewSort2.sort(signViews);

    model.addAttribute("signsView", signViews);
    fillModelWithFavorites(model, principal);
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("isAll", false);
    model.addAttribute("isMostCommented", false);
    model.addAttribute("isLowCommented", false);
    model.addAttribute("isMostRating", false);
    model.addAttribute("isLowRating", false);
    model.addAttribute("favoriteId", favoriteId);

    return "signs";
  }


  @RequestMapping(value = "/sec/signs/mostcommented")
  public String signsMostCommented(@RequestParam("isMostCommented") boolean isMostCommented, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE, HOME_URL);

    Long[] longList = null;
    if (isMostCommented == true) {
      longList = services.sign().lowCommented();
      model.addAttribute("isLowCommented", true);
      model.addAttribute("isMostCommented", false);
    } else {
      longList = services.sign().mostCommented();
      model.addAttribute("isMostCommented", true);
      model.addAttribute("isLowCommented", false);
    }

    List<SignsView> signsView = new ArrayList<>();
    SignsView signView;

    for (int i=0; i < longList.length; i++) {
      Long idSign = longList[i];
      Sign sign = services.sign().withIdSignsView(idSign);
      signView = SignsView.from(sign, services, user.lastConnectionDate);
      signsView.add(signView);
    }


    model.addAttribute("signsView", signsView);
    fillModelWithFavorites(model, principal);
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("isAll", false);
    model.addAttribute("isMostRating", false);
    model.addAttribute("isLowRating", false);

    return "signs";
  }

  @RequestMapping(value = "/sec/signs/mostrating")
  public String signsMostRating(@RequestParam("isMostRating") boolean isMostRating,Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    fillModelWithContext(model, "sign.list", principal, SHOW_ADD_FAVORITE, HOME_URL);

    List<Object[]> objectList;

    if (isMostRating == true) {
      objectList = services.sign().lowRating();
      model.addAttribute("isLowRating", true);
      model.addAttribute("isMostRating", false);
    } else {
      objectList = services.sign().mostRating();
      model.addAttribute("isMostRating", true);
      model.addAttribute("isLowRating", false);
    }


    List<SignsView> signsView = new ArrayList<>();

    objectList.stream()
            .map(objectArray -> ((BigInteger)objectArray[0]).longValue())
            .forEach(idSign -> {
              SignsView signView;
              Sign sign = services.sign().withIdSignsView(idSign);
              signView = SignsView.from(sign, services, user.lastConnectionDate);
              signsView.add(signView);
            });

    model.addAttribute("signsView", signsView);
    fillModelWithFavorites(model, principal);
    model.addAttribute("requestCreationView", new RequestCreationView());
    model.addAttribute("signCreationView", new SignCreationView());
    model.addAttribute("isAll", false);
    model.addAttribute("isMostCommented", false);
    model.addAttribute("isLowCommented", false);

    return "signs";
  }

  @RequestMapping(value = "/sign/{signId}")
  public String sign(HttpServletRequest req, @PathVariable long signId, Principal principal, Model model) {
    String referer = req.getHeader("Referer");
    String backUrl = referer != null && referer.contains(SIGNS_URL) ? SIGNS_URL : HOME_URL;
    fillModelWithContext(model, "sign.info", principal, SHOW_ADD_FAVORITE, backUrl);
    model.addAttribute("commentCreationView", new CommentCreationView());
    fillModelWithFavorites(model, principal);
    model.addAttribute("favoriteCreationView", new FavoriteCreationView());

    Sign sign = services.sign().withIdSignsView(signId);
    if (principal != null) {
      User user = services.user().withUserName(principal.getName());
      Object[] queryRating = services.sign().RatingForSignByUser(signId, user.id);
      RatingData ratingData = new RatingData(queryRating);
      model.addAttribute("ratingData", ratingData);
      List<Object[]> queryAllComments = services.sign().AllCommentsForSign(signId);
      List<CommentData> commentDatas = queryAllComments.stream()
        .map(objectArray -> new CommentData(objectArray))
        .collect(Collectors.toList());
      model.addAttribute("commentDatas", commentDatas);
    }
    model.addAttribute("signView", sign);


    return "sign";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/sign/{signId}/detail")
  public String signDetail(@PathVariable long signId, Principal principal, Model model)  {
    fillModelWithContext(model, "sign.detail", principal, SHOW_ADD_FAVORITE, signUrl(signId));
    fillModelWithFavorites(model, principal);
    model.addAttribute("favoriteCreationView", new FavoriteCreationView());
    Sign sign = services.sign().withIdSignsView(signId);
    if (principal != null) {
      User user = services.user().withUserName(principal.getName());
      Object[] queryRating = services.sign().RatingForSignByUser(signId, user.id);
      RatingData ratingData = new RatingData(queryRating);
      model.addAttribute("ratingData", ratingData);
      List<Object[]> queryAllComments = services.sign().AllCommentsForSign(signId);
      List<CommentData> commentDatas = queryAllComments.stream()
        .map(objectArray -> new CommentData(objectArray))
        .collect(Collectors.toList());
      model.addAttribute("commentDatas", commentDatas);
    }
    List<Object[]> queryAllVideosHistory = services.sign().AllVideosHistoryForSign(signId);
    List<VideoHistoryData> videoHistoryDatas = queryAllVideosHistory.stream()
      .map(objectArray -> new VideoHistoryData(objectArray))
      .collect(Collectors.toList());
    model.addAttribute("videoHistoryDatas", videoHistoryDatas);

    model.addAttribute("signView", sign);
    model.addAttribute("signCreationView", new SignCreationView());

    return "sign-detail";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sign/{signId}/associates")
  public String associates(@PathVariable long signId, Principal principal, Model model)  {
    fillModelWithContext(model, "sign.associated", principal, HIDE_ADD_FAVORITE, signUrl(signId));
    //fillModelWithSign(model, signId, principal);

    List<Object[]> querySigns = services.sign().AssociateSigns(signId, signId);
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithCommentList = Arrays.asList(services.sign().lowCommented());

    List<SignView2> signViews = signViewsData.stream()
      .map(signViewData -> new SignView2(
        signViewData,
        signWithCommentList.contains(signViewData.id),
        SignView2.createdAfterLastConnection(signViewData.createDate, services.user().withUserName(principal.getName()) == null ? null : services.user().withUserName(principal.getName()).lastConnectionDate))
      )
      .collect(Collectors.toList());

    SignsViewSort2 signsViewSort2 = new SignsViewSort2();
    signViews = signsViewSort2.sort(signViews);

    model.addAttribute("signsView", signViews);

    return "sign-associates";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/sign/{signId}/associate-form")
  public String associate(@PathVariable long signId, Principal principal, Model model)  {
    fillModelWithContext(model, "sign.associate-with", principal, HIDE_ADD_FAVORITE, signUrl(signId));
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

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/sign/{signId}/replace", method = RequestMethod.POST)
  public String replaceSign(@PathVariable long signId, @ModelAttribute SignCreationView signCreationView, Principal principal) {
    User user = services.user().withUserName(principal.getName());
    Sign sign = services.sign().replace(user.id, signId, signCreationView.getVideoUrl());

    log.info("replaceSign: username = {} / sign id = {} / video url = {}", user.username, signId, signCreationView.getVideoUrl());

    return showSign(sign.id);
  }

  private String signUrl(long signId) {
    return "/sign/" + signId;
  }

  private String showSign(long signId) {
    return "redirect:/sign/" + signId;
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

    List<SignView2> signViews = signViewsData.stream()
      .map(signViewData -> buildSignView(signViewData, signWithCommentList, user))
      .collect(Collectors.toList());

    SignsViewSort2 signsViewSort2 = new SignsViewSort2();
    signViews = signsViewSort2.sort(signViews);

    model.addAttribute("signsView", signViews);
    model.addAttribute("signCreationView", new SignCreationView());
  }

  private SignView2 buildSignView(SignViewData signViewData, List<Long> signWithCommentList, User user) {
    return new SignView2(
      signViewData,
      signWithCommentList.contains(signViewData.id),
      SignView2.createdAfterLastConnection(signViewData.createDate, user == null ? null : user.lastConnectionDate));
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

  private void fillModelWithFavorites(Model model, Principal principal) {
    if (AuthentModel.isAuthenticated(principal)) {
      User user = services.user().withUserName(principal.getName());
      List<FavoriteModalView> myFavorites = FavoriteModalView.from(services.favorite().favoritesforUser(user.id));
      model.addAttribute("myFavorites", myFavorites);
    }
  }

}
