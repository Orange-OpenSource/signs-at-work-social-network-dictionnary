package com.orange.signsatwork.biz.webservice.controller;

import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.persistence.model.CommentData;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.webservice.model.CommentCreationViewApi;
import com.orange.signsatwork.biz.webservice.model.CommentResponseApi;
import com.orange.signsatwork.biz.webservice.model.CommentViewApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class CommentRestController {
  @Autowired
  Services services;


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_COMMENTS)
  public ResponseEntity<?> comments(@PathVariable long videoId) {

    List<Object[]> queryAllComments = services.video().AllCommentsForVideo(videoId);
    List<CommentViewApi> commentViewApis = queryAllComments.stream()
      .map(objectArray -> new CommentViewApi(objectArray))
      .collect(Collectors.toList());

    return new ResponseEntity<>(commentViewApis, HttpStatus.OK);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_COMMENTS, method = RequestMethod.POST, headers = {"content-type=application/json"})
  public CommentResponseApi addComment(@PathVariable long videoId, @RequestBody CommentCreationViewApi commentCreationViewApi, HttpServletResponse response, Principal principal) {
    CommentResponseApi commentResponseApi = new CommentResponseApi();
    User user = services.user().withUserName(principal.getName());
    services.video().createVideoComment(videoId, user.id, commentCreationViewApi.getText());

    return commentResponseApi;
  }
}
