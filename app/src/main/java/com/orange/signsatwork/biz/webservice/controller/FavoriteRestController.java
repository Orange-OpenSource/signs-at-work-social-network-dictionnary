package com.orange.signsatwork.biz.webservice.controller;

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

import com.orange.signsatwork.DalymotionToken;
import com.orange.signsatwork.SpringRestClient;
import com.orange.signsatwork.biz.domain.AuthTokenInfo;
import com.orange.signsatwork.biz.domain.Sign;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.domain.Video;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.view.model.SignCreationView;
import com.orange.signsatwork.biz.webservice.model.SignId;
import com.orange.signsatwork.biz.webservice.model.SignView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;

/**
 * Types that carry this annotation are treated as controllers where @RequestMapping
 * methods assume @ResponseBody semantics by default, ie return json body.
 */
@Slf4j
@RestController
/** Rest controller: returns a json body */
public class FavoriteRestController {

  @Autowired
  Services services;



  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_FAVORITE_VIDEO_ASSOCIATE, method = RequestMethod.POST)
  public String favoriteAssociateVideo(@RequestBody List<Long> favoriteVideosIds, @PathVariable long favoriteId, HttpServletResponse response) {

    services.favorite().changeFavoriteVideos(favoriteId, favoriteVideosIds);

    response.setStatus(HttpServletResponse.SC_OK);
    return "/sec/favorite/" + favoriteId;
  }

}
