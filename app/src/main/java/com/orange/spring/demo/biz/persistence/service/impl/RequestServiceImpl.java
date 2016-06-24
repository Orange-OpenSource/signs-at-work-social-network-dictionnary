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

import com.orange.spring.demo.biz.domain.*;
import com.orange.spring.demo.biz.persistence.model.CommunityDB;
import com.orange.spring.demo.biz.persistence.model.RequestDB;
import com.orange.spring.demo.biz.persistence.model.SignDB;
import com.orange.spring.demo.biz.persistence.model.UserDB;
import com.orange.spring.demo.biz.persistence.repository.RequestRepository;
import com.orange.spring.demo.biz.persistence.repository.SignRepository;
import com.orange.spring.demo.biz.persistence.repository.UserRepository;
import com.orange.spring.demo.biz.persistence.service.CommunityService;
import com.orange.spring.demo.biz.persistence.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
  private final UserRepository userRepository;
  private final RequestRepository requestRepository;
  private final SignRepository signRepository;

  @Override
  public Requests all() {
    return requestsFrom(requestRepository.findAll());
  }

  @Override
  public Request withId(long id) {
    return requestFrom(requestRepository.findOne(id));
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
  public Request changeSignRequest(long requestId, long signId) {
    RequestDB requestDB = requestRepository.findOne(requestId);
    SignDB signDB = signRepository.findOne(signId);
    requestDB.setSign(signDB);

    requestDB = requestRepository.save(requestDB);
    return requestFrom(requestDB);
  }

  @Override
  public Request create(Request request) {
    RequestDB requestDB = requestRepository.save(requestDBFrom(request));
    return requestFrom(requestDB);
  }

  private Requests requestsFrom(Iterable<RequestDB> requestsDB) {
    List<Request> requests = new ArrayList<>();
    requestsDB.forEach(requestDB -> requests.add(requestFrom(requestDB)));
    return new Requests(requests);
  }

  private Request requestFrom(RequestDB requestDB) {
    return new Request(requestDB.getId(), requestDB.getName(), requestDB.getRequestDate(), SignServiceImpl.signFrom(requestDB.getSign()));
  }

  private RequestDB requestDBFrom(Request request) {
    RequestDB requestDB = new RequestDB(request.name, request.requestDate);
    return requestDB;
  }
}
