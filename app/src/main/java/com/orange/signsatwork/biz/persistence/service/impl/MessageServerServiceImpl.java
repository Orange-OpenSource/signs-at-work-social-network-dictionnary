package com.orange.signsatwork.biz.persistence.service.impl;

import com.orange.signsatwork.biz.domain.MessageServer;
import com.orange.signsatwork.biz.domain.MessagesServer;
import com.orange.signsatwork.biz.domain.Request;
import com.orange.signsatwork.biz.domain.Requests;
import com.orange.signsatwork.biz.persistence.model.MessageServerDB;
import com.orange.signsatwork.biz.persistence.model.RequestDB;
import com.orange.signsatwork.biz.persistence.repository.MessageServerRepository;
import com.orange.signsatwork.biz.persistence.service.MessageServerService;
import com.orange.signsatwork.biz.persistence.service.Services;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageServerServiceImpl implements MessageServerService {
  private final MessageServerRepository messageServerRepository;

  @Override
  public void addMessageServer(MessageServer messageServer) {
    MessageServerDB messageServerDB = messageServerRepository.save(MessageServerDBFrom(messageServer));
  }

  @Override
  public MessagesServer messagesServerAllAsc() {
    return messagesServerFrom(messageServerRepository.findMessagesServerAllAsc());
  }

  @Override
  public MessagesServer messagesServerAllDesc() {
    return messagesServerFrom(messageServerRepository.findMessagesServerAllDesc());
  }

  private MessageServerDB MessageServerDBFrom(MessageServer messageServer) {
    return new MessageServerDB(messageServer.date, messageServer.type, messageServer.values, messageServer.action);
  }

  private MessagesServer messagesServerFrom(Iterable<MessageServerDB> messagesServerDB) {
    List<MessageServer> messagesServer = new ArrayList<>();
    messagesServerDB.forEach(messageServerDB -> messagesServer.add(messageServerFrom(messageServerDB)));
    return new MessagesServer(messagesServer);
  }

  static MessageServer messageServerFrom(MessageServerDB messageServerDB) {
    if (messageServerDB == null) {
      return null;
    }
    return new MessageServer(messageServerDB.getId(), messageServerDB.getDate(), messageServerDB.getType(), messageServerDB.getVal(), messageServerDB.getAction());
  }
}
