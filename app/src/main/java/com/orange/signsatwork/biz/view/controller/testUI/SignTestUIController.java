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

import com.orange.signsatwork.biz.domain.Sign;
import com.orange.signsatwork.biz.persistence.service.*;
import com.orange.signsatwork.biz.view.model.AuthentModel;
import com.orange.signsatwork.biz.view.model.SignProfileView;
import com.orange.signsatwork.biz.view.model.SignView;
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
public class SignTestUIController {

  @Autowired
  private UserService userService;
  @Autowired
  private CommunityService communityService;
  @Autowired
  private RequestService requestService;
  @Autowired
  private FavoriteService favoriteService;
  @Autowired
  private SignService signService;
  @Autowired
  private VideoService videoService;
  @Autowired
  private CommentService commentService;
  @Autowired
  MessageByLocaleService messageByLocaleService;

  @Secured("ROLE_USER")
  @RequestMapping("/sec/testUI/signs")
  public String signs(Model model) {
    model.addAttribute("title", messageByLocaleService.getMessage("signs"));
    List<SignView> signsView = SignView.from(signService.all());
    model.addAttribute("signs", signsView);
    return "testUI/signs";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/testUI/sign/{id}")
  public String signDetails(@PathVariable long id, Model model) {
    Sign sign = signService.withIdLoadAssociates(id);

    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("sign_details"));

    SignProfileView signProfileView = new SignProfileView(sign, signService);
    model.addAttribute("signProfileView", signProfileView);

    return "testUI/sign";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/testUI/sign/{signId}/add/signs", method = RequestMethod.POST)
  public String changeSignAssociates(
          HttpServletRequest req, @PathVariable long signId, Model model) {

    List<Long> associateSignsIds =
            transformAssociateSignsIdsToLong(req.getParameterMap().get("associateSignsIds"));

    signService.changeSignAssociates(signId, associateSignsIds);

    return signDetails(signId, model);
  }


  private List<Long> transformAssociateSignsIdsToLong(String[] associateSignsIds) {
    if (associateSignsIds == null) {
      return new ArrayList<>();
    }
    return Arrays.asList(associateSignsIds).stream()
            .map(Long::parseLong)
            .collect(Collectors.toList());
  }
}
