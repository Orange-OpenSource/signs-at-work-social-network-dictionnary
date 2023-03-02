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

import com.orange.signsatwork.biz.domain.Communities;
import com.orange.signsatwork.biz.domain.Community;
import com.orange.signsatwork.biz.domain.CommunityType;

import java.util.List;

public interface CommunityService {
  Communities all();

  Communities all(CommunityType communityType);

  List<Object[]> allForFavorite(long userId);

  List<Object[]> allForJob(long userId);

  Communities forUser(long id);

  List<Object[]> forCommunitiesUser(long id);

  Community withId(long id);

  Community create(long userId, Community community);

  void delete(Community community);

  Community withCommunityName(String communityName);

  Communities forFavorite(long favoriteId);

  Community changeCommunityUsers(long communityId, List<Long> usersIds);

  Communities search(String communityName);

  List<Object[]> searchBis(String communityName);

  Community updateName(long communityId, String communityName);

  Community removeMeFromCommunity(long communityId, long userId);

  Community updateDescriptionText(long communityId, String communityDescriptionText);

  Community changeDescriptionVideo(long communityId, String communityDescriptionVideo);

  Community deleteCommunityVideoDescription(long communityId);
}
