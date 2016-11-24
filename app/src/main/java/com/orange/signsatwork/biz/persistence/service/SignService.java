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

import com.orange.signsatwork.biz.domain.UrlFileUploadDailymotion;
import com.orange.signsatwork.biz.domain.Sign;
import com.orange.signsatwork.biz.domain.Signs;
import com.orange.signsatwork.biz.domain.VideoDailyMotion;

import java.util.Date;
import java.util.List;

public interface SignService {

  Long[] lowCommented();

  Long[] mostCommented();

  List<Object[]> mostRating();

  List<Object[]> lowRating();

  Signs allOrderByCreateDateAsc();

  Signs all();

  Signs createAfterLastDateConnection(Date lastConnectionDate);

  Signs createBeforeLastDateConnection(Date lastConnectionDate);

  Signs allBySearchTerm(String searchTerm);

  Signs allBySearchTermOrderByCreateDateDesc(String searchTerm);

  Signs createAfterLastDateConnectionBySearchTerm(Date lastConnectionDate, String searchTerm);

  Signs createBeforeLastDateConnectionBySearchTerm(Date lastConnectionDate, String searchTerm);

  Signs forFavorite(long id);

  Sign withId(long id);

  Sign withIdSignsView(long id);

  Sign withIdLoadAssociates(long id);

  Sign changeSignAssociates(long signId, List<Long> associateSignsIds);

  Sign create(Sign sign);

  Sign create(long userId, String signName, String signUrl, String pictureUri);

  Sign replace(long userId, long signId, String signUrl);

  String getStreamUrl(String signUrl);

  UrlFileUploadDailymotion getUrlFileUpload();

  VideoDailyMotion getVideoDailyMotionDetails(String id, String url);

  void delete(Sign sign);
}
