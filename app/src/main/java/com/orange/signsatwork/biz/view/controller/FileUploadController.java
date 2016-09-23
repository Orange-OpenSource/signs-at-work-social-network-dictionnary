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

import com.orange.signsatwork.biz.domain.Sign;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.storage.StorageFileNotFoundException;
import com.orange.signsatwork.biz.storage.StorageService;
import com.orange.signsatwork.biz.view.model.SignCreationView;
import lombok.extern.slf4j.Slf4j;
import org.jcodec.api.JCodecException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;

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

//    @GetMapping("/")
//    public String listUploadedFiles(Model model) throws IOException {
//
//        model.addAttribute("files", storageService
//                .loadAll()
//                .map(path ->
//                        MvcUriComponentsBuilder
//                                .fromMethodName(FileUploadController.class, "serveFile", path.getFileName().toString())
//                                .build().toString())
//                .collect(Collectors.toList()));
//
//        return "uploadForm";
//    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+file.getFilename()+"\"")
                .body(file);
    }

//    @PostMapping("/")
//    public String handleFileUpload(@RequestParam("file") MultipartFile file,
//                                   RedirectAttributes redirectAttributes) {
//
//        storageService.store(file);
//        redirectAttributes.addFlashAttribute("message",
//                "You successfully uploaded " + file.getOriginalFilename() + "!");
//
//        return "redirect:/";
//    }

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

    private String showSign(long signId) {
        return "redirect:/sign/" + signId;
    }
}

