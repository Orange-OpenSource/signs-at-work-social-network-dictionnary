package com.orange.signsatwork.biz.webservice.controller;

import com.orange.signsatwork.biz.domain.*;
import com.orange.signsatwork.biz.persistence.model.CommentData;
import com.orange.signsatwork.biz.persistence.model.SignData;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.view.model.AuthentModel;
import com.orange.signsatwork.biz.webservice.model.CommentCreationViewApi;
import com.orange.signsatwork.biz.webservice.model.CommentResponseApi;
import com.orange.signsatwork.biz.webservice.model.CommentViewApi;
import com.orange.signsatwork.biz.webservice.model.VideoResponseApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class CommentRestController {
  @Autowired
  Services services;

  @Autowired
  MessageByLocaleService messageByLocaleService;

  @Autowired
  private Environment environment;
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
    commentCreationViewApi.clearXss();
    services.video().createVideoComment(videoId, user.id, commentCreationViewApi.getText());

    return commentResponseApi;
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_SEC_COMMENT, method = RequestMethod.DELETE)
  public CommentResponseApi deleteComment(@PathVariable long signId, @PathVariable long videoId, @PathVariable long commentId, HttpServletResponse response, HttpServletRequest request, Principal principal) {
    CommentResponseApi commentResponseApi = new CommentResponseApi();
    User user = services.user().withUserName(principal.getName());
    List<String> emails = new ArrayList<>();
    String videoName;
    Sign sign = services.sign().withId(signId);
    Video video = services.video().withId(videoId);
    Comment comment = services.comment().withId(commentId);
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy à HH:mm");
    String commentDate = df.format(comment.commentDate);
    emails.add(video.user.username);
    emails.add(comment.user.username);
    emails = emails.stream().distinct().collect(Collectors.toList());
    services.comment().delete(comment);
    if ((video.idForName == 0) || (sign.nbVideo == 1)) {
      videoName = sign.name;
    } else {
      videoName = sign.name + "_" + video.idForName;
    }
    if (emails.size() != 0) {
      List<String> finalEmails = emails;
      Runnable task = () -> {
        String title, bodyMail;
        final String url = getAppUrl() + "/sign/" + sign.id + "/" + video.id;
        title = messageByLocaleService.getMessage("comment_delete_title", new Object[]{videoName});
        bodyMail = messageByLocaleService.getMessage("comment_delete_body", new Object[]{comment.user.name(), comment.commentDate, url, user.name()});
        log.info("send mail email = {} / title = {} / body = {}", finalEmails.toString(), title, bodyMail);
        services.emailService().sendCommentDeleteMessage(finalEmails.toArray(new String[finalEmails.size()]), title, user.name(), comment.user.name(), commentDate, url, videoName, request.getLocale());
      };

      new Thread(task).start();
    } else {
      String values = user.name() + ';' + videoName + ';' + comment.user.name() + ';' + commentDate ;
      MessageServer messageServer = new MessageServer(new Date(), "CommentDeleteMessage", values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);
    }
    return commentResponseApi;
  }

  private String getAppUrl() {
    return environment.getProperty("app.url");
  }
}
