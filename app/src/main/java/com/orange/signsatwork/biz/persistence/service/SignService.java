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


  List<Object[]> mostRecentWithoutDate(long userId);

  List<Object[]> mostRecentWithoutDate();

  List<Object[]> lowRecentWithoutDate(long userId);

  List<Object[]> lowRecentWithoutDate();

  Long[] mostViewed(long userId);

  Long[] mostViewed();

  Long[] lowViewed(long userId);

  Long[] lowCommented(long userId);

  Long[] mostCommented(long userId);

  Long[] mostCommented();

  Long[] mostRating(long userId);

  Long[] mostRating();

  Long[] lowRating(long userId);

  Long[] lowRating();

  List<Object[]> SignsForSignsView(long userId);

  List<Object[]> SignsForSignsView();

  List<Object[]> SignsAlphabeticalOrderAscSignsView(long userId);

  List<Object[]> SignsAlphabeticalOrderDescSignsView(long userId);

  List<Object[]> SignsAlphabeticalOrderAscSignsView();

  List<Object[]> SignsAlphabeticalOrderDescSignsView();

  List<Object[]> AllVideosHistoryForSign(long signId);

  List<Object[]> AllVideosForSign(long userId, long signId);

  List<Object[]> AllVideosForAllSigns(long userId);

  Long[] SignsBellowToFavoriteByUser(long userId);

  Signs all();

  Sign withId(long id);

  Sign withIdSignsView(long id);

  Signs withName(String name);

  Signs withNameIgnoreCase(String name);

  Sign create(Sign sign);

  Sign create(long userId, String signName, String signUrl, String pictureUri);

  void updateWithDailymotionInfo(long signId, long videoId, String pictureUri, String videoUrl);

  Signs search(String signName);

  List<Object[]> searchBis(String signName);

  List<Object[]> searchFull(String signName);

  Sign addNewVideo(long userId, long signId, String signUrl, String pictureUri);

  Sign replace(long signId, long videoId, String signUrl, String pictureUri);

  UrlFileUploadDailymotion getUrlFileUpload();

  VideoDailyMotion getVideoDailyMotionDetails(String id, String url);

  void delete(Sign sign);

  Request requestForSign(Sign sign);

  Long NbRatingForSign(long signId);

  Sign changeSignTextDefinition(long signId, String signTextDefinition);

  Sign changeSignVideoDefinition(long signId, String signVideoDefinition);

 Sign deleteSignVideoDefinition(long signId);

  void renameSign(Long signId, String name);

  void renameSignAndAssociateToRequest(Long signId, long requestId, String name);

}
