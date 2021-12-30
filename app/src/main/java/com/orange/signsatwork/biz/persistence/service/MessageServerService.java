package com.orange.signsatwork.biz.persistence.service;

import com.orange.signsatwork.biz.domain.MessageServer;
import com.orange.signsatwork.biz.domain.MessagesServer;
import com.orange.signsatwork.biz.domain.Requests;

public interface MessageServerService {

  void addMessageServer(MessageServer messageServer);

  MessagesServer messagesServerAllAsc();

  MessagesServer messagesServerAllDesc();
}
