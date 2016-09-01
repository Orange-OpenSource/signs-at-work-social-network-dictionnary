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

import com.orange.signsatwork.biz.domain.Sign;
import com.orange.signsatwork.biz.domain.Signs;
import com.orange.signsatwork.biz.persistence.model.SignDB;
import com.orange.signsatwork.biz.persistence.model.UserDB;
import com.orange.signsatwork.biz.persistence.model.VideoDB;
import com.orange.signsatwork.biz.persistence.repository.*;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.persistence.service.SignService;
import com.vimeo.networking.Configuration;
import com.vimeo.networking.Vimeo;
import com.vimeo.networking.VimeoClient;
import com.vimeo.networking.callbacks.ModelCallback;
import com.vimeo.networking.model.PictureCollection;
import com.vimeo.networking.model.Video;
import com.vimeo.networking.model.error.VimeoError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

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

  @Override
  public Long[] mostCommented() {
    return commentRepository.findMostCommented();
  }

  @Override
  public Long[] lowCommented() {
    return commentRepository.findLowCommented();
  }

  @Override
  public List<Object[]> mostRating() {
    return ratingRepository.findMostRating();
  }

  @Override
  public List<Object[]> lowRating() {
    return ratingRepository.findLowRating();
  }

  @Override
  public Signs all() {
    return signsFrom(signRepository.findAll());
  }

  @Override
  public Signs createAfterLastDateConnection(Date lastConnectionDate) {
    return signsFrom(signRepository.findSignCreateAfterLastDateConnection(lastConnectionDate));
  }

  @Override
  public Signs createBeforeLastDateConnection(Date lastConnectionDate) {
    return signsFrom(signRepository.findSignCreateBeforeLastDateConnection(lastConnectionDate));
  }


  @Override
  public Signs allBySearchTerm(String searchTerm) {
    return signsFrom(signRepository.findAllBySearchTerm(searchTerm));
  }

  @Override
  public Signs createAfterLastDateConnectionBySearchTerm(Date lastConnectionDate, String searchTerm) {
    return signsFrom(signRepository.findSignCreateAfterLastDateConnectionBySearchTerm(lastConnectionDate, searchTerm));
  }

  @Override
  public Signs createBeforeLastDateConnectionBySearchTerm(Date lastConnectionDate, String searchTerm) {
    return signsFrom(signRepository.findSignCreateBeforeLastDateConnectionBySearchTerm(lastConnectionDate, searchTerm));
  }

  @Override
  public Sign withId(long id) {
    return signFrom(signRepository.findOne(id), services);
  }

  @Override
  public Sign withIdLoadAssociates(long id) {
    return signFromWithAssociates(signRepository.findOne(id));
  }

  @Override
  public Signs forFavorite(long favoriteId) {
    return signsFrom(
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



  private void waitForPictureUri(final VideoDB videoDB, final SignDB signDB, String signUrl) {

    if (signUrl.startsWith("http")) {
      URL videoUrl = null;
      try {
        videoUrl = new URL(signUrl);
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }

      if (videoUrl.getHost().equals("vimeo.com")) {

        String uri = "videos" + videoUrl.getPath();
        VimeoClient.getInstance().fetchNetworkContent(uri, new ModelCallback<Video>(Video.class) {
          @Override
          public void success(Video video) {
            String pictureUri = video.pictures.sizes.get(3).link; // Résolution 640x480
            String vimeoUrl = "https://player.vimeo.com/video"+ uri.substring(6, uri.length());

            synchronized (videoDB) {
              videoDB.setPictureUri(pictureUri);
              videoDB.setUrl(vimeoUrl);
              signDB.setUrl(vimeoUrl);
              videoDB.notifyAll();
            }
          }

          @Override
          public void failure(VimeoError error) {
            // voice the error
          }
        });


        synchronized (videoDB) {
          try {
            videoDB.wait(15000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }

    return;
  }


  @Override
  public Sign create(long userId, String signName, String signUrl) {
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

      waitForPictureUri(videoDB, signDB, signUrl);

      signDB.setCreateDate(now);
      List<VideoDB> videoDBList = new ArrayList<>();
      videoDBList.add(videoDB);
      signDB.setVideos(videoDBList);
      videoDB.setSign(signDB);

      videoRepository.save(videoDB);
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

      waitForPictureUri(videoDB, signDB, signUrl);

      videoDB.setSign(signDB);
      signDB.getVideos().add(videoDB);

      videoRepository.save(videoDB);
      signDB = signRepository.save(signDB);

      userDB.getVideos().add(videoDB);
      userRepository.save(userDB);

      userRepository.findAll().forEach(userDB1 -> System.out.println("user id: " + userDB1.getId()));
      signDB.getVideos().stream().forEach(videoDB1 -> System.out.println("video user: " + videoDB1.getUser()));
    }
    return signFrom(signDB, services);
  }


  @Override
  public Sign replace(long userId, long signId, String signUrl) {
    SignDB signDB = signRepository.findOne(signId);
    UserDB userDB = userRepository.findOne(userId);


    Date now = new Date();

    VideoDB videoDB = new VideoDB();
    videoDB.setUrl(signUrl);
    videoDB.setCreateDate(now);
    videoDB.setUser(userDB);

    signDB.setUrl(signUrl);
    signDB.setCreateDate(now);
    videoDB.setSign(signDB);
    signDB.getVideos().add(videoDB);

    videoRepository.save(videoDB);
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
      new Sign(signDB.getId(), signDB.getName(), signDB.getUrl(), signDB.getCreateDate(), VideoServiceImpl.videosFrom(signDB.getVideos()), null, null, services.video(), services.comment());
  }

  Sign signFromWithAssociates(SignDB signDB) {
    return signDB == null ? null :
      new Sign(signDB.getId(), signDB.getName(), signDB.getUrl(), signDB.getCreateDate(), null, signsFrom(signDB.getAssociates()).ids(), signsFrom(signDB.getReferenceBy()).ids(), services.video(), services.comment());
  }

  private SignDB signDBFrom(Sign sign) {
    return new SignDB(sign.name, sign.url, sign.createDate);
  }
}
