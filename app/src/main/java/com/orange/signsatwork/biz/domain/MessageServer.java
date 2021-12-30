package com.orange.signsatwork.biz.domain;

import lombok.RequiredArgsConstructor;

import java.util.Date;

@RequiredArgsConstructor
public class MessageServer {
  public final long id;
  public final Date date;
  public final String type;
  public final String values;
  public final ActionType action;

  public MessageServer(Date date, String type, String values, ActionType action) {
    this.id= 0L;
    this.date = date;
    this.type = type;
    this.values = values;
    this.action = action;
  }
}
