package com.orange.signsatwork.biz.view.model;

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

import com.orange.signsatwork.biz.persistence.service.UserService;
import com.orange.signsatwork.biz.security.AppSecurityAdmin;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

public class AuthentModel {

  public static boolean isAuthenticated(Principal principal) {
    return principal != null && principal.getName() != null;
  }

  public static void addAuthenticatedModel(Model model, boolean isAuthenticated) {
    model.addAllAttributes(authenticatedModel(isAuthenticated));
  }

  public static void addAuthentModelWithUserDetails(Model model, Principal principal, UserService userService) {
    boolean authenticated = isAuthenticated(principal);
    addAuthenticatedModel(model, authenticated);
    model.addAttribute("authenticatedUsername",
            authenticated ? principal.getName() : "Please sign in");
    model.addAttribute("isAdmin", authenticated && isAdmin(principal));
    if (authenticated && !isAdmin(principal)) {
      model.addAttribute("user", userService.withUserName(principal.getName()));
    }
  }

  private static Map<String, Object> authenticatedModel(boolean isAuthenticated) {
    Map<String, Object> modelMap = new HashMap<>();
    modelMap.put("isAuthenticated", isAuthenticated);
    return modelMap;
  }

  private static boolean isAdmin(Principal principal) {
    return AppSecurityAdmin.isAdmin(principal.getName());
  }
}
