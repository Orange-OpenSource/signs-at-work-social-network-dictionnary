package com.orange.signsatwork;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Component
public class AppProfile {

  @Autowired
  private Environment environment;

  private boolean devProfile;
  private Proxy proxy;
  private DailymotionAccess dailymotionAccess;


  public boolean isDevProfile() {
    return devProfile;
  }

  public Proxy proxy() {
    return proxy;
  }

  public DailymotionAccess dailymotionAccess() {
    return dailymotionAccess;
  }


  @PostConstruct
  private void init() {
    initDevProfile();
    initProxy();
    initDailyMotion();
  }


  private void initDailyMotion() {
    String grantType = environment.getProperty("app.dailymotion.grant_type");
    String clientId = environment.getProperty("app.dailymotion.client_id");
    String clientSecret = environment.getProperty("app.dailymotion.client_secret");
    String username = environment.getProperty("app.dailymotion.username");
    String password = environment.getProperty("app.dailymotion.password");
    dailymotionAccess = new DailymotionAccess(grantType, clientId, clientSecret, username, password );
  }

  private void initProxy() {
    String proxyServer = environment.getProperty("app.proxy.server");
    String proxyPort = environment.getProperty("app.proxy.port");
    proxy = new Proxy(proxyServer, proxyPort);
  }

  private void initDevProfile() {
    String[] profiles = environment.getActiveProfiles();
    devProfile = Arrays.stream(profiles)
            .filter(profile -> profile.equals("dev"))
            .findAny()
            .isPresent();
  }

  public boolean isHttps() {
    return environment.getProperty("server.port").equals("8443");
  }
}
