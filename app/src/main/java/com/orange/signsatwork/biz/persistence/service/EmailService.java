package com.orange.signsatwork.biz.persistence.service;

/**
 * Created by obelix on 24/07/17.
 */

public interface EmailService {

  public void sendSimpleMessage(String to, String subject, String text);
}
