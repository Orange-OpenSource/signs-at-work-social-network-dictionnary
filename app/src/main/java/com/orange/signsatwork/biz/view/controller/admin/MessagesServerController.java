package com.orange.signsatwork.biz.view.controller.admin;

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
import com.orange.signsatwork.biz.persistence.model.CommunityViewData;
import com.orange.signsatwork.biz.persistence.service.CommunityService;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.persistence.service.UserService;
import com.orange.signsatwork.biz.view.model.*;
import lombok.extern.slf4j.Slf4j;
import org.jcodec.api.JCodecException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class MessagesServerController {

  @Autowired
  private UserAdminController userAdminController;

  @Autowired
  private Services services;
  @Autowired
  private UserService userService;
  @Autowired
  private CommunityService communityService;
  @Autowired
  MessageByLocaleService messageByLocaleService;

  @Value("${app.admin.username}")
  String adminUsername;

  @Value("${app.name}")
  String appName;


  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/messages-server")
  public String messagesServer(@RequestParam("isAllAsc") boolean isAllAsc, Model model) {
    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("server_message"));
    model.addAttribute("appName", appName);

    MessagesServer queryMessagesServer;
    if (isAllAsc == true) {
      queryMessagesServer = services.messageServerService().messagesServerAllDesc();
      model.addAttribute("isAllDesc", true);
      model.addAttribute("isAllAsc", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");
    } else {
      queryMessagesServer = services.messageServerService().messagesServerAllAsc();
      model.addAttribute("isAllAsc", true);
      model.addAttribute("isAllDesc", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }

    List<MessageServerView> messagesServerView = queryMessagesServer.stream()
      .map(messageServer -> new MessageServerView(messageServer.id, messageServer.date, messageServer.type, messageServer.values, createMessageText(messageServer.type, messageServer.values), messageServer.action))
      .collect(Collectors.toList());

    model.addAttribute("messagesServer", messagesServerView);

    model.addAttribute("isRequestCreateUserAsc", false);
    model.addAttribute("isRequestCreateUserDesc", false);
    model.addAttribute("isRequestChangeUserLoginAsc", false);
    model.addAttribute("isRequestChangeUserLoginDesc", false);
    model.addAttribute("isUserProfilActionAsc", false);
    model.addAttribute("isUserProfilActionDesc", false);
    model.addAttribute("isCommunityActionAsc", false);
    model.addAttribute("isCommunityActionDesc", false);
    model.addAttribute("isRequestAsc", false);
    model.addAttribute("isRequestDesc", false);
    model.addAttribute("isShareFavoriteAsc", false);
    model.addAttribute("isShareFavoriteDesc", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("all_message"));
    model.addAttribute("classDropdownTitle", "  all-signs_blue pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");

    return "admin/messages-server";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/messages-server/frame")
  public String messagesServerIsAllFrame(@RequestParam("isAllAsc") boolean isAllAsc, Model model) {
    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("server_message"));
    model.addAttribute("appName", appName);

    MessagesServer queryMessagesServer;
    if (isAllAsc == true) {
      queryMessagesServer = services.messageServerService().messagesServerAllDesc();
      model.addAttribute("isAllDesc", true);
      model.addAttribute("isAllAsc", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");
    } else {
      queryMessagesServer = services.messageServerService().messagesServerAllAsc();
      model.addAttribute("isAllAsc", true);
      model.addAttribute("isAllDesc", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }

    List<MessageServerView> messagesServerView = queryMessagesServer.stream()
      .map(messageServer -> new MessageServerView(messageServer.id, messageServer.date, messageServer.type, messageServer.values, createMessageText(messageServer.type, messageServer.values), messageServer.action))
      .collect(Collectors.toList());

    model.addAttribute("messagesServer", messagesServerView);

    model.addAttribute("isRequestCreateUserAsc", false);
    model.addAttribute("isRequestCreateUserDesc", false);
    model.addAttribute("isRequestChangeUserLoginAsc", false);
    model.addAttribute("isRequestChangeUserLoginDesc", false);
    model.addAttribute("isUserProfilActionAsc", false);
    model.addAttribute("isUserProfilActionDesc", false);
    model.addAttribute("isCommunityActionAsc", false);
    model.addAttribute("isCommunityActionDesc", false);
    model.addAttribute("isRequestAsc", false);
    model.addAttribute("isRequestDesc", false);
    model.addAttribute("isShareFavoriteAsc", false);
    model.addAttribute("isShareFavoriteDesc", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("all_message"));
    model.addAttribute("classDropdownTitle", "  all-signs_blue pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");

    return "fragments/frame-messages-server";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/requestCreateUser/frame")
  public String messagesServerisRequestCreateUserFrame(@RequestParam("isRequestCreateUserAsc") boolean isRequestCreateUserAsc, Model model) {
    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("server_message"));
    model.addAttribute("appName", appName);

    MessagesServer queryMessagesServer;
    if (isRequestCreateUserAsc == true) {
      queryMessagesServer = services.messageServerService().messagesServerCreateUserDesc();
      model.addAttribute("isRequestCreateUserDesc", true);
      model.addAttribute("isRequestCreateUserAsc", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");
    } else {
      queryMessagesServer = services.messageServerService().messagesServerCreateUserAsc();
      model.addAttribute("isRequestCreateUserAsc", true);
      model.addAttribute("isRequestCreateUserDesc", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }

    List<MessageServerView> messagesServerView = queryMessagesServer.stream()
      .map(messageServer -> new MessageServerView(messageServer.id, messageServer.date, messageServer.type, messageServer.values, createMessageText(messageServer.type, messageServer.values), messageServer.action))
      .collect(Collectors.toList());

    model.addAttribute("messagesServer", messagesServerView);

    model.addAttribute("isAllAsc", false);
    model.addAttribute("isAllDesc", false);
    model.addAttribute("isRequestChangeUserLoginAsc", false);
    model.addAttribute("isRequestChangeUserLoginDesc", false);
    model.addAttribute("isUserProfilActionAsc", false);
    model.addAttribute("isUserProfilActionDesc", false);
    model.addAttribute("isCommunityActionAsc", false);
    model.addAttribute("isCommunityActionDesc", false);
    model.addAttribute("isRequestAsc", false);
    model.addAttribute("isRequestDesc", false);
    model.addAttribute("isShareFavoriteAsc", false);
    model.addAttribute("isShareFavoriteDesc", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("requestCreateUser"));
    model.addAttribute("classDropdownTitle", "  member_add_blue_circle_filter pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");

    return "fragments/frame-messages-server";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/requestChangeUserLogin/frame")
  public String messagesServerisRequestChangeUserLoginFrame(@RequestParam("isRequestChangeUserLoginAsc") boolean isRequestChangeUserLoginAsc, Model model) {
    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("server_message"));
    model.addAttribute("appName", appName);

    MessagesServer queryMessagesServer;
    if (isRequestChangeUserLoginAsc == true) {
      queryMessagesServer = services.messageServerService().messagesServerChangeUserLoginDesc();
      model.addAttribute("isRequestChangeUserLoginDesc", true);
      model.addAttribute("isRequestChangeUserLoginAsc", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");
    } else {
      queryMessagesServer = services.messageServerService().messagesServerChangeUserLoginAsc();
      model.addAttribute("isRequestChangeUserLoginAsc", true);
      model.addAttribute("isRequestChangeUserLoginDesc", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }

    List<MessageServerView> messagesServerView = queryMessagesServer.stream()
      .map(messageServer -> new MessageServerView(messageServer.id, messageServer.date, messageServer.type, messageServer.values, createMessageText(messageServer.type, messageServer.values), messageServer.action))
      .collect(Collectors.toList());

    model.addAttribute("messagesServer", messagesServerView);

    model.addAttribute("isAllAsc", false);
    model.addAttribute("isAllDesc", false);
    model.addAttribute("isRequestCreateUserAsc", false);
    model.addAttribute("isRequestCreateUserDesc", false);
    model.addAttribute("isUserProfilActionAsc", false);
    model.addAttribute("isUserProfilActionDesc", false);
    model.addAttribute("isCommunityActionAsc", false);
    model.addAttribute("isCommunityActionDesc", false);
    model.addAttribute("isRequestAsc", false);
    model.addAttribute("isRequestDesc", false);
    model.addAttribute("isShareFavoriteAsc", false);
    model.addAttribute("isShareFavoriteDesc", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("requestChangeUserLogin"));
    model.addAttribute("classDropdownTitle", "  member_blue_circle_filter pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");

    return "fragments/frame-messages-server";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/userProfilAction/frame")
  public String messagesServerisUserProfilActioFrame(@RequestParam("isUserProfilActionAsc") boolean isUserProfilActionAsc, Model model) {
    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("server_message"));
    model.addAttribute("appName", appName);

    MessagesServer queryMessagesServer;
    if (isUserProfilActionAsc == true) {
      queryMessagesServer = services.messageServerService().messagesServerUserProfilActionDesc();
      model.addAttribute("isUserProfilActionDesc", true);
      model.addAttribute("isUserProfilActionAsc", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");
    } else {
      queryMessagesServer = services.messageServerService().messagesServerUserProfilActionAsc();
      model.addAttribute("isUserProfilActionAsc", true);
      model.addAttribute("isUserProfilActionDesc", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }

    List<MessageServerView> messagesServerView = queryMessagesServer.stream()
      .map(messageServer -> new MessageServerView(messageServer.id, messageServer.date, messageServer.type, messageServer.values, createMessageText(messageServer.type, messageServer.values), messageServer.action))
      .collect(Collectors.toList());

    model.addAttribute("messagesServer", messagesServerView);

    model.addAttribute("isAllAsc", false);
    model.addAttribute("isAllDesc", false);
    model.addAttribute("isRequestCreateUserAsc", false);
    model.addAttribute("isRequestCreateUserDesc", false);
    model.addAttribute("isRequestChangeUserLoginAsc", false);
    model.addAttribute("isRequestChangeUserLoginDesc", false);
    model.addAttribute("isCommunityActionAsc", false);
    model.addAttribute("isCommunityActionDesc", false);
    model.addAttribute("isRequestAsc", false);
    model.addAttribute("isRequestDesc", false);
    model.addAttribute("isShareFavoriteAsc", false);
    model.addAttribute("isShareFavoriteDesc", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("userProfilAction"));
    model.addAttribute("classDropdownTitle", "  member_blue_circle_filter pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");

    return "fragments/frame-messages-server";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/communityAction/frame")
  public String messagesServerisCommunityActionFrame(@RequestParam("isCommunityActionAsc") boolean isCommunityActionAsc, Model model) {
    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("server_message"));
    model.addAttribute("appName", appName);

    MessagesServer queryMessagesServer;
    if (isCommunityActionAsc == true) {
      queryMessagesServer = services.messageServerService().messagesServerCommunityActionDesc();
      model.addAttribute("isCommunityActionDesc", true);
      model.addAttribute("isCommunityActionAsc", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");
    } else {
      queryMessagesServer = services.messageServerService().messagesServerCommunityActionAsc();
      model.addAttribute("isCommunityActionAsc", true);
      model.addAttribute("isCommunityActionDesc", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }

    List<MessageServerView> messagesServerView = queryMessagesServer.stream()
      .map(messageServer -> new MessageServerView(messageServer.id, messageServer.date, messageServer.type, messageServer.values, createMessageText(messageServer.type, messageServer.values), messageServer.action))
      .collect(Collectors.toList());

    model.addAttribute("messagesServer", messagesServerView);

    model.addAttribute("isAllAsc", false);
    model.addAttribute("isAllDesc", false);
    model.addAttribute("isRequestCreateUserAsc", false);
    model.addAttribute("isRequestCreateUserDesc", false);
    model.addAttribute("isRequestChangeUserLoginAsc", false);
    model.addAttribute("isRequestChangeUserLoginDesc", false);
    model.addAttribute("isUserProfilActionAsc", false);
    model.addAttribute("isUserProfilActionDesc", false);
    model.addAttribute("isRequestAsc", false);
    model.addAttribute("isRequestDesc", false);
    model.addAttribute("isShareFavoriteAsc", false);
    model.addAttribute("isShareFavoriteDesc", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("communityAction"));
    model.addAttribute("classDropdownTitle", "  group_project_blue_circle_filter pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");

    return "fragments/frame-messages-server";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/request/frame")
  public String messagesServerisRequestFrame(@RequestParam("isRequestAsc") boolean isRequestAsc, Model model) {
    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("server_message"));
    model.addAttribute("appName", appName);

    MessagesServer queryMessagesServer;
    if (isRequestAsc == true) {
      queryMessagesServer = services.messageServerService().messagesServerRequestDesc();
      model.addAttribute("isRequestDesc", true);
      model.addAttribute("isRequestAsc", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");
    } else {
      queryMessagesServer = services.messageServerService().messagesServerRequestAsc();
      model.addAttribute("isRequestAsc", true);
      model.addAttribute("isRequestDesc", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }

    List<MessageServerView> messagesServerView = queryMessagesServer.stream()
      .map(messageServer -> new MessageServerView(messageServer.id, messageServer.date, messageServer.type, messageServer.values, createMessageText(messageServer.type, messageServer.values), messageServer.action))
      .collect(Collectors.toList());

    model.addAttribute("messagesServer", messagesServerView);

    model.addAttribute("isAllAsc", false);
    model.addAttribute("isAllDesc", false);
    model.addAttribute("isRequestCreateUserAsc", false);
    model.addAttribute("isRequestCreateUserDesc", false);
    model.addAttribute("isRequestChangeUserLoginAsc", false);
    model.addAttribute("isRequestChangeUserLoginDesc", false);
    model.addAttribute("isUserProfilActionAsc", false);
    model.addAttribute("isUserProfilActionDesc", false);
    model.addAttribute("isCommunityActionAsc", false);
    model.addAttribute("isCommunityActionDesc", false);
    model.addAttribute("isShareFavoriteAsc", false);
    model.addAttribute("isShareFavoriteDesc", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("request"));
    model.addAttribute("classDropdownTitle", "  sign_ask-for_blue_filter pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");

    return "fragments/frame-messages-server";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/sec/admin/shareFavorite/frame")
  public String messagesServerisShareFavoriteFrame(@RequestParam("isShareFavoriteAsc") boolean isShareFavoriteAsc, Model model) {
    AuthentModel.addAuthenticatedModel(model, true);
    model.addAttribute("title", messageByLocaleService.getMessage("server_message"));
    model.addAttribute("appName", appName);

    MessagesServer queryMessagesServer;
    if (isShareFavoriteAsc == true) {
      queryMessagesServer = services.messageServerService().messagesServerShareFavoriteDesc();
      model.addAttribute("isShareFavoriteDesc", true);
      model.addAttribute("isShareFavoriteAsc", false);
      model.addAttribute("classDropdownDirection", "  up_black pull-right");
    } else {
      queryMessagesServer = services.messageServerService().messagesServerShareFavoriteAsc();
      model.addAttribute("isShareFavoriteAsc", true);
      model.addAttribute("isShareFavoriteDesc", false);
      model.addAttribute("classDropdownDirection", "  down_black pull-right");
    }

    List<MessageServerView> messagesServerView = queryMessagesServer.stream()
      .map(messageServer -> new MessageServerView(messageServer.id, messageServer.date, messageServer.type, messageServer.values, createMessageText(messageServer.type, messageServer.values), messageServer.action))
      .collect(Collectors.toList());

    model.addAttribute("messagesServer", messagesServerView);

    model.addAttribute("isAllAsc", false);
    model.addAttribute("isAllDesc", false);
    model.addAttribute("isRequestCreateUserAsc", false);
    model.addAttribute("isRequestCreateUserDesc", false);
    model.addAttribute("isRequestChangeUserLoginAsc", false);
    model.addAttribute("isRequestChangeUserLoginDesc", false);
    model.addAttribute("isUserProfilActionAsc", false);
    model.addAttribute("isUserProfilActionDesc", false);
    model.addAttribute("isCommunityActionAsc", false);
    model.addAttribute("isCommunityActionDesc", false);
    model.addAttribute("isRequestAsc", false);
    model.addAttribute("isRequestDesc", false);
    model.addAttribute("dropdownTitle", messageByLocaleService.getMessage("shareFavorite"));
    model.addAttribute("classDropdownTitle", "  pinlist_shared_filter pull-left");
    model.addAttribute("classDropdownSize", "btn btn-default dropdown-toggle");

    return "fragments/frame-messages-server";
  }

  private String createMessageText(String type, String values) {
    Object[] valuesToArray = Arrays.stream(values.split(";")).toArray();
    valuesToArray[0] = "<span class=\"font-weight_normal\">" + valuesToArray[0] + "</span>";
    if (type.equals("RequestCreateUserMessage")) {
      valuesToArray[1] = "<span class=\"font-weight_normal\">" + valuesToArray[1] + "</span>";
    }
    String messageText = messageByLocaleService.getMessage(type, valuesToArray);
    return messageText;
  }

}
