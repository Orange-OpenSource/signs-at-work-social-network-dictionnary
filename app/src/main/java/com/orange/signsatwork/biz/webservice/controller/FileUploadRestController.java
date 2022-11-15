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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orange.signsatwork.DalymotionToken;
import com.orange.signsatwork.SpringRestClient;
import com.orange.signsatwork.biz.domain.*;
import com.orange.signsatwork.biz.nativeinterface.NativeInterface;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.storage.StorageService;
import com.orange.signsatwork.biz.view.model.RequestCreationView;
import com.orange.signsatwork.biz.view.model.SignCreationView;
import com.orange.signsatwork.biz.webservice.model.RequestResponse;
import com.orange.signsatwork.biz.webservice.model.Stream;
import com.orange.signsatwork.biz.webservice.model.Streams;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.json.JSONParser;
import org.jcodec.api.JCodecException;
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
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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


  String VIDEO_THUMBNAIL_FIELDS = "thumbnail_url,thumbnail_60_url,thumbnail_120_url,thumbnail_180_url,thumbnail_240_url,thumbnail_360_url,thumbnail_480_url,thumbnail_720_url,";
  String VIDEO_EMBED_FIELD = "embed_url";
  String VIDEO_STATUS = ",status";

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD, method = RequestMethod.POST)
  public String uploadRecordedVideoFile(@RequestBody VideoFile videoFile, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleRecordedVideoFileOnServer(videoFile, OptionalLong.empty(), OptionalLong.empty(), OptionalLong.empty(), principal, response, requestHttp);
    } else {
      return handleRecordedVideoFile(videoFile, OptionalLong.empty(), OptionalLong.empty(), OptionalLong.empty(), principal, response, requestHttp);
    }
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FROM_REQUEST, method = RequestMethod.POST)
  public String uploadRecordedVideoFileFromRequest(@RequestBody VideoFile videoFile, @PathVariable long requestId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleRecordedVideoFileOnServer(videoFile, OptionalLong.of(requestId), OptionalLong.empty(), OptionalLong.empty(), principal, response, requestHttp);
    } else {
      return handleRecordedVideoFile(videoFile, OptionalLong.of(requestId), OptionalLong.empty(), OptionalLong.empty(), principal, response, requestHttp);
    }
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FROM_SIGN, method = RequestMethod.POST)
  public String uploadRecordedVideoFileFromSign(@RequestBody VideoFile videoFile, @PathVariable long signId, @PathVariable long videoId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleRecordedVideoFileOnServer(videoFile, OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.of(videoId), principal, response, requestHttp);
    } else {
      return handleRecordedVideoFile(videoFile, OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.of(videoId), principal, response, requestHttp);
    }
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_NEW_VIDEO, method = RequestMethod.POST)
  public String uploadRecordedVideoFileForNewVideo(@RequestBody VideoFile videoFile, @PathVariable long signId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleRecordedVideoFileOnServer(videoFile, OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.empty(), principal, response, requestHttp);
    } else {
      return handleRecordedVideoFile(videoFile, OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.empty(), principal, response, requestHttp);
    }
  }

  private String handleRecordedVideoFile(VideoFile videoFile, OptionalLong requestId,OptionalLong signId, OptionalLong videoId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    log.info("VideoFile "+videoFile);
    log.info("VideoFile name"+videoFile.name);
    String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");
    String videoUrl = null;
    String file = environment.getProperty("app.file") + "/" + videoFile.name;
    String thumbnailFile = environment.getProperty("app.file") + "/thumbnail/" + videoFile.name.replace(".webm", ".png");

    log.info("taille fichier "+videoFile.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.servlet.multipart.max-request-size")));

    if (videoFile.contents.length() > parseSize(environment.getProperty("spring.servlet.multipart.max-request-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorFileSize");
    }


    try {
      //This will decode the String which is encoded by using Base64 class
      extracted(videoFile, file);
    }
    catch(Exception errorUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorUploadFile");
    }

    try {
      GenerateThumbnail(thumbnailFile, file);
    } catch (Exception errorEncondingFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorThumbnailFile");
    }

    try {
      String dailymotionId;
      AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
      if (authTokenInfo.isExpired()) {
        dalymotionToken.retrieveToken();
        authTokenInfo = dalymotionToken.getAuthTokenInfo();
      }

      User user = services.user().withUserName(principal.getName());

      UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


      File fileMp4 = new File(file);
      Resource resource = new FileSystemResource(fileMp4.getAbsolutePath());
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


      String pictureUri = thumbnailFile;
      videoUrl= file;

      Sign sign;
      Video video;
      if (signId.isPresent() && (videoId.isPresent())) {
          video = services.video().withId(videoId.getAsLong());
          if (video.url.contains("http")) {
            dailymotionId = video.url.substring(video.url.lastIndexOf('/') + 1);
            try {
              DeleteVideoOnDailyMotion(dailymotionId);
            } catch (Exception errorDailymotionDeleteVideo) {
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
            }
          }
          sign = services.sign().replace(signId.getAsLong(), videoId.getAsLong(), videoUrl, pictureUri);
          sendMailAndUpdateMessageServeurIfAdmin(signId, requestHttp, user, sign, video);
      } else if (signId.isPresent() && !(videoId.isPresent())) {
        sign = services.sign().addNewVideo(user.id, signId.getAsLong(), videoUrl, pictureUri);
      } else {
         sign = services.sign().create(user.id, videoFile.signNameRecording, videoUrl, pictureUri);
        log.info("handleRecordedVideoFile : username = {} / sign name = {} / video url = {}", user.username, videoFile.signNameRecording, videoUrl);
      }


      if (requestId.isPresent()) {
        services.request().changeSignRequest(requestId.getAsLong(), sign.id);
      }

      if (sign.lastVideoId != 0) {
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
      }

      response.setStatus(HttpServletResponse.SC_OK);

      return "/sec/sign/" + Long.toString(sign.id) + "/" + Long.toString(sign.lastVideoId) + "/detail";
    }
    catch(Exception errorDailymotionUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorDailymotionUploadFile");
    }
  }

  private String handleRecordedVideoFileOnServer(VideoFile videoFile, OptionalLong requestId,OptionalLong signId, OptionalLong videoId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    log.info("VideoFile "+videoFile);
    log.info("VideoFile name"+videoFile.name);
    String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");
    String videoUrl = null;
    String file = environment.getProperty("app.file") + "/" + videoFile.name;
    String thumbnailFile = environment.getProperty("app.file") + "/thumbnail/" + videoFile.name.replace(".webm", ".png");

    log.info("taille fichier "+videoFile.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.servlet.multipart.max-request-size")));

    if (videoFile.contents.length() > parseSize(environment.getProperty("spring.servlet.multipart.max-request-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorFileSize");
    }


    try {
      //This will decode the String which is encoded by using Base64 class
      extracted(videoFile, file);
    }
    catch(Exception errorUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorUploadFile");
    }

    try {
      GenerateThumbnail(thumbnailFile, file);
    } catch (Exception errorEncondingFile) {
      DeleteFilesOnServer(file, null);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorThumbnailFile");
    }


    User user = services.user().withUserName(principal.getName());

    String pictureUri = thumbnailFile;
    videoUrl= file;

    Sign sign;
    Video video;
    if (signId.isPresent() && (videoId.isPresent())) {
      video = services.video().withId(videoId.getAsLong());
      DeleteFilesOnServer(video.url, video.pictureUri);
      sign = services.sign().replace(signId.getAsLong(), videoId.getAsLong(), videoUrl, pictureUri);
      sendMailAndUpdateMessageServeurIfAdmin(signId, requestHttp, user, sign, video);
    } else if (signId.isPresent() && !(videoId.isPresent())) {
      sign = services.sign().addNewVideo(user.id, signId.getAsLong(), videoUrl, pictureUri);
    } else {
      sign = services.sign().create(user.id, videoFile.signNameRecording, videoUrl, pictureUri);
      log.info("handleRecordedVideoFileOnServer : username = {} / sign name = {} / video url = {}", user.username, videoFile.signNameRecording, videoUrl);
    }


    if (requestId.isPresent()) {
      services.request().changeSignRequest(requestId.getAsLong(), sign.id);
    }

    response.setStatus(HttpServletResponse.SC_OK);
    return "/sec/sign/" + Long.toString(sign.id) + "/" + Long.toString(sign.lastVideoId) + "/detail";

  }

  private void sendMailAndUpdateMessageServeurIfAdmin(OptionalLong signId, HttpServletRequest requestHttp, User user, Sign sign, Video video) {
    User admin = services.user().getAdmin();
    if (user.username == admin.username) {
      String videoName;
      if ((video.idForName == 0) || (sign.nbVideo == 1)) {
        videoName = sign.name;
      } else {
        videoName = sign.name + "_" + video.idForName;
      }
      String title = messageByLocaleService.getMessage("update_video_title", new Object[]{videoName});
      String bodyMail = messageByLocaleService.getMessage("update_video_body", new Object[]{getAppUrl() + "/sign/" + sign.id + "/" + video.id});
      String messageType = "UpdateVideoSendEmailMessage";
      Videos videos = services.video().forSign(signId.getAsLong());
      List<String> emails = videos.stream().filter(v-> v.user.email != null).map(v -> v.user.email).collect(Collectors.toList());
      emails = emails.stream().distinct().collect(Collectors.toList());
      if (emails.size() != 0) {
        List<String> finalEmails = emails;
        String finalMessageType = messageType;
        Runnable task = () -> {
          log.info("send mail email = {} / title = {} / body = {}", finalEmails.toString(), title, bodyMail);
          services.emailService().sendVideoMessage(finalEmails.toArray(new String[finalEmails.size()]), title, bodyMail, videoName, finalMessageType, requestHttp.getLocale());
        };
        new Thread(task).start();
      } else {
        messageType = "UpdateVideoMessage";
        String values = admin.username + ';' + videoName;
        MessageServer messageServer = new MessageServer(new Date(), messageType, values, ActionType.NO);
        services.messageServerService().addMessageServer(messageServer);
      }
    }
  }

  private void DeleteFilesOnServer(String url, String pictureUri) {
    if (url != null) {
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

  private void extracted(VideoFile videoFile, String file) throws IOException {
    String  lexical = videoFile.contents.substring(videoFile.contents.indexOf(",") + 1);
    byte[] videoByte = DatatypeConverter.parseBase64Binary(lexical);

    new FileOutputStream(file).write(videoByte);
  }

  private void GenerateThumbnail(String thumbnailFile, String fileOutput) {
    String cmdGenerateThumbnail;

    cmdGenerateThumbnail = String.format("input=\"%s\"&&dur=$(ffprobe -loglevel error -show_entries format=duration -of default=nk=1:nw=1 \"$input\")&&ffmpeg -y -ss \"$(echo \"$dur / 2\" | bc -l | sed -e 's/^-\\./-0./' -e 's/^\\./0./')\" -i  \"$input\" -vframes 1 -s 360x360 -vf \"scale=(iw*sar)*max(360.1/(iw*sar)\\,360.1/ih):ih*max(360.1/(iw*sar)\\,360.1/ih), crop=360:360\" \"%s\"", fileOutput, thumbnailFile);
    String cmdGenerateThumbnailFilterLog = "/tmp/ffmpeg.log";
    NativeInterface.launch(cmdGenerateThumbnail, null, cmdGenerateThumbnailFilterLog);
  }

  private String SearchFileCodec(String file) {
    String cmdFileCodec = String.format("ffprobe -v error -select_streams v:0 -show_entries stream=codec_name -of default=noprint_wrappers=1:nokey=1 %s", file);
    String fileCodec = NativeInterface.launchAndGetOutput(cmdFileCodec, null, null);
    return fileCodec;
  }

  private Streams SearchFileInfo(String file) throws JsonProcessingException {
    String cmdFileInfo = String.format("ffprobe -v quiet -print_format json -show_streams -select_streams v:0 %s", file);
    String fileInfo = NativeInterface.launchAndGetOutput(cmdFileInfo, null, null);
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    Streams streams = objectMapper.readValue(fileInfo, Streams.class);
    return streams;
  }

  private void EncodeFileInH264(String file, String fileOutput) {
    String cmd;

    cmd = String.format("ffmpeg -i %s -c:v libx264 -crf 20 -c:a copy %s", file, fileOutput);

    NativeInterface.launch(cmd, null, null);
  }

  private void ReduceFileSizeInChangingResolution(String file, String fileOutput) {
    String cmd;
    cmd = String.format("ffmpeg -i %s -filter:v \"scale='min(1280,iw)':min'(720,ih)':force_original_aspect_ratio=decrease,pad=1280:720:(ow-iw)/2:(oh-ih)/2,crop=1280:720\" %s", file, fileOutput);

    NativeInterface.launch(cmd, null, null);
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
  public String uploadSelectedVideoFile(@RequestParam("file") MultipartFile file, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) throws IOException, JCodecException, InterruptedException {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleSelectedVideoFileUploadOnServer(file, OptionalLong.empty(), OptionalLong.empty(), OptionalLong.empty(), signCreationView, principal, response, requestHttp);
    } else {
      return handleSelectedVideoFileUpload(file, OptionalLong.empty(), OptionalLong.empty(), OptionalLong.empty(), signCreationView, principal, response, requestHttp);
    }
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FROM_REQUEST, method = RequestMethod.POST)
  public String createSignFromUploadondailymotion(@RequestParam("file") MultipartFile file,@PathVariable long requestId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) throws IOException, JCodecException, InterruptedException {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleSelectedVideoFileUploadOnServer(file, OptionalLong.of(requestId), OptionalLong.empty(), OptionalLong.empty(), signCreationView, principal, response, requestHttp);
    } else {
      return handleSelectedVideoFileUpload(file, OptionalLong.of(requestId), OptionalLong.empty(), OptionalLong.empty(), signCreationView, principal, response, requestHttp);
    }
  }
  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FROM_SIGN, method = RequestMethod.POST)
  public String createSignFromUploadondailymotionFromSign(@RequestParam("file") MultipartFile file,@PathVariable long signId, @PathVariable long videoId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) throws IOException, JCodecException, InterruptedException {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleSelectedVideoFileUploadOnServer(file, OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.of(videoId), signCreationView, principal, response, requestHttp);
    } else {
      return handleSelectedVideoFileUpload(file, OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.of(videoId), signCreationView, principal, response, requestHttp);
    }
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_NEW_VIDEO, method = RequestMethod.POST)
  public String createSignFromUploadondailymotionForNewVideo(@RequestParam("file") MultipartFile file,@PathVariable long signId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) throws IOException, JCodecException, InterruptedException {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleSelectedVideoFileUploadOnServer(file, OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.empty(), signCreationView, principal, response, requestHttp);
    } else {
      return handleSelectedVideoFileUpload(file, OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.empty(), signCreationView, principal, response, requestHttp);
    }

  }

  private String handleSelectedVideoFileUpload(@RequestParam("file") MultipartFile file, OptionalLong requestId, OptionalLong signId, OptionalLong videoId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) throws InterruptedException {
      String videoUrl = null;
      String fileName = environment.getProperty("app.file") +"/" + file.getOriginalFilename();
      String thumbnailFile = environment.getProperty("app.file") + "/thumbnail/" + file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf('.')) + ".png";
      File inputFile;

    try {
      storageService.store(file);
      inputFile = storageService.load(file.getOriginalFilename()).toFile();
    } catch (Exception errorLoadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorUploadFile");
    }

    try {
      GenerateThumbnail(thumbnailFile, inputFile.getAbsolutePath());
    } catch (Exception errorEncondingFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorThumbnailFile");
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
      String videosUrl = REST_SERVICE_URI + "/videos";
      ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange(videosUrl,
        HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
      VideoDailyMotion videoDailyMotion = response1.getBody();


      String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD + VIDEO_STATUS;
      String id = videoDailyMotion.id;

      String pictureUri = thumbnailFile;
      videoUrl= fileName;

      Sign sign;
      Video video;
      if (signId.isPresent() && (videoId.isPresent())) {
        video = services.video().withId(videoId.getAsLong());
        if (video.url.contains("http")) {
          dailymotionId = video.url.substring(video.url.lastIndexOf('/') + 1);
          try {
            DeleteVideoOnDailyMotion(dailymotionId);
          } catch (Exception errorDailymotionDeleteVideo) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
          }
        }
        sign = services.sign().replace(signId.getAsLong(), videoId.getAsLong(), videoUrl, pictureUri);
        sendMailAndUpdateMessageServeurIfAdmin(signId, requestHttp, user, sign, video);
      } else if (signId.isPresent() && !(videoId.isPresent())) {
        sign = services.sign().addNewVideo(user.id, signId.getAsLong(), videoUrl, pictureUri);
      } else {
        sign = services.sign().create(user.id, signCreationView.getSignName(), videoUrl, pictureUri);
      }

      log.info("handleSelectedVideoFileUpload : username = {} / sign name = {} / video url = {}", user.username, signCreationView.getSignName(), videoUrl);

      if (requestId.isPresent()) {
        services.request().changeSignRequest(requestId.getAsLong(), sign.id);
      }

      if (sign.lastVideoId != 0) {
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
      }

      response.setStatus(HttpServletResponse.SC_OK);

      return "/sec/sign/" + Long.toString(sign.id) + "/" + Long.toString(sign.lastVideoId) + "/detail";
    } catch (Exception errorDailymotionUploadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorDailymotionUploadFile");
    }
  }

  private String handleSelectedVideoFileUploadOnServer(@RequestParam("file") MultipartFile file, OptionalLong requestId, OptionalLong signId, OptionalLong videoId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) throws InterruptedException {
    String videoUrl = null;
    String newFileName = UUID.randomUUID().toString() + "." + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
    String newAbsoluteFileName = environment.getProperty("app.file") +"/" + newFileName;
    String thumbnailFile = environment.getProperty("app.file") + "/thumbnail/" + newFileName.substring(0, newFileName.lastIndexOf('.')) + ".png";
    File inputFile;
    Streams streamInfo;
    String newAbsoluteFileNameWithExtensionMp4 = newAbsoluteFileName.substring(0, newFileName.lastIndexOf('.')) + ".mp4";

    try {
      storageService.store(file);
      inputFile = storageService.load(file.getOriginalFilename()).toFile();
      File newName = new File(newAbsoluteFileName);
      inputFile.renameTo(newName);
    } catch (Exception errorLoadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorUploadFile");
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
        (file.getSize() >= parseSize(environment.getProperty("file-size-max-to-reduce"))) &&
        (streamInfo.getStreams().stream().findFirst().get().getTags().getRotate() == null)) {
        ReduceFileSizeInChangingResolution(newAbsoluteFileName, newAbsoluteFileNameWithExtensionMp4);
        newAbsoluteFileName = newAbsoluteFileNameWithExtensionMp4;
      }
    }


    try {
      GenerateThumbnail(thumbnailFile, newAbsoluteFileName);
    } catch (Exception errorEncondingFile) {
      DeleteFilesOnServer(newAbsoluteFileName, null);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorThumbnailFile");
    }


    User user = services.user().withUserName(principal.getName());

    String pictureUri = thumbnailFile;
    videoUrl= newAbsoluteFileName;

    Sign sign;
    Video video;
    if (signId.isPresent() && (videoId.isPresent())) {
      video = services.video().withId(videoId.getAsLong());
      DeleteFilesOnServer(video.url, video.pictureUri);
      sign = services.sign().replace(signId.getAsLong(), videoId.getAsLong(), videoUrl, pictureUri);
      sendMailAndUpdateMessageServeurIfAdmin(signId, requestHttp, user, sign, video);
    } else if (signId.isPresent() && !(videoId.isPresent())) {
      sign = services.sign().addNewVideo(user.id, signId.getAsLong(), videoUrl, pictureUri);
    } else {
      sign = services.sign().create(user.id, signCreationView.getSignName(), videoUrl, pictureUri);
    }

    log.info("handleSelectedVideoFileUploadOnServer : username = {} / sign name = {} / video url = {}", user.username, signCreationView.getSignName(), videoUrl);

    if (requestId.isPresent()) {
      services.request().changeSignRequest(requestId.getAsLong(), sign.id);
    }

    response.setStatus(HttpServletResponse.SC_OK);
    return "/sec/sign/" + Long.toString(sign.id) + "/" + Long.toString(sign.lastVideoId) + "/detail";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_JOB_DESCRIPTION, method = RequestMethod.POST)
  public String uploadSelectedVideoFileForJobDescription(@RequestParam("file") MultipartFile file, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) throws IOException, JCodecException, InterruptedException {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleSelectedVideoFileUploadForProfilOnServer(file, OptionalLong.empty(), principal, "JobDescription", response, requestHttp);
    } else {
      return handleSelectedVideoFileUploadForProfil(file, OptionalLong.empty(), principal, "JobDescription", response, requestHttp);
    }
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_JOB_DESCRIPTION_FOR_USER, method = RequestMethod.POST)
  public String uploadSelectedVideoFileForJobDescriptionForUser(@RequestParam("file") MultipartFile file, @PathVariable long userId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) throws IOException, JCodecException, InterruptedException {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleSelectedVideoFileUploadForProfilOnServer(file, OptionalLong.of(userId), principal, "JobDescription", response, requestHttp);
    } else {
      return handleSelectedVideoFileUploadForProfil(file, OptionalLong.of(userId), principal, "JobDescription", response, requestHttp);
    }
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_NAME, method = RequestMethod.POST)
  public String uploadSelectedVideoFileForName(@RequestParam("file") MultipartFile file, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) throws IOException, JCodecException, InterruptedException {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleSelectedVideoFileUploadForProfilOnServer(file, OptionalLong.empty(), principal, "Name", response, requestHttp);
    } else {
      return handleSelectedVideoFileUploadForProfil(file, OptionalLong.empty(), principal, "Name", response, requestHttp);
    }
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_NAME_FOR_USER, method = RequestMethod.POST)
  public String uploadSelectedVideoFileForNameForUser(@RequestParam("file") MultipartFile file, @PathVariable long userId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) throws IOException, JCodecException, InterruptedException {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleSelectedVideoFileUploadForProfilOnServer(file, OptionalLong.of(userId), principal, "Name", response, requestHttp);
    } else {
      return handleSelectedVideoFileUploadForProfil(file, OptionalLong.of(userId), principal, "Name", response, requestHttp);
    }
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_DELETE_VIDEO_FILE_FOR_NAME, method = RequestMethod.PUT)
  public String deleteVideoFileForName(Principal principal, HttpServletResponse response) throws IOException, JCodecException, InterruptedException {
      String dailymotionId;
      User user = services.user().withUserName(principal.getName());
      if (user.nameVideo != null) {
        if (user.nameVideo.contains("http")) {
          dailymotionId = user.nameVideo.substring(user.nameVideo.lastIndexOf('/') + 1);
          try {
            DeleteVideoOnDailyMotion(dailymotionId);
          } catch (Exception errorDailymotionDeleteVideo) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
          }
        } else {
          DeleteFilesOnServer(user.nameVideo, user.namePicture);
        }
        services.user().changeNameVideoUrl(user, null, null);
      }
      response.setStatus(HttpServletResponse.SC_OK);
      return "/sec/my_profil";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_SEC_DELETE_VIDEO_FILE_FOR_NAME_FOR_USER, method = RequestMethod.PUT)
  public String deleteVideoFileForNameForUser(Principal principal, @PathVariable long userId, HttpServletResponse response, HttpServletRequest requestHttp) throws IOException, JCodecException, InterruptedException {
    String dailymotionId;
    String title, body = null, messageServer = null;
    User user = services.user().withId(userId);
    User admin = services.user().getAdmin();
    title = messageByLocaleService.getMessage("admin_change_profil");

    if (user.nameVideo != null) {
      if (user.nameVideo.contains("http")) {
        dailymotionId = user.nameVideo.substring(user.nameVideo.lastIndexOf('/') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionId);
        } catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
        }
      } else {
        DeleteFilesOnServer(user.nameVideo, user.namePicture);
      }
      services.user().changeNameVideoUrl(user, null, null);
      body = messageByLocaleService.getMessage("admin_delete_your_name_lsf", new Object[]{admin.username});
      messageServer = "UserDeleteNameLsfMessage";
    }

    if (body != null) {
      String finalBody = body;
      String finalMessageServer = messageServer;
      User finalUser = user;
      Runnable task = () -> {
        log.info("send mail email = {} / title = {} / body = {}", finalUser.username, title, finalBody);
        services.emailService().sendUpdateProfilUserByAdminMessage(finalUser.username, title, finalUser.username, finalBody, finalMessageServer, requestHttp.getLocale());
      };

      new Thread(task).start();
    }
    response.setStatus(HttpServletResponse.SC_OK);
    return "/sec/my_profil";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_DELETE_VIDEO_FILE_FOR_JOB, method = RequestMethod.PUT)
  public String deleteVideoFileForJob(Principal principal, HttpServletResponse response) throws IOException, JCodecException, InterruptedException {
    String dailymotionId;
    User user = services.user().withUserName(principal.getName());
    if (user.jobDescriptionVideo != null) {
      if (user.jobDescriptionVideo.contains("http")) {
        dailymotionId = user.jobDescriptionVideo.substring(user.jobDescriptionVideo.lastIndexOf('/') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionId);
        } catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
        }
      } else {
        DeleteFilesOnServer(user.jobDescriptionVideo, user.jobDescriptionPicture);
      }
      services.user().changeDescriptionVideoUrl(user, null, null);
    }
    response.setStatus(HttpServletResponse.SC_OK);
    return "/sec/my_profil";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_SEC_DELETE_VIDEO_FILE_FOR_JOB_FOR_USER, method = RequestMethod.PUT)
  public String deleteVideoFileForJobForUser(Principal principal, @PathVariable long userId, HttpServletResponse response, HttpServletRequest requestHttp) throws IOException, JCodecException, InterruptedException {
    String dailymotionId;
    String title, body = null, messageServer = null;
    User user = services.user().withId(userId);
    User admin = services.user().getAdmin();
    title = messageByLocaleService.getMessage("admin_change_profil");

    if (user.jobDescriptionVideo != null) {
      if (user.jobDescriptionVideo.contains("http")) {
        dailymotionId = user.jobDescriptionVideo.substring(user.jobDescriptionVideo.lastIndexOf('/') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionId);
        } catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
        }
      } else {
        DeleteFilesOnServer(user.jobDescriptionVideo, user.jobDescriptionPicture);
      }
      services.user().changeDescriptionVideoUrl(user, null, null);
      body = messageByLocaleService.getMessage("admin_delete_your_job_description_lsf", new Object[]{admin.username});
      messageServer = "UserDeleteJobDescriptionLsfMessage";
    }
    if (body != null) {
      String finalBody = body;
      String finalMessageServer = messageServer;
      User finalUser = user;
      Runnable task = () -> {
        log.info("send mail email = {} / title = {} / body = {}", finalUser.username, title, finalBody);
        services.emailService().sendUpdateProfilUserByAdminMessage(finalUser.username, title, finalUser.username, finalBody, finalMessageServer, requestHttp.getLocale());
      };

      new Thread(task).start();
    }
    response.setStatus(HttpServletResponse.SC_OK);
    return "/sec/my_profil";
  }

  private String handleSelectedVideoFileUploadForProfil(@RequestParam("file") MultipartFile file, OptionalLong userId, Principal principal, String inputType, HttpServletResponse response, HttpServletRequest requestHttp) throws InterruptedException {
    {
      String title, bodyMail = null, messageServer = null;
      String videoUrl = null;
      String fileName = environment.getProperty("app.file") + "/" + file.getOriginalFilename();
      String thumbnailFile = environment.getProperty("app.file") + "/thumbnail/" + file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf('.')) + ".png";
      File inputFile;

      User admin = services.user().getAdmin();
      title = messageByLocaleService.getMessage("admin_change_profil");

      try {
        storageService.store(file);
        inputFile = storageService.load(file.getOriginalFilename()).toFile();
      } catch (Exception errorLoadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorUploadFile");
      }

      try {
        GenerateThumbnail(thumbnailFile, inputFile.getAbsolutePath());
      } catch (Exception errorEncondingFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorThumbnailFile");
      }

      try {
        String dailymotionId;
        String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");
        AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
        if (authTokenInfo.isExpired()) {
          dalymotionToken.retrieveToken();
          authTokenInfo = dalymotionToken.getAuthTokenInfo();
        }

        User user;
        if (userId.isPresent()) {
          user = services.user().withId(userId.getAsLong());
        } else {
          user = services.user().withUserName(principal.getName());
        }

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
        if (inputType.equals("JobDescription")) {
          body.add("title", messageByLocaleService.getMessage("user.job_description"));
        } else {
          body.add("title", messageByLocaleService.getMessage("user.name_LSF"));
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


        String pictureUri = thumbnailFile;
        videoUrl= fileName;

          if (inputType.equals("JobDescription")) {
            if (user.jobDescriptionVideo != null) {
              if (user.jobDescriptionVideo.contains("http")) {
                dailymotionId = user.jobDescriptionVideo.substring(user.jobDescriptionVideo.lastIndexOf('/') + 1);
                try {
                  DeleteVideoOnDailyMotion(dailymotionId);
                } catch (Exception errorDailymotionDeleteVideo) {
                  response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                  return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
                }
              }
            }
            services.user().changeDescriptionVideoUrl(user, videoUrl, pictureUri);
            bodyMail = messageByLocaleService.getMessage("admin_change_your_job_description_lsf", new Object[]{admin.username});
            messageServer = "UserChangeJobDescriptionLsfMessage";
          } else {
            if (user.nameVideo != null) {
              if (user.nameVideo.contains("http")) {
                dailymotionId = user.nameVideo.substring(user.nameVideo.lastIndexOf('/') + 1);
                try {
                  DeleteVideoOnDailyMotion(dailymotionId);
                } catch (Exception errorDailymotionDeleteVideo) {
                  response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                  return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
                }
              }
            }
            services.user().changeNameVideoUrl(user, videoUrl, pictureUri);
            bodyMail = messageByLocaleService.getMessage("admin_change_your_name_lsf", new Object[]{admin.username});
            messageServer = "UserChangeNameLsfMessage";
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
          if (!dailyMotion.embed_url.isEmpty()) {
            if (inputType.equals("JobDescription")) {
              services.user().changeDescriptionVideoUrl(user, dailyMotion.embed_url, dailyMotion.thumbnail_360_url);
            } else {
              services.user().changeNameVideoUrl(user, dailyMotion.embed_url, dailyMotion.thumbnail_360_url);
            }
          }

        };

        new Thread(task).start();

        if (bodyMail != null && userId.isPresent()) {
          String finalBody = bodyMail;
          String finalMessageServer = messageServer;
          User finalUser = user;
          Runnable taskSendMail = () -> {
            log.info("send mail email = {} / title = {} / body = {}", finalUser.username, title, finalBody);
            services.emailService().sendUpdateProfilUserByAdminMessage(finalUser.username, title, finalUser.username, finalBody, finalMessageServer, requestHttp.getLocale());
          };

          new Thread(taskSendMail).start();
        }

        response.setStatus(HttpServletResponse.SC_OK);
        return "";
      } catch (Exception errorDailymotionUploadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorDailymotionUploadFile");
      }
    }
  }

  private String handleSelectedVideoFileUploadForProfilOnServer(@RequestParam("file") MultipartFile file, OptionalLong userId, Principal principal, String inputType, HttpServletResponse response, HttpServletRequest requestHttp) throws InterruptedException {
    {
      String title, body = null, messageServer = null;
      String videoUrl = null;
      String newFileName = UUID.randomUUID().toString() + "." + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
      String newAbsoluteFileName = environment.getProperty("app.file") +"/" + newFileName;
      String thumbnailFile = environment.getProperty("app.file") + "/thumbnail/" + newFileName.substring(0, newFileName.lastIndexOf('.')) + ".png";
      File inputFile;
      Streams streamInfo;
      String newAbsoluteFileNameWithExtensionMp4 = newAbsoluteFileName.substring(0, newFileName.lastIndexOf('.')) + ".mp4";

      User admin = services.user().getAdmin();
      title = messageByLocaleService.getMessage("admin_change_profil");

      try {
        storageService.store(file);
        inputFile = storageService.load(file.getOriginalFilename()).toFile();
        File newName = new File(newAbsoluteFileName);
        inputFile.renameTo(newName);
      } catch (Exception errorLoadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorUploadFile");
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
          (file.getSize() >= parseSize(environment.getProperty("file-size-max-to-reduce"))) &&
          (streamInfo.getStreams().stream().findFirst().get().getTags().getRotate() == null)) {
          ReduceFileSizeInChangingResolution(newAbsoluteFileName, newAbsoluteFileNameWithExtensionMp4);
          newAbsoluteFileName = newAbsoluteFileNameWithExtensionMp4;
        }
      }

      try {
        GenerateThumbnail(thumbnailFile, newAbsoluteFileName);
      } catch (Exception errorEncondingFile) {
        DeleteFilesOnServer(newAbsoluteFileName, null);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorThumbnailFile");
      }

      User user;
      if (userId.isPresent()) {
        user = services.user().withId(userId.getAsLong());
      } else {
        user = services.user().withUserName(principal.getName());
      }
      String pictureUri = thumbnailFile;
      videoUrl= newAbsoluteFileName;

      if (inputType.equals("JobDescription")) {
        if (user.jobDescriptionVideo != null) {
          DeleteFilesOnServer(user.jobDescriptionVideo, user.jobDescriptionPicture);
        }
        services.user().changeDescriptionVideoUrl(user, videoUrl, pictureUri);
        body = messageByLocaleService.getMessage("admin_change_your_job_description_lsf", new Object[]{admin.username});
        messageServer = "UserChangeJobDescriptionLsfMessage";
      } else {
        if (user.nameVideo != null) {
          DeleteFilesOnServer(user.nameVideo, user.namePicture);
        }
        services.user().changeNameVideoUrl(user, videoUrl, pictureUri);
        body = messageByLocaleService.getMessage("admin_change_your_name_lsf", new Object[]{admin.username});
        messageServer = "UserChangeNameLsfMessage";
      }

      if (body != null && userId.isPresent()) {
        String finalBody = body;
        String finalMessageServer = messageServer;
        User finalUser = user;
        Runnable task = () -> {
          log.info("send mail email = {} / title = {} / body = {}", finalUser.username, title, finalBody);
          services.emailService().sendUpdateProfilUserByAdminMessage(finalUser.username, title, finalUser.username, finalBody, finalMessageServer, requestHttp.getLocale());
        };

        new Thread(task).start();
      }

      response.setStatus(HttpServletResponse.SC_OK);
      return "";
    }
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_JOB_DESCRIPTION, method = RequestMethod.POST)
  public String uploadRecordedVideoFileForJobDescription(@RequestBody VideoFile videoFile, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleRecordedVideoFileForProfilOnServer(videoFile, OptionalLong.empty(), principal, "JobDescription", response, requestHttp);
    } else {
      return handleRecordedVideoFileForProfil(videoFile, OptionalLong.empty(), principal, "JobDescription", response, requestHttp);
    }
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_JOB_DESCRIPTION_FOR_USER, method = RequestMethod.POST)
  public String uploadRecordedVideoFileForJobDescription(@RequestBody VideoFile videoFile, @PathVariable long userId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleRecordedVideoFileForProfilOnServer(videoFile,  OptionalLong.of(userId),  principal, "JobDescription", response, requestHttp);
    } else {
      return handleRecordedVideoFileForProfil(videoFile,  OptionalLong.of(userId), principal, "JobDescription", response, requestHttp);
    }
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_NAME, method = RequestMethod.POST)
  public String uploadRecordedVideoFileForName(@RequestBody VideoFile videoFile, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleRecordedVideoFileForProfilOnServer(videoFile,  OptionalLong.empty(), principal, "Name", response, requestHttp);
    } else {
      return handleRecordedVideoFileForProfil(videoFile,  OptionalLong.empty(), principal, "Name", response, requestHttp);
    }
  }
  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_NAME_FOR_USER, method = RequestMethod.POST)
  public String uploadRecordedVideoFileForNameForUser(@RequestBody VideoFile videoFile, @PathVariable long userId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleRecordedVideoFileForProfilOnServer(videoFile, OptionalLong.of(userId), principal, "Name", response, requestHttp);
    } else {
      return handleRecordedVideoFileForProfil(videoFile, OptionalLong.of(userId), principal, "Name", response, requestHttp);
    }
  }

  private String handleRecordedVideoFileForProfil(VideoFile videoFile, OptionalLong userId, Principal principal, String inputType, HttpServletResponse response, HttpServletRequest requestHttp) {
    log.info("VideoFile "+videoFile);
    log.info("VideoFile name"+videoFile.name);
    String title, bodyMail = null, messageServer = null;
    String videoUrl = null;
    String file = environment.getProperty("app.file") + "/" + videoFile.name;
    String thumbnailFile = environment.getProperty("app.file") + "/thumbnail/" + videoFile.name.replace(".webm", ".png");

    log.info("taille fichier "+videoFile.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.servlet.multipart.max-request-size")));

    User admin = services.user().getAdmin();
    title = messageByLocaleService.getMessage("admin_change_profil");


    if (videoFile.contents.length() > parseSize(environment.getProperty("spring.servlet.multipart.max-request-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorFileSize");
    }


    try {
      //This will decode the String which is encoded by using Base64 class
      extracted(videoFile, file);
    }
    catch(Exception errorUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorUploadFile");
    }


    try {
      GenerateThumbnail(thumbnailFile, file);
    } catch (Exception errorEncondingFile) {
      DeleteFilesOnServer(file, null);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorThumbnailFile");
    }

    try {
      String dailymotionId;
      String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");

      AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
      if (authTokenInfo.isExpired()) {
        dalymotionToken.retrieveToken();
        authTokenInfo = dalymotionToken.getAuthTokenInfo();
      }

      User user;
      if (userId.isPresent()) {
        user = services.user().withId(userId.getAsLong());
      } else {
        user = services.user().withUserName(principal.getName());
      }

      UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


      File fileMp4 = new File(file);
      Resource resource = new FileSystemResource(fileMp4.getAbsolutePath());
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

      ResponseEntity<FileUploadDailymotion> responseDailymmotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
        HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
      FileUploadDailymotion fileUploadDailyMotion = responseDailymmotion.getBody();


      MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
      body.add("url", fileUploadDailyMotion.url);
      if (inputType.equals("JobDescription")) {
        body.add("title", messageByLocaleService.getMessage("user.job_description"));
      } else {
        body.add("title", messageByLocaleService.getMessage("user.name_LSF"));
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

      String pictureUri = thumbnailFile;
      videoUrl= file;

        if (inputType.equals("JobDescription")) {
          if (user.jobDescriptionVideo != null) {
            if (user.jobDescriptionVideo.contains("http")) {
              dailymotionId = user.jobDescriptionVideo.substring(user.jobDescriptionVideo.lastIndexOf('/') + 1);
              try {
                DeleteVideoOnDailyMotion(dailymotionId);
              } catch (Exception errorDailymotionDeleteVideo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
              }
            }
          }
          services.user().changeDescriptionVideoUrl(user, videoUrl, pictureUri);
          bodyMail = messageByLocaleService.getMessage("admin_change_your_job_description_lsf", new Object[]{admin.username});
          messageServer = "UserChangeJobDescriptionLsfMessage";
        } else {
          if (user.nameVideo != null) {
            if (user.nameVideo.contains("http")) {
              dailymotionId = user.nameVideo.substring(user.nameVideo.lastIndexOf('/') + 1);
              try {
                DeleteVideoOnDailyMotion(dailymotionId);
              } catch (Exception errorDailymotionDeleteVideo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
              }
            }
          }
          services.user().changeNameVideoUrl(user, videoUrl, pictureUri);
          bodyMail = messageByLocaleService.getMessage("admin_change_your_name_lsf", new Object[]{admin.username});
          messageServer = "UserChangeNameLsfMessage";
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
        if (!dailyMotion.embed_url.isEmpty()) {
          if (inputType.equals("JobDescription")) {
            services.user().changeDescriptionVideoUrl(user, dailyMotion.embed_url, dailyMotion.thumbnail_360_url);
          } else {
            services.user().changeNameVideoUrl(user, dailyMotion.embed_url, dailyMotion.thumbnail_360_url);
          }
        }

      };

      new Thread(task).start();

      if (bodyMail != null && userId.isPresent()) {
        String finalBody = bodyMail;
        String finalMessageServer = messageServer;
        User finalUser = user;
        Runnable taskSendMail = () -> {
          log.info("send mail email = {} / title = {} / body = {}", finalUser.username, title, finalBody);
          services.emailService().sendUpdateProfilUserByAdminMessage(finalUser.username, title, finalUser.username, finalBody, finalMessageServer, requestHttp.getLocale());
        };

        new Thread(taskSendMail).start();
      }

      response.setStatus(HttpServletResponse.SC_OK);
      return "";

    }
    catch(Exception errorDailymotionUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorDailymotionUploadFile");
    }
  }

  private String handleRecordedVideoFileForProfilOnServer(VideoFile videoFile, OptionalLong userId, Principal principal, String inputType, HttpServletResponse response, HttpServletRequest requestHttp) {
    log.info("VideoFile "+videoFile);
    log.info("VideoFile name"+videoFile.name);
    String title, body = null, messageServer = null;
    String videoUrl = null;
    String file = environment.getProperty("app.file") + "/" + videoFile.name;
    String thumbnailFile = environment.getProperty("app.file") + "/thumbnail/" + videoFile.name.replace(".webm", ".png");

    log.info("taille fichier "+videoFile.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.servlet.multipart.max-request-size")));

    User admin = services.user().getAdmin();
    title = messageByLocaleService.getMessage("admin_change_profil");

    if (videoFile.contents.length() > parseSize(environment.getProperty("spring.servlet.multipart.max-request-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorFileSize");
    }


    try {
      //This will decode the String which is encoded by using Base64 class
      extracted(videoFile, file);
    }
    catch(Exception errorUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorUploadFile");
    }


    try {
      GenerateThumbnail(thumbnailFile, file);
    } catch (Exception errorEncondingFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorThumbnailFile");
    }

    User user;
    if (userId.isPresent()) {
      user = services.user().withId(userId.getAsLong());
    } else {
      user = services.user().withUserName(principal.getName());
    }

    String pictureUri = thumbnailFile;
    videoUrl= file;

    if (inputType.equals("JobDescription")) {
      if (user.jobDescriptionVideo != null) {
        DeleteFilesOnServer(user.jobDescriptionVideo, user.jobDescriptionPicture);
      }
      services.user().changeDescriptionVideoUrl(user, videoUrl, pictureUri);
      body = messageByLocaleService.getMessage("admin_change_your_job_description_lsf", new Object[]{admin.username});
      messageServer = "UserChangeJobDescriptionLsfMessage";
    } else {
      if (user.nameVideo != null) {
        if (user.nameVideo.contains("http")) {
          DeleteFilesOnServer(user.nameVideo, user.namePicture);
        }
      }
      services.user().changeNameVideoUrl(user, videoUrl, pictureUri);
      body = messageByLocaleService.getMessage("admin_change_your_name_lsf", new Object[]{admin.username});
      messageServer = "UserChangeNameLsfMessage";
    }

    if (body != null && userId.isPresent()) {
      String finalBody = body;
      String finalMessageServer = messageServer;
      User finalUser = user;
      Runnable task = () -> {
        log.info("send mail email = {} / title = {} / body = {}", finalUser.username, title, finalBody);
        services.emailService().sendUpdateProfilUserByAdminMessage(finalUser.username, title, finalUser.username, finalBody, finalMessageServer, requestHttp.getLocale());
      };

      new Thread(task).start();
    }

    response.setStatus(HttpServletResponse.SC_OK);
    return "";

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


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_REQUEST_DESCRIPTION, method = RequestMethod.POST)
  public RequestResponse uploadSelectedVideoFileForRequestDescription(@RequestParam("file") MultipartFile file, @PathVariable long requestId, @ModelAttribute RequestCreationView requestCreationView, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) throws IOException, JCodecException, InterruptedException {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleSelectedVideoFileUploadForRequestDescriptionOnServer(file, requestId, requestCreationView, principal, response, requestHttp);
    } else {
      return handleSelectedVideoFileUploadForRequestDescription(file, requestId, requestCreationView, principal, response, requestHttp);
    }
  }

  private RequestResponse handleSelectedVideoFileUploadForRequestDescription(@RequestParam("file") MultipartFile file, @PathVariable long requestId, @ModelAttribute RequestCreationView requestCreationView, Principal principal, HttpServletResponse response, HttpServletRequest requsetHttp) throws InterruptedException {
    {
      String videoUrl = null;
      String fileName = environment.getProperty("app.file") + "/" + file.getOriginalFilename();
      File inputFile;
      Request request = null;
      RequestResponse requestResponse = new RequestResponse();

      try {
        storageService.store(file);
        inputFile = storageService.load(file.getOriginalFilename()).toFile();
      } catch (Exception errorLoadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        requestResponse.errorType = 3;
        requestResponse.errorMessage = messageByLocaleService.getMessage("errorUploadFile");
        return requestResponse;
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
        body.add("title", messageByLocaleService.getMessage("request.title_description_LSF", new Object[]{requestCreationView.getRequestName()}));
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
            request = services.request().withId(requestId);
            if (request.requestVideoDescription != null) {
              if (request.requestVideoDescription.contains("http")) {
                dailymotionId = request.requestVideoDescription.substring(request.requestVideoDescription.lastIndexOf('/') + 1);
                try {
                  DeleteVideoOnDailyMotion(dailymotionId);
                } catch (Exception errorDailymotionDeleteVideo) {
                  response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                  requestResponse.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
                  return requestResponse;
                }
              }
            }
            services.request().changeRequestVideoDescription(requestId, videoUrl);

          } else {
            if (services.sign().withName(requestCreationView.getRequestName()).list().isEmpty()) {
              if (services.request().withName(requestCreationView.getRequestName()).list().isEmpty()) {
                request = services.request().create(user.id, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription(), videoUrl);
                log.info("createRequest: username = {} / request name = {}", user.username, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription());
                emails = services.user().findEmailForUserHaveSameCommunityAndCouldCreateSign(user.id);
                title = messageByLocaleService.getMessage("request_created_by_user_title", new Object[]{user.name()});
                bodyMail = messageByLocaleService.getMessage("request_created_by_user_body", new Object[]{user.name(), request.name, getAppUrl() + "/sec/other-request-detail/" + request.id});

                if (emails.size() != 0) {
                  Request finalRequest = request;
                  Runnable task = () -> {
                    log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
                    services.emailService().sendRequestMessage(emails.toArray(new String[emails.size()]), title, user.name(), finalRequest.name, getAppUrl() + "/sec/other-request-detail/" + finalRequest.id, requsetHttp.getLocale());
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
        requestResponse.requestId = request.id;
        return requestResponse;

      } catch (Exception errorDailymotionUploadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        requestResponse.errorType = 3;
        requestResponse.errorMessage = messageByLocaleService.getMessage("errorDailymotionUploadFile");
        return requestResponse;
      }
    }
  }

  private RequestResponse handleSelectedVideoFileUploadForRequestDescriptionOnServer(@RequestParam("file") MultipartFile file, @PathVariable long requestId, @ModelAttribute RequestCreationView requestCreationView, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) throws InterruptedException {
    {
      String videoUrl = null;
      String newFileName = UUID.randomUUID().toString() + "." + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
      String newAbsoluteFileName = environment.getProperty("app.file") +"/" + newFileName;
      Request request = null;
      RequestResponse requestResponse = new RequestResponse();
      File inputFile;
      Streams streamInfo;
      String newAbsoluteFileNameWithExtensionMp4 = newAbsoluteFileName.substring(0, newFileName.lastIndexOf('.')) + ".mp4";


      try {
        storageService.store(file);
        inputFile = storageService.load(file.getOriginalFilename()).toFile();
        File newName = new File(newAbsoluteFileName);
        inputFile.renameTo(newName);
      } catch (Exception errorLoadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        requestResponse.errorType = 3;
        requestResponse.errorMessage = messageByLocaleService.getMessage("errorUploadFile");
        return requestResponse;
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
          (file.getSize() >= parseSize(environment.getProperty("file-size-max-to-reduce"))) &&
          (streamInfo.getStreams().stream().findFirst().get().getTags().getRotate() == null)) {
          ReduceFileSizeInChangingResolution(newAbsoluteFileName, newAbsoluteFileNameWithExtensionMp4);
          newAbsoluteFileName = newAbsoluteFileNameWithExtensionMp4;
        }
      }

      User user = services.user().withUserName(principal.getName());

      videoUrl= newAbsoluteFileName;
      List<String> emails;
      String title, bodyMail;

      if (requestId != 0) {
        request = services.request().withId(requestId);
        if (request.requestVideoDescription != null) {
          DeleteFilesOnServer(request.requestVideoDescription, null);
        }
        services.request().changeRequestVideoDescription(requestId, videoUrl);
      } else {
        if (services.sign().withName(requestCreationView.getRequestName()).list().isEmpty()) {
          if (services.request().withName(requestCreationView.getRequestName()).list().isEmpty()) {
            request = services.request().create(user.id, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription(), videoUrl);
            log.info("createRequest: username = {} / request name = {}", user.username, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription());
            emails = services.user().findEmailForUserHaveSameCommunityAndCouldCreateSign(user.id);
            title = messageByLocaleService.getMessage("request_created_by_user_title", new Object[]{user.name()});
            bodyMail = messageByLocaleService.getMessage("request_created_by_user_body", new Object[]{user.name(), request.name, getAppUrl() + "/sec/other-request-detail/" + request.id});

            if (emails.size() != 0) {
              Request finalRequest = request;
              Runnable task = () -> {
                log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
                services.emailService().sendRequestMessage(emails.toArray(new String[emails.size()]), title, user.name(), finalRequest.name, getAppUrl() + "/sec/other-request-detail/" + finalRequest.id, requestHttp.getLocale());
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
      }

      response.setStatus(HttpServletResponse.SC_OK);
      requestResponse.requestId = request.id;
      return requestResponse;
    }
  }
  private String getAppUrl() {
    return environment.getProperty("app.url");
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_REQUEST_DESCRIPTION , method = RequestMethod.POST)
  public RequestResponse uploadRecordedVideoFileForRequestDescription(@RequestBody VideoFile videoFile, @PathVariable long requestId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleRecordedVideoFileForRequestDescriptionOnServer(videoFile, requestId, principal, response, requestHttp);
    } else {
      return handleRecordedVideoFileForRequestDescription(videoFile, requestId, principal, response, requestHttp);
    }
  }

  private RequestResponse handleRecordedVideoFileForRequestDescription(VideoFile videoFile, @PathVariable long requestId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    log.info("VideoFile "+videoFile);
    log.info("VideoFile name"+videoFile.name);
    RequestResponse requestResponse = new RequestResponse();
    String videoUrl = null;
    String file = environment.getProperty("app.file") + "/" + videoFile.name;

    log.info("taille fichier "+videoFile.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.servlet.multipart.max-request-size")));

    if (videoFile.contents.length() > parseSize(environment.getProperty("spring.servlet.multipart.max-request-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      requestResponse.errorMessage = messageByLocaleService.getMessage("errorFileSize");
      return requestResponse;
    }


    try {
      //This will decode the String which is encoded by using Base64 class
      extracted(videoFile, file);
    }
    catch(Exception errorUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      requestResponse.errorMessage = messageByLocaleService.getMessage("errorUploadFile");
      return requestResponse;
    }


    try {
      String dailymotionId;
      Request request = null;
      String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");

      AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
      if (authTokenInfo.isExpired()) {
        dalymotionToken.retrieveToken();
        authTokenInfo = dalymotionToken.getAuthTokenInfo();
      }

      User user = services.user().withUserName(principal.getName());

      UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


      File fileMp4 = new File(file);
      Resource resource = new FileSystemResource(fileMp4.getAbsolutePath());
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

      ResponseEntity<FileUploadDailymotion> responseDailymmotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
        HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
      FileUploadDailymotion fileUploadDailyMotion = responseDailymmotion.getBody();


      MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
      body.add("url", fileUploadDailyMotion.url);
      body.add("title", messageByLocaleService.getMessage("request.title_description_LSF", new Object[]{videoFile.requestNameRecording}));
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

      videoUrl= file;
      List<String> emails;
      String title, bodyMail;

        if (requestId != 0) {
          request = services.request().withId(requestId);
          if (request.requestVideoDescription != null) {
            if (request.requestVideoDescription.contains("http")) {
              dailymotionId = request.requestVideoDescription.substring(request.requestVideoDescription.lastIndexOf('/') + 1);
              try {
                DeleteVideoOnDailyMotion(dailymotionId);
              } catch (Exception errorDailymotionDeleteVideo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                requestResponse.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
                return requestResponse;
              }
            }
          }
          services.request().changeRequestVideoDescription(requestId, videoUrl);

        } else {
          if (services.sign().withName(videoFile.requestNameRecording).list().isEmpty()) {
            if (services.request().withName(videoFile.requestNameRecording).list().isEmpty()) {
              request = services.request().create(user.id, videoFile.requestNameRecording, videoFile.requestTextDescriptionRecording, videoUrl);
              log.info("createRequest: username = {} / request name = {}", user.username, videoFile.requestNameRecording, videoFile.requestTextDescriptionRecording);
              emails = services.user().findEmailForUserHaveSameCommunityAndCouldCreateSign(user.id);
              title = messageByLocaleService.getMessage("request_created_by_user_title", new Object[]{user.name()});
              bodyMail = messageByLocaleService.getMessage("request_created_by_user_body", new Object[]{user.name(), request.name, getAppUrl() + "/sec/other-request-detail/" + request.id});

              if (emails.size() != 0) {
                Request finalRequest = request;
                Runnable task = () -> {
                  log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
                  services.emailService().sendRequestMessage(emails.toArray(new String[emails.size()]), title, user.name(), finalRequest.name, getAppUrl() + "/sec/other-request-detail/" + finalRequest.id, requestHttp.getLocale());
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
            requestResponse.signId = services.sign().withName(videoFile.requestNameRecording).list().get(0).id;
            return requestResponse;
          }

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
      requestResponse.requestId = request.id;
      return requestResponse;

    } catch (Exception errorDailymotionUploadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      requestResponse.errorType = 3;
      requestResponse.errorMessage = messageByLocaleService.getMessage("errorDailymotionUploadFile");
      return requestResponse;
    }
  }

  private RequestResponse handleRecordedVideoFileForRequestDescriptionOnServer(VideoFile videoFile, @PathVariable long requestId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    log.info("VideoFile "+videoFile);
    log.info("VideoFile name"+videoFile.name);
    RequestResponse requestResponse = new RequestResponse();
    String videoUrl = null;
    String file = environment.getProperty("app.file") + "/" + videoFile.name;

    log.info("taille fichier "+videoFile.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.servlet.multipart.max-request-size")));

    if (videoFile.contents.length() > parseSize(environment.getProperty("spring.servlet.multipart.max-request-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      requestResponse.errorMessage = messageByLocaleService.getMessage("errorFileSize");
      return requestResponse;
    }


    try {
      //This will decode the String which is encoded by using Base64 class
      extracted(videoFile, file);
    }
    catch(Exception errorUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      requestResponse.errorMessage = messageByLocaleService.getMessage("errorUploadFile");
      return requestResponse;
    }

    Request request = null;
    User user = services.user().withUserName(principal.getName());
    videoUrl= file;
    List<String> emails;
    String title, bodyMail;

    if (requestId != 0) {
      request = services.request().withId(requestId);
      if (request.requestVideoDescription != null) {
        DeleteFilesOnServer(request.requestVideoDescription, null);
      }
      services.request().changeRequestVideoDescription(requestId, videoUrl);

    } else {
      if (services.sign().withName(videoFile.requestNameRecording).list().isEmpty()) {
        if (services.request().withName(videoFile.requestNameRecording).list().isEmpty()) {
          request = services.request().create(user.id, videoFile.requestNameRecording, videoFile.requestTextDescriptionRecording, videoUrl);
          log.info("createRequest: username = {} / request name = {}", user.username, videoFile.requestNameRecording, videoFile.requestTextDescriptionRecording);
          emails = services.user().findEmailForUserHaveSameCommunityAndCouldCreateSign(user.id);
          title = messageByLocaleService.getMessage("request_created_by_user_title", new Object[]{user.name()});
          bodyMail = messageByLocaleService.getMessage("request_created_by_user_body", new Object[]{user.name(), request.name, getAppUrl() + "/sec/other-request-detail/" + request.id});

          if (emails.size() != 0) {
            Request finalRequest = request;
            Runnable task = () -> {
              log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
              services.emailService().sendRequestMessage(emails.toArray(new String[emails.size()]), title, user.name(), finalRequest.name, getAppUrl() + "/sec/other-request-detail/" + finalRequest.id, requestHttp.getLocale());
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
        requestResponse.signId = services.sign().withName(videoFile.requestNameRecording).list().get(0).id;
        return requestResponse;
      }

    }
    response.setStatus(HttpServletResponse.SC_OK);
    requestResponse.requestId = request.id;
    return requestResponse;

  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_SIGN_DEFINITION , method = RequestMethod.POST)
  public String uploadRecordedVideoFileForSignDefinition(@RequestBody VideoFile videoFile, @PathVariable long signId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleRecordedVideoFileForSignDefinitionOnServer(videoFile, signId, principal, response, requestHttp);
    } else {
      return handleRecordedVideoFileForSignDefinition(videoFile, signId, principal, response, requestHttp);
    }
  }

  private String handleRecordedVideoFileForSignDefinition(VideoFile videoFile, @PathVariable long signId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    log.info("VideoFile "+videoFile);
    log.info("VideoFile name"+videoFile.name);

    String title = null, bodyMail = null, messageType = null;
    String videoUrl = null;
    String file = environment.getProperty("app.file") + "/" + videoFile.name;

    log.info("taille fichier "+videoFile.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.servlet.multipart.max-request-size")));


    if (videoFile.contents.length() > parseSize(environment.getProperty("spring.servlet.multipart.max-request-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorFileSize");
    }


    try {
      //This will decode the String which is encoded by using Base64 class
      extracted(videoFile, file);
    }
    catch(Exception errorUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorUploadFile");
    }


    try {
      String dailymotionId;
      Sign sign = null;
      String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");
      sign = services.sign().withId(signId);
      Videos videos = services.video().forSign(signId);
      List<String> emails = videos.stream().filter(v-> v.user.email != null).map(v -> v.user.email).collect(Collectors.toList());
      emails = emails.stream().distinct().collect(Collectors.toList());
      AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
      if (authTokenInfo.isExpired()) {
        dalymotionToken.retrieveToken();
        authTokenInfo = dalymotionToken.getAuthTokenInfo();
      }

      User user = services.user().withUserName(principal.getName());

      UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


      File fileMp4 = new File(file);
      Resource resource = new FileSystemResource(fileMp4.getAbsolutePath());
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

      ResponseEntity<FileUploadDailymotion> responseDailymmotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
        HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
      FileUploadDailymotion fileUploadDailyMotion = responseDailymmotion.getBody();


      MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
      body.add("url", fileUploadDailyMotion.url);
      body.add("title", messageByLocaleService.getMessage("sign.title_description_LSF", new Object[]{sign.name}));
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


      videoUrl= file;

      if (changeSignDefinition(signId, response, requestHttp, title, bodyMail, messageType, videoUrl, sign, emails, user))
        return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");

/*        if (sign.videoDefinition != null) {
          if (sign.videoDefinition.contains("http")) {
            dailymotionId = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
            try {
              DeleteVideoOnDailyMotion(dailymotionId);
            } catch (Exception errorDailymotionDeleteVideo) {
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
            }
          }
          title = messageByLocaleService.getMessage("update_sign_definition_title", new Object[]{sign.name});
          bodyMail = messageByLocaleService.getMessage("update_sign_definition_body", new Object[]{sign.name});
          messageType = "UpdateSignDefinitionMessage";
        } else {
          title = messageByLocaleService.getMessage("add_sign_definition_title", new Object[]{sign.name});
          bodyMail = messageByLocaleService.getMessage("add_sign_definition_body", new Object[]{sign.name});
          messageType = "AddSignDefinitionMessage";
        }
        services.sign().changeSignVideoDefinition(signId, videoUrl);
        if (emails.size() != 0 && user.username == services.user().getAdmin().username) {
          Sign finalSign = sign;
          Runnable task = () -> {
            log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
            services.emailService().sendSignDefinitionMessage(emails.toArray(new String[emails.size()]), title, bodyMail, finalSign.name, messageType, request.getLocale());
          };
          new Thread(task).start();
        }*/

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
            services.sign().changeSignVideoDefinition(signId, dailyMotion.embed_url);
          }
        };

        new Thread(task).start();
        response.setStatus(HttpServletResponse.SC_OK);
        return Long.toString(sign.id);

    } catch (Exception errorDailymotionUploadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorDailymotionUploadFile");
    }
  }

  private String handleRecordedVideoFileForSignDefinitionOnServer(VideoFile videoFile, @PathVariable long signId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    log.info("VideoFile "+videoFile);
    log.info("VideoFile name"+videoFile.name);

    String title = null, bodyMail = null, messageType = null;
    String videoUrl = null;
    String file = environment.getProperty("app.file") + "/" + videoFile.name;

    log.info("taille fichier "+videoFile.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.servlet.multipart.max-request-size")));

    User user = services.user().withUserName(principal.getName());

    if (videoFile.contents.length() > parseSize(environment.getProperty("spring.servlet.multipart.max-request-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorFileSize");
    }


    try {
      //This will decode the String which is encoded by using Base64 class
      extracted(videoFile, file);
    }
    catch(Exception errorUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorUploadFile");
    }

    Sign sign = services.sign().withId(signId);
    Videos videos = services.video().forSign(signId);
    List<String> emails = videos.stream().filter(v-> v.user.email != null).map(v -> v.user.email).collect(Collectors.toList());
    emails = emails.stream().distinct().collect(Collectors.toList());
    videoUrl= file;

    changeSignDefinitionOnServer(signId, requestHttp, title, bodyMail, messageType, videoUrl, sign, emails, user);


 /*   if (sign.videoDefinition != null) {
      DeleteFilesOnServer(sign.videoDefinition, null);
      title = messageByLocaleService.getMessage("update_sign_definition_title", new Object[]{sign.name});
      bodyMail = messageByLocaleService.getMessage("update_sign_definition_body", new Object[]{sign.name});
      messageType = "UpdateSignDefinitionMessage";
    } else {
      title = messageByLocaleService.getMessage("add_sign_definition_title", new Object[]{sign.name});
      bodyMail = messageByLocaleService.getMessage("add_sign_definition_body", new Object[]{sign.name});
      messageType = "AddSignDefinitionMessage";
    }
    services.sign().changeSignVideoDefinition(signId, videoUrl);

    if (emails.size() != 0 && user.username == services.user().getAdmin().username) {
      Runnable task = () -> {
        log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
        services.emailService().sendSignDefinitionMessage(emails.toArray(new String[emails.size()]), title, bodyMail, sign.name, messageType, request.getLocale());
      };
      new Thread(task).start();
    }*/

    response.setStatus(HttpServletResponse.SC_OK);
    return Long.toString(sign.id);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_SIGN_DEFINITION, method = RequestMethod.POST)
  public String uploadSelectedVideoFileForSignDefinition(@RequestParam("file") MultipartFile file, @PathVariable long signId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) throws IOException, JCodecException, InterruptedException {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleSelectedVideoFileUploadForSignDefinitionOnServer(file, signId, principal, response, requestHttp);
    } else {
      return handleSelectedVideoFileUploadForSignDefinition(file, signId, principal, response, requestHttp);
    }
  }

  private String handleSelectedVideoFileUploadForSignDefinition(@RequestParam("file") MultipartFile file, @PathVariable long signId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) throws InterruptedException {
    {
      String title = null, bodyMail = null, messageType = null;
      String videoUrl = null;
      String fileName = environment.getProperty("app.file") + "/" + file.getOriginalFilename();
      File inputFile;
      Sign sign = null;
      sign = services.sign().withId(signId);
      Videos videos = services.video().forSign(signId);
      List<String> emails = videos.stream().filter(v-> v.user.email != null).map(v -> v.user.email).collect(Collectors.toList());
      emails = emails.stream().distinct().collect(Collectors.toList());
      try {
        storageService.store(file);
        inputFile = storageService.load(file.getOriginalFilename()).toFile();
      } catch (Exception errorLoadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorUploadFile");
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
        body.add("title", messageByLocaleService.getMessage("sign.title_description_LSF", new Object[]{sign.name}));
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

        if (changeSignDefinition(signId, response, requestHttp, title, bodyMail, messageType, videoUrl, sign, emails, user))
          return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");


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
            services.sign().changeSignVideoDefinition(signId, dailyMotion.embed_url);
          }
        };

        new Thread(task).start();
        response.setStatus(HttpServletResponse.SC_OK);
        return Long.toString(sign.id);

      } catch (Exception errorDailymotionUploadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorDailymotionUploadFile");
      }
    }
  }

  private boolean changeSignDefinition(long signId, HttpServletResponse response, HttpServletRequest requestHttp, String title, String bodyMail, String messageType, String videoUrl, Sign sign, List<String> emails, User user) {
    String dailymotionId;
    User admin = services.user().getAdmin();
    Request request = services.sign().requestForSign(sign);
    if (request != null) {
      if (request.requestVideoDescription != null && sign.videoDefinition != null) {
        if (!request.requestVideoDescription.equals(sign.videoDefinition)) {
          if (sign.videoDefinition != null) {
            if (sign.videoDefinition.contains("http")) {
              dailymotionId = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
              try {
                DeleteVideoOnDailyMotion(dailymotionId);
              } catch (Exception errorDailymotionDeleteVideo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return true;
              }
            } else {
              DeleteFilesOnServer(sign.videoDefinition, null);
            }
            title = messageByLocaleService.getMessage("update_sign_definition_title", new Object[]{sign.name});
            bodyMail = messageByLocaleService.getMessage("update_sign_definition_body", new Object[]{sign.name});
            messageType = "UpdateSignDefinitionSendEmailMessage";
          } else {
            title = messageByLocaleService.getMessage("add_sign_definition_title", new Object[]{sign.name});
            bodyMail = messageByLocaleService.getMessage("add_sign_definition_body", new Object[]{sign.name});
            messageType = "AddSignDefinitionSendEmailMessage";
          }
        }
      }
    } else {
      if (sign.videoDefinition != null) {
        if (sign.videoDefinition.contains("http")) {
          dailymotionId = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
          try {
            DeleteVideoOnDailyMotion(dailymotionId);
          } catch (Exception errorDailymotionDeleteVideo) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return true;
          }
        } else {
          DeleteFilesOnServer(sign.videoDefinition, null);
        }
        title = messageByLocaleService.getMessage("update_sign_definition_title", new Object[]{sign.name});
        bodyMail = messageByLocaleService.getMessage("update_sign_definition_body", new Object[]{sign.name});
        messageType = "UpdateSignDefinitionSendEmailMessage";
      } else {
        title = messageByLocaleService.getMessage("add_sign_definition_title", new Object[]{sign.name});
        bodyMail = messageByLocaleService.getMessage("add_sign_definition_body", new Object[]{sign.name});
        messageType = "AddSignDefinitionSendEmailMessage";
      }
    }
    if (user.username == admin.username) {
      if (emails.size() != 0) {
        final String finalTitle = title;
        final String finalBodyMail = bodyMail;
        final String finalMessageType = messageType;
        final String finalSignName = sign.name;
        Runnable task = () -> {
          log.info("send mail email = {} / title = {} / body = {}", emails.toString(), finalTitle, finalBodyMail);
          services.emailService().sendSignDefinitionMessage(emails.toArray(new String[emails.size()]), finalTitle, finalBodyMail, finalSignName, finalMessageType, requestHttp.getLocale());
        };
        new Thread(task).start();
      } else {
        if (messageType.equals("UpdateSignDefinitionSendEmailMessage")) {
          messageType = "UpdateSignDefinitionMessage";
        } else if (messageType.equals("AddSignDefinitionSendEmailMessage")) {
          messageType = "AddSignDefinitionMessage";
        }
        String values = admin.username + ';' + sign.name;
        MessageServer messageServer = new MessageServer(new Date(), messageType, values, ActionType.NO);
        services.messageServerService().addMessageServer(messageServer);
      }
    }

    services.sign().changeSignVideoDefinition(signId, videoUrl);
    return false;
  }

  private void changeSignDefinitionOnServer(long signId, HttpServletRequest requestHttp, String title, String bodyMail, String messageType, String videoUrl, Sign sign, List<String> emails, User user) {
    User admin = services.user().getAdmin();
    Request request = services.sign().requestForSign(sign);
    if (request != null) {
      if (request.requestVideoDescription != null && sign.videoDefinition != null) {
        if (!request.requestVideoDescription.equals(sign.videoDefinition)) {
          if (sign.videoDefinition != null) {
            DeleteFilesOnServer(sign.videoDefinition, null);
            title = messageByLocaleService.getMessage("update_sign_definition_title", new Object[]{sign.name});
            bodyMail = messageByLocaleService.getMessage("update_sign_definition_body", new Object[]{sign.name});
            messageType = "UpdateSignDefinitionSendEmailMessage";
          } else {
            title = messageByLocaleService.getMessage("add_sign_definition_title", new Object[]{sign.name});
            bodyMail = messageByLocaleService.getMessage("add_sign_definition_body", new Object[]{sign.name});
            messageType = "AddSignDefinitionSendEmailMessage";
          }
        }
      }
    } else {
      if (sign.videoDefinition != null) {
        DeleteFilesOnServer(sign.videoDefinition, null);
        title = messageByLocaleService.getMessage("update_sign_definition_title", new Object[]{sign.name});
        bodyMail = messageByLocaleService.getMessage("update_sign_definition_body", new Object[]{sign.name});
        messageType = "UpdateSignDefinitionSendEmailMessage";
      } else {
        title = messageByLocaleService.getMessage("add_sign_definition_title", new Object[]{sign.name});
        bodyMail = messageByLocaleService.getMessage("add_sign_definition_body", new Object[]{sign.name});
        messageType = "AddSignDefinitionSendEmailMessage";
      }
    }
    if (user.username == admin.username) {
      if (emails.size() != 0) {
        final String finalTitle = title;
        final String finalBodyMail = bodyMail;
        final String finalMessageType = messageType;
        final String finalSignName = sign.name;
        Runnable task = () -> {
          log.info("send mail email = {} / title = {} / body = {}", emails.toString(), finalTitle, finalBodyMail);
          services.emailService().sendSignDefinitionMessage(emails.toArray(new String[emails.size()]), finalTitle, finalBodyMail, finalSignName, finalMessageType, requestHttp.getLocale());
        };
        new Thread(task).start();
      } else {
        if (messageType.equals("UpdateSignDefinitionSendEmailMessage")) {
          messageType = "UpdateSignDefinitionMessage";
        } else if (messageType.equals("AddSignDefinitionSendEmailMessage")) {
          messageType = "AddSignDefinitionMessage";
        }
        String values = admin.username + ';' + sign.name;
        MessageServer messageServer = new MessageServer(new Date(), messageType, values, ActionType.NO);
        services.messageServerService().addMessageServer(messageServer);
      }
    }
    services.sign().changeSignVideoDefinition(signId, videoUrl);
    return;
  }

  private String handleSelectedVideoFileUploadForSignDefinitionOnServer(@RequestParam("file") MultipartFile file, @PathVariable long signId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) throws InterruptedException {
    {
      String title = null, bodyMail = null, messageType = null;
      String videoUrl = null;
      String newFileName = UUID.randomUUID().toString() + "." + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
      String newAbsoluteFileName = environment.getProperty("app.file") +"/" + newFileName;
      Sign sign = services.sign().withId(signId);
      Videos videos = services.video().forSign(signId);
      List<String> emails = videos.stream().filter(v-> v.user.email != null).map(v -> v.user.email).collect(Collectors.toList());
      emails = emails.stream().distinct().collect(Collectors.toList());
      File inputFile;
      Streams streamInfo;
      String newAbsoluteFileNameWithExtensionMp4 = newAbsoluteFileName.substring(0, newFileName.lastIndexOf('.')) + ".mp4";


      User user = services.user().withUserName(principal.getName());

      try {
        storageService.store(file);
        inputFile = storageService.load(file.getOriginalFilename()).toFile();
        File newName = new File(newAbsoluteFileName);
        inputFile.renameTo(newName);
      } catch (Exception errorLoadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorUploadFile");
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
          (file.getSize() >= parseSize(environment.getProperty("file-size-max-to-reduce"))) &&
          (streamInfo.getStreams().stream().findFirst().get().getTags().getRotate() == null)) {
          ReduceFileSizeInChangingResolution(newAbsoluteFileName, newAbsoluteFileNameWithExtensionMp4);
          newAbsoluteFileName = newAbsoluteFileNameWithExtensionMp4;
        }
      }

      videoUrl= newAbsoluteFileName;

      changeSignDefinitionOnServer(signId, requestHttp, title, bodyMail, messageType, videoUrl, sign, emails, user);

/*      if (sign.videoDefinition != null) {
        Request request = services.sign().requestForSign(sign);
        if (request != null) {
          if (request.requestVideoDescription != null && sign.videoDefinition != null) {
            if (!request.requestVideoDescription.equals(sign.videoDefinition)) {
              DeleteFilesOnServer(sign.videoDefinition, null);
            }
          }
        }
      }
      services.sign().changeSignVideoDefinition(signId, videoUrl);*/

      response.setStatus(HttpServletResponse.SC_OK);
      return Long.toString(sign.id);
    }
  }
  /****/
  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_COMMUNITY_DESCRIPTION , method = RequestMethod.POST)
  public String uploadRecordedVideoFileForCommunityDescription(@RequestBody VideoFile videoFile, @PathVariable long communityId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleRecordedVideoFileForCommunityDescriptionOnServer(videoFile, communityId, principal, response, requestHttp);
    } else {
      return handleRecordedVideoFileForCommunityDescription(videoFile, communityId, principal, response, requestHttp);
    }
  }

  private String handleRecordedVideoFileForCommunityDescription(VideoFile videoFile, @PathVariable long communityId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    log.info("VideoFile "+videoFile);
    log.info("VideoFile name"+videoFile.name);

    String videoUrl = null;
    String file = environment.getProperty("app.file") + "/" + videoFile.name;

    log.info("taille fichier "+videoFile.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.servlet.multipart.max-request-size")));

    if (videoFile.contents.length() > parseSize(environment.getProperty("spring.servlet.multipart.max-request-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorFileSize");
    }


    try {
      //This will decode the String which is encoded by using Base64 class
      extracted(videoFile, file);
    }
    catch(Exception errorUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorUploadFile");
    }


    try {
      String dailymotionId;
      Community community = null;
      String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");
      community = services.community().withId(communityId);

      AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
      if (authTokenInfo.isExpired()) {
        dalymotionToken.retrieveToken();
        authTokenInfo = dalymotionToken.getAuthTokenInfo();
      }

      User user = services.user().withUserName(principal.getName());

      UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


      File fileMp4 = new File(file);
      Resource resource = new FileSystemResource(fileMp4.getAbsolutePath());
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

      ResponseEntity<FileUploadDailymotion> responseDailymmotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
        HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
      FileUploadDailymotion fileUploadDailyMotion = responseDailymmotion.getBody();


      MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
      body.add("url", fileUploadDailyMotion.url);
      body.add("title", messageByLocaleService.getMessage("community.title_description_LSF", new Object[]{community.name}));
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


      videoUrl= file;

        if (community.descriptionVideo != null) {
          if (community.descriptionVideo.contains("http")) {
            dailymotionId = community.descriptionVideo.substring(community.descriptionVideo.lastIndexOf('/') + 1);
            try {
              DeleteVideoOnDailyMotion(dailymotionId);
            } catch (Exception errorDailymotionDeleteVideo) {
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
            }
          }
        }
        services.community().changeDescriptionVideo(communityId, videoUrl);
        List<String> emails = community.users.stream().filter(u-> u.email != null).map(u -> u.email).collect(Collectors.toList());
        emails = emails.stream().distinct().collect(Collectors.toList());
        if (emails.size() != 0) {
          Community finalCommunity = community;
          List<String> finalEmails = emails;
          Runnable task = () -> {
            String title, bodyMail;
            final String urlDescriptionCommunity = getAppUrl() + "/sec/community/" + finalCommunity.id + "/description";
            title = messageByLocaleService.getMessage("community_description_changed_by_user_title");
            bodyMail = messageByLocaleService.getMessage("community_description_changed_by_user_body", new Object[]{user.name(), finalCommunity.name, urlDescriptionCommunity});
            log.info("send mail email = {} / title = {} / body = {}", finalEmails.toString(), title, bodyMail);
            services.emailService().sendCommunityAddDescriptionMessage(finalEmails.toArray(new String[finalEmails.size()]), title, user.name(), finalCommunity.name, urlDescriptionCommunity, requestHttp.getLocale());
          };

          new Thread(task).start();
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
        if (!dailyMotion.embed_url.isEmpty()) {
          services.community().changeDescriptionVideo(communityId, dailyMotion.embed_url);
        }
      };

      new Thread(task).start();
      response.setStatus(HttpServletResponse.SC_OK);
      return Long.toString(community.id);

    } catch (Exception errorDailymotionUploadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorDailymotionUploadFile");
    }
  }

  private String handleRecordedVideoFileForCommunityDescriptionOnServer(VideoFile videoFile, @PathVariable long communityId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) {
    log.info("VideoFile "+videoFile);
    log.info("VideoFile name"+videoFile.name);

    String videoUrl = null;
    String file = environment.getProperty("app.file") + "/" + videoFile.name;

    log.info("taille fichier "+videoFile.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.servlet.multipart.max-request-size")));

    if (videoFile.contents.length() > parseSize(environment.getProperty("spring.servlet.multipart.max-request-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorFileSize");
    }


    try {
      //This will decode the String which is encoded by using Base64 class
      extracted(videoFile, file);
    }
    catch(Exception errorUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorUploadFile");
    }


    Community community = services.community().withId(communityId);

    User user = services.user().withUserName(principal.getName());

    videoUrl= file;

    if (community.descriptionVideo != null) {
      DeleteFilesOnServer(community.descriptionVideo, null);
    }
    services.community().changeDescriptionVideo(communityId, videoUrl);
    List<String> emails = community.users.stream().filter(u-> u.email != null).map(u -> u.email).collect(Collectors.toList());
    emails = emails.stream().distinct().collect(Collectors.toList());
    if (emails.size() != 0) {
      Community finalCommunity = community;
      List<String> finalEmails = emails;
      Runnable task = () -> {
        String title, bodyMail;
        final String urlDescriptionCommunity = getAppUrl() + "/sec/community/" + finalCommunity.id + "/description";
        title = messageByLocaleService.getMessage("community_description_changed_by_user_title");
        bodyMail = messageByLocaleService.getMessage("community_description_changed_by_user_body", new Object[]{user.name(), finalCommunity.name, urlDescriptionCommunity});
        log.info("send mail email = {} / title = {} / body = {}", finalEmails.toString(), title, bodyMail);
        services.emailService().sendCommunityAddDescriptionMessage(finalEmails.toArray(new String[finalEmails.size()]), title, user.name(), finalCommunity.name, urlDescriptionCommunity, requestHttp.getLocale());
      };

      new Thread(task).start();
    }

    response.setStatus(HttpServletResponse.SC_OK);
    return Long.toString(community.id);

  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_COMMUNITY_DESCRIPTION, method = RequestMethod.POST)
  public String uploadSelectedVideoFileForCommunityDescription(@RequestParam("file") MultipartFile file, @PathVariable long communityId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) throws IOException, JCodecException, InterruptedException {
    if (environment.getProperty("app.dailymotion_url").isEmpty()) {
      return handleSelectedVideoFileUploadForCommunityDescriptionOnServer(file, communityId, principal, response, requestHttp);
    } else {
      return handleSelectedVideoFileUploadForCommunityDescription(file, communityId, principal, response, requestHttp);
    }
  }

  private String handleSelectedVideoFileUploadForCommunityDescription(@RequestParam("file") MultipartFile file, @PathVariable long communityId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) throws InterruptedException {
    {
      String videoUrl = null;
      String fileName = environment.getProperty("app.file") + "/" + file.getOriginalFilename();
      File inputFile;
      Community community = null;
      community = services.community().withId(communityId);

      try {
        storageService.store(file);
        inputFile = storageService.load(file.getOriginalFilename()).toFile();
      } catch (Exception errorLoadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorUploadFile");
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
        body.add("title", messageByLocaleService.getMessage("community.title_description_LSF", new Object[]{community.name}));
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

          if (community.descriptionVideo != null) {
            if (community.descriptionVideo.contains("http")) {
              dailymotionId = community.descriptionVideo.substring(community.descriptionVideo.lastIndexOf('/') + 1);
              try {
                DeleteVideoOnDailyMotion(dailymotionId);
              } catch (Exception errorDailymotionDeleteVideo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
              }
            }
          }
          services.community().changeDescriptionVideo(communityId, videoUrl);
          List<String> emails = community.users.stream().filter(u-> u.email != null).map(u -> u.email).collect(Collectors.toList());
          emails = emails.stream().distinct().collect(Collectors.toList());
          if (emails.size() != 0) {
            Community finalCommunity = community;
            List<String> finalEmails = emails;
            Runnable task = () -> {
              String title, bodyMail;
              final String urlDescriptionCommunity = getAppUrl() + "/sec/community/" + finalCommunity.id + "/description";
              title = messageByLocaleService.getMessage("community_description_changed_by_user_title");
              bodyMail = messageByLocaleService.getMessage("community_description_changed_by_user_body", new Object[]{user.name(), finalCommunity.name, urlDescriptionCommunity});
              log.info("send mail email = {} / title = {} / body = {}", finalEmails.toString(), title, bodyMail);
              services.emailService().sendCommunityAddDescriptionMessage(finalEmails.toArray(new String[finalEmails.size()]), title, user.name(), finalCommunity.name, urlDescriptionCommunity, requestHttp.getLocale());
            };

            new Thread(task).start();
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
          if (!dailyMotion.embed_url.isEmpty()) {
            services.community().changeDescriptionVideo(communityId, dailyMotion.embed_url);
          }
        };

        new Thread(task).start();
        response.setStatus(HttpServletResponse.SC_OK);
        return Long.toString(community.id);

      } catch (Exception errorDailymotionUploadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorDailymotionUploadFile");
      }
    }
  }

  private String handleSelectedVideoFileUploadForCommunityDescriptionOnServer(@RequestParam("file") MultipartFile file, @PathVariable long communityId, Principal principal, HttpServletResponse response, HttpServletRequest requestHttp) throws InterruptedException {
    {
      String videoUrl = null;
      String newFileName = UUID.randomUUID().toString() + "." + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
      String newAbsoluteFileName = environment.getProperty("app.file") +"/" + newFileName;
      Community community = services.community().withId(communityId);
      File inputFile;
      Streams streamInfo;
      String newAbsoluteFileNameWithExtensionMp4 = newAbsoluteFileName.substring(0, newFileName.lastIndexOf('.')) + ".mp4";


      try {
        storageService.store(file);
        inputFile = storageService.load(file.getOriginalFilename()).toFile();
        File newName = new File(newAbsoluteFileName);
        inputFile.renameTo(newName);
      } catch (Exception errorLoadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorUploadFile");
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
          (file.getSize() >= parseSize(environment.getProperty("file-size-max-to-reduce"))) &&
          (streamInfo.getStreams().stream().findFirst().get().getTags().getRotate() == null)) {
          ReduceFileSizeInChangingResolution(newAbsoluteFileName, newAbsoluteFileNameWithExtensionMp4);
          newAbsoluteFileName = newAbsoluteFileNameWithExtensionMp4;
        }
      }

      User user = services.user().withUserName(principal.getName());

      videoUrl= newAbsoluteFileName;

      if (community.descriptionVideo != null) {
        DeleteFilesOnServer(community.descriptionVideo, null);
      }
      services.community().changeDescriptionVideo(communityId, videoUrl);
      List<String> emails = community.users.stream().filter(u-> u.email != null).map(u -> u.email).collect(Collectors.toList());
      emails = emails.stream().distinct().collect(Collectors.toList());
      if (emails.size() != 0) {
        Community finalCommunity = community;
        List<String> finalEmails = emails;
        Runnable task = () -> {
          String title, bodyMail;
          final String urlDescriptionCommunity = getAppUrl() + "/sec/community/" + finalCommunity.id + "/description";
          title = messageByLocaleService.getMessage("community_description_changed_by_user_title");
          bodyMail = messageByLocaleService.getMessage("community_description_changed_by_user_body", new Object[]{user.name(), finalCommunity.name, urlDescriptionCommunity});
          log.info("send mail email = {} / title = {} / body = {}", finalEmails.toString(), title, bodyMail);
          services.emailService().sendCommunityAddDescriptionMessage(finalEmails.toArray(new String[finalEmails.size()]), title, user.name(), finalCommunity.name, urlDescriptionCommunity, requestHttp.getLocale());
        };

        new Thread(task).start();
      }

      response.setStatus(HttpServletResponse.SC_OK);
      return Long.toString(community.id);

    }
  }
}
