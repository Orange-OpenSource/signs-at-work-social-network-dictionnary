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
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.view.model.FavoriteCreationView;
import com.orange.signsatwork.biz.view.model.FavoriteProfileView;
import com.orange.signsatwork.biz.view.model.SignView2;
import com.orange.signsatwork.biz.view.model.SignsViewSort2;
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
    model.addAttribute("title", favorite.name);
    model.addAttribute("backUrl", "/");
    model.addAttribute("favoriteView", favorite);

    List<Object[]> querySigns = services.sign().SignsForFavoriteView(favoriteId);
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());

    List<Long> signWithCommentList = Arrays.asList(services.sign().lowCommented());

    List<SignView2> signViews = signViewsData.stream()
      .map(signViewData -> new SignView2(
        signViewData,
        signWithCommentList.contains(signViewData.id),
        SignView2.createdAfterLastDeconnection(signViewData.createDate, user == null ? null : user.lastDeconnectionDate))
      )
      .collect(Collectors.toList());

    SignsViewSort2 signsViewSort2 = new SignsViewSort2();
    signViews = signsViewSort2.sort(signViews);

    model.addAttribute("signsView", signViews);

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
    favorite = favorite.loadSigns();
    if (favorite.signs != null) {
      services.favorite().changeFavoriteSigns(duplicateFavorite.id, favorite.signsIds());
    }

    return showFavorite(duplicateFavorite.id);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorite/{favoriteId}/manage-favorite")
  public String manageFavorite(@PathVariable long favoriteId, Model model)  {

    Favorite favorite = services.favorite().withId(favoriteId);
    model.addAttribute("title", messageByLocaleService.getMessage("favorite.manage"));
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
    model.addAttribute("title", favorite.name);
    model.addAttribute("backUrl", "/sec/favorite/" + favoriteId);
    FavoriteProfileView favoriteProfileView = new FavoriteProfileView(favorite);
    model.addAttribute("favoriteProfileView", favoriteProfileView);

    List<Object[]> querySigns = services.sign().SignsForSignsView();
    List<SignViewData> signViewsData = querySigns.stream()
      .map(objectArray -> new SignViewData(objectArray))
      .collect(Collectors.toList());
    model.addAttribute("signsView", signViewsData);

    return "favorite-associate-sign";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorite/{favoriteId}/add/sign/{signId}")
  public String addSign(@PathVariable long favoriteId, @PathVariable long signId,  Model model)  {
    Favorite favorite = services.favorite().withId(favoriteId);
    favorite = favorite.loadSigns();
    List<Long> signsIds = favorite.signsIds();
    signsIds.add(signId);
    services.favorite().changeFavoriteSigns(favorite.id, signsIds);

    model.addAttribute("title", favorite.name);
    model.addAttribute("backUrl", "/sec/favorite/" + favoriteId);
    FavoriteProfileView favoriteProfileView = new FavoriteProfileView(favorite);
    model.addAttribute("favoriteProfileView", favoriteProfileView);

    return showFavorite(favoriteId);
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorite/create_favorite_add_sign/{signId}")
  public String createAndAddSign(@ModelAttribute FavoriteCreationView favoriteCreationView, Principal principal, @PathVariable long signId,  Model model)  {
    User user = services.user().withUserName(principal.getName());
    Favorite favorite = services.favorite().create(user.id, favoriteCreationView.getFavoriteName());

    favorite = favorite.loadSigns();
    List<Long> signsIds = favorite.signsIds();
    signsIds.add(signId);
    services.favorite().changeFavoriteSigns(favorite.id, signsIds);

    model.addAttribute("title", favorite.name);
    model.addAttribute("backUrl", "/sec/favorite/" + favorite.id);
    FavoriteProfileView favoriteProfileView = new FavoriteProfileView(favorite);
    model.addAttribute("favoriteProfileView", favoriteProfileView);

    return showFavorite(favorite.id);
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/favorite/{favoriteId}/add/signs", method = RequestMethod.POST)
  public String changeFavoriteSigns(
          HttpServletRequest req, @PathVariable long favoriteId) {

    List<Long> signsIds =
            transformSignsIdsToLong(req.getParameterMap().get("favoriteSignsIds"));

    services.favorite().changeFavoriteSigns(favoriteId, signsIds);

    return showFavorite(favoriteId);
  }

  private List<Long> transformSignsIdsToLong(String[] favoriteSignsIds) {
    if (favoriteSignsIds == null) {
      return new ArrayList<>();
    }
    return Arrays.asList(favoriteSignsIds).stream()
            .map(Long::parseLong)
            .collect(Collectors.toList());
  }
}
