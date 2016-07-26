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

import com.orange.signsatwork.biz.domain.Sign;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.persistence.service.SignService;
import com.orange.signsatwork.biz.view.model.AuthentModel;
import com.orange.signsatwork.biz.view.model.SignProfileView;
import com.orange.signsatwork.biz.view.model.SignView;
import com.orange.signsatwork.biz.view.model.SignCreationView;
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
    fillModelWithSigns(model);
    model.addAttribute("showTooltip", true);
    return "signs";
  }

  @RequestMapping(value = "/sign/{signId}")
  public String sign(HttpServletRequest req, @PathVariable long signId, Principal principal, Model model) {
    String referer = req.getHeader("Referer");
    String backUrl = referer.contains(SIGNS_URL) ? SIGNS_URL : HOME_URL;
    fillModelWithContext(model, "sign.info", principal, SHOW_ADD_FAVORITE, backUrl);
    fillModelWithSign(model, signId, principal);
    return "sign";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/sign/{signId}/detail")
  public String signDetail(@PathVariable long signId, Principal principal, Model model)  {
    fillModelWithContext(model, "sign.detail", principal, SHOW_ADD_FAVORITE, signUrl(signId));
    fillModelWithSign(model, signId, principal);
    return "sign-detail";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sign/{signId}/associates")
  public String associates(@PathVariable long signId, Principal principal, Model model)  {
    fillModelWithContext(model, "sign.associated", principal, HIDE_ADD_FAVORITE, signUrl(signId));
    fillModelWithSign(model, signId, principal);
    return "sign-associates";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/sign/{signId}/associate-form")
  public String associate(@PathVariable long signId, Principal principal, Model model)  {
    fillModelWithContext(model, "sign.associate-with", principal, HIDE_ADD_FAVORITE, signUrl(signId));
    fillModelWithSign(model, signId, principal);
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
    Sign sign = services.sign().create(user.id, signCreationView.getSignName(), signCreationView.getVideoUrl());

    log.info("createSign: username = {} / sign name = {} / video url = {}", user.username, signCreationView.getSignName(), signCreationView.getVideoUrl());

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

  private void fillModelWithSigns(Model model) {
    List<SignView> signsView = SignView.from(services.sign().all());
    model.addAttribute("signs", signsView);
    model.addAttribute("signCreationView", new SignCreationView());
  }

  private void fillModelWithSign(Model model, long signId, Principal principal) {
    SignService signService = services.sign();
    Sign sign = signService.withIdLoadAssociates(signId);

    SignProfileView signProfileView = AuthentModel.isAuthenticated(principal) ?
            new SignProfileView(sign, signService, services.user().withUserName(principal.getName())) :
            new SignProfileView(sign, signService);
    model.addAttribute("signProfileView", signProfileView);
  }

  private List<Long> transformAssociateSignsIdsToLong(String[] associateSignsIds) {
    return associateSignsIds == null ? new ArrayList<>() :
      Arrays.asList(associateSignsIds).stream()
            .map(Long::parseLong)
            .collect(Collectors.toList());
  }
}
