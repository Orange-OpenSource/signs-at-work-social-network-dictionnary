package com.orange.signsatwork.biz.webservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Stream {
  private String codec_name;
  private int width;
  private int height;
  private Tags tags;
}
