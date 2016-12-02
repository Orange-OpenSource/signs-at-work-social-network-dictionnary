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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
  private final RatingRepository ratingRepository;
  private final Services services;

  @Autowired
  SpringRestClient springRestClient;
  @Autowired
  DalymotionToken dalymotionToken;
  @Autowired
  AppProfile appProfile;

  String REST_SERVICE_URI = "https://api.dailymotion.com";
  String VIDEO_THUMBNAIL_FIELDS = "thumbnail_url,thumbnail_60_url,thumbnail_120_url,thumbnail_180_url,thumbnail_240_url,thumbnail_360_url,thumbnail_480_url,thumbnail_720_url,";
  String VIDEO_STREAM_FIELDS = "stream_h264_hd1080_url,stream_h264_hd_url,stream_h264_hq_url,stream_h264_qhd_url,stream_h264_uhd_url,stream_h264_url,";
  String VIDEO_EMBED_FIELD = "embed_url";

  @Override
  public UrlFileUploadDailymotion getUrlFileUpload() {
    RestTemplate restTemplate = springRestClient.buildRestTemplate();
    HttpEntity<String> request = new HttpEntity<String>(getHeaders());
    ResponseEntity<UrlFileUploadDailymotion> response = restTemplate.exchange(REST_SERVICE_URI + "/file/upload", HttpMethod.GET, request, UrlFileUploadDailymotion.class);
    UrlFileUploadDailymotion urlfileUploadDailyMotion = response.getBody();
    return urlfileUploadDailyMotion;
  }


  @Override
  public String getStreamUrl(String signUrl) {
    if (signUrl.contains("www.dailymotion.com/embed/video")) {
      if ((dalymotionToken.getDailymotionCache().cacheurl.get(signUrl) == null) || (dalymotionToken.getAuthTokenInfo().isExpired())) {

          URL videoUrl = null;
          try {
            videoUrl = new URL(signUrl);
          } catch (MalformedURLException e) {
            e.printStackTrace();
          }

          String path = videoUrl.getPath();
          String id = path.substring(path.lastIndexOf('/') + 1);

          VideoDailyMotion videoDailyMotion = getVideoDailyMotionDetails(id, REST_SERVICE_URI+"/video/"+id+"?ssl_assets=true&fields=" + VIDEO_STREAM_FIELDS + VIDEO_EMBED_FIELD);

          if (isUrlValid(videoDailyMotion.stream_h264_url) && isUrlValid(videoDailyMotion.embed_url)) {
            dalymotionToken.getDailymotionCache().append(videoDailyMotion.embed_url, videoDailyMotion.stream_h264_url);
            log.warn("getSreamUrl : embed_url = {} / stream_h264_url = {}", videoDailyMotion.embed_url, videoDailyMotion.stream_h264_url);
          }
        }
    }
    return dalymotionToken.getDailymotionCache().cacheurl.get(signUrl);
  }

  @Override
  public Long[] mostCommented() {
    return commentRepository.findMostCommented();
  }

  @Override
  public Long[] lowCommented() {
    return commentRepository.findLowCommented();
  }

  @Override
  public Long[] mostRating() {
    return ratingRepository.findMostRating();
  }

  @Override
  public Long[] lowRating() {
    return ratingRepository.findLowRating();
  }

  @Override
  public List<Object[]> SignsForSignsView() {
    return signRepository.findSignsForSignsView();
  }

  @Override
  public List<Object[]> SignsForFavoriteView(long favoriteId) {
    return signRepository.findSignsForFavoriteView(favoriteId);
  }

  @Override
  public List<Object[]> AssociateSigns(long signId, long associateSignId) {
    return signRepository.findAssociateSigns(signId, associateSignId);
  }

  @Override
  public Object[] RatingForSignByUser(long signId, long userId) {
    return signRepository.findRatingForSignByUser(signId, userId);
  }


  @Override
  public List<Object[]> AllCommentsForSign(long signId) {
    return signRepository.findAllCommentsForSign(signId);
  }

  @Override
  public List<Object[]> AllVideosHistoryForSign(long signId) {
    return signRepository.findAllVideosHistoryForSign(signId);
  }

  @Override
  public List<Object[]> SignsForSignsViewBySearchTerm(String searchTerm) {
    return signRepository.findSignsForSignsViewBySearchTerm(searchTerm);
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
  public Sign withIdLoadAssociates(long id) {
    return signFromWithAssociates(signRepository.findOne(id));
  }

  @Override
  public Signs forFavorite(long favoriteId) {
    return signsFromSignsView(
            signRepository.findByFavorite(favoriteRepository.findOne(favoriteId))
    );
  }

  @Override
  public Sign changeSignAssociates(long signId, List<Long> associateSignsIds) {
    SignDB signDB = withDBId(signId);
    List<SignDB> signReferenceBy = signDB.getReferenceBy();

    signReferenceBy.stream()
            .filter(R -> !associateSignsIds.contains(R.getId()))
            .forEach(R -> {
      R.getAssociates().remove(signDB);
      signRepository.save(R);
    });

    List<SignDB> newSignAssociates = new ArrayList<>();
    for (Long id : associateSignsIds ) {
      SignDB signDB1 = withDBId(id);
      newSignAssociates.add(signDB1);
    }

    signDB.setAssociates(newSignAssociates);
    signDB.setReferenceBy(new ArrayList<>());
    signRepository.save(signDB);

    return signFrom(signDB, services);
  }

  @Override
  public Sign create(Sign sign) {
    SignDB signDB = signRepository.save(signDBFrom(sign));
    return signFrom(signDB, services);
  }


  private boolean isUrlValid(String url) {
    return url != null && !url.isEmpty();
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

      videoDB.setPictureUri(pictureUri);

      signDB.setCreateDate(now);
      List<VideoDB> videoDBList = new ArrayList<>();
      videoDBList.add(videoDB);
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

      videoDB.setPictureUri(pictureUri);

      videoDB.setSign(signDB);
      signDB.getVideos().add(videoDB);


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
  public Sign replace(long userId, long signId, String signUrl, String pictureUri) {
    SignDB signDB = signRepository.findOne(signId);
    UserDB userDB = userRepository.findOne(userId);


    Date now = new Date();

    VideoDB videoDB = new VideoDB();
    videoDB.setUrl(signUrl);
    videoDB.setCreateDate(now);
    videoDB.setUser(userDB);

    signDB.setUrl(signUrl);
    signDB.setCreateDate(now);

    videoDB.setPictureUri(pictureUri);

    videoDB.setSign(signDB);
    signDB.getVideos().add(videoDB);

    videoRepository.save(videoDB);
    signDB.setLastVideoId(videoDB.getId());
    signDB = signRepository.save(signDB);

    userDB.getVideos().add(videoDB);
    userRepository.save(userDB);

    userRepository.findAll().forEach(userDB1 -> System.out.println("user id: " + userDB1.getId()));
    signDB.getVideos().stream().forEach(videoDB1 -> System.out.println("video user: " + videoDB1.getUser()));

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
    signDB.getFavorites().forEach(favoriteDB -> favoriteDB.getSigns().remove(signDB));
    signDB.getReferenceBy().forEach(s -> s.getAssociates().remove(signDB));
    signRepository.delete(signDB);
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
      new Sign(signDB.getId(), signDB.getName(), signDB.getUrl(), signDB.getCreateDate(), signDB.getLastVideoId(), VideoServiceImpl.videosFrom(signDB.getVideos()), null, null, services.video(), services.comment());
  }

  Sign signFromWithAssociates(SignDB signDB) {
    return signDB == null ? null :
      new Sign(signDB.getId(), signDB.getName(), signDB.getUrl(), signDB.getCreateDate(), signDB.getLastVideoId(), null, signsFromSignsView(signDB.getAssociates()).ids(), signsFromSignsView(signDB.getReferenceBy()).ids(), services.video(), services.comment());
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
      new Sign(signDB.getId(), signDB.getName(), signDB.getUrl(), signDB.getCreateDate(), signDB.getLastVideoId(), null, null, null, services.video(), services.comment());
  }

  static Sign signFromRequestsView(SignDB signDB, Services services) {
    return signDB == null ? null :
      new Sign(signDB.getId(), signDB.getName(), signDB.getUrl(), signDB.getCreateDate(), 0, null, null, null, services.video(), services.comment());
  }
}
