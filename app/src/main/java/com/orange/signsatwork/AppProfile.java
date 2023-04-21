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

import com.orange.signsatwork.biz.domain.Communities;
import com.orange.signsatwork.biz.domain.CommunityType;
import com.orange.signsatwork.biz.persistence.model.CommunityDB;
import com.orange.signsatwork.biz.persistence.repository.CommunityRepository;
import com.orange.signsatwork.biz.persistence.service.Services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static com.orange.signsatwork.biz.domain.CommunityType.Job;

@Component
public class AppProfile {

  @Autowired
  private Environment environment;

  @Autowired
  private Services services;

  @Autowired
  private CommunityRepository communityRepository;

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
    initCommunities();
  }


  private void initDailyMotion() {
    String dailymotionUrl = environment.getProperty("app.dailymotion_url");
    if (!dailymotionUrl.isEmpty()) {
      String grantType = environment.getProperty("app.dailymotion.grant_type");
      String clientId = environment.getProperty("app.dailymotion.client_id");
      String clientSecret = environment.getProperty("app.dailymotion.client_secret");
      String username = environment.getProperty("app.dailymotion.username");
      String password = environment.getProperty("app.dailymotion.password");
      dailymotionAccess = new DailymotionAccess(dailymotionUrl, grantType, clientId, clientSecret, username, password);
    }
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

  private void initCommunities() {
    Set<CommunityDB> set = new HashSet<>();
    List<String> instanceCommunitiesName = Arrays.asList(environment.getProperty("app.instances").split(","));
    List<CommunityDB> databaseInstanceCommunities = communityRepository.findAll(CommunityType.Instance);
    List<String> databaseInstanceCommunitiesName = databaseInstanceCommunities.stream().map(d -> d.getName()
    ).collect(Collectors.toList());
    if (databaseInstanceCommunitiesName != null) {
      instanceCommunitiesName.stream().map(communityName -> {
        if (!databaseInstanceCommunitiesName.contains(communityName)) {
          Collections.addAll(set, new CommunityDB(communityName, CommunityType.Instance));
        }
        return null;
      }).collect(Collectors.toList());
    } else {
      instanceCommunitiesName.stream().map(communityName -> {
        Collections.addAll(set, new CommunityDB(communityName, CommunityType.Instance));
        return  null;
      }).collect(Collectors.toList());
    }
    List<CommunityDB> databasePublicCommunities = communityRepository.findAll(CommunityType.Public);
    if (databasePublicCommunities.size() == 0) {
      Collections.addAll(set, new CommunityDB("Public", CommunityType.Public));
    }

    if (!set.isEmpty()) {
      communityRepository.saveAll(set);
    }
  }
}
