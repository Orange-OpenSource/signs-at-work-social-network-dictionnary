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

import com.orange.signsatwork.biz.domain.Ratings;
import com.orange.signsatwork.biz.domain.Video;
import com.orange.signsatwork.biz.domain.Videos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VideoView {
  private long id;
  private long idForName;
  private String url;
  private String pictureUri;
  private Date createDate;
  // TODO Transform RatingDB en Rating
  private Ratings ratings;

  public Video toVideo() {
    return new Video(id, idForName, url, pictureUri, 0, 0, createDate, null, null, null, null, null);
  }

  public static VideoView from(Video video) {
    return new VideoView(video.id, video.idForName, video.url, video.pictureUri, video.createDate, video.ratings);

  }

  public static List<VideoView> from(Videos videos) {
    return videos
            .stream()
            .map(VideoView::from)
            .collect(Collectors.toList());
  }
}
