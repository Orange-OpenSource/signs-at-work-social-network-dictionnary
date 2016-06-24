package com.orange.spring.demo.biz.persistence.service.impl;

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

import com.orange.spring.demo.biz.domain.Request;
import com.orange.spring.demo.biz.domain.Requests;
import com.orange.spring.demo.biz.domain.Video;
import com.orange.spring.demo.biz.domain.Videos;
import com.orange.spring.demo.biz.persistence.model.RequestDB;
import com.orange.spring.demo.biz.persistence.model.SignDB;
import com.orange.spring.demo.biz.persistence.model.VideoDB;
import com.orange.spring.demo.biz.persistence.repository.RequestRepository;
import com.orange.spring.demo.biz.persistence.repository.SignRepository;
import com.orange.spring.demo.biz.persistence.repository.UserRepository;
import com.orange.spring.demo.biz.persistence.repository.VideoRepository;
import com.orange.spring.demo.biz.persistence.service.RequestService;
import com.orange.spring.demo.biz.persistence.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {
  private final VideoRepository videoRepository;

  @Override
  public Videos all() {
    return videosFrom(videoRepository.findAll());
  }

  @Override
  public Video withId(long id) {
    return videoFrom(videoRepository.findOne(id));
  }


  static Videos videosFrom(Iterable<VideoDB> videosDB) {
    List<Video> videos = new ArrayList<>();
    videosDB.forEach(videoDB -> videos.add(videoFrom(videoDB)));
    return new Videos(videos);
  }

  static Video videoFrom(VideoDB videoDB) {
    return new Video(videoDB.getId(), videoDB.getUrl(), videoDB.getCreateDate(), UserServiceImpl.userFromSignView(videoDB.getUser()), null);
  }

  private VideoDB videoDBFrom(Video video) {
    VideoDB videoDB = new VideoDB(video.url, video.createDate);
    return videoDB;
  }
}
