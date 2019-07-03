package com.orange.signsatwork.biz.webservice.controller;

import com.orange.signsatwork.biz.webservice.MultipartFileSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;




@Slf4j
@RestController
public class VideoController {

  @Autowired
  private Environment environment;

  @RequestMapping(RestApi.WS_SEC_VIDEOS_ON_SERVER)
  public void getVideo(@PathVariable String name, HttpServletResponse response, HttpServletRequest request) throws Exception {
    String path = environment.getProperty("app.file")+name;
    MultipartFileSender.fromURIString(path)
      .with(request)
      .with(response)
      .serveResource();
  }

}
