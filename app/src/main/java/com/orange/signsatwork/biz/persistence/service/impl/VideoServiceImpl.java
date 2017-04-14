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

import com.orange.signsatwork.biz.domain.*;
import com.orange.signsatwork.biz.persistence.model.*;
import com.orange.signsatwork.biz.persistence.repository.*;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.persistence.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VideoServiceImpl implements VideoService {
  private final VideoRepository videoRepository;
  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final RatingRepository ratingRepository;
  private final SignRepository signRepository;
  private final FavoriteRepository favoriteRepository;
  private final Services services;

  @Override
  public Videos all() {
    return videosFrom(videoRepository.findAll());
  }

  @Override
  public Video withId(long videoId) {
    return videoFrom(videoRepository.findOne(videoId));
  }


  @Override
  public List<Object[]> AssociateVideos(long videoId, long associateVideoId) {
    return videoRepository.findAssociateVideos(videoId, associateVideoId);
  }

  @Override
  public void increaseNbView(long videoId) {
    VideoDB videoDB = videoRepository.findOne(videoId);
    long nbView = videoDB.getNbView();
    videoDB.setNbView(nbView+1);
    videoRepository.save(videoDB);
    return;
  }

  @Override
  public Comment createVideoComment(long videoId, long userId, String commentText) {
    VideoDB videoDB = videoRepository.findOne(videoId);
    UserDB userDB = userRepository.findOne(userId);

    CommentDB commentDB = new CommentDB();
    commentDB.setCommentDate(new Date());
    commentDB.setText(commentText);
    commentDB.setVideo(videoDB);
    commentDB.setUser(userDB);

    commentRepository.save(commentDB);
    long nbComment = videoDB.getNbComment();
    videoDB.setNbComment(nbComment +1);
    videoRepository.save(videoDB);

    return CommentServiceImpl.commentFrom(commentDB);
  }

  @Override
  public RatingDat createVideoRating(long videoId, long userId, Rating rating) {
    VideoDB videoDB = videoRepository.findOne(videoId);
    UserDB userDB = userRepository.findOne(userId);
    Object[] queryRating = services.video().RatingForVideoByUser(videoId, userId);
    RatingData ratingData = new RatingData(queryRating);

    RatingDB ratingDB = new RatingDB();
    ratingDB.setUser(userDB);
    ratingDB.setVideo(videoDB);
    ratingDB.setRatingDate(new Date());
    ratingDB.setRating(rating);

    ratingRepository.save(ratingDB);

    long averageRate = videoDB.getAverageRate();
    if (rating.equals(Rating.Positive) && (!ratingData.ratePositive)) {
      averageRate = averageRate + 1;
    } else if (rating.equals(Rating.Negative) && (!ratingData.rateNegative)) {
      averageRate = averageRate - 1;
    }
    videoDB.setAverageRate(averageRate);
    videoRepository.save(videoDB);

    return RatingServiceImpl.ratingFrom(ratingDB);
  }

  @Override
  public Object[] RatingForVideoByUser(long videoId, long userId) {
    return videoRepository.findRatingForVideoByUser(videoId, userId);
  }

  @Override
  public List<Object[]> AllCommentsForVideo(long videoId) {
    return videoRepository.findAllCommentsForVideo(videoId);
  }

  @Override
  public Videos forSign(long signId) {
    return videosFrom(videoRepository.findBySign(signRepository.findOne(signId)));
  }

  @Override
  @Transactional
  public Rating ratingFor(Video video, long userId) {
    VideoDB videoDB = videoRepository.findOne(video.id);
    List<RatingDB> videos = videoDB.getRatings();
    Optional<RatingDB> rating = videos.stream()
            .filter(ratingDB -> ratingDB.getUser().getId() == userId)
            .findAny();

    return rating.isPresent() ? rating.get().getRating() : Rating.NoRate;
  }

  @Override
  public Videos forUser(long userId) {
    return videosFrom(videoRepository.findByUser(userRepository.findOne(userId)));
  }

  @Override
  public void delete(Video video) {
    VideoDB videoDB = videoRepository.findOne(video.id);
    SignDB signDB = videoDB.getSign();
    if ((signDB.getLastVideoId() == video.id) && (signDB.getNbVideo() > 1)) {
      VideoDB lastVideoDB = signDB.getVideos().get((int) signDB.getNbVideo() - 2);
      long lastVideoId = lastVideoDB.getId();
      signDB.setLastVideoId(lastVideoId);
      signDB.setCreateDate(lastVideoDB.getCreateDate());
      signDB.setUrl(lastVideoDB.getUrl());
    }

    signDB.setNbVideo(signDB.getNbVideo()-1);
    signRepository.save(signDB);

    List<CommentDB> commentDBs = new ArrayList<>();
    commentDBs.addAll(videoDB.getComments());
    List<RatingDB> ratingDBs = new ArrayList<>();
    ratingDBs.addAll(videoDB.getRatings());

    commentDBs.forEach(commentDB -> services.comment().delete(CommentServiceImpl.commentFrom(commentDB)));
    ratingDBs.forEach(ratingDB -> services.rating().delete(ratingDB));

    videoDB.getUser().getVideos().remove(videoDB);
    videoDB.getSign().getVideos().remove(videoDB);
    videoDB.getReferenceBy().forEach(s -> s.getAssociates().remove(videoDB));
    videoDB.getFavorites().forEach(favoriteDB -> favoriteDB.getVideos().remove(videoDB));

    videoRepository.delete(videoDB);

  }

  static Videos videosFrom(Iterable<VideoDB> videosDB) {
    List<Video> videos = new ArrayList<>();
    videosDB.forEach(videoDB -> videos.add(videoFrom(videoDB)));
    return new Videos(videos);
  }

  static Video videoFrom(VideoDB videoDB) {
    return new Video(videoDB.getId(), videoDB.getIdForName(), videoDB.getUrl(), videoDB.getPictureUri(), 0, videoDB.getAverageRate(), videoDB.getCreateDate(), UserServiceImpl.userFromSignView(videoDB.getUser()), null, RatingServiceImpl.ratingsFrom(videoDB.getRatings()),null,null);
  }


  static Video videoFromRatingView(VideoDB videoDB) {
    return new Video(videoDB.getId(), videoDB.getIdForName(), videoDB.getUrl(), videoDB.getPictureUri(), 0, 0, videoDB.getCreateDate(), null, null, null, null, null);
  }

  static Videos videosFromSignsView(Iterable<VideoDB> videosDB) {
    List<Video> videos = new ArrayList<>();
    videosDB.forEach(videoDB -> videos.add(videoFromSignsView(videoDB)));
    return new Videos(videos);
  }

  static Video videoFromSignsView(VideoDB videoDB) {
    return new Video(videoDB.getId(), videoDB.getIdForName(), videoDB.getUrl(), videoDB.getPictureUri(), 0, 0, videoDB.getCreateDate(), null, null, null, null, null);
  }

  @Override
  public Video changeVideoAssociates(long videoId, List<Long> associateVideosIds) {
    VideoDB videoDB = videoRepository.findOne(videoId);
    List<VideoDB> videoReferenceBy = videoDB.getReferenceBy();

    if (videoReferenceBy != null) {
      videoReferenceBy.stream()
        .filter(R -> !associateVideosIds.contains(R.getId()))
        .forEach(R -> {
          R.getAssociates().remove(videoDB);
          videoRepository.save(R);
        });
    }

    List<VideoDB> newVideoAssociates = new ArrayList<>();
    for (Long id : associateVideosIds ) {
      VideoDB videoDB1 = videoRepository.findOne(id);
      newVideoAssociates.add(videoDB1);
    }

    videoDB.setAssociates(newVideoAssociates);
    videoDB.setReferenceBy(new ArrayList<>());
    videoRepository.save(videoDB);

    return videoFrom(videoDB);
  }

  @Override
  public Video withIdLoadAssociates(long id) {
    return videoFromWithAssociates(videoRepository.findOne(id));
  }

  Video videoFromWithAssociates(VideoDB videoDB) {
    return videoDB == null ? null :
      new Video(videoDB.getId(), videoDB.getIdForName(), videoDB.getUrl(), videoDB.getPictureUri(), 0, 0, videoDB.getCreateDate(), null, null, null, videosFromSignsView(videoDB.getAssociates()).ids(), videosFromSignsView(videoDB.getReferenceBy()).ids());
  }

  @Override
  public Videos forFavorite(long favoriteId) {
    return videosFromSignsView(
      videoRepository.findByFavorite(favoriteRepository.findOne(favoriteId))
    );
  }

  @Override
  public List<Object[]> VideosForFavoriteView(long favoriteId) {
    return videoRepository.findVideosForFavoriteView(favoriteId);
  }

  @Override
  public Long[] VideosForAllFavoriteByUser(long userId) {
    return videoRepository.findVideosForAllFavoriteByUser(userId);
  }

  @Override
  public Long NbFavoriteBelowVideoForUser(long videoId, long userId) {
    return videoRepository.findNbFavoriteBelowVideoForUser(videoId, userId);
  }

  @Override
  public List<Object[]> AllVideosCreateByUser(long userId) {
    return videoRepository.findAllVideosCreateByUser(userId);
  }
}
