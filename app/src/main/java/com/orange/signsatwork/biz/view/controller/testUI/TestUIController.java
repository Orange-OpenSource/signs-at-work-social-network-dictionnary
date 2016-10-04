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

import com.orange.signsatwork.biz.domain.Community;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.persistence.service.CommunityService;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.persistence.service.UserService;
import com.orange.signsatwork.biz.storage.StorageService;
import com.orange.signsatwork.biz.view.model.AuthentModel;
import com.orange.signsatwork.biz.view.model.CommunityView;
import com.orange.signsatwork.biz.view.model.UserCreationView;
import com.orange.signsatwork.biz.view.model.UserView;
import org.jcodec.api.JCodecException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;

@Controller
public class TestUIController {

  private final StorageService storageService;

  @Autowired
  public TestUIController(StorageService storageService) {
    this.storageService = storageService;
  }

  @Autowired
  private UserTestUIController userTestUIController;

  @Autowired
  private Services services;
  @Autowired
  private UserService userService;
  @Autowired
  private CommunityService communityService;
  @Autowired
  MessageByLocaleService messageByLocaleService;

  @Secured("ROLE_USER")
  @RequestMapping("/sec/testUI")
  public String testUI(Principal principal, Model model) {

    AuthentModel.addAuthentModelWithUserDetails(model, principal, services.user());
    model.addAttribute("title", messageByLocaleService.getMessage("testUI_page"));

    return "testUI/index";
  }


  @Secured("ROLE_ADMIN")
  @RequestMapping("/sec/testUI/users")
  public String users(Model model) {

    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("users"));
    model.addAttribute("users", UserView.from(userService.all()));
    return "testUI/users";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping("/sec/testUI/communities")
  public String communities(Model model) {
    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("communities"));
    model.addAttribute("communities", CommunityView.from(communityService.all()));
    return "testUI/communities";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping("/sec/testUI/manage_communities_users")
  public String manage_communities_users(Model model) {
    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("manage_communities_users"));
    model.addAttribute("user", new UserCreationView());
    model.addAttribute("community", new CommunityView());
    return "testUI/manage_communities_users";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/testUI/community/{id}")
  public String community(@PathVariable long id, Model model) {
    Community community = communityService.withId(id);
    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("community_details"));
    model.addAttribute("community", community);
    return "testUI/community";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/testUI/user/create", method = RequestMethod.POST)
  public String user(@RequestParam("fileVideoName") MultipartFile fileVideoName, @RequestParam("fileVideoJob") MultipartFile fileVideoJob, @RequestParam("fileVideoActivity") MultipartFile fileVideoActivity,  @ModelAttribute UserCreationView userCreationView, Model model) throws IOException, JCodecException {
    storageService.store(fileVideoName);

    storageService.store(fileVideoJob);
    //File inputFileVideoJob = storageService.load(fileVideoJob.getOriginalFilename()).toFile();
    storageService.store(fileVideoActivity);
    //File inputfileVideoActivity = storageService.load(fileVideoActivity.getOriginalFilename()).toFile();

    userCreationView.setNameVideo("/files/" + fileVideoName.getOriginalFilename());
    userCreationView.setJobVideoDescription("/files/" + fileVideoJob.getOriginalFilename());
    userCreationView.setActivityVideoDescription("/files/" + fileVideoActivity.getOriginalFilename());

    User user = userService.create(userCreationView.toUser(), userCreationView.getPassword());
    userService.createUserFavorite(user.id, messageByLocaleService.getMessage("default_favorite"));
    return userTestUIController.userDetails(user.id, model);
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/testUI/community/create", method = RequestMethod.POST)
  public String community(@ModelAttribute CommunityView communityView, Model model) {
    Community community = communityService.create(communityView.toCommunity());
    return community(community.id, model);
  }


}
