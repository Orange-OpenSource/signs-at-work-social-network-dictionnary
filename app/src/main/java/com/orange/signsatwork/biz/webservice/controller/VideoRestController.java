package com.orange.signsatwork.biz.webservice.controller;

import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.webservice.MultipartFileSender;
import com.orange.signsatwork.biz.webservice.model.FavoriteCreationViewApi;
import com.orange.signsatwork.biz.webservice.model.FavoriteResponseApi;
import com.orange.signsatwork.biz.webservice.model.VideoFavoriteViewApi;
import com.orange.signsatwork.biz.webservice.model.VideoViewApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;


@Slf4j
@RestController
public class VideoRestController {

  @Autowired
  private Environment environment;

  @Autowired
  Services services;

  @RequestMapping(RestApi.WS_SEC_VIDEOS_ON_SERVER)
  public void getVideo(@PathVariable String name, HttpServletResponse response, HttpServletRequest request) throws Exception {
    String path = environment.getProperty("app.file") + name;
    MultipartFileSender.fromURIString(path)
      .with(request)
      .with(response)
      .serveResource();
  }

/*  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_VIDEO_FAVORITES_ASSOCIATE, method = RequestMethod.POST)
  public void videoAssociateFavorites(@RequestBody List<Long> videoFavoriteIds, @PathVariable long videoId, HttpServletResponse response) {

    services.video().AddVideoToFavorites(videoId, videoFavoriteIds);

    response.setStatus(HttpServletResponse.SC_OK);
    return;
  }*/

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_VIDEO_FAVORITES_ASSOCIATE, method = RequestMethod.PUT)
  public void videoAssociateFavorite(@RequestBody VideoViewApi videoViewApi, @PathVariable long videoId, HttpServletResponse response) {
    services.video().AddVideoToFavorites(videoId, videoViewApi.getVideoFavoriteIds());

    response.setStatus(HttpServletResponse.SC_OK);
    return;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_VIDEO_FAVORITES_ASSOCIATE, method = RequestMethod.POST)
  public void videoAssociateFavorites(@RequestBody VideoFavoriteViewApi videoFavoriteViewApi, @PathVariable long videoId, HttpServletResponse response) {

    services.video().VideoToFavorites(videoId, videoFavoriteViewApi.getVideoFavoritesIdsCheck(), videoFavoriteViewApi.getVideoFavoritesIdsNoCheck());

    response.setStatus(HttpServletResponse.SC_OK);
    return;
  }
}
