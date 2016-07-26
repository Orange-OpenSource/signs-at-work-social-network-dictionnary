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

import com.orange.signsatwork.biz.domain.Favorite;
import com.orange.signsatwork.biz.persistence.service.FavoriteService;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.SignService;
import com.orange.signsatwork.biz.view.model.AuthentModel;
import com.orange.signsatwork.biz.view.model.FavoriteProfileView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FavoriteTestUIController {

  @Autowired
  private FavoriteService favoriteService;
  @Autowired
  private SignService signService;
  @Autowired
  MessageByLocaleService messageByLocaleService;

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/testUI/favorite/{id}")
  public String favoriteDetails(@PathVariable long id, Model model) {
    Favorite favorite = favoriteService.withId(id);

    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("favorite_details"));
    FavoriteProfileView favoriteProfileView = new FavoriteProfileView(favorite, signService);
    model.addAttribute("favoriteProfileView", favoriteProfileView);

    return "testUI/favorite";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/testUI/favorite/{favoriteId}/add/signs", method = RequestMethod.POST)
  public String changeFavoriteSigns(
          HttpServletRequest req, @PathVariable long favoriteId, Model model) {

    List<Long> signsIds =
            transformSignsIdsToLong(req.getParameterMap().get("favoriteSignsIds"));

    favoriteService.changeFavoriteSigns(favoriteId, signsIds);

    return favoriteDetails(favoriteId, model);
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
