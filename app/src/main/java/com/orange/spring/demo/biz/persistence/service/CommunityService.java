package com.orange.spring.demo.biz.persistence.service;

import com.orange.spring.demo.biz.domain.Communities;
import com.orange.spring.demo.biz.domain.Community;

public interface CommunityService {
  Communities all();

  Community withId(long id);

  Community create(Community community);
}
