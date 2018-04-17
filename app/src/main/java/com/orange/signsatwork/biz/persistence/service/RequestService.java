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

import com.orange.signsatwork.biz.domain.Request;
import com.orange.signsatwork.biz.domain.Requests;
import com.orange.signsatwork.biz.persistence.model.SignDB;

import java.util.List;

public interface RequestService {
  Requests all();

  Requests requestsforUser(long id);

  Requests requestsforUserWithoutSignAssociate(long userId);

  Requests requestsforUserWithSignAssociate(long userId);

  Requests requestsforOtherUserWithoutSignAssociate(long userId);

  Request changeSignRequest(long requestId, long signId);

  Request changeRequestTextDescription(long requestId, String requestTextDescription);

  Request changeRequestVideoDescription(long requestId, String requestVideoDescription);

  Request withId(long id);

  Requests withName(String name);

  Request create(Request request);

  Request rename(long requestId, String requestName, String requestTextDescription);

  Request create(long userId, String requestName, String requestTextDescription);

  Request create(long userId, String requestName, String requestTextDescription, String requestVideoDescription);

  Request priorise(long requestId);

  void delete(Request request);

  List<Object[]> requestsByNameWithNoAssociateSign(String requestName, long userId);

  List<Object[]> requestsByNameWithAssociateSign(String requestName, long userId);

  Requests myRequestMostRecent(long userId);

  Requests myRequestlowRecent(long userId);

  Requests myRequestAlphabeticalOrderAsc(long userId);

  Requests myRequestAlphabeticalOrderDesc(long userId);

  Requests otherRequestWithNoSignMostRecent(long userId);

  Requests otherRequestWithNoSignlowRecent(long userId);

  Requests otherRequestWithNoSignAlphabeticalOrderAsc(long userId);

  Requests otherRequestWithNoSignAlphabeticalOrderDesc(long userId);
}
