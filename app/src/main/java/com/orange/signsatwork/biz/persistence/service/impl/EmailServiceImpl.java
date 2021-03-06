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

import com.orange.signsatwork.biz.persistence.service.EmailService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

@Component
public class EmailServiceImpl implements EmailService {

  @Value("${app.admin.username}")
  String adminUsername;

  @Value("${app.name}")
  String appName;

  @Autowired
  public JavaMailSender emailSender;
  @Autowired
  TemplateEngine templateEngine;

  public void sendRequestMessage(String[] to, String subject, String userName, String requestName, String url, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
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

     /* File file = ResourceUtils.getFile("classpath:public/img/logo_and_texte.png");
      System.out.println("File Found : " + file.exists());*/
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

  public void sendCommunityCreateMessage(String[] to, String subject, String userName, String communityName, String url, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
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

  public void sendCommunityRenameMessage(String[] to, String subject, String oldName, String newName, String url, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
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

  public void sendCommunityDeleteMessage(String[] to, String subject, String userName, String communityName, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
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
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      String htmlContent = templateEngine.process("email-delete-community", ctx);
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

  public void sendCommunityRemoveMessage(String[] to, String subject, String communityName, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
      } else {
        imageName = "logo-text_blue-background.png";
      }
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(adminUsername);
      Context ctx = new Context(locale);
      ctx.setVariable("community_name", communityName);
      ctx.setVariable("imageResourceName", imageName);
      ctx.setVariable("appName", appName);
      String htmlContent = templateEngine.process("email-remove-community", ctx);
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

  public void sendResetPasswordMessage(String to, String subject, String url, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
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

  public void sendCreatePasswordMessageAfterChangeEmail(String to, String subject, String url, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
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

  public void sendCommunityAddDescriptionMessage(String[] to, String subject, String userName, String communityName, String url, Locale locale) {
    InputStream imageIs = null;
    String imageName;
    try {
      if (appName.equals("Signs@Form")) {
        imageName = "logo-textForm_blue-background.png";
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
      String htmlContent = templateEngine.process("email-add-description-community", ctx);
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

  public void sendSimpleMessage(String to, String subject, String text) {

    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(to);
      message.setSubject(subject);
      message.setText(text);
      message.setFrom(adminUsername);
      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    }
  }
}
