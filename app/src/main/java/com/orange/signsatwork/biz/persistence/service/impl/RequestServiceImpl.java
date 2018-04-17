package com.orange.signsatwork.biz.persistence.service.impl;

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
import com.orange.signsatwork.biz.domain.Signs;
import com.orange.signsatwork.biz.persistence.model.RequestDB;
import com.orange.signsatwork.biz.persistence.model.SignDB;
import com.orange.signsatwork.biz.persistence.model.UserDB;
import com.orange.signsatwork.biz.persistence.repository.RequestRepository;
import com.orange.signsatwork.biz.persistence.repository.SignRepository;
import com.orange.signsatwork.biz.persistence.repository.UserRepository;
import com.orange.signsatwork.biz.persistence.service.RequestService;
import com.orange.signsatwork.biz.persistence.service.Services;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {
  private final UserRepository userRepository;
  private final RequestRepository requestRepository;
  private final SignRepository signRepository;
  private final SignServiceImpl signServiceImpl;
  private final Services services;

  @Override
  public Requests all() {
    return requestsFrom(requestRepository.findAll());
  }

  @Override
  public Request withId(long id) {
    return requestFrom(requestRepository.findOne(id), services);
  }


  @Override
  public Requests withName(String name) {
    return requestsFrom(requestRepository.findByName(name));
  }

  @Override
  public Requests requestsforUser(long userId) {
    return requestsFrom(
            requestRepository.findByUser(userRepository.findOne(userId))
    );
  }


  @Override
  public Requests requestsforUserWithoutSignAssociate(long userId) {
    return requestsFrom(
            requestRepository.findByUserWithoutSignAssociate(userRepository.findOne(userId))
    );
  }


  @Override
  public Requests requestsforUserWithSignAssociate(long userId) {
    return requestsFrom(
            requestRepository.findByUserWithSignAssociate(userRepository.findOne(userId))
    );
  }

  @Override
  public Requests requestsforOtherUserWithoutSignAssociate(long userId) {
    return requestsFrom(
            requestRepository.findByOtherUserWithoutSignAssociate(userRepository.findOne(userId))
    );
  }

  @Override
  public Request changeSignRequest(long requestId, long signId) {
    RequestDB requestDB = requestRepository.findOne(requestId);
    SignDB signDB = signRepository.findOne(signId);
    requestDB.setSign(signDB);

    requestDB = requestRepository.save(requestDB);

    signDB.setVideoDefinition(requestDB.getRequestVideoDescription());
    signDB.setTextDefinition(requestDB.getRequestTextDescription());
    signDB = signRepository.save(signDB);

    return requestFrom(requestDB, services);
  }

  @Override
  public Request create(Request request) {
    RequestDB requestDB = requestRepository.save(requestDBFrom(request));
    return requestFrom(requestDB, services);
  }

  @Override
  public Request priorise(long requestId) {
    RequestDB requestDB = requestRepository.findOne(requestId);

    requestDB.setRequestDate(new Date());
    requestRepository.save(requestDB);


    return requestFrom(requestDB, services);
  }

  @Override
  public Request create(long userId, String requestName, String requestTextDescription) {
    RequestDB requestDB;
    UserDB userDB = userRepository.findOne(userId);

    requestDB = new RequestDB();
    requestDB.setRequestDate(new Date());
    requestDB.setName(requestName);
    requestDB.setRequestTextDescription(requestTextDescription);
    requestDB.setUser(userDB);
    requestRepository.save(requestDB);

    userDB.getRequests().add(requestDB);
    userRepository.save(userDB);

    return requestFrom(requestDB, services);
  }

  @Override
  public Request create(long userId, String requestName, String requestTextDescription, String requestVideoDescription) {
    RequestDB requestDB;
    UserDB userDB = userRepository.findOne(userId);

    requestDB = new RequestDB();
    requestDB.setRequestDate(new Date());
    requestDB.setName(requestName);
    requestDB.setRequestTextDescription(requestTextDescription);
    requestDB.setRequestVideoDescription(requestVideoDescription);
    requestDB.setUser(userDB);
    requestRepository.save(requestDB);

    userDB.getRequests().add(requestDB);
    userRepository.save(userDB);

    return requestFrom(requestDB, services);
  }

  @Override
  public Request rename(long requestId, String requestName, String requestTextDescription) {
    RequestDB requestDB = requestRepository.findOne(requestId);

    requestDB.setName(requestName);
    requestDB.setRequestTextDescription(requestTextDescription);
    requestRepository.save(requestDB);

    return requestFrom(requestDB, services);
  }

  @Override
  public void delete(Request request) {
    RequestDB requestDB = requestRepository.findOne(request.id);
    requestDB.getUser().getRequests().remove(requestDB);
    requestDB.setUser(null);
    requestRepository.delete(requestDB);
  }

  private Requests requestsFrom(Iterable<RequestDB> requestsDB) {
    List<Request> requests = new ArrayList<>();
    requestsDB.forEach(requestDB -> requests.add(requestFrom(requestDB, services)));
    return new Requests(requests);
  }

  static Request requestFrom(RequestDB requestDB, Services services) {
    return new Request(requestDB.getId(), requestDB.getName(), requestDB.getRequestTextDescription(), requestDB.getRequestVideoDescription(), requestDB.getRequestDate(), SignServiceImpl.signFromRequestsView(requestDB.getSign(),  services), UserServiceImpl.userFromSignView(requestDB.getUser()));
  }

  private RequestDB requestDBFrom(Request request) {
    return new RequestDB(request.name, request.requestDate);
  }

  @Override
  public List<Object[]> requestsByNameWithNoAssociateSign(String requestName, long userId) {
    List<Object[]>  requestsMatches = requestRepository.findRequestsByNameWithNoAssociateSign(requestName, userId);

    return requestsMatches;
  }

  @Override
  public List<Object[]> requestsByNameWithAssociateSign(String requestName, long userId) {
    List<Object[]>  requestsMatches = requestRepository.findRequestsByNameWithAssociateSign(requestName, userId);

    return requestsMatches;
  }

  @Override
  public Request changeRequestTextDescription(long requestId, String requestTextDescription) {
    RequestDB requestDB = requestRepository.findOne(requestId);

    requestDB.setRequestTextDescription(requestTextDescription);
    requestRepository.save(requestDB);

    return requestFrom(requestDB, services);
  }

  @Override
  public Request changeRequestVideoDescription(long requestId, String requestVideoDescription) {
    RequestDB requestDB = requestRepository.findOne(requestId);

    requestDB.setRequestVideoDescription(requestVideoDescription);
    requestRepository.save(requestDB);

    return requestFrom(requestDB, services);
  }

  @Override
  public Requests myRequestMostRecent(long userId) {
    return requestsFrom(requestRepository.findMyRequestMostRecent(userRepository.findOne(userId)));
  }

  @Override
  public Requests myRequestlowRecent(long userId) {
    return requestsFrom(requestRepository.findMyRequestLowRecent(userRepository.findOne(userId)));
  }

  @Override
  public Requests myRequestAlphabeticalOrderDesc(long userId) {
    return requestsFrom(requestRepository.findMyRequestAlphabeticalOrderDesc(userRepository.findOne(userId)));
  }

  @Override
  public Requests myRequestAlphabeticalOrderAsc(long userId) {
    return requestsFrom(requestRepository.findMyRequestAlphabeticalOrderAsc(userRepository.findOne(userId)));
  }

  @Override
  public Requests otherRequestWithNoSignMostRecent(long userId) {
    return requestsFrom(requestRepository.findOtherRequestWithNoSignMostRecent(userRepository.findOne(userId)));
  }

  @Override
  public Requests otherRequestWithNoSignlowRecent(long userId) {
    return requestsFrom(requestRepository.findOtherRequestWithNoSignLowRecent(userRepository.findOne(userId)));
  }

  @Override
  public Requests otherRequestWithNoSignAlphabeticalOrderDesc(long userId) {
    return requestsFrom(requestRepository.findOtherRequestWithNoSignAlphabeticalOrderDesc(userRepository.findOne(userId)));
  }

  @Override
  public Requests otherRequestWithNoSignAlphabeticalOrderAsc(long userId) {
    return requestsFrom(requestRepository.findOtherRequestWithNoSignAlphabeticalOrderAsc(userRepository.findOne(userId)));
  }
}
