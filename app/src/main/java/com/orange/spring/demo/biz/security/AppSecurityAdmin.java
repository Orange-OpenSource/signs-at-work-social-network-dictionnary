package com.orange.spring.demo.biz.security;

/*
 * #%L
 * Spring demo
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

import com.orange.spring.demo.biz.persistence.model.UserDB;
import com.orange.spring.demo.biz.persistence.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class AppSecurityAdmin {
  public static final String ADMIN_USERNAME = "admin";
  public static final String ADMIN_PASSWORD = "admin";

  @Autowired
  UserRepository userRepository;

  @Autowired
  PasswordEncoder passwordEncoder;

  @PostConstruct
  private void init() {
    boolean adminDoesNotExist = userRepository.findByUsername(ADMIN_USERNAME).isEmpty();
    if (adminDoesNotExist) {
      createAndPersistAdmin();
    }
    log.warn(adminDoesNotExist ? "Create admin user" : "Admin exists");
  }

  private void createAndPersistAdmin() {
    UserDB userDB = new UserDB(ADMIN_USERNAME, passwordEncoder.encode(ADMIN_PASSWORD), "", "", "", "", "");
    userRepository.save(userDB);
  }

  public static boolean isAdmin(String username) {
    return username.equals(ADMIN_USERNAME);
  }
}
