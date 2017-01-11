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
import com.orange.signsatwork.biz.nativeinterface.NativeInterface;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.storage.StorageService;
import com.orange.signsatwork.biz.view.model.SignCreationView;
import lombok.extern.slf4j.Slf4j;
import org.jcodec.api.JCodecException;
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

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.OptionalLong;

/**
 * Types that carry this annotation are treated as controllers where @RequestMapping
 * methods assume @ResponseBody semantics by default, ie return json body.
 */
@Slf4j
@RestController
/** Rest controller: returns a json body */
public class FileUploadRestController {

  @Autowired
  private StorageService storageService;

  @Autowired
  Services services;
  @Autowired
  DalymotionToken dalymotionToken;
  @Autowired
  private SpringRestClient springRestClient;
  @Autowired
  MessageByLocaleService messageByLocaleService;
  @Autowired
  private Environment environment;


  String REST_SERVICE_URI = "https://api.dailymotion.com";
  String VIDEO_THUMBNAIL_FIELDS = "thumbnail_url,thumbnail_60_url,thumbnail_120_url,thumbnail_180_url,thumbnail_240_url,thumbnail_360_url,thumbnail_480_url,thumbnail_720_url,";
  String VIDEO_EMBED_FIELD = "embed_url";

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD, method = RequestMethod.POST)
  public String uploadRecordedVideoFile(@RequestBody VideoFile videoFile, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFile(videoFile, OptionalLong.empty(), OptionalLong.empty(),  principal, response);
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FROM_REQUEST, method = RequestMethod.POST)
  public String uploadRecordedVideoFileFromRequest(@RequestBody VideoFile videoFile, @PathVariable long requestId, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFile(videoFile, OptionalLong.of(requestId), OptionalLong.empty(),  principal, response);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FROM_SIGN, method = RequestMethod.POST)
  public String uploadRecordedVideoFileFromSign(@RequestBody VideoFile videoFile, @PathVariable long signId, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFile(videoFile, OptionalLong.empty(), OptionalLong.of(signId),  principal, response);
  }

  private String handleRecordedVideoFile(VideoFile videoFile, OptionalLong requestId,OptionalLong signId, Principal principal, HttpServletResponse response) {
    log.info("VideoFile "+videoFile);
    log.info("VideoFile name"+videoFile.name);
    String videoUrl = null;
    String file = "/data/" + videoFile.name;
    String fileOutput = file.replace(".webm", ".mp4");

    log.info("taille fichier "+videoFile.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.http.multipart.max-file-size")));

    if (videoFile.contents.length() > parseSize(environment.getProperty("spring.http.multipart.max-file-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorFileSize");
    }


    try {
      //This will decode the String which is encoded by using Base64 class
      byte[] videoByte = DatatypeConverter.parseBase64Binary(videoFile.contents.substring(videoFile.contents.indexOf(",") + 1));

      new FileOutputStream(file).write(videoByte);
    }
    catch(Exception errorUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorUploadFile");
    }

    try {
      String cmd;

      cmd = String.format("mencoder %s -vf scale=640:-1 -ovc x264 -o %s", file, fileOutput);

      String cmdFilterLog = "/tmp/mencoder.log";
      NativeInterface.launch(cmd, null, cmdFilterLog);
    }
    catch(Exception errorEncondingFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorEncondingFile");
    }

    try {
      AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
      if (authTokenInfo.isExpired()) {
        dalymotionToken.retrieveToken();
        authTokenInfo = dalymotionToken.getAuthTokenInfo();
      }

      User user = services.user().withUserName(principal.getName());

      UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


      File fileMp4 = new File(fileOutput);
      Resource resource = new FileSystemResource(fileMp4.getAbsolutePath());
      MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
      parts.add("file", resource);

      RestTemplate restTemplate = springRestClient.buildRestTemplate();

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);


      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

      ResponseEntity<FileUploadDailymotion> responseDailymmotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
        HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
      FileUploadDailymotion fileUploadDailyMotion = responseDailymmotion.getBody();


      MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
      body.add("url", fileUploadDailyMotion.url);
      if (signId.isPresent()){
        body.add("title",services.sign().withId(signId.getAsLong()).name);
      }else{
        body.add("title", videoFile.signNameRecording);
      }
      body.add("channel", "Tech");
      body.add("published", true);


      RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
      HttpHeaders headers1 = new HttpHeaders();
      headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
      headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

      HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
      ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange("https://api.dailymotion.com/videos",
        HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
      VideoDailyMotion videoDailyMotion = response1.getBody();


      String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD;
      do {
        videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
        Thread.sleep(2 * 1000);
      }
      while ((videoDailyMotion.thumbnail_360_url == null) || (videoDailyMotion.embed_url == null) || (videoDailyMotion.thumbnail_360_url.contains("no-such-asset")));


      String pictureUri = null;
      if (!videoDailyMotion.thumbnail_360_url.isEmpty()) {
        pictureUri = videoDailyMotion.thumbnail_360_url;
        log.warn("handleFileUpload : thumbnail_360_url = {}", videoDailyMotion.thumbnail_360_url);
      }

      if (!videoDailyMotion.embed_url.isEmpty()) {
        videoUrl = videoDailyMotion.embed_url;
        log.warn("handleFileUpload : embed_url = {}", videoDailyMotion.embed_url);
      }
      Sign sign;
      if (signId.isPresent()) {
          sign = services.sign().replace(user.id, signId.getAsLong(), videoUrl, pictureUri);
      }else{
         sign = services.sign().create(user.id, videoFile.signNameRecording, videoUrl, pictureUri);
        log.info("handleFileUpload : username = {} / sign name = {} / video url = {}", user.username, videoFile.signNameRecording, videoUrl);
          }


      if (requestId.isPresent()) {
        services.request().changeSignRequest(requestId.getAsLong(), sign.id);
      }

      response.setStatus(HttpServletResponse.SC_OK);
      //return Long.toString(sign.id);
      return "/sec/sign/" + Long.toString(sign.id) + "/" + Long.toString(sign.lastVideoId) + "/detail";
    }
    catch(Exception errorDailymotionUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorDailymotionUploadFile");
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

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD, method = RequestMethod.POST)
  public String uploadSelectedVideoFile(@RequestParam("file") MultipartFile file, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) throws IOException, JCodecException, InterruptedException {
    return handleSelectedVideoFileUpload(file, OptionalLong.empty(), OptionalLong.empty(), signCreationView, principal, response);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FROM_REQUEST, method = RequestMethod.POST)
  public String createSignFromUploadondailymotion(@RequestParam("file") MultipartFile file,@PathVariable long requestId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) throws IOException, JCodecException, InterruptedException {
    return handleSelectedVideoFileUpload(file, OptionalLong.of(requestId), OptionalLong.empty(), signCreationView, principal, response);

  }
  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FROM_SIGN, method = RequestMethod.POST)
  public String createSignFromUploadondailymotionFromSign(@RequestParam("file") MultipartFile file,@PathVariable long signId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) throws IOException, JCodecException, InterruptedException {
    return handleSelectedVideoFileUpload(file, OptionalLong.empty(), OptionalLong.of(signId), signCreationView, principal, response);

  }

  private String handleSelectedVideoFileUpload(@RequestParam("file") MultipartFile file, OptionalLong requestId, OptionalLong signId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) throws InterruptedException {

    try {
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
      body.add("channel", "Tech");
      body.add("published", true);


      RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
      HttpHeaders headers1 = new HttpHeaders();
      headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
      headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

      HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
      ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange("https://api.dailymotion.com/videos",
        HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
      VideoDailyMotion videoDailyMotion = response1.getBody();


      String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD;
      do {
        videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
        Thread.sleep(2 * 1000);
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
      if (signId.isPresent()) {
        sign = services.sign().replace(user.id, signId.getAsLong(), signCreationView.getVideoUrl(), pictureUri);
      } else {
        sign = services.sign().create(user.id, signCreationView.getSignName(), signCreationView.getVideoUrl(), pictureUri);
      }
      log.info("handleSelectedVideoFileUpload : username = {} / sign name = {} / video url = {}", user.username, signCreationView.getSignName(), signCreationView.getVideoUrl());

      if (requestId.isPresent()) {
        services.request().changeSignRequest(requestId.getAsLong(), sign.id);
      }

      response.setStatus(HttpServletResponse.SC_OK);
      //return (Long.toString(sign.id));
      return "/sec/sign/" + Long.toString(sign.id) + "/" + Long.toString(sign.lastVideoId) + "/detail";
    } catch (Exception errorDailymotionUploadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorDailymotionUploadFile");
    }
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_JOB_DESCRIPTION, method = RequestMethod.POST)
  public String uploadSelectedVideoFileForJobDescription(@RequestParam("file") MultipartFile file, Principal principal, HttpServletResponse response) throws IOException, JCodecException, InterruptedException {
    return handleSelectedVideoFileUploadForProfil(file, principal, "JobDescription", response);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_NAME, method = RequestMethod.POST)
  public String uploadSelectedVideoFileForName(@RequestParam("file") MultipartFile file, Principal principal, HttpServletResponse response) throws IOException, JCodecException, InterruptedException {
    return handleSelectedVideoFileUploadForProfil(file, principal, "Name", response);
  }

  private String handleSelectedVideoFileUploadForProfil(@RequestParam("file") MultipartFile file, Principal principal, String inputType, HttpServletResponse response) throws InterruptedException {
    {
      try {
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
        if (inputType.equals("JobDescription")) {
          body.add("title", "Description du métier de " + user.firstName + " " + user.lastName);
        } else {
          body.add("title", user.firstName + " " + user.lastName);
        }
        body.add("channel", "Tech");
        body.add("published", true);


        RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
        HttpHeaders headers1 = new HttpHeaders();
        headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
        headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
        ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange("https://api.dailymotion.com/videos",
          HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
        VideoDailyMotion videoDailyMotion = response1.getBody();


        String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD;
        do {
          videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
          Thread.sleep(2 * 1000);
        }
        while ((videoDailyMotion.thumbnail_360_url == null) || (videoDailyMotion.embed_url == null) || (videoDailyMotion.thumbnail_360_url.contains("no-such-asset")));


        if (!videoDailyMotion.embed_url.isEmpty()) {
          if (inputType.equals("JobDescription")) {
            services.user().changeDescriptionVideoUrl(user, videoDailyMotion.embed_url);
          } else {
            services.user().changeNameVideoUrl(user, videoDailyMotion.embed_url);
          }

          log.warn("handleSelectedVideoFileUploadForProfil : embed_url = {}", videoDailyMotion.embed_url);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        if (inputType.equals("JobDescription")) {
          return "/sec/description-detail";
        } else {
          return "/sec/name-detail";
        }
      } catch (Exception errorDailymotionUploadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorDailymotionUploadFile");
      }
    }
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_JOB_DESCRIPTION, method = RequestMethod.POST)
  public String uploadRecordedVideoFileForJobDescription(@RequestBody VideoFile videoFile, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFileForProfil(videoFile, principal, "JobDescription", response);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_NAME, method = RequestMethod.POST)
  public String uploadRecordedVideoFileForName(@RequestBody VideoFile videoFile, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFileForProfil(videoFile, principal, "Name", response);
  }

  private String handleRecordedVideoFileForProfil(VideoFile videoFile, Principal principal, String inputType, HttpServletResponse response) {
    log.info("VideoFile "+videoFile);
    log.info("VideoFile name"+videoFile.name);
    String videoUrl = null;
    String file = "/data/" + videoFile.name;
    String fileOutput = file.replace(".webm", ".mp4");

    log.info("taille fichier "+videoFile.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.http.multipart.max-file-size")));

    if (videoFile.contents.length() > parseSize(environment.getProperty("spring.http.multipart.max-file-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorFileSize");
    }


    try {
      //This will decode the String which is encoded by using Base64 class
      byte[] videoByte = DatatypeConverter.parseBase64Binary(videoFile.contents.substring(videoFile.contents.indexOf(",") + 1));

      new FileOutputStream(file).write(videoByte);
    }
    catch(Exception errorUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorUploadFile");
    }

    try {
      String cmd;

      cmd = String.format("mencoder %s -vf scale=640:-1 -ovc x264 -o %s", file, fileOutput);

      String cmdFilterLog = "/tmp/mencoder.log";
      NativeInterface.launch(cmd, null, cmdFilterLog);
    }
    catch(Exception errorEncondingFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorEncondingFile");
    }

    try {
      AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
      if (authTokenInfo.isExpired()) {
        dalymotionToken.retrieveToken();
        authTokenInfo = dalymotionToken.getAuthTokenInfo();
      }

      User user = services.user().withUserName(principal.getName());

      UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


      File fileMp4 = new File(fileOutput);
      Resource resource = new FileSystemResource(fileMp4.getAbsolutePath());
      MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
      parts.add("file", resource);

      RestTemplate restTemplate = springRestClient.buildRestTemplate();

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);


      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

      ResponseEntity<FileUploadDailymotion> responseDailymmotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
        HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
      FileUploadDailymotion fileUploadDailyMotion = responseDailymmotion.getBody();


      MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
      body.add("url", fileUploadDailyMotion.url);
      if (inputType.equals("JobDescription")) {
        body.add("title", "Description du métier de " + user.firstName + " " + user.lastName);
      } else {
        body.add("title", user.firstName + " " + user.lastName);
      }

      body.add("channel", "Tech");
      body.add("published", true);


      RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
      HttpHeaders headers1 = new HttpHeaders();
      headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
      headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

      HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
      ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange("https://api.dailymotion.com/videos",
        HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
      VideoDailyMotion videoDailyMotion = response1.getBody();


      String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD;
      do {
        videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
        Thread.sleep(2 * 1000);
      }
      while ((videoDailyMotion.thumbnail_360_url == null) || (videoDailyMotion.embed_url == null) || (videoDailyMotion.thumbnail_360_url.contains("no-such-asset")));


      if (!videoDailyMotion.embed_url.isEmpty()) {
        if (inputType.equals("JobDescription")) {
          services.user().changeDescriptionVideoUrl(user, videoDailyMotion.embed_url);
        } else {
          services.user().changeNameVideoUrl(user, videoDailyMotion.embed_url);
        }
        log.warn("handleRecordedVideoFileForProfil : embed_url = {}", videoDailyMotion.embed_url);
      }

      response.setStatus(HttpServletResponse.SC_OK);
      if (inputType.equals("JobDescription")) {
        return "/sec/description-detail";
      } else {
        return "/sec/name-detail";
      }

    }
    catch(Exception errorDailymotionUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorDailymotionUploadFile");
    }
  }


}
