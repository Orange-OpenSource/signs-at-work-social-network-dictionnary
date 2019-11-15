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
import com.orange.signsatwork.biz.domain.*;
import com.orange.signsatwork.biz.persistence.model.RequestViewData;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.storage.StorageService;
import com.orange.signsatwork.biz.view.model.AuthentModel;
import com.orange.signsatwork.biz.view.model.RequestCreationView;
import com.orange.signsatwork.biz.view.model.SignCreationView;
import com.orange.signsatwork.biz.webservice.model.RequestCreationViewApi;
import com.orange.signsatwork.biz.webservice.model.RequestResponse;
import com.orange.signsatwork.biz.webservice.model.RequestResponseApi;
import com.orange.signsatwork.biz.webservice.model.RequestViewApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Types that carry this annotation are treated as controllers where @RequestMapping
 * methods assume @ResponseBody semantics by default, ie return json body.
 */
@Slf4j
@RestController
/** Rest controller: returns a json body */
public class RequestRestController {

  @Autowired
  Services services;
  @Autowired
  DalymotionToken dalymotionToken;
  @Autowired
  private SpringRestClient springRestClient;
  @Autowired
  MessageByLocaleService messageByLocaleService;
  @Autowired
  private StorageService storageService;
  @Autowired
  private Environment environment;

  String VIDEO_THUMBNAIL_FIELDS = "thumbnail_url,thumbnail_60_url,thumbnail_120_url,thumbnail_180_url,thumbnail_240_url,thumbnail_360_url,thumbnail_480_url,thumbnail_720_url,";
  String VIDEO_EMBED_FIELD = "embed_url";

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_REQUEST_CREATE, method = RequestMethod.POST)
  public RequestResponse createRequest(@RequestBody RequestCreationView requestCreationView, Principal principal, HttpServletResponse response, HttpServletRequest req) {
    List<String> emails;
    String title, bodyMail;
    Request request;
    RequestResponse requestResponse = new RequestResponse();
    User user = services.user().withUserName(principal.getName());
    if (services.sign().withName(requestCreationView.getRequestName()).list().isEmpty()) {
      if (services.request().withName(requestCreationView.getRequestName()).list().isEmpty()) {
         request = services.request().create(user.id, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription());
        log.info("createRequest: username = {} / request name = {}", user.username, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription());
        emails = services.user().findEmailForUserHaveSameCommunityAndCouldCreateSign(user.id);
        title = messageByLocaleService.getMessage("request_created_by_user_title", new Object[]{user.name()});
        bodyMail = messageByLocaleService.getMessage("request_created_by_user_body", new Object[]{user.name(), request.name, getAppUrl(req) + "/sec/other-request-detail/" + request.id});

        Runnable task = () -> {
          log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
          services.emailService().sendRequestMessage(emails.toArray(new String[emails.size()]), title, user.name(), request.name, getAppUrl(req) + "/sec/other-request-detail/" + request.id );
        };

        new Thread(task).start();


      } else {
          response.setStatus(HttpServletResponse.SC_CONFLICT);
          requestResponse.errorType = 1;
          requestResponse.errorMessage = messageByLocaleService.getMessage("request.already_exists");
          return requestResponse;
      }
    } else {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      requestResponse.errorType = 2;
      requestResponse.errorMessage = messageByLocaleService.getMessage("sign.already_exists");
      requestResponse.signId = services.sign().withName(requestCreationView.getRequestName()).list().get(0).id;
      return requestResponse;
    }

    requestResponse.requestId = request.id;
    return requestResponse;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_REQUEST_RENAME, method = RequestMethod.POST)
  public RequestResponse renameRequest(@RequestBody RequestCreationView requestCreationView, @PathVariable long requestId, HttpServletResponse response) {
    RequestResponse requestResponse = new RequestResponse();
    Request request = services.request().withId(requestId);
    if (!request.name.equals(requestCreationView.getRequestName())) {
      if (services.sign().withName(requestCreationView.getRequestName()).list().isEmpty()) {
        if (services.request().withName(requestCreationView.getRequestName()).list().isEmpty()) {
          services.request().rename(requestId, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription());
          log.info("renameRequest:  request name  = {} / request requestTextDescription = {} ", requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription());

        } else {
          response.setStatus(HttpServletResponse.SC_CONFLICT);
          requestResponse.errorType = 1;
          requestResponse.errorMessage = messageByLocaleService.getMessage("request.already_exists");
          return requestResponse;
        }
      } else {
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        requestResponse.errorType = 2;
        requestResponse.errorMessage = messageByLocaleService.getMessage("sign.already_exists");
        requestResponse.signId = services.sign().withName(requestCreationView.getRequestName()).list().get(0).id;
        return requestResponse;
      }

    } else {
      if (!request.requestTextDescription.equals(requestCreationView.getRequestTextDescription())) {
        services.request().rename(requestId, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription());
      }
    }
    requestResponse.requestId = request.id;
    return requestResponse;
  }




  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_REQUEST_PRIORISE, method = RequestMethod.POST)
  public void  requestPriorised(@PathVariable long requestId, HttpServletResponse response) {
    services.request().priorise(requestId);
    response.setStatus(HttpServletResponse.SC_OK);
    return;
  }



  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_REQUEST_DELETE, method = RequestMethod.DELETE)
  public String  requestDeleted(@PathVariable long requestId, HttpServletResponse response, Principal principal) {
    String dailymotionId;
    User user = services.user().withUserName(principal.getName());
    Requests queryRequests = services.request().requestsforUser(user.id);
    boolean isRequestBelowToMe = queryRequests.stream().anyMatch(request -> request.id == requestId);
    if (!isRequestBelowToMe) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return messageByLocaleService.getMessage("request_not_below_to_you");
    }

    Request request = services.request().withId(requestId);
    services.request().delete(request);
    if (request.requestVideoDescription !=  null) {
      dailymotionId = request.requestVideoDescription.substring(request.requestVideoDescription.lastIndexOf('/') + 1);
      try {
        DeleteVideoOnDailyMotion(dailymotionId);
      } catch (Exception errorDailymotionDeleteVideo) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
      }
    }
    response.setStatus(HttpServletResponse.SC_OK);
    return "/sec/requests";
  }

  private void DeleteVideoOnDailyMotion(String dailymotionId) {

    AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
    if (authTokenInfo.isExpired()) {
      dalymotionToken.retrieveToken();
      authTokenInfo = dalymotionToken.getAuthTokenInfo();
    }

    final String uri = environment.getProperty("app.dailymotion_url") + "/video/"+dailymotionId;
    RestTemplate restTemplate = springRestClient.buildRestTemplate();

    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add("Authorization", "Bearer " + authTokenInfo.getAccess_token());

    HttpEntity<?> request = new HttpEntity<Object>(headers);

    restTemplate.exchange(uri, HttpMethod.DELETE, request, String.class );

    return;
  }

  /** API REST For Android and IOS **/


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_MY_REQUESTS)
  public ResponseEntity<?> myRequests(@RequestParam("sort") Optional<String> sort, Principal principal) {

    User user = services.user().withUserName(principal.getName());

    String messageError;
    Requests queryRequests;

    List<RequestViewApi> myRequestsViewApi = new ArrayList<>();
    if (sort.isPresent()) {
      if (sort.get().equals("name")) {
        queryRequests = services.request().myRequestAlphabeticalOrderAsc(user.id);
      } else if (sort.get().equals("-name")) {
        queryRequests = services.request().myRequestAlphabeticalOrderDesc(user.id);
      } else if (sort.get().equals("date")) {
        queryRequests = services.request().myRequestMostRecent(user.id);
      } else if (sort.get().equals("-date")) {
        queryRequests = services.request().myRequestlowRecent(user.id);
      } else {
        messageError = messageByLocaleService.getMessage("filter_not_exits", new Object[]{sort.get()});
        return new ResponseEntity<>(messageError, HttpStatus.BAD_REQUEST);
      }
      myRequestsViewApi.addAll(queryRequests.stream().map(request -> new RequestViewApi(request)).collect(Collectors.toList()));
    } else {
      queryRequests = services.request().requestsforUser(user.id);
      List<RequestViewApi> myrequestsViewApiWithSignAssociate = queryRequests.stream().filter(request -> request.sign != null).map(request -> new RequestViewApi(request)).collect(Collectors.toList());
      List<RequestViewApi> myrequestsViewApiWithoutSignAssociate = queryRequests.stream().filter(request -> request.sign == null).map(request -> new RequestViewApi(request)).collect(Collectors.toList());
      myRequestsViewApi.addAll(myrequestsViewApiWithSignAssociate);
      myRequestsViewApi.addAll(myrequestsViewApiWithoutSignAssociate);
    }

    return  new ResponseEntity<>(myRequestsViewApi, HttpStatus.OK);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_OTHER_REQUESTS)
  public ResponseEntity<?> otherRequests(@RequestParam("sort") Optional<String> sort, Principal principal) {

    User user = services.user().withUserName(principal.getName());

    String messageError;

    Requests queryRequests;
    List<RequestViewApi> otherRequestsViewApi = new ArrayList<>();
    if (sort.isPresent()) {
      if (sort.get().equals("name")) {
        queryRequests = services.request().otherRequestWithNoSignAlphabeticalOrderAsc(user.id);
      } else if (sort.get().equals("-name")) {
        queryRequests = services.request().otherRequestWithNoSignAlphabeticalOrderDesc(user.id);
      } else if (sort.get().equals("date")) {
        queryRequests = services.request().otherRequestWithNoSignMostRecent(user.id);
      } else if (sort.get().equals("-date")) {
        queryRequests = services.request().otherRequestWithNoSignlowRecent(user.id);
      } else {
        messageError = messageByLocaleService.getMessage("filter_not_exits", new Object[]{sort.get()});
        return new ResponseEntity<>(messageError, HttpStatus.BAD_REQUEST);
      }
      otherRequestsViewApi.addAll(queryRequests.stream().map(request -> new RequestViewApi(request)).collect(Collectors.toList()));
    } else {
      messageError = messageByLocaleService.getMessage("only_filter_date_or_name");
      return new ResponseEntity<>(messageError, HttpStatus.BAD_REQUEST);
    }

    return  new ResponseEntity<>(otherRequestsViewApi, HttpStatus.OK);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_REQUESTS)
  public ResponseEntity<?> allRequests(@RequestParam("name") String name, Principal principal) {

    User user = services.user().withUserName(principal.getName());

    String messageError;

    if (name.isEmpty()) {
      messageError = messageByLocaleService.getMessage("field_name_is_empty");
      return new ResponseEntity<>(messageError, HttpStatus.BAD_REQUEST);
    }

    List<RequestViewApi> requestsWithSameName = new ArrayList<>();
    List<Object[]> queryRequestsWithNoASsociateSign = services.request().requestsByNameWithNoAssociateSign(name, user.id);
    List<RequestViewApi> requestViewDatasWithNoAssociateSign =  queryRequestsWithNoASsociateSign.stream()
      .map(objectArray -> new RequestViewApi(objectArray))
      .collect(Collectors.toList());
    requestsWithSameName.addAll(requestViewDatasWithNoAssociateSign);


    List<Object[]> queryRequestsWithASsociateSign = services.request().requestsByNameWithAssociateSign(name, user.id);
    List<RequestViewApi> requestViewDatasWithAssociateSign =  queryRequestsWithASsociateSign.stream()
      .map(objectArray -> new RequestViewApi(objectArray))
      .collect(Collectors.toList());
    requestsWithSameName.addAll(requestViewDatasWithAssociateSign);

    List<RequestViewApi> allRequestsWithSameName = requestsWithSameName.stream().map(requestViewApi -> new RequestViewApi(services.request().withId(requestViewApi.getId()))).collect(Collectors.toList());


    return  new ResponseEntity<>(allRequestsWithSameName, HttpStatus.OK);
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_REQUEST)
  public ResponseEntity<?> request(@PathVariable long requestId) {

    Request request = services.request().withId(requestId);

    return new ResponseEntity<>( new RequestViewApi(request), HttpStatus.OK);

  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_REQUEST, method = RequestMethod.DELETE)
  public RequestResponseApi apiDeleteRequest(@PathVariable long requestId, HttpServletResponse response, Principal principal) {
    RequestResponseApi requestResponseApi = new RequestResponseApi();
    String dailymotionId;
    User user = services.user().withUserName(principal.getName());
    Requests queryRequests = services.request().requestsforUser(user.id);
    boolean isRequestBelowToMe = queryRequests.stream().anyMatch(request -> request.id == requestId);
    if (!isRequestBelowToMe) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      requestResponseApi.errorMessage = messageByLocaleService.getMessage("request_not_below_to_you");
      return requestResponseApi;
    }

    Request request = services.request().withId(requestId);
    services.request().delete(request);
    if (request.requestVideoDescription !=  null) {
      dailymotionId = request.requestVideoDescription.substring(request.requestVideoDescription.lastIndexOf('/') + 1);
      try {
        DeleteVideoOnDailyMotion(dailymotionId);
      } catch (Exception errorDailymotionDeleteVideo) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        requestResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
        return requestResponseApi;
      }
    }
    response.setStatus(HttpServletResponse.SC_OK);
    return requestResponseApi;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_REQUEST, method = RequestMethod.PUT,  headers = {"content-type=multipart/mixed","content-type=multipart/form-data"})
  public RequestResponseApi modifyRequest(@RequestPart("file") Optional<MultipartFile> file, @RequestPart("data") Optional<RequestCreationViewApi> requestCreationViewApi, @PathVariable long requestId, HttpServletResponse response, HttpServletRequest req, Principal principal) throws InterruptedException {
    RequestResponseApi requestResponseApi = new RequestResponseApi();
    Request request = services.request().withId(requestId);
    User user = services.user().withUserName(principal.getName());
    if (request.user.id != user.id) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      requestResponseApi.errorMessage = messageByLocaleService.getMessage("updateRequestForbiden");
      return requestResponseApi;
    }

    if (requestCreationViewApi.isPresent()) {
      if ((requestCreationViewApi.get().getName() != null) && !request.name.equals(requestCreationViewApi.get().getName())) {
        if (services.sign().withName(requestCreationViewApi.get().getName()).list().isEmpty()) {
          if (services.request().withName(requestCreationViewApi.get().getName()).list().isEmpty()) {
            services.request().updateName(requestId, requestCreationViewApi.get().getName());
            log.info("renameRequest:  request name  = {} / request textDescription = {} ", requestCreationViewApi.get().getName(), requestCreationViewApi.get().getTextDescription());

          } else {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            requestResponseApi.errorMessage = messageByLocaleService.getMessage("request.already_exists");
            return requestResponseApi;
          }
        } else {
          response.setStatus(HttpServletResponse.SC_CONFLICT);
          requestResponseApi.errorMessage = messageByLocaleService.getMessage("sign.already_exists");
          requestResponseApi.signId = services.sign().withName(requestCreationViewApi.get().getName()).list().get(0).id;
          return requestResponseApi;
        }

      }

      if ((requestCreationViewApi.get().getTextDescription() != null)) {
        services.request().changeRequestTextDescription(requestId, requestCreationViewApi.get().getTextDescription());
      }

      if ((requestCreationViewApi.get().getDate() != null)) {
        services.request().updateDate(requestId, requestCreationViewApi.get().getDate());
      }

    }

    if (file.isPresent()) {
      return createRequestWithVideoFileForRequestDescription(file.get(), requestId, Optional.empty(), principal, response, req);
    }

    response.setStatus(HttpServletResponse.SC_OK);
    requestResponseApi.requestId = request.id;
    return requestResponseApi;
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_REQUESTS, method = RequestMethod.POST,  headers = {"content-type=multipart/mixed","content-type=multipart/form-data"})
  public RequestResponseApi createRequest(@RequestPart("file") Optional<MultipartFile> file, @RequestPart("data") RequestCreationViewApi requestCreationViewApi, HttpServletResponse response, HttpServletRequest req, Principal principal) throws InterruptedException {
    List<String> emails;
    String title, bodyMail;
    Request request;
    RequestResponseApi requestResponseApi = new RequestResponseApi();
    User user = services.user().withUserName(principal.getName());

    if (!services.sign().withName(requestCreationViewApi.getName()).list().isEmpty()) {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      requestResponseApi.errorMessage = messageByLocaleService.getMessage("sign.already_exists");
      requestResponseApi.signId = services.sign().withName(requestCreationViewApi.getName()).list().get(0).id;
      return requestResponseApi;
    }
    if (!services.request().withName(requestCreationViewApi.getName()).list().isEmpty()) {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      requestResponseApi.errorMessage = messageByLocaleService.getMessage("request.already_exists");
      return requestResponseApi;
    }

    if (!file.isPresent()) {
          request = services.request().create(user.id, requestCreationViewApi.getName(), requestCreationViewApi.getTextDescription());
          log.info("createRequest: username = {} / request name = {}", user.username, requestCreationViewApi.getName(), requestCreationViewApi.getTextDescription());
          emails = services.user().findEmailForUserHaveSameCommunityAndCouldCreateSign(user.id);
          title = messageByLocaleService.getMessage("request_created_by_user_title", new Object[]{user.name()});
          bodyMail = messageByLocaleService.getMessage("request_created_by_user_body", new Object[]{user.name(), request.name, getAppUrl(req) + "/sec/other-request-detail/" + request.id});

          Runnable task = () -> {
            log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
            services.emailService().sendRequestMessage(emails.toArray(new String[emails.size()]), title, user.name(), request.name, getAppUrl(req) + "/sec/other-request-detail/" + request.id);
          };

          new Thread(task).start();

          requestResponseApi.requestId = request.id;
          return requestResponseApi;

    } else {
      return createRequestWithVideoFileForRequestDescription(file.get(), 0,  Optional.of(requestCreationViewApi) , principal, response, req);
    }

  }




  private RequestResponseApi createRequestWithVideoFileForRequestDescription(@RequestParam("file") MultipartFile file, @PathVariable long requestId, @ModelAttribute Optional<RequestCreationViewApi> requestCreationViewApi, Principal principal, HttpServletResponse response, HttpServletRequest req) throws InterruptedException {
    {
      String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");
      Request request = null;
      RequestResponseApi requestResponseApi = new RequestResponseApi();
      try {
        String dailymotionId;
        AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
        if (authTokenInfo.isExpired()) {
          dalymotionToken.retrieveToken();
          authTokenInfo = dalymotionToken.getAuthTokenInfo();
        }

        log.info("Avant storage");
        User user = services.user().withUserName(principal.getName());
        storageService.store(file);
        File inputFile = storageService.load(file.getOriginalFilename()).toFile();
        log.info("Aprés storage et load file");

        UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();

        if (requestId != 0) {
          request = services.request().withId(requestId);
        }

        Resource resource = new FileSystemResource(inputFile.getAbsolutePath());
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        parts.add("file", resource);

        RestTemplate restTemplate = springRestClient.buildRestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);


        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

        log.info("url POST "+urlfileUploadDailymotion.upload_url);
        ResponseEntity<FileUploadDailymotion> responseDailyMotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
          HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
        FileUploadDailymotion fileUploadDailyMotion = responseDailyMotion.getBody();
        log.info("body "+fileUploadDailyMotion.toString());


        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("url", fileUploadDailyMotion.url);
        if (requestId != 0) {
          body.add("title", "Description LSF de la demande " + request.name);
        } else {
          body.add("title", "Description LSF de la demande " + requestCreationViewApi.get().getName());
        }
        body.add("channel", "tech");
        body.add("published", true);
        body.add("private", true);


        RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
        HttpHeaders headers1 = new HttpHeaders();
        headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
        headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
        String videoUrl = REST_SERVICE_URI + "/videos";
        log.info("url POST "+videoUrl);
        ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange(videoUrl,
          HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
        VideoDailyMotion videoDailyMotion = response1.getBody();
        log.info("body "+response1.getBody().toString());

        log.info("Avant load file in dailymotion");

        String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD;
        int i=0;
        do {
          videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
          Thread.sleep(2 * 1000);
          if (i > 30) {
            break;
          }
          i++;
        }
        while ((videoDailyMotion.thumbnail_360_url == null) || (videoDailyMotion.embed_url == null) || (videoDailyMotion.thumbnail_360_url.contains("no-such-asset")));

        List<String> emails;
        String title, bodyMail;
        if (!videoDailyMotion.embed_url.isEmpty()) {
          if (requestId != 0) {
            if (request.requestVideoDescription != null) {
              dailymotionId = request.requestVideoDescription.substring(request.requestVideoDescription.lastIndexOf('/') + 1);
              try {
                DeleteVideoOnDailyMotion(dailymotionId);
              }
              catch (Exception errorDailymotionDeleteVideo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                requestResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
                return  requestResponseApi;
              }
            }
            services.request().changeRequestVideoDescription(requestId, videoDailyMotion.embed_url);

          } else {
            if (services.sign().withName(requestCreationViewApi.get().getName()).list().isEmpty()) {
              if (services.request().withName(requestCreationViewApi.get().getName()).list().isEmpty()) {
                request = services.request().create(user.id, requestCreationViewApi.get().getName(), requestCreationViewApi.get().getTextDescription(), videoDailyMotion.embed_url);
                log.info("createRequest: username = {} / request name = {}", user.username, requestCreationViewApi.get().getName(), requestCreationViewApi.get().getTextDescription());
                emails = services.user().findEmailForUserHaveSameCommunityAndCouldCreateSign(user.id);
                title = messageByLocaleService.getMessage("request_created_by_user_title", new Object[]{user.name()});
                bodyMail = messageByLocaleService.getMessage("request_created_by_user_body", new Object[]{user.name(), request.name, getAppUrl(req) + "/sec/other-request-detail/" + request.id});

                Request finalRequest = request;
                Runnable task = () -> {
                  log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
                  services.emailService().sendRequestMessage(emails.toArray(new String[emails.size()]), title, user.name(), finalRequest.name, getAppUrl(req) + "/sec/other-request-detail/" + finalRequest.id );
                };

                new Thread(task).start();
              } else {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                requestResponseApi.errorMessage = messageByLocaleService.getMessage("request.already_exists");
                return requestResponseApi;
              }
            } else {
              response.setStatus(HttpServletResponse.SC_CONFLICT);
              requestResponseApi.errorMessage = messageByLocaleService.getMessage("sign.already_exists");
              requestResponseApi.signId = services.sign().withName(requestCreationViewApi.get().getName()).list().get(0).id;
              return requestResponseApi;
            }
            log.warn("handleSelectedVideoFileUploadForRequestDescription : embed_url = {}", videoDailyMotion.embed_url);
          }
        }

        response.setStatus(HttpServletResponse.SC_OK);
        requestResponseApi.requestId = request.id;
        return requestResponseApi;

      } catch (Exception errorDailymotionUploadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        requestResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionUploadFile");
        return requestResponseApi;
      }
    }
  }

  private String getAppUrl(HttpServletRequest request) {
    return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
  }

  @Secured("ROLE_USER_A")
  @RequestMapping(value = RestApi.WS_SEC_REQUEST_SIGNS, method = RequestMethod.POST,  headers = {"content-type=multipart/mixed","content-type=multipart/form-data"})
  public RequestResponseApi createSignAssociateToRequest(@RequestPart("file") MultipartFile file, @PathVariable long requestId, @RequestPart("data") SignCreationView signCreationView, HttpServletResponse response, Principal principal) throws InterruptedException {

    RequestResponseApi requestResponseApi = new RequestResponseApi();
    if (!AuthentModel.hasRole("ROLE_USER_A")) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      requestResponseApi.errorMessage = messageByLocaleService.getMessage("forbidden_action");
      return requestResponseApi;
    }

    Request request = services.request().withId(requestId);
    User user = services.user().withUserName(principal.getName());

    if (request.user.id == user.id) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      requestResponseApi.errorMessage = messageByLocaleService.getMessage("dont_create_sign_on_your_request");
      return requestResponseApi;
    }

    if (!services.sign().withName(signCreationView.getSignName()).list().isEmpty()) {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      requestResponseApi.errorMessage = messageByLocaleService.getMessage("sign.already_exists");
      requestResponseApi.signId = services.sign().withName(signCreationView.getSignName()).list().get(0).id;
      return requestResponseApi;
    }


    return handleSelectedVideoFileUpload(file,  OptionalLong.of(requestId), OptionalLong.empty(), OptionalLong.empty(), signCreationView, principal, response);
  }

  private RequestResponseApi handleSelectedVideoFileUpload(@RequestParam("file") MultipartFile file, OptionalLong requestId, OptionalLong signId, OptionalLong videoId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) throws InterruptedException {

    String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");
    RequestResponseApi requestResponseApi = new RequestResponseApi();
    try {
      String dailymotionId;

      AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
      if (authTokenInfo.isExpired()) {
        dalymotionToken.retrieveToken();
        authTokenInfo = dalymotionToken.getAuthTokenInfo();
      }

      User user = services.user().withUserName(principal.getName());
      storageService.store(file);
      File inputFile = storageService.load(file.getOriginalFilename()).toFile();

      UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


      Resource resource = new FileSystemResource(inputFile.getAbsolutePath());
      MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
      parts.add("file", resource);

      RestTemplate restTemplate = springRestClient.buildRestTemplate();

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);


      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

      ResponseEntity<FileUploadDailymotion> responseDailyMotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
        HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
      FileUploadDailymotion fileUploadDailyMotion = responseDailyMotion.getBody();


      MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
      body.add("url", fileUploadDailyMotion.url);
      if (signId.isPresent()) {
        body.add("title", services.sign().withId(signId.getAsLong()).name);
      } else {
        body.add("title", signCreationView.getSignName());
      }
      body.add("channel", "tech");
      body.add("published", true);
      body.add("private", true);


      RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
      HttpHeaders headers1 = new HttpHeaders();
      headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
      headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

      HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
      String videoUrl = REST_SERVICE_URI + "/videos";
      ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange(videoUrl,
        HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
      VideoDailyMotion videoDailyMotion = response1.getBody();


      String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD;
      int i=0;
      do {
        videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
        Thread.sleep(2 * 1000);
        if (i > 30) {
          break;
        }
        i++;
      }
      while ((videoDailyMotion.thumbnail_360_url == null) || (videoDailyMotion.embed_url == null) || (videoDailyMotion.thumbnail_360_url.contains("no-such-asset")));


      String pictureUri = null;
      if (!videoDailyMotion.thumbnail_360_url.isEmpty()) {
        pictureUri = videoDailyMotion.thumbnail_360_url;
        log.warn("handleSelectedVideoFileUpload : thumbnail_360_url = {}", videoDailyMotion.thumbnail_360_url);
      }

      if (!videoDailyMotion.embed_url.isEmpty()) {
        signCreationView.setVideoUrl(videoDailyMotion.embed_url);
        log.warn("handleSelectedVideoFileUpload : embed_url = {}", videoDailyMotion.embed_url);
      }

      Sign sign;
      if (signId.isPresent() && (videoId.isPresent())) {
        sign = services.sign().withId(signId.getAsLong());
        dailymotionId = sign.url.substring(sign.url.lastIndexOf('/') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionId);
        }
        catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          requestResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
          return requestResponseApi;
        }
        sign = services.sign().replace(signId.getAsLong(), videoId.getAsLong(), signCreationView.getVideoUrl(), pictureUri);
      } else if (signId.isPresent() && !(videoId.isPresent())) {
        sign = services.sign().addNewVideo(user.id, signId.getAsLong(), signCreationView.getVideoUrl(), pictureUri);
      } else {
        sign = services.sign().create(user.id, signCreationView.getSignName(), signCreationView.getVideoUrl(), pictureUri);
      }

      log.info("handleSelectedVideoFileUpload : username = {} / sign name = {} / video url = {}", user.username, signCreationView.getSignName(), signCreationView.getVideoUrl());

      if (requestId.isPresent()) {
        services.request().changeSignRequest(requestId.getAsLong(), sign.id);
      }

      response.setStatus(HttpServletResponse.SC_OK);
      requestResponseApi.signId = sign.id;
      return requestResponseApi;

    } catch (Exception errorDailymotionUploadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      requestResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionUploadFile");
      return requestResponseApi;
    }
  }
}
