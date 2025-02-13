package com.orange.signsatwork.biz.persistence.service.impl;

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

import com.orange.signsatwork.biz.domain.ActionType;
import com.orange.signsatwork.biz.domain.Community;
import com.orange.signsatwork.biz.domain.CommunityType;
import com.orange.signsatwork.biz.domain.MessageServer;
import com.orange.signsatwork.biz.persistence.repository.MessageServerRepository;
import com.orange.signsatwork.biz.persistence.repository.UserRepository;
import com.orange.signsatwork.biz.persistence.service.EmailService;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.MessageServerService;
import com.orange.signsatwork.biz.persistence.service.Services;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.*;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.sun.org.apache.xalan.internal.lib.ExsltDatetime.date;

@Component
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
  private final Services services;

  @Value("${app.admin.username}")
  String adminUsername;

  @Value("${app.name}")
  String appName;

  @Autowired
  public JavaMailSender emailSender;
  @Autowired
  TemplateEngine templateEngine;

  @Autowired
  private Environment environment;

  @Autowired
  MessageByLocaleService messageByLocaleService;

  public void sendRequestMessage(String[] to, String subject, String userName, String requestName, String url, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      } else if (appName.equals("Signs@LMB")) {
        imageName = "logo-text_S@LMB_purple-background.png";
      } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("user_name", userName);
      ctx.setVariable("request_name", requestName);
      ctx.setVariable("url", url);
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      String htmlContent = templateEngine.process("email", ctx);
      helper.setText(htmlContent, true);

      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");

      List<String> toList = Arrays.asList(to);
      String values = userName + ';' + toList.stream().collect(Collectors.joining(", ")) + ';' + requestName;
      MessageServer messageServer = new MessageServer(new Date(), "RequestMessage", values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);

      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void sendFavoriteShareMessage(String[] to, String subject, String userName, String favoriteName, String url, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      }  else if (appName.equals("Signs@LMB")) {
      imageName = "logo-text_S@LMB_purple-background.png";
     } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("user_name", userName);
      ctx.setVariable("favorite_name", favoriteName);
      ctx.setVariable("url", url);
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      String htmlContent = templateEngine.process("email-favorite", ctx);
      helper.setText(htmlContent, true);

      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");

      List<String> toList = Arrays.asList(to);
      String values = userName + ';' + favoriteName + ';' + toList.stream().collect(Collectors.joining(", "));
      MessageServer messageServer = new MessageServer(new Date(), "FavoriteShareMessage", values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);

      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void sendCommunityCreateMessage(String[] to, String subject, String userName, String communityName, List<String> names, String url, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      }  else if (appName.equals("Signs@LMB")) {
      imageName = "logo-text_S@LMB_purple-background.png";
      } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("user_name", userName);
      ctx.setVariable("community_name", communityName);
      ctx.setVariable("url", url);
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      String htmlContent = templateEngine.process("email-community", ctx);
      helper.setText(htmlContent, true);

      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");

      List<String> toList = Arrays.asList(to);
      String values = userName + ';' + messageByLocaleService.getMessage(CommunityType.Project.toString()) +';' + communityName + ';' + names.stream().collect(Collectors.joining(", ")) +';' + toList.stream().collect(Collectors.joining(", "));
      MessageServer messageServer = new MessageServer(new Date(), "CreateProjectCommunitySendEmailMessage", values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);

      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void sendCommunityAddMessage(String[] to, String subject, String userName, List<String> names, CommunityType communityType, String communityName, String url, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      }  else if (appName.equals("Signs@LMB")) {
      imageName = "logo-text_S@LMB_purple-background.png";
      } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("user_name", userName);
      ctx.setVariable("community_name", communityName);
      ctx.setVariable("community_type", messageByLocaleService.getMessage(communityType.toString()));
      ctx.setVariable("url", url);
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      String htmlContent = templateEngine.process("email-add-community", ctx);
      helper.setText(htmlContent, true);

      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");

      List<String> toList = Arrays.asList(to);
      String values = userName + ';' + names.stream().collect(Collectors.joining(", ")) +';' + messageByLocaleService.getMessage(communityType.toString()) +';' + communityName + ';' + toList.stream().collect(Collectors.joining(", "));

      MessageServer messageServer = new MessageServer(new Date(), "UsersAddToCommunitySendEmailMessage", values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);

      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void sendCommunityRenameMessage(String[] to, String subject, String userName, CommunityType communityType, String oldName, String newName, String url, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      } else if (appName.equals("Signs@LMB")) {
        imageName = "logo-text_S@LMB_purple-background.png";
      } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("community_type", messageByLocaleService.getMessage(communityType.toString()));
      ctx.setVariable("old_name", oldName);
      ctx.setVariable("new_name", newName);
      ctx.setVariable("url", url);
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      String htmlContent = templateEngine.process("email-rename-community", ctx);
      helper.setText(htmlContent, true);

      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");

      List<String> toList = Arrays.asList(to);
      String values = userName + ';' + messageByLocaleService.getMessage(communityType.toString()) +';' + oldName + ';' + newName + ';' + toList.stream().collect(Collectors.joining(", "));
      MessageServer messageServer = new MessageServer(new Date(), "RenameCommunitySendEmailMessage", values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);

      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void sendCommunityDeleteMessage(String[] to, String subject, String userName, CommunityType communityType, String communityName, List<String> names, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      } else if (appName.equals("Signs@LMB")) {
        imageName = "logo-text_S@LMB_purple-background.png";
      } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("user_name", userName);
      ctx.setVariable("community_name", communityName);
      ctx.setVariable("community_type", messageByLocaleService.getMessage(communityType.toString()));
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      String htmlContent = templateEngine.process("email-delete-community", ctx);
      helper.setText(htmlContent, true);

      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");

      List<String> toList = Arrays.asList(to);
      String values = userName + ';' + messageByLocaleService.getMessage(communityType.toString()) +';' + communityName + ';' + names.stream().collect(Collectors.joining(", ")) +';' + toList.stream().collect(Collectors.joining(", "));
      MessageServer messageServer = new MessageServer(new Date(), "DeleteCommunitySendEmailMessage", values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);

      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void sendDeleteLockUnLockUserMessage(String to, String subject, String userName, String username, String body1, String body2, String messServer, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      } else if (appName.equals("Signs@LMB")) {
        imageName = "logo-text_S@LMB_purple-background.png";
      } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      ctx.setVariable("body_1", messageByLocaleService.getMessage(body1));
      ctx.setVariable("body_2", messageByLocaleService.getMessage(body2));
      String htmlContent = templateEngine.process("email-delete-lock-unlock-user", ctx);
      helper.setText(htmlContent, true);

      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");

      String values = adminUsername + ";" + userName + ";" + username;
      MessageServer messageServer = new MessageServer(new Date(), messServer, values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);

      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void sendCommunityRemoveMessage(String[] to, String subject, String userName, List<String> names, CommunityType communityType, String communityName, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      } else if (appName.equals("Signs@LMB")) {
        imageName = "logo-text_S@LMB_purple-background.png";
      } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("community_type", messageByLocaleService.getMessage(communityType.toString()));
      ctx.setVariable("community_name", communityName);
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      String htmlContent = templateEngine.process("email-remove-community", ctx);
      helper.setText(htmlContent, true);
      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");

      List<String> toList = Arrays.asList(to);
      String values = userName + ';' + names.stream().collect(Collectors.joining(", ")) +';' + messageByLocaleService.getMessage(communityType.toString()) +';' + communityName + ';' + toList.stream().collect(Collectors.joining(", "));

      MessageServer messageServer = new MessageServer(new Date(), "UsersRemoveToCommunitySendEmailMessage", values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);

      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void sendResetPasswordMessage(String to, String subject, String userName, String url, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      } else if (appName.equals("Signs@LMB")) {
        imageName = "logo-text_S@LMB_purple-background.png";
      } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("url", url);
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      String htmlContent = templateEngine.process("email-reset-password", ctx);
      helper.setText(htmlContent, true);
      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");

      String values = userName;
      MessageServer messageServer = new MessageServer(new Date(), "ResetPasswordMessage", values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);

      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }


  public void sendCreatePasswordMessage(String to, String subject, String username, String url, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      } else if (appName.equals("Signs@LMB")) {
        imageName = "logo-text_S@LMB_purple-background.png";
      } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("username", username);
      ctx.setVariable("url", url);
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      String htmlContent = templateEngine.process("email-create-password", ctx);
      helper.setText(htmlContent, true);
      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");

      String values = adminUsername + ";" + username;
      MessageServer messageServer = new MessageServer(new Date(), "CreatePasswordMessage", values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);

      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void sendCreatePasswordMessageAfterChangeEmail(String to, String subject, String username, String url, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      } else if (appName.equals("Signs@LMB")) {
        imageName = "logo-text_S@LMB_purple-background.png";
      } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("url", url);
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      String htmlContent = templateEngine.process("email-create-password-after-change-email", ctx);
      helper.setText(htmlContent, true);
      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");

      String values = adminUsername + ";" + username;
      MessageServer messageServer = new MessageServer(new Date(), "CreatePasswordMessageAfterChangeEmailMessage", values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);

      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void sendCommunityDescriptionMessage(String[] to, String subject, String body, String userName, CommunityType communityType, String communityName, String url, String messageType, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      } else if (appName.equals("Signs@LMB")) {
        imageName = "logo-text_S@LMB_purple-background.png";
      } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("user_name", userName);
      ctx.setVariable("community_name", communityName);
      ctx.setVariable("url", url);
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      ctx.setVariable("body_1", body);
      String htmlContent = templateEngine.process("email-description-community", ctx);
      helper.setText(htmlContent, true);
      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");

      List<String> toList = Arrays.asList(to);
      String values = userName + ';' + messageByLocaleService.getMessage(communityType.toString()) +';' + communityName + ';'  + toList.stream().collect(Collectors.joining(", "));

      MessageServer messageServer = new MessageServer(new Date(), messageType, values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);


      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void sendCreateUserMessage(String to, String subject, Date date, String username, String url, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      } else if (appName.equals("Signs@LMB")) {
        imageName = "logo-text_S@LMB_purple-background.png";
      } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("date", date);
      ctx.setVariable("username", username);
      ctx.setVariable("url", url);
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      String htmlContent = templateEngine.process("email-create-user", ctx);
      helper.setText(htmlContent, true);
      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");


      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void sendChangeEmailMessage(String to, String subject, Date date, String name, String username, String url, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      } else if (appName.equals("Signs@LMB")) {
        imageName = "logo-text_S@LMB_purple-background.png";
      } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("date", date);
      ctx.setVariable("name", name);
      ctx.setVariable("username", username);
      ctx.setVariable("url", url);
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      String htmlContent = templateEngine.process("email-change-email", ctx);
      helper.setText(htmlContent, true);
      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");


      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void sendCanceledCreateUserChangeEmailMessage(String to, String subject, String bodyMail, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      } else if (appName.equals("Signs@LMB")) {
        imageName = "logo-text_S@LMB_purple-background.png";
      } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      ctx.setVariable("bodyMail", bodyMail);
      String htmlContent = templateEngine.process("email-canceled-create-user", ctx);
      helper.setText(htmlContent, true);
      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");


      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void sendSimpleMessage(String to, String subject, String text, String type, String values) {

    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(to);
      message.setSubject(subject);
      message.setText(text);
      message.setFrom(adminUsername);

      MessageServer messageServer = new MessageServer(new Date(), type, values, ActionType.TODO);
      services.messageServerService().addMessageServer(messageServer);

      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    }
  }

  private String getAppUrl() {
    return environment.getProperty("app.url");
  }


  public void sendUpdateProfilUserByAdminMessage(String to, String subject, String username, String body, String messageServerType,  Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      } else if (appName.equals("Signs@LMB")) {
        imageName = "logo-text_S@LMB_purple-background.png";
      } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      ctx.setVariable("body", body);
      String htmlContent = templateEngine.process("email-update-profil-by-admin", ctx);
      helper.setText(htmlContent, true);

      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");

      String values = adminUsername + ";" + username;
      MessageServer messageServer = new MessageServer(new Date(), messageServerType, values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);

      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void sendCommentDeleteMessage(String[] to, String subject, String userNameDeleted, String userName, String commentDate, String url, String videoName, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      } else if (appName.equals("Signs@LMB")) {
        imageName = "logo-text_S@LMB_purple-background.png";
      } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("body", messageByLocaleService.getMessage("comment_delete_body_1", new Object[]{userName, commentDate, url, userNameDeleted}));
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      String htmlContent = templateEngine.process("email-update-data-sign-by-admin", ctx);
      helper.setText(htmlContent, true);
      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");

      List<String> toList = Arrays.asList(to);
      String values = adminUsername + ';' + toList.stream().collect(Collectors.joining(", ")) + ';' + userNameDeleted + ';' + videoName + ';' + userName + ';' + commentDate;
      MessageServer messageServer = new MessageServer(new Date(), "CommentDeleteByUserSendEmailMessage", values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);

      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void sendSignDefinitionMessage(String[] to, String subject, String body, String signName, String messageType, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      } else if (appName.equals("Signs@LMB")) {
        imageName = "logo-text_S@LMB_purple-background.png";
      } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("body", body);
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      String htmlContent = templateEngine.process("email-update-data-sign-by-admin", ctx);
      helper.setText(htmlContent, true);
      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");

      List<String> toList = Arrays.asList(to);
      String values = adminUsername + ';' + toList.stream().collect(Collectors.joining(", ")) + ';' + signName;
      MessageServer messageServer = new MessageServer(new Date(), messageType, values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);

      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void sendRequestDescriptionMessage(String to, String subject, String body, String requestName, String messageType, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      } else if (appName.equals("Signs@LMB")) {
        imageName = "logo-text_S@LMB_purple-background.png";
      } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("body", body);
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      String htmlContent = templateEngine.process("email-update-data-request-by-admin", ctx);
      helper.setText(htmlContent, true);
      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");

      List<String> toList = Arrays.asList(to);
      String values = adminUsername + ';' + toList.stream().collect(Collectors.joining(", ")) + ';' + requestName;
      MessageServer messageServer = new MessageServer(new Date(), messageType, values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);

      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void sendVideoMessage(String[] to, String subject, String body, String videoName, String messageType, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      } else if (appName.equals("Signs@LMB")) {
        imageName = "logo-text_S@LMB_purple-background.png";
      } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("body", body);
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      String htmlContent = templateEngine.process("email-update-data-sign-by-admin", ctx);
      helper.setText(htmlContent, true);
      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");

      List<String> toList = Arrays.asList(to);
      String values = adminUsername + ';' + toList.stream().collect(Collectors.joining(", ")) + ';' + videoName;
      MessageServer messageServer = new MessageServer(new Date(), messageType, values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);

      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
  public void sendRenameSignMessage(String[] to, String subject, String body, String name, String oldName, String messageType, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else if (appName.equals("Signs@ADIS")){
        imageName = "logo-textADIS_blue-white.png";
      } else if (appName.equals("Signs@LMB")) {
        imageName = "logo-text_S@LMB_purple-background.png";
      } else if (appName.equals("Signs@ANVOL")) {
        imageName = "logo-text_Anvol_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("body", body);
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      String htmlContent = templateEngine.process("email-update-data-sign-by-admin", ctx);
      helper.setText(htmlContent, true);
      imageIs = this.getClass().getClassLoader().getResourceAsStream(imageName);
      byte[] imageByteArray = org.jcodec.common.IOUtils.toByteArray(imageIs);
      InputStreamSource imageSource = new ByteArrayResource((imageByteArray));

      helper.addInline(imageName, imageSource, "image/png");

      List<String> toList = Arrays.asList(to);
      String values = adminUsername + ';' + toList.stream().collect(Collectors.joining(", ")) + ';' + oldName + ';' + name;
      MessageServer messageServer = new MessageServer(new Date(), messageType, values, ActionType.NO);
      services.messageServerService().addMessageServer(messageServer);

      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (imageIs != null) {
        try {
          imageIs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
