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

import com.orange.signsatwork.biz.domain.Sign;
import com.orange.signsatwork.biz.persistence.model.SignViewData;
import com.orange.signsatwork.biz.persistence.model.VideoViewData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VideoFavoriteView {
  private long signId;
  private String signName;
  private String videoName;
  private Date createDate;
  private long videoId;
  private String url;
  private String pictureUri;
  private long nbView;
  private long averageRate;
  private long nbComment;
  private long idForName;
  private long nbVideo;
  private String label;


  public VideoFavoriteView(VideoViewData videoViewData, String label) {
    signId = videoViewData.signId;
    signName = videoViewData.signName;
    videoName = videoViewData.videoName;
    createDate = videoViewData.createDate;
    videoId = videoViewData.videoId;
    url = videoViewData.url;
    pictureUri = videoViewData.pictureUri;
    nbView = videoViewData.nbView;
    averageRate = videoViewData.averageRate;
    nbComment = videoViewData.nbComment;
    idForName = videoViewData.idForName;
    nbVideo = videoViewData.nbVideo;
    this.label = label;
  }
}
