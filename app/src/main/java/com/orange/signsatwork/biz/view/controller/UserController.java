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

import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.persistence.service.UserService;
import com.orange.signsatwork.biz.storage.StorageService;
import com.orange.signsatwork.biz.view.model.FavoriteCreationView;
import com.orange.signsatwork.biz.view.model.FavoriteModalView;
import com.orange.signsatwork.biz.view.model.UserCreationView;
import org.jcodec.api.JCodecException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
public class UserController {

  @Autowired
  private StorageService storageService;

  @Autowired
  private Services services;
  @Autowired
  MessageByLocaleService messageByLocaleService;

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/profile")
  public String userDetails(Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());

    model.addAttribute("title", messageByLocaleService.getMessage("profile"));
    model.addAttribute("user", user);
    fillModelWithFavorites(model, user);
    model.addAttribute("favoriteCreationView", new FavoriteCreationView());

    return "profile";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/job-detail")
  public String jobDetails(Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());
    model.addAttribute("title", messageByLocaleService.getMessage("job"));
    model.addAttribute("user", user);
    model.addAttribute("userCreationView", new UserCreationView());
    model.addAttribute("backUrl", "/sec/profile");

    return "job-detail";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/profile/job", method = RequestMethod.POST)
  public String changeUserJob(
 @ModelAttribute UserCreationView userCreationView, Principal principal, Model model) {
    UserService userService = services.user();

    User user = userService.withUserName(principal.getName());
    userService.changeJob(user, userCreationView.getJob());

    return jobDetails(principal, model);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/profile/entity", method = RequestMethod.POST)
  public String changeUserEntity(
 @ModelAttribute UserCreationView userCreationView, Principal principal, Model model) {
    UserService userService = services.user();

    User user = userService.withUserName(principal.getName());
    userService.changeEntity(user, userCreationView.getEntity());

    return jobDetails(principal, model);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/profile/description", method = RequestMethod.POST)
  public String changeUserDescription(
 @ModelAttribute UserCreationView userCreationView, Principal principal, Model model) {
    UserService userService = services.user();

    User user = userService.withUserName(principal.getName());
    userService.changeDescription(user, userCreationView.getJobTextDescription());

    return descriptionDetails(principal, model);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/description-detail")
  public String descriptionDetails(Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());
    model.addAttribute("title", messageByLocaleService.getMessage("description"));
    model.addAttribute("user", user);
    model.addAttribute("backUrl", "/sec/profile");

    return "description-detail";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/name-detail")
  public String nameDetails(Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());
    model.addAttribute("title", user.firstName + ' ' + user.lastName);
    model.addAttribute("user", user);
    model.addAttribute("backUrl", "/sec/profile");

    return "name-detail";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/input-profile")
  public String inputProfile(Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());
    model.addAttribute("title", messageByLocaleService.getMessage("profile"));
    model.addAttribute("user", user);
    model.addAttribute("backUrl", "/sec/profile");

    return "input-profile";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/profile/profileEmpty", method = RequestMethod.POST)
  public String createUserProfile(
    @RequestParam("fileVideoName") MultipartFile fileVideoName, @RequestParam("fileVideoJob") MultipartFile fileVideoJob,
    @ModelAttribute UserCreationView userCreationView, Principal principal, Model model) {

    if (!fileVideoName.isEmpty()) {
      storageService.store(fileVideoName);
      userCreationView.setNameVideo("/files/" + fileVideoName.getOriginalFilename());
    }

    if (!fileVideoJob.isEmpty()) {
      storageService.store(fileVideoJob);
      userCreationView.setJobVideoDescription("/files/" + fileVideoJob.getOriginalFilename());
    }

    UserService userService = services.user();

    User user = userService.withUserName(principal.getName());
    userService.createProfile(user, userCreationView.getLastName(), userCreationView.getFirstName(), userCreationView.getNameVideo(), userCreationView.getJob(), userCreationView.getEntity(), userCreationView.getJobTextDescription(), userCreationView.getJobVideoDescription());
    return userDetails(principal, model);
  }



  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/profile/lastName", method = RequestMethod.POST)
  public String changeUserLastName(
    @ModelAttribute UserCreationView userCreationView, Principal principal, Model model) {
    UserService userService = services.user();

    User user = userService.withUserName(principal.getName());
    userService.changeLastName(user, userCreationView.getLastName());

    return nameDetails(principal, model);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/profile/name", method = RequestMethod.POST)
  public String changeUserFirstName(
    @ModelAttribute UserCreationView userCreationView, Principal principal, Model model) {
    UserService userService = services.user();

    User user = userService.withUserName(principal.getName());
    userService.changeFirstName(user, userCreationView.getFirstName());

    return nameDetails(principal, model);
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/input-job")
  public String inputJob(Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());
    model.addAttribute("title", messageByLocaleService.getMessage("job"));
    model.addAttribute("user", user);
    model.addAttribute("backUrl", "/sec/profile");

    return "input-job";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/input-description", method = RequestMethod.POST)
  public String inputJobDescription(Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());
    model.addAttribute("title", messageByLocaleService.getMessage("description"));
    model.addAttribute("user", user);
    model.addAttribute("backUrl", "/sec/profile");

    return "input-description";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/sign/changeNameLSFfromupload", method = RequestMethod.POST)
  public String changeNameLSF(@RequestParam("file") MultipartFile file, Principal principal, Model model) throws IOException, JCodecException {
    User user = services.user().withUserName(principal.getName());
    storageService.store(file);

    String videoWebPath = "/files/" + file.getOriginalFilename();
    services.user().changeNameVideoUrl(user, videoWebPath);
    if (user.firstName.isEmpty() && user.lastName.isEmpty() && user.job.isEmpty() && user.entity.isEmpty() && user.jobTextDescription.isEmpty() ){
      model.addAttribute("isUserEmpty", true);
      return inputProfile(principal,model);
    } else {
      model.addAttribute("isUserEmpty", false);
    }

    return nameDetails(principal,model);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/sign/changeDescriptionLSFfromupload", method = RequestMethod.POST)
  public String changeDescriptionLSF(@RequestParam("file") MultipartFile file, Principal principal, Model model) throws IOException, JCodecException {
    User user = services.user().withUserName(principal.getName());
    storageService.store(file);

    String videoWebPath = "/files/" + file.getOriginalFilename();
    services.user().changeDescriptionVideoUrl(user, videoWebPath);


    return descriptionDetails(principal,model);
  }



  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/profile-from-community/{communityId}/{userId}")
  public String userDetails(@PathVariable long userId, @PathVariable long communityId, Principal principal, Model model) {
    User user = services.user().withId(userId);
    user = user.loadVideos();
    model.addAttribute("title", user.firstName + ' ' + user.lastName);
    model.addAttribute("backUrl", "/sec/community/"+communityId);
    model.addAttribute("user", user);

    return "profile-from-community";
  }

  private void fillModelWithFavorites(Model model, User user) {
    List<FavoriteModalView> myFavorites = FavoriteModalView.from(services.favorite().favoritesforUser(user.id));
    model.addAttribute("myFavorites", myFavorites);
  }

}
