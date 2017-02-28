package com.orange.signsatwork.biz.view.model;

/*
 * #%L
 * videos at work
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


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VideosViewSort {

  /**
   * Sort criteria:
   *  - created since last connection first, then
   *  - modified (comment added or video changed) since last connection, then
   *  - the rest
   * @param videos to sort
   * @return sorted videos
   */
  public List<VideoView2> sort(List<VideoView2> videos) {
    List<VideoView2> createdSinceLastDeconnection = videos.stream()
      .filter(VideoView2::createdSinceLastDeconnection)
      .collect(Collectors.toList());

    videos.removeAll(createdSinceLastDeconnection);

    List<VideoView2> commentAdded = videos.stream()
      .filter(VideoView2::hasComment)
      .collect(Collectors.toList());

    videos.removeAll(commentAdded);

    List<VideoView2> viewAdded = videos.stream()
      .filter(VideoView2::hasView)
      .collect(Collectors.toList());

    videos.removeAll(viewAdded);

    List<VideoView2> positiveRateAdded = videos.stream()
      .filter(VideoView2::hasPositiveRate)
      .collect(Collectors.toList());

    videos.removeAll(positiveRateAdded);

    List<VideoView2> favoriteAdded = videos.stream()
      .filter(VideoView2::belowToFavorite)
      .collect(Collectors.toList());

    videos.removeAll(favoriteAdded);

    List<VideoView2> sortedvideos = new ArrayList<>();
    sortedvideos.addAll(createdSinceLastDeconnection);
    sortedvideos.addAll(commentAdded);
    sortedvideos.addAll(viewAdded);
    sortedvideos.addAll(videos);
    sortedvideos.addAll(positiveRateAdded);
    sortedvideos.addAll(favoriteAdded);

    return sortedvideos;
  }
}
