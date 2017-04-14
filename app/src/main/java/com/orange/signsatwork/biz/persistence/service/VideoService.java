package com.orange.signsatwork.biz.persistence.service;

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

import java.util.List;

public interface VideoService {
  Videos all();

  Video withId(long videoId);

  List<Object[]> AssociateVideos(long videoId, long associateVideoId);

  void increaseNbView(long videoId);

  Comment createVideoComment(long videoId, long userId, String commentText);

  RatingDat createVideoRating(long videoId, long userId, Rating rating);

  Object[] RatingForVideoByUser(long videoId, long userId);

  List<Object[]> AllCommentsForVideo(long videoId);

  Videos forSign(long signId);

  Rating ratingFor(Video video, long userId);

  Videos forUser(long userId);

  void delete(Video video);

  Video changeVideoAssociates(long videoId, List<Long> associateVideosIds);

  Video withIdLoadAssociates(long id);

  Videos forFavorite(long favoriteId);

  List<Object[]> VideosForFavoriteView(long favoriteId);

  Long[] VideosForAllFavoriteByUser(long userId);

  Long NbFavoriteBelowVideoForUser(long videoId, long userId);

  List<Object[]> AllVideosCreateByUser(long userId);

}
