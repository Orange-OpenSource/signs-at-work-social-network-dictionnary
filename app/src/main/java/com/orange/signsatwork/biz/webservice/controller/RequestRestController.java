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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.signsatwork.DalymotionToken;
import com.orange.signsatwork.SpringRestClient;
import com.orange.signsatwork.biz.domain.*;
import com.orange.signsatwork.biz.nativeinterface.NativeInterface;
import com.orange.signsatwork.biz.persistence.model.RequestViewData;
import com.orange.signsatwork.biz.persistence.model.SignViewData;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.security.AppSecurityAdmin;
import com.orange.signsatwork.biz.storage.StorageService;
import com.orange.signsatwork.biz.view.model.AuthentModel;
import com.orange.signsatwork.biz.view.model.RequestCreationView;
import com.orange.signsatwork.biz.view.model.SignCreationView;
import com.orange.signsatwork.biz.webservice.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

  @Autowired
  private AppSecurityAdmin appSecurityAdmin;

  String VIDEO_THUMBNAIL_FIELDS = "thumbnail_url,thumbnail_60_url,thumbnail_120_url,thumbnail_180_url,thumbnail_240_url,thumbnail_360_url,thumbnail_480_url,thumbnail_720_url,";
  String VIDEO_EMBED_FIELD = "embed_url";
  String VIDEO_STATUS = ",status";

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_REQUEST_CREATE, method = RequestMethod.POST)
  public RequestResponse createRequest(@RequestBody RequestCreationView requestCreationView, Principal principal, HttpServletResponse response, HttpServletRequest req) {
    List<String> emails;
    String title, bodyMail;
    Request request;
    RequestResponse requestResponse = new RequestResponse();
    User user = services.user().withUserName(principal.getName());
    requestCreationView.clearXss();
    if (services.sign().withName(requestCreationView.getRequestName()).list().isEmpty()) {
      if (services.request().withName(requestCreationView.getRequestName()).list().isEmpty()) {
         request = services.request().create(user.id, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription(), "");
        log.info("createRequest: username = {} / request name = {}", user.username, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription());
        emails = services.user().findEmailForUserHaveSameCommunityAndCouldCreateSign(user.id);
        title = messageByLocaleService.getMessage("request_created_by_user_title", new Object[]{user.name()});
        bodyMail = messageByLocaleService.getMessage("request_created_by_user_body", new Object[]{user.name(), request.name, getAppUrl() + "/sec/other-request-detail/" + request.id});
        if (emails.size() != 0) {
          Runnable task = () -> {
            log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
            services.emailService().sendRequestMessage(emails.toArray(new String[emails.size()]), title, user.name(), request.name, getAppUrl() + "/sec/other-request-detail/" + request.id, req.getLocale());
          };

          new Thread(task).start();
        }


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
    requestCreationView.clearXss();
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




  @Secured({"ROLE_USER", "ROLE_ADMIN"})
  @RequestMapping(value = RestApi.WS_SEC_REQUEST_PRIORISE, method = RequestMethod.POST)
  public void  requestPriorised(@PathVariable long requestId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    Boolean isAdmin = appSecurityAdmin.isAdmin(principal);
    User admin = services.user().getAdmin();
    Request request = services.request().withId(requestId);
    services.request().priorise(requestId);
    if (isAdmin) {
      String title = messageByLocaleService.getMessage("priorise_request_title", new Object[]{request.name});
      String bodyMail = messageByLocaleService.getMessage("priorise_request_body", new Object[]{request.name});
      String messageType = "PrioriseRequestSendEmailMessage";
      String email = request.user.email;
      if (!email.isEmpty()) {
        String finalMessageType = messageType;
        Runnable task = () -> {
          log.info("send mail email = {} / title = {} / body = {}", email, title, bodyMail);
          services.emailService().sendRequestDescriptionMessage(email, title, bodyMail, request.name, finalMessageType, requestHttp.getLocale());
        };
        new Thread(task).start();
      } else {
        messageType = "PrioriseRequestMessage";
        String values = admin.username + ';' + request.name;
        MessageServer messageServer = new MessageServer(new Date(), messageType, values, ActionType.NO);
        services.messageServerService().addMessageServer(messageServer);
      }
    }
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
    if (request.requestVideoDescription != null) {
      if (request.requestVideoDescription.contains("http")) {
        dailymotionId = request.requestVideoDescription.substring(request.requestVideoDescription.lastIndexOf('=') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionId);
        } catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
        }
      } else {
        DeleteFileOnServer(request.requestVideoDescription);
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
      myRequestsViewApi.addAll(queryRequests.stream().map(request -> new RequestViewApi(request, true)).collect(Collectors.toList()));
    } else {
      queryRequests = services.request().requestsforUser(user.id);
      List<RequestViewApi> myrequestsViewApiWithSignAssociate = queryRequests.stream().filter(request -> request.sign != null).map(request -> new RequestViewApi(request, true)).collect(Collectors.toList());
      List<RequestViewApi> myrequestsViewApiWithoutSignAssociate = queryRequests.stream().filter(request -> request.sign == null).map(request -> new RequestViewApi(request, true)).collect(Collectors.toList());
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
      otherRequestsViewApi.addAll(queryRequests.stream().map(request -> new RequestViewApi(request, false)).collect(Collectors.toList()));
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
    List<Object[]> queryRequestsWithNoASsociateSign = services.request().requestsByNameWithNoAssociateSign(name.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE"), user.id);
    List<RequestViewApi> requestViewDatasWithNoAssociateSign =  queryRequestsWithNoASsociateSign.stream()
      .map(objectArray -> new RequestViewApi(objectArray))
      .collect(Collectors.toList());
    requestsWithSameName.addAll(requestViewDatasWithNoAssociateSign);


    List<Object[]> queryRequestsWithASsociateSign = services.request().requestsByNameWithAssociateSign(name.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE"), user.id);
    List<RequestViewApi> requestViewDatasWithAssociateSign =  queryRequestsWithASsociateSign.stream()
      .map(objectArray -> new RequestViewApi(objectArray))
      .collect(Collectors.toList());
    requestsWithSameName.addAll(requestViewDatasWithAssociateSign);

    List<RequestViewApi> allRequestsWithSameName = requestsWithSameName.stream().map(requestViewApi -> new RequestViewApi(services.request().withId(requestViewApi.getId()), requestViewApi.getIsCreatedByMe())).collect(Collectors.toList());


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
      if (request.requestVideoDescription.contains("http")) {
        dailymotionId = request.requestVideoDescription.substring(request.requestVideoDescription.lastIndexOf('=') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionId);
        } catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          requestResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
          return requestResponseApi;
        }
      } else {
        DeleteFileOnServer(request.requestVideoDescription);
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
      requestCreationViewApi.get().clearXss();
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
      if (environment.getProperty("app.dailymotion_url").isEmpty()) {
        return createRequestWithVideoFileForRequestDescriptionOnServer(file.get(), requestId, Optional.empty(), principal, response, req);
      } else {
        return createRequestWithVideoFileForRequestDescription(file.get(), requestId, Optional.empty(), principal, response, req);
      }
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
    requestCreationViewApi.clearXss();
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
          request = services.request().create(user.id, requestCreationViewApi.getName(), requestCreationViewApi.getTextDescription(), "");
          log.info("createRequest: username = {} / request name = {}", user.username, requestCreationViewApi.getName(), requestCreationViewApi.getTextDescription());
          emails = services.user().findEmailForUserHaveSameCommunityAndCouldCreateSign(user.id);
          title = messageByLocaleService.getMessage("request_created_by_user_title", new Object[]{user.name()});
          bodyMail = messageByLocaleService.getMessage("request_created_by_user_body", new Object[]{user.name(), request.name, getAppUrl() + "/sec/other-request-detail/" + request.id});

         if (emails.size() != 0) {
            Runnable task = () -> {
              log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
              services.emailService().sendRequestMessage(emails.toArray(new String[emails.size()]), title, user.name(), request.name, getAppUrl() + "/sec/other-request-detail/" + request.id, req.getLocale());
            };

            new Thread(task).start();
          }
          requestResponseApi.requestId = request.id;
          return requestResponseApi;

    } else {
      if (environment.getProperty("app.dailymotion_url").isEmpty()) {
        return createRequestWithVideoFileForRequestDescriptionOnServer(file.get(), 0, Optional.of(requestCreationViewApi), principal, response, req);
      }
      else {
          return createRequestWithVideoFileForRequestDescription(file.get(), 0,  Optional.of(requestCreationViewApi) , principal, response, req);
        }
    }

  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_ADMIN_REQUEST, method = RequestMethod.DELETE)
  public RequestResponseApi adminDeleteRequest(@PathVariable long requestId, HttpServletResponse response, Principal principal, HttpServletRequest requestHttp) {
    RequestResponseApi requestResponseApi = new RequestResponseApi();
    String dailymotionId;
    String title, bodyMail, messageType;

    User admin = services.user().getAdmin();
    Request request = services.request().withId(requestId);
    services.request().deleteFromAdmin(request);

    title = messageByLocaleService.getMessage("delete_request_title", new Object[]{request.name});
    bodyMail = messageByLocaleService.getMessage("delete_request_body", new Object[]{request.name});
    messageType = "DeleteRequestSendEmailMessage";
    String email = request.user.email;
    if (!email.isEmpty()) {
      String finalMessageType = messageType;
      Runnable task = () -> {
        log.info("send mail email = {} / title = {} / body = {}", email, title, bodyMail);
        services.emailService().sendRequestDescriptionMessage(email, title, bodyMail, request.name, finalMessageType, requestHttp.getLocale());
      };
      new Thread(task).start();
    } else {
      messageType = "DeleteRequestMessage";
      String values = admin.username + ';' + request.name;
      MessageServer messageServer = new MessageServer(new Date(), messageType, values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);
    }

    if ((request.requestVideoDescription !=  null) && (request.sign.videoDefinition != null) && (request.sign.videoDefinition != request.requestVideoDescription)) {
      if (request.requestVideoDescription.contains("http")) {
        dailymotionId = request.requestVideoDescription.substring(request.requestVideoDescription.lastIndexOf('=') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionId);
        } catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          requestResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
          return requestResponseApi;
        }
      } else {
        DeleteFileOnServer(request.requestVideoDescription);
      }
    }
    response.setStatus(HttpServletResponse.SC_OK);
    return requestResponseApi;
  }

  @Secured({"ROLE_ADMIN", "ROLE_USER"})
  @RequestMapping(value = RestApi.WS_SEC_DELETE_VIDEO_FILE_FOR_REQUEST_DESCRIPTION, method = RequestMethod.POST)
  public String deleteVideoRequestDescription(@PathVariable long requestId, HttpServletResponse response, HttpServletRequest requestHttp, Principal principal) {
    Boolean isAdmin = appSecurityAdmin.isAdmin(principal);
    Request request = services.request().withId(requestId);
    if (request.requestVideoDescription != null) {
      if (request.requestVideoDescription.contains("http")) {
        String dailymotionIdForRequestDescription = request.requestVideoDescription.substring(request.requestVideoDescription.lastIndexOf('=') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionIdForRequestDescription);
        } catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
        }
      } else {
        DeleteFilesOnServer(request.requestVideoDescription, null);
      }
    }
    services.request().deleteRequestVideoDescription(requestId);
    if (isAdmin) {
      String title = messageByLocaleService.getMessage("delete_request_description_title", new Object[]{request.name});
      String bodyMail = messageByLocaleService.getMessage("delete_request_description_body", new Object[]{request.name});
      String messageType = "DeleteRequestDescriptionMessage";
      String email = request.user.email;
      if (!email.isEmpty()) {
        messageType = "DeleteRequestDescriptionSendEmailMessage";
        final String finalTitle = title;
        final String finalBodyMail = bodyMail;
        final String finalMessageType = messageType;
        final String finalRequestName = request.name;
        String finalEmail = email;
        Runnable task = () -> {
          log.info("send mail email = {} / title = {} / body = {}", finalEmail, finalTitle, finalBodyMail);
          services.emailService().sendRequestDescriptionMessage(finalEmail, finalTitle, finalBodyMail, finalRequestName, finalMessageType, requestHttp.getLocale());
        };
        new Thread(task).start();
      } else {
        User admin = services.user().getAdmin();
        String values = admin.username + ';' + request.name;
        MessageServer messageServer = new MessageServer(new Date(), messageType, values, ActionType.NO);
        services.messageServerService().addMessageServer(messageServer);
      }
    }

    response.setStatus(HttpServletResponse.SC_OK);
    return "";
  }
  private RequestResponseApi createRequestWithVideoFileForRequestDescription(@RequestParam("file") MultipartFile file, @PathVariable long requestId, @ModelAttribute Optional<RequestCreationViewApi> requestCreationViewApi, Principal principal, HttpServletResponse response, HttpServletRequest req) throws InterruptedException {
    {
      String videoUrl = null;
      String fileName = environment.getProperty("app.file") + "/" + file.getOriginalFilename();
      File inputFile;
      Request request = null;
      RequestResponseApi requestResponseApi = new RequestResponseApi();

      try {
        storageService.store(file);
        inputFile = storageService.load(file.getOriginalFilename()).toFile();
      } catch (Exception errorLoadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        requestResponseApi.errorMessage = messageByLocaleService.getMessage("errorUploadFile");
        return requestResponseApi;
      }

      try {
        String dailymotionId;
        String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");
        AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
        if (authTokenInfo.isExpired()) {
          dalymotionToken.retrieveToken();
          authTokenInfo = dalymotionToken.getAuthTokenInfo();
        }

        User user = services.user().withUserName(principal.getName());

        UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();

        if (requestId != 0) {
          request = services.request().withId(requestId);
        }

        Resource resource = new FileSystemResource(inputFile.getAbsolutePath());
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        parts.add("file", resource);

        RestTemplate restTemplate = springRestClient.buildRestTemplate();
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));


        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

        log.info("url POST "+urlfileUploadDailymotion.upload_url);
        ResponseEntity<FileUploadDailymotion> responseDailyMotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
          HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
        FileUploadDailymotion fileUploadDailyMotion = responseDailyMotion.getBody();
        log.info("body "+fileUploadDailyMotion.toString());


        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("url", fileUploadDailyMotion.url);
        if (requestId != 0) {
          body.add("title", messageByLocaleService.getMessage("request.title_description_LSF", new Object[]{request.name}));
        } else {
          body.add("title", messageByLocaleService.getMessage("request.title_description_LSF", new Object[]{requestCreationViewApi.get().getName()}));
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
        String videosUrl = REST_SERVICE_URI + "/videos";
        ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange(videosUrl,
          HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
        VideoDailyMotion videoDailyMotion = response1.getBody();

        String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD + VIDEO_STATUS;
        String id = videoDailyMotion.id;
        videoUrl= fileName;

        List<String> emails;
        String title, bodyMail;
        if (requestId != 0) {
          if (request.requestVideoDescription != null) {
            if (request.requestVideoDescription.contains("http")) {
              dailymotionId = request.requestVideoDescription.substring(request.requestVideoDescription.lastIndexOf('=') + 1);
              try {
                DeleteVideoOnDailyMotion(dailymotionId);
              } catch (Exception errorDailymotionDeleteVideo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                requestResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
                return requestResponseApi;
              }
            }
          }
          services.request().changeRequestVideoDescription(requestId, videoUrl);

        } else {
          if (services.sign().withName(requestCreationViewApi.get().getName()).list().isEmpty()) {
            if (services.request().withName(requestCreationViewApi.get().getName()).list().isEmpty()) {
              request = services.request().create(user.id, requestCreationViewApi.get().getName(), requestCreationViewApi.get().getTextDescription(), videoUrl);
              log.info("createRequest: username = {} / request name = {}", user.username, requestCreationViewApi.get().getName(), requestCreationViewApi.get().getTextDescription());
              emails = services.user().findEmailForUserHaveSameCommunityAndCouldCreateSign(user.id);
              title = messageByLocaleService.getMessage("request_created_by_user_title", new Object[]{user.name()});
              bodyMail = messageByLocaleService.getMessage("request_created_by_user_body", new Object[]{user.name(), request.name, getAppUrl() + "/sec/other-request-detail/" + request.id});

              if (emails.size() != 0) {
                Request finalRequest = request;
                Runnable task = () -> {
                  log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
                  services.emailService().sendRequestMessage(emails.toArray(new String[emails.size()]), title, user.name(), finalRequest.name, getAppUrl() + "/sec/other-request-detail/" + finalRequest.id, req.getLocale());
                };

                new Thread(task).start();
              }
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

        Request finalRequest1 = request;
        Runnable task = () -> {
          int i = 0;
          VideoDailyMotion dailyMotion;
          do {
            dailyMotion = services.sign().getVideoDailyMotionDetails(id, url);
            try {
              Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            if (i > 60) {
              break;
            }
            i++;
            log.info("status " + dailyMotion.status);
          }
          while (!dailyMotion.status.equals("published"));
          if (!dailyMotion.embed_url.isEmpty()) {
            services.request().changeRequestVideoDescription(finalRequest1.id, dailyMotion.embed_url);
          }
        };
        new Thread(task).start();

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

  private RequestResponseApi createRequestWithVideoFileForRequestDescriptionOnServer(@RequestParam("file") MultipartFile file, @PathVariable long requestId, @ModelAttribute Optional<RequestCreationViewApi> requestCreationViewApi, Principal principal, HttpServletResponse response, HttpServletRequest req) throws InterruptedException {
    {
      String videoUrl = null;
      String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
      String newFileName = UUID.randomUUID().toString() + "." + fileExtension;
      String newAbsoluteFileName = environment.getProperty("app.file") +"/" + newFileName;
      Request request = null;
      RequestResponseApi requestResponseApi = new RequestResponseApi();
      File inputFile;
      Streams streamInfo;
      String newAbsoluteFileNameWithExtensionMp4 = newAbsoluteFileName.substring(0, newAbsoluteFileName.lastIndexOf('.')) + ".mp4";

      try {
        storageService.store(file);
        inputFile = storageService.load(file.getOriginalFilename()).toFile();
        File newName = new File(newAbsoluteFileName);
        inputFile.renameTo(newName);
      } catch (Exception errorLoadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        requestResponseApi.errorMessage = messageByLocaleService.getMessage("errorUploadFile");
        return requestResponseApi;
      }

      try {
        streamInfo = SearchFileInfo(newAbsoluteFileName);
      } catch (Exception errorSearchFileInfo) {
        streamInfo = new Streams(new ArrayList<>());
      }

      if (streamInfo.getStreams().stream().findFirst().get().getCodec_name().equals("hevc")) {
        EncodeFileInH264(newAbsoluteFileName, newAbsoluteFileNameWithExtensionMp4);
        newAbsoluteFileName = newAbsoluteFileNameWithExtensionMp4;
      } else {
        if ((streamInfo.getStreams().stream().findFirst().get().getWidth() == 1920) &&
          (file.getSize() >= parseSize(environment.getProperty("file-size-max-to-reduce")))) {
          ReduceFileSizeInChangingResolution(newAbsoluteFileName, newAbsoluteFileNameWithExtensionMp4);
          newAbsoluteFileName = newAbsoluteFileNameWithExtensionMp4;
        } else {
          if (fileExtension.equals("webm")) {
            TransformWebmInMp4(newAbsoluteFileName, newAbsoluteFileNameWithExtensionMp4);
            DeleteFileOnServer(newAbsoluteFileName);
            newAbsoluteFileName = newAbsoluteFileNameWithExtensionMp4;
          }
        }
      }

      User user = services.user().withUserName(principal.getName());

      videoUrl= newAbsoluteFileName;
      List<String> emails;
      String title, bodyMail;
      if (requestId != 0) {
        request = services.request().withId(requestId);
        if (request.requestVideoDescription != null) {
         DeleteFileOnServer(request.requestVideoDescription);
        }
        services.request().changeRequestVideoDescription(requestId, videoUrl);

      } else {
        if (services.sign().withName(requestCreationViewApi.get().getName()).list().isEmpty()) {
          if (services.request().withName(requestCreationViewApi.get().getName()).list().isEmpty()) {
            request = services.request().create(user.id, requestCreationViewApi.get().getName(), requestCreationViewApi.get().getTextDescription(), videoUrl);
            log.info("createRequest: username = {} / request name = {}", user.username, requestCreationViewApi.get().getName(), requestCreationViewApi.get().getTextDescription());
            emails = services.user().findEmailForUserHaveSameCommunityAndCouldCreateSign(user.id);
            title = messageByLocaleService.getMessage("request_created_by_user_title", new Object[]{user.name()});
            bodyMail = messageByLocaleService.getMessage("request_created_by_user_body", new Object[]{user.name(), request.name, getAppUrl() + "/sec/other-request-detail/" + request.id});

            if (emails.size() != 0) {
              Request finalRequest = request;
              Runnable task = () -> {
                log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
                services.emailService().sendRequestMessage(emails.toArray(new String[emails.size()]), title, user.name(), finalRequest.name, getAppUrl() + "/sec/other-request-detail/" + finalRequest.id, req.getLocale());
              };

              new Thread(task).start();
            }
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

      response.setStatus(HttpServletResponse.SC_OK);
      requestResponseApi.requestId = request.id;
      return requestResponseApi;

    }
  }

  private Streams SearchFileInfo(String file) throws JsonProcessingException {
    String cmdFileInfo = String.format("ffprobe -v quiet -print_format json -show_streams -select_streams v:0 %s", file);
    String fileInfo = NativeInterface.launchAndGetOutput(cmdFileInfo, null, null);
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    Streams streams = objectMapper.readValue(fileInfo, Streams.class);
    return streams;
  }


  private void ReduceFileSizeInChangingResolution(String file, String fileOutput) {
    String cmd;
    if (file.equals(fileOutput)) {
      String fileTmp =  fileOutput.substring(0, fileOutput.lastIndexOf('.')) + ".tmp.mp4";
      cmd = String.format("ffmpeg -i %s -filter:v \"scale='min(1280,iw)':min'(720,ih)':force_original_aspect_ratio=decrease,pad=1280:720:(ow-iw)/2:(oh-ih)/2,crop=1280:720\" %s", file, fileTmp);
      NativeInterface.launch(cmd, null, null);
      Path source = Paths.get(fileTmp);
      Path target = Paths.get(file);
      try{
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      cmd = String.format("ffmpeg -i %s -filter:v \"scale='min(1280,iw)':min'(720,ih)':force_original_aspect_ratio=decrease,pad=1280:720:(ow-iw)/2:(oh-ih)/2,crop=1280:720\" %s", file, fileOutput);
      NativeInterface.launch(cmd, null, null);
    }

  }

  public static long parseSize(String text) {
    double d = Double.parseDouble(text.replaceAll("[GMK]B$", ""));
    long l = Math.round(d * 1024 * 1024 * 1024L);
    switch (text.charAt(Math.max(0, text.length() - 2))) {
      default:  l /= 1024;
      case 'K': l /= 1024;
      case 'M': l /= 1024;
      case 'G': return l;
    }
  }
  private String SearchFileCodec(String file) {
    String cmdFileCodec = String.format("ffprobe -v error -select_streams v:0 -show_entries stream=codec_name -of default=noprint_wrappers=1:nokey=1 %s", file);
    String fileCodec = NativeInterface.launchAndGetOutput(cmdFileCodec, null, null);
    return fileCodec;
  }

  private void EncodeFileInH264(String file, String fileOutput) {
    String cmd;

    cmd = String.format("ffmpeg -i %s -c:v libx264 -crf 20 -c:a copy %s", file, fileOutput);

    NativeInterface.launch(cmd, null, null);
  }

  private void TransformWebmInMp4(String file, String fileOutput) {
    String cmd;

    cmd = String.format("ffmpeg -fflags +genpts -i %s -r 25 %s", file, fileOutput);

    NativeInterface.launch(cmd, null, null);
  }

  private void DeleteFileOnServer(String url) {
    if (url != null) {
      File video = new File(url);
      if (video.exists()) {
        video.delete();
      }
    }
  }

  private String getAppUrl() {
    return environment.getProperty("app.url");
  }

  @Secured("ROLE_USER_A")
  @RequestMapping(value = RestApi.WS_SEC_REQUEST_SIGNS, method = RequestMethod.POST,  headers = {"content-type=multipart/mixed","content-type=multipart/form-data"})
  public RequestResponseApi createSignAssociateToRequest(@RequestPart("file") MultipartFile file, @PathVariable long requestId, @RequestPart("data") SignCreationViewApi signCreationViewApi, HttpServletResponse response, Principal principal) throws InterruptedException {

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

    signCreationViewApi.clearXss();
    if (!services.sign().withName(signCreationViewApi.getName()).list().isEmpty()) {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      requestResponseApi.errorMessage = messageByLocaleService.getMessage("sign.already_exists");
      requestResponseApi.signId = services.sign().withName(signCreationViewApi.getName()).list().get(0).id;
      return requestResponseApi;
    }

    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleSelectedVideoFileUploadOnServer(file, OptionalLong.of(requestId), OptionalLong.empty(), OptionalLong.empty(), signCreationViewApi, principal, response);
    } else {
      return handleSelectedVideoFileUpload(file, OptionalLong.of(requestId), OptionalLong.empty(), OptionalLong.empty(), signCreationViewApi, principal, response);
    }
  }

  private void GenerateThumbnail(String thumbnailFile, String fileOutput, String logFile) {
    String cmdGenerateThumbnail;

    cmdGenerateThumbnail = String.format("input=\"%s\"&&dur=$(ffprobe -loglevel error -show_entries format=duration -of default=nk=1:nw=1 \"$input\")&&ffmpeg -y -ss \"$(echo \"$dur / 2\" | bc -l | sed -e 's/^-\\./-0./' -e 's/^\\./0./')\" -i  \"$input\" -vframes 1 -s 360x360 -vf \"scale=(iw*sar)*max(360.1/(iw*sar)\\,360.1/ih):ih*max(360.1/(iw*sar)\\,360.1/ih), crop=360:360\" \"%s\"", fileOutput, thumbnailFile);
    NativeInterface.launch(cmdGenerateThumbnail, null, logFile);
  }

  private RequestResponseApi handleSelectedVideoFileUpload(@RequestParam("file") MultipartFile file, OptionalLong requestId, OptionalLong signId, OptionalLong videoId, @ModelAttribute SignCreationViewApi signCreationViewApi, Principal principal, HttpServletResponse response) throws InterruptedException {
    String videoUrl = null;
    String fileName = environment.getProperty("app.file") + "/" + file.getOriginalFilename();
    String thumbnailFile = environment.getProperty("app.file") + "/thumbnail/" + file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf('.')) + ".png";
    String logFile = "/tmp/" + file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf('.')) + ".log";
    File inputFile;

    RequestResponseApi requestResponseApi = new RequestResponseApi();

    try {
      storageService.store(file);
      inputFile = storageService.load(file.getOriginalFilename()).toFile();
    } catch (Exception errorLoadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      requestResponseApi.errorMessage = messageByLocaleService.getMessage("errorUploadFile");
      return requestResponseApi;
    }

    try {
      GenerateThumbnail(thumbnailFile, inputFile.getAbsolutePath(), logFile);
    } catch (Exception errorEncondingFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      requestResponseApi.errorMessage = messageByLocaleService.getMessage("errorThumbnailFile");
      return requestResponseApi;
    }

    try {
      String dailymotionId;
      String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");

      AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
      if (authTokenInfo.isExpired()) {
        dalymotionToken.retrieveToken();
        authTokenInfo = dalymotionToken.getAuthTokenInfo();
      }

      User user = services.user().withUserName(principal.getName());

      UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


      Resource resource = new FileSystemResource(inputFile.getAbsolutePath());
      MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
      parts.add("file", resource);

      RestTemplate restTemplate = springRestClient.buildRestTemplate();
      MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
      mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
      restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));


      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

      ResponseEntity<FileUploadDailymotion> responseDailyMotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
        HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
      FileUploadDailymotion fileUploadDailyMotion = responseDailyMotion.getBody();


      MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
      body.add("url", fileUploadDailyMotion.url);
      if (signId.isPresent()) {
        body.add("title", services.sign().withId(signId.getAsLong()).name);
      } else {
        body.add("title", signCreationViewApi.getName());
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
      String videosUrl = REST_SERVICE_URI + "/videos";
      ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange(videosUrl,
        HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
      VideoDailyMotion videoDailyMotion = response1.getBody();


      String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD + VIDEO_STATUS;
      String pictureUri = null;
      String id = videoDailyMotion.id;

        pictureUri = thumbnailFile;
        videoUrl= fileName;

      Sign sign;
      Video video;
      if (signId.isPresent() && (videoId.isPresent())) {
       /* sign = services.sign().withId(signId.getAsLong());*/
        video = services.video().withId(videoId.getAsLong());
        if (video.url.contains("http")) {
          dailymotionId = video.url.substring(video.url.lastIndexOf('=') + 1);
          try {
            DeleteVideoOnDailyMotion(dailymotionId);
          } catch (Exception errorDailymotionDeleteVideo) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            requestResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
            return requestResponseApi;
          }
        }
        sign = services.sign().replace(signId.getAsLong(), videoId.getAsLong(), videoUrl, pictureUri);
      } else if (signId.isPresent() && !(videoId.isPresent())) {
        sign = services.sign().addNewVideo(user.id, signId.getAsLong(), videoUrl, pictureUri);
      } else {
        sign = services.sign().create(user.id, signCreationViewApi.getName(), videoUrl, pictureUri);
      }

      log.info("handleSelectedVideoFileUpload : username = {} / sign name = {} / video url = {}", user.username, signCreationViewApi.getName(), videoDailyMotion.embed_url);

      if (requestId.isPresent()) {
        services.request().changeSignRequest(requestId.getAsLong(), sign.id);
      }

        Runnable task = () -> {
          int i = 0;
          VideoDailyMotion dailyMotion;
          do {
            dailyMotion = services.sign().getVideoDailyMotionDetails(id, url);
            try {
              Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            if (i > 60) {
              break;
            }
            i++;
            log.info("status " + dailyMotion.status);
          }
          while (!dailyMotion.status.equals("published"));
          services.sign().updateWithDailymotionInfo(sign.id, sign.lastVideoId, dailyMotion.thumbnail_360_url, dailyMotion.embed_url);
        };

        new Thread(task).start();


      response.setStatus(HttpServletResponse.SC_OK);
      requestResponseApi.signId = sign.id;
      requestResponseApi.videoId = sign.lastVideoId;
      return requestResponseApi;

    } catch (Exception errorDailymotionUploadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      requestResponseApi.errorMessage = messageByLocaleService.getMessage("errorDailymotionUploadFile");
      return requestResponseApi;
    }
  }
  private RequestResponseApi handleSelectedVideoFileUploadOnServer(@RequestParam("file") MultipartFile file, OptionalLong requestId, OptionalLong signId, OptionalLong videoId, @ModelAttribute SignCreationViewApi signCreationViewApi, Principal principal, HttpServletResponse response) throws InterruptedException {
    String videoUrl = null;
    String newFileName = UUID.randomUUID().toString() + "." + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
    String newAbsoluteFileName = environment.getProperty("app.file") +"/" + newFileName;
    String thumbnailFile = environment.getProperty("app.file") + "/thumbnail/" + newFileName.substring(0, newFileName.lastIndexOf('.')) + ".png";
    String logFile = "/tmp/" + newFileName.substring(0, newFileName.lastIndexOf('.')) + ".log";
    File inputFile;

    RequestResponseApi requestResponseApi = new RequestResponseApi();

    try {
      storageService.store(file);
      inputFile = storageService.load(file.getOriginalFilename()).toFile();
      File newName = new File(newAbsoluteFileName);
      inputFile.renameTo(newName);
    } catch (Exception errorLoadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      requestResponseApi.errorMessage = messageByLocaleService.getMessage("errorUploadFile");
      return requestResponseApi;
    }

    try {
      GenerateThumbnail(thumbnailFile, newAbsoluteFileName, logFile);
    } catch (Exception errorEncondingFile) {
      DeleteFilesOnServer(newAbsoluteFileName, null);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      requestResponseApi.errorMessage = messageByLocaleService.getMessage("errorThumbnailFile");
      return requestResponseApi;
    }

    User user = services.user().withUserName(principal.getName());

    String pictureUri = thumbnailFile;
    videoUrl= newAbsoluteFileName;

    Sign sign;
    Video video;
    if (signId.isPresent() && (videoId.isPresent())) {
      /* sign = services.sign().withId(signId.getAsLong());*/
      video = services.video().withId(videoId.getAsLong());
      DeleteFilesOnServer(video.url, video.pictureUri);
      sign = services.sign().replace(signId.getAsLong(), videoId.getAsLong(), videoUrl, pictureUri);
    } else if (signId.isPresent() && !(videoId.isPresent())) {
      sign = services.sign().addNewVideo(user.id, signId.getAsLong(), videoUrl, pictureUri);
    } else {
      sign = services.sign().create(user.id, signCreationViewApi.getName(), videoUrl, pictureUri);
    }

    if (requestId.isPresent()) {
      services.request().changeSignRequest(requestId.getAsLong(), sign.id);
    }


    response.setStatus(HttpServletResponse.SC_OK);
    requestResponseApi.signId = sign.id;
    requestResponseApi.videoId = sign.lastVideoId;
    return requestResponseApi;

  }
  private void DeleteFilesOnServer(String url, String pictureUri) {
    if (url!= null) {
      File video = new File(url);
      if (video.exists()) {
        video.delete();
      }
    }
    if (pictureUri != null) {
      File thumbnail = new File(pictureUri);
      if (thumbnail.exists()) {
        thumbnail.delete();
      }
    }
  }

}
