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

import java.util.Date;
import java.util.List;

public interface SignService {

  List<Object[]> mostRecent(Date lastConnectionDate);

  List<Object[]> lowRecent(Date lastConnectionDate);

  List<Object[]> mostRecentWithoutDate();

  List<Object[]> lowRecentWithoutDate();


  Long[] mostViewed();

  Long[] lowViewed();

  Long[] lowCommented();

  Long[] mostCommented();

  Long[] mostRating();

  Long[] lowRating();

  List<Object[]> SignsForSignsView();

  List<Object[]> SignsAndRequestsAlphabeticalOrderAscSignsView(long userId);

  List<Object[]> SignsAndRequestsAlphabeticalOrderDescSignsView(long userId);

  List<Object[]> SignsAlphabeticalOrderAscSignsView();

  List<Object[]> SignsAlphabeticalOrderDescSignsView();

  List<Object[]> AllVideosHistoryForSign(long signId);

  List<Object[]> AllVideosForSign(long signId);

  List<Object[]> AllVideosForAllSigns();

  Long[] SignsBellowToFavoriteByUser(long userId);

  Signs all();

  Sign withId(long id);

  Sign withIdSignsView(long id);

  Signs withName(String name);

  Sign create(Sign sign);

  Sign create(long userId, String signName, String signUrl, String pictureUri);

  Signs search(String signName);

  Sign addNewVideo(long userId, long signId, String signUrl, String pictureUri);

  Sign replace(long signId, long videoId, String signUrl, String pictureUri);

  UrlFileUploadDailymotion getUrlFileUpload();

  VideoDailyMotion getVideoDailyMotionDetails(String id, String url);

  void delete(Sign sign);

  Request requestForSign(Sign sign);

  Long NbRatingForSign(long signId);

  Sign changeSignTextDefinition(long signId, String signTextDefinition);

  Sign changeSignVideoDefinition(long signId, String signVideoDefinition);
}
