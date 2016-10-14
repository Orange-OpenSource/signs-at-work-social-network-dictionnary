package com.orange.signsatwork.biz.view.controller;

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
import com.orange.signsatwork.biz.domain.*;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.storage.StorageFileNotFoundException;
import com.orange.signsatwork.biz.storage.StorageService;
import com.orange.signsatwork.biz.view.model.SignCreationView;
import lombok.extern.slf4j.Slf4j;
import org.jcodec.api.JCodecException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.Principal;
import java.util.Arrays;

@Slf4j
@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }
    @Autowired
    private Services services;
    @Autowired
    DalymotionToken dalymotionToken;
    String REST_SERVICE_URI = "https://api.dailymotion.com";
    String VIDEO_THUMBNAIL_FIELDS = "thumbnail_url,thumbnail_60_url,thumbnail_120_url,thumbnail_180_url,thumbnail_240_url,thumbnail_360_url,thumbnail_480_url,thumbnail_720_url,";
    String VIDEO_STREAM_FIELDS = "stream_h264_hd1080_url,stream_h264_hd_url,stream_h264_hq_url,stream_h264_qhd_url,stream_h264_uhd_url,stream_h264_url,";
    String VIDEO_EMBED_FIELD = "embed_url";


    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+file.getFilename()+"\"")
                .body(file);
    }


    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

    @Secured("ROLE_USER")
    @RequestMapping(value = "/sec/sign/createfromupload", method = RequestMethod.POST)
    public String createSignFromUpload(@RequestParam("file") MultipartFile file, @ModelAttribute SignCreationView signCreationView, Principal principal) throws IOException, JCodecException {

        User user = services.user().withUserName(principal.getName());

        storageService.store(file);
        File inputFile = storageService.load(file.getOriginalFilename()).toFile();

        storageService.generateThumbnail(inputFile);

        signCreationView.setVideoUrl("/files/" + file.getOriginalFilename());
        Sign sign = services.sign().create(user.id, signCreationView.getSignName(), signCreationView.getVideoUrl(), "/files/" + inputFile.getName() + ".jpg");

        log.info("createSignFromUpload: username = {} / sign name = {} / video url = {}", user.username, signCreationView.getSignName(), signCreationView.getVideoUrl());

        return showSign(sign.id);
    }

    @Secured("ROLE_USER")
    @RequestMapping(value = "/sec/request/{requestId}/add/signfromupload", method = RequestMethod.POST)
    public String changeSignRequest(@RequestParam("file") MultipartFile file, @PathVariable long requestId, @ModelAttribute SignCreationView signCreationView, Principal principal) throws IOException, JCodecException {

        User user = services.user().withUserName(principal.getName());
        storageService.store(file);
        File inputFile = storageService.load(file.getOriginalFilename()).toFile();

        storageService.generateThumbnail(inputFile);

        signCreationView.setVideoUrl("/files/" + file.getOriginalFilename());

        Sign sign = services.sign().create(user.id, signCreationView.getSignName(), signCreationView.getVideoUrl(), "/files/" + inputFile.getName() + ".jpg");
        services.request().changeSignRequest(requestId, sign.id);
        log.info("createSignFromUpload: username = {} / sign name = {} / video url = {} and associate to request = {} ", user.username, signCreationView.getSignName(), signCreationView.getVideoUrl(),requestId);

        return "redirect:/sign/" + sign.id;
    }

    private String showSign(long signId) {
        return "redirect:/sign/" + signId;
    }

    @Secured("ROLE_USER")
    @RequestMapping(value = "/sec/sign/createfromuploadondailymotion", method = RequestMethod.POST)
    public String createSignFromUploadondailymotion(@RequestParam("file") MultipartFile file, @ModelAttribute SignCreationView signCreationView, Principal principal) throws IOException, JCodecException, InterruptedException {

        AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
        if (authTokenInfo.isExpired()) {
            dalymotionToken.retrieveToken();
            authTokenInfo = dalymotionToken.getAuthTokenInfo();
        }

        SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 3128));
        clientHttpRequestFactory.setProxy(proxy);

        User user = services.user().withUserName(principal.getName());
        storageService.store(file);
        File inputFile = storageService.load(file.getOriginalFilename()).toFile();

        UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


        Resource resource = new FileSystemResource(inputFile.getAbsolutePath());
        MultiValueMap<String, Object> parts =  new LinkedMultiValueMap<String, Object>();
        parts.add("file", resource);

        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);


        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

        ResponseEntity<FileUploadDailymotion> response = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
                        HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
        FileUploadDailymotion fileUploadDailyMotion = response.getBody();


        MultiValueMap<String, Object> body =  new LinkedMultiValueMap<String, Object>();
        body.add("url", fileUploadDailyMotion.url);
        body.add("title", signCreationView.getSignName());
        body.add("channel","Tech");
        body.add("published", true);


        RestTemplate restTemplate1 = new RestTemplate(clientHttpRequestFactory);
        HttpHeaders headers1 = new HttpHeaders();
        headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers1.set("Authorization", "Bearer "+ authTokenInfo.getAccess_token());
        headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
        ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange("https://api.dailymotion.com/videos",
                HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
        VideoDailyMotion videoDailyMotion= response1.getBody();


        String url= REST_SERVICE_URI+"/video/"+videoDailyMotion.id+"?fields="+VIDEO_THUMBNAIL_FIELDS + VIDEO_STREAM_FIELDS + VIDEO_EMBED_FIELD;
        do {
            videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url );
            Thread.sleep(2*1000);
        } while ((videoDailyMotion.stream_h264_url == null) || (videoDailyMotion.thumbnail_360_url == null)  || (videoDailyMotion.embed_url == null) || (videoDailyMotion.thumbnail_360_url.contains("no-such-asset")));




        String pictureUri = null;
        if (!videoDailyMotion.thumbnail_360_url.isEmpty()) {
            pictureUri = videoDailyMotion.thumbnail_360_url;
        }
        if ((!videoDailyMotion.stream_h264_url.isEmpty()) && (!videoDailyMotion.embed_url.isEmpty())) {
            dalymotionToken.getDailymotionCache().append(videoDailyMotion.embed_url, videoDailyMotion.stream_h264_url);
        }

        if (!videoDailyMotion.embed_url.isEmpty()) {
            signCreationView.setVideoUrl(videoDailyMotion.embed_url);
        }


        Sign sign = services.sign().create(user.id, signCreationView.getSignName(), signCreationView.getVideoUrl(), pictureUri);

        log.info("createSignFromUpload: username = {} / sign name = {} / video url = {}", user.username, signCreationView.getSignName(), signCreationView.getVideoUrl());

        return showSign(sign.id);
    }
}

