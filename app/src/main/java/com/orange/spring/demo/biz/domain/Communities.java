package com.orange.spring.demo.biz.domain;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class Communities {
  public final List<Community> communities;

  public Stream<Community> stream() {
    return communities.stream();
  }
}
