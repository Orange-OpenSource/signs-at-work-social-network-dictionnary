package com.orange.signsatwork.biz.persistence.service.impl;

import com.orange.signsatwork.biz.persistence.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailServiceImpl implements EmailService {

  @Autowired
  public JavaMailSender emailSender;

  public void sendSimpleMessage(String to, String subject, String text) {

    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(to);
      message.setSubject(subject);
      message.setText(text);
      emailSender.send(message);
    } catch (MailException exception) {
      exception.printStackTrace();
    }
  }
}