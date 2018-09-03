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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class EmailServiceImpl implements EmailService {

  @Autowired
  public JavaMailSender emailSender;
  @Autowired
  TemplateEngine templateEngine;

  public void sendSimpleMessage(String[] to, String subject, String userName, String requestName, String url) {

    try {
      /*SimpleMailMessage message = new SimpleMailMessage();*/
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      /*message.setTo(to);
      message.setSubject(subject);
      message.setText(text);
      message.setFrom("admin@admin.com");*/
     helper.setTo(to);
     helper.setSubject(subject);
     helper.setFrom("admin@admin.com");
     Context ctx = new Context();
     ctx.setVariable("user_name", userName);
     ctx.setVariable("request_name", requestName);
     ctx.setVariable("url", url);

     String htmlContent = templateEngine.process("email", ctx);
     helper.setText(htmlContent, "true");
      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    }
  }
}
