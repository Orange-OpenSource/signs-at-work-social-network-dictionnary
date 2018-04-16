package com.orange.signsatwork.biz.persistence.service.impl;

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

import com.orange.signsatwork.AppProfile;
import com.orange.signsatwork.DalymotionToken;
import com.orange.signsatwork.SpringRestClient;
import com.orange.signsatwork.biz.domain.*;
import com.orange.signsatwork.biz.persistence.model.RequestDB;
import com.orange.signsatwork.biz.persistence.model.SignDB;
import com.orange.signsatwork.biz.persistence.model.UserDB;
import com.orange.signsatwork.biz.persistence.model.VideoDB;
import com.orange.signsatwork.biz.persistence.repository.*;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.persistence.service.SignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SignServiceImpl implements SignService {
  private final UserRepository userRepository;
  private final FavoriteRepository favoriteRepository;
  private final SignRepository signRepository;
  private final VideoRepository videoRepository;
  private final CommentRepository commentRepository;
  private final RequestRepository requestRepository;
  private final Services services;

  @Autowired
  SpringRestClient springRestClient;
  @Autowired
  DalymotionToken dalymotionToken;
  @Autowired
  AppProfile appProfile;

  String REST_SERVICE_URI = "https://api.dailymotion.com";

  @Override
  public UrlFileUploadDailymotion getUrlFileUpload() {
    RestTemplate restTemplate = springRestClient.buildRestTemplate();
    HttpEntity<String> request = new HttpEntity<String>(getHeaders());
    ResponseEntity<UrlFileUploadDailymotion> response = restTemplate.exchange(REST_SERVICE_URI + "/file/upload", HttpMethod.GET, request, UrlFileUploadDailymotion.class);
    UrlFileUploadDailymotion urlfileUploadDailyMotion = response.getBody();
    return urlfileUploadDailyMotion;
  }

  @Override
  public List<Object[]>  mostRecent(Date lastConnectionDate) {
    return signRepository.findMostRecent(lastConnectionDate);
  }

  @Override
  public List<Object[]>  lowRecent(Date lastConnectionDate) {
    return signRepository.findLowRecent(lastConnectionDate);
  }

  @Override
  public List<Object[]>  mostRecentWithoutDate() {
    return signRepository.findMostRecentWithoutDate();
  }

  @Override
  public List<Object[]>  lowRecentWithoutDate() {
    return signRepository.findLowRecentWithoutDate();
  }

  @Override
  public Long[] mostViewed() {
    return signRepository.findMostViewed();
  }

  @Override
  public Long[] lowViewed() {
    return signRepository.findLowViewed();
  }


  @Override
  public Long[] mostCommented() {
    return signRepository.findMostCommented();
  }

  @Override
  public Long[] lowCommented() {
    return signRepository.findLowCommented();
  }

  @Override
  public Long[] mostRating() {
    return signRepository.findMostRating();
  }

  @Override
  public Long[] lowRating() {
    return signRepository.findLowRating();
  }

  @Override
  public List<Object[]> SignsForSignsView() {
    return signRepository.findSignsForSignsView();
  }


  @Override
  public List<Object[]> SignsAndRequestsAlphabeticalOrderAscSignsView(long userId) {
    return signRepository.findSignsAndRequestsAlphabeticalOrderAscForSignsView(userId);
  }

  @Override
  public List<Object[]> SignsAndRequestsAlphabeticalOrderDescSignsView(long userId) {
    return signRepository.findSignsAndRequestsAlphabeticalOrderDescForSignsView(userId);
  }

  @Override
  public List<Object[]> SignsAlphabeticalOrderAscSignsView() {
    return signRepository.findSignsAlphabeticalOrderAscForSignsView();
  }

  @Override
  public List<Object[]> SignsAlphabeticalOrderDescSignsView() {
    return signRepository.findSignsAlphabeticalOrderDescForSignsView();
  }

  @Override
  public Long[] SignsBellowToFavoriteByUser(long userId) {
    return signRepository.findSignsBellowToFavoriteByUser(userId);
  }


  @Override
  public List<Object[]> AllVideosHistoryForSign(long signId) {
    return signRepository.findAllVideosHistoryForSign(signId);
  }

  @Override
  public List<Object[]> AllVideosForSign(long signId) {
    return signRepository.findAllVideosForSign(signId);
  }

  @Override
  public List<Object[]> AllVideosForAllSigns() {
    return signRepository.findAllVideosForAllSigns();
  }


  @Override
  public Signs all() {
    return signsFrom(signRepository.findAll());
  }


  @Override
  public Sign withId(long id) {
    return signFrom(signRepository.findOne(id), services);
  }

  @Override
  public Sign withIdSignsView(long id) {
    return signFromSignsView(signRepository.findOne(id), services);
  }


  @Override
  public Signs withName(String name) {
    return signsFromSignsView(signRepository.findByName(name));
  }



  @Override
  public Sign create(Sign sign) {
    SignDB signDB = signRepository.save(signDBFrom(sign));
    return signFrom(signDB, services);
  }


  @Override
  public VideoDailyMotion getVideoDailyMotionDetails(String id, String url) {
    log.info("get video details for id {} with url {}", id, url);

    RestTemplate restTemplate = springRestClient.buildRestTemplate();;
    HttpEntity<String> request = new HttpEntity<String>(getHeaders());
    ResponseEntity<VideoDailyMotion> response = restTemplate.exchange(url, HttpMethod.GET, request, VideoDailyMotion.class);
    VideoDailyMotion videoDailyMotion = response.getBody();

    log.info("videoDailyMotion: " + videoDailyMotion.toString());

    return videoDailyMotion;
  }

  private HttpHeaders getHeaders(){

    AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
    if (authTokenInfo.isExpired()) {
      dalymotionToken.retrieveToken();
      authTokenInfo = dalymotionToken.getAuthTokenInfo();
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    headers.set("Authorization", "Bearer "+ authTokenInfo.getAccess_token());
    return headers;
  }

  @Override
  public Sign create(long userId, String signName, String signUrl, String pictureUri) {
    SignDB signDB;
    UserDB userDB = userRepository.findOne(userId);

    List<SignDB> signsMatches = signRepository.findByName(signName);
    if (signsMatches.isEmpty()) {
      Date now = new Date();
      VideoDB videoDB = new VideoDB();
      videoDB.setUrl(signUrl);
      videoDB.setUser(userDB);
      videoDB.setCreateDate(now);

      signDB = new SignDB();
      signDB.setName(signName);
      signDB.setUrl(signUrl);
      signDB.setNbVideo(1L);

      videoDB.setPictureUri(pictureUri);

      signDB.setCreateDate(now);
      List<VideoDB> videoDBList = new ArrayList<>();
      videoDBList.add(videoDB);
      videoDB.setIdForName(1L);
      signDB.setVideos(videoDBList);
      videoDB.setSign(signDB);

      videoRepository.save(videoDB);
      signDB.setLastVideoId(videoDB.getId());
      signDB = signRepository.save(signDB);

      userDB.getVideos().add(videoDB);
      userRepository.save(userDB);

    } else {
      Date now = new Date();

      VideoDB videoDB = new VideoDB();
      videoDB.setUrl(signUrl);
      videoDB.setCreateDate(now);
      videoDB.setUser(userDB);

      signDB = signsMatches.get(0);
      signDB.setCreateDate(now);
      signDB.setUrl(signUrl);
      long nbVideo = signDB.getNbVideo();
      signDB.setNbVideo(nbVideo+1L);
      long idForName = signDB.getVideos().get((int) nbVideo-1).getIdForName();

      videoDB.setPictureUri(pictureUri);

      videoDB.setSign(signDB);
      signDB.getVideos().add(videoDB);

      videoDB.setIdForName(idForName+1);

      videoRepository.save(videoDB);
      signDB.setLastVideoId(videoDB.getId());
      signDB = signRepository.save(signDB);

      userDB.getVideos().add(videoDB);
      userRepository.save(userDB);

      userRepository.findAll().forEach(userDB1 -> System.out.println("user id: " + userDB1.getId()));
      signDB.getVideos().stream().forEach(videoDB1 -> System.out.println("video user: " + videoDB1.getUser()));
    }
    return signFrom(signDB, services);
  }

  @Override
  public Signs search(String signName) {
    List<SignDB> signsMatches = signRepository.findByNameStartingWith(signName);

    return signsFromSignsView(signsMatches);
  }


  @Override
  public Sign addNewVideo(long userId, long signId, String signUrl, String pictureUri) {
    SignDB signDB = signRepository.findOne(signId);
    UserDB userDB = userRepository.findOne(userId);

    Date now = new Date();

    VideoDB videoDB = new VideoDB();
    videoDB.setUrl(signUrl);
    videoDB.setCreateDate(now);
    videoDB.setUser(userDB);

    signDB.setCreateDate(now);
    signDB.setUrl(signUrl);
    long nbVideo = signDB.getNbVideo();
    signDB.setNbVideo(nbVideo+1L);
    long idForName = signDB.getVideos().get((int) nbVideo-1).getIdForName();

    videoDB.setPictureUri(pictureUri);

    videoDB.setSign(signDB);
    signDB.getVideos().add(videoDB);

    videoDB.setIdForName(idForName+1);
    videoRepository.save(videoDB);
    signDB.setLastVideoId(videoDB.getId());
    signDB = signRepository.save(signDB);

    userDB.getVideos().add(videoDB);
    userRepository.save(userDB);

    userRepository.findAll().forEach(userDB1 -> System.out.println("user id: " + userDB1.getId()));
    signDB.getVideos().stream().forEach(videoDB1 -> System.out.println("video user: " + videoDB1.getUser()));

    List<Long> associatesVideoDBId = signDB.getVideos().stream()
      .map(video -> video.getId())
      .collect(Collectors.toList());
    associatesVideoDBId.remove(videoDB.getId());
    services.video().changeVideoAssociates(videoDB.getId(), associatesVideoDBId);

    return signFrom(signDB, services);
  }


  @Override
  public Sign replace(long signId, long videoId, String signUrl, String pictureUri) {
    SignDB signDB = signRepository.findOne(signId);

    Date now = new Date();

    VideoDB videoDB = videoRepository.findOne(videoId);
    videoDB.setUrl(signUrl);
    videoDB.setCreateDate(now);

    signDB.setUrl(signUrl);
    signDB.setCreateDate(now);

    videoDB.setPictureUri(pictureUri);


    videoRepository.save(videoDB);
    signDB.setLastVideoId(videoDB.getId());
    signRepository.save(signDB);

    return signFrom(signDB, services);
  }

  @Override
  public void delete(Sign sign) {
    SignDB signDB = signRepository.findOne(sign.id);
    List<VideoDB> videoDBs = new ArrayList<>();
    videoDBs.addAll(signDB.getVideos());
    videoDBs.stream()
            .map(videoDB -> services.video().withId(videoDB.getId()))
            .forEach(video -> services.video().delete(video));
    RequestDB requestDB = requestRepository.findBySign(signDB);
    if (requestDB != null) {
      requestDB.setSign(null);
    }

    signRepository.delete(signDB);
  }

  @Override
  public Request requestForSign(Sign sign) {
    SignDB signDB = signRepository.findOne(sign.id);
    RequestDB requestDB = requestRepository.findBySign(signDB);
    if (requestDB != null) {
      return requestFrom(requestDB, services);
    } else return null;
  }

  static Request requestFrom(RequestDB requestDB, Services services) {
    return new Request(requestDB.getId(), requestDB.getName(), requestDB.getRequestTextDescription(), requestDB.getRequestVideoDescription(), requestDB.getRequestDate(), SignServiceImpl.signFromRequestsView(requestDB.getSign(),  services), UserServiceImpl.userFromSignView(requestDB.getUser()));
  }

  @Override
  public Long NbRatingForSign(long signId) {
    return signRepository.findNbRatingForSign(signId);
  }


  @Override
  public Sign changeSignTextDefinition(long signId, String signTextDefinition) {
    SignDB signDB = signRepository.findOne(signId);

    signDB.setTextDefinition(signTextDefinition);
    signRepository.save(signDB);

    return signFrom(signDB, services);
  }

  @Override
  public Sign changeSignVideoDefinition(long signId, String signVideoDefinition) {
    SignDB signDB = signRepository.findOne(signId);

    signDB.setVideoDefinition(signVideoDefinition);
    signRepository.save(signDB);

    return signFrom(signDB, services);
  }

  private SignDB withDBId(long id) {
    return signRepository.findOne(id);
  }

  Signs signsFrom(Iterable<SignDB> signsDB) {
    List<Sign> signs = new ArrayList<>();
    signsDB.forEach(signDB -> signs.add(signFrom(signDB, services)));
    return new Signs(signs);
  }

  static Sign signFrom(SignDB signDB, Services services) {
    return signDB == null ? null :
      new Sign(signDB.getId(), signDB.getName(), signDB.getTextDefinition(), signDB.getVideoDefinition(), signDB.getUrl(), signDB.getCreateDate(), signDB.getLastVideoId(), signDB.getNbVideo(),VideoServiceImpl.videosFrom(signDB.getVideos()), services.video(), services.comment());
  }


  private SignDB signDBFrom(Sign sign) {
    return new SignDB(sign.name, sign.url, sign.createDate);
  }

  Signs signsFromSignsView(Iterable<SignDB> signsDB) {
    List<Sign> signs = new ArrayList<>();
    signsDB.forEach(signDB -> signs.add(signFromSignsView(signDB, services)));
    return new Signs(signs);
  }

  static Sign signFromSignsView(SignDB signDB, Services services) {
    return signDB == null ? null :
      new Sign(signDB.getId(), signDB.getName(), signDB.getTextDefinition(), signDB.getVideoDefinition(), signDB.getUrl(), signDB.getCreateDate(), signDB.getLastVideoId(), signDB.getNbVideo(), null, services.video(), services.comment());
  }

  static Sign signFromRequestsView(SignDB signDB, Services services) {
    return signDB == null ? null :
      new Sign(signDB.getId(), signDB.getName(), signDB.getTextDefinition(), signDB.getVideoDefinition(), signDB.getUrl(), signDB.getCreateDate(), 0, signDB.getNbVideo(), null, services.video(), services.comment());
  }
}
