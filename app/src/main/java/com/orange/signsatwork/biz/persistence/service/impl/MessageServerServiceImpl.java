package com.orange.signsatwork.biz.persistence.service.impl;

import com.orange.signsatwork.biz.domain.*;
import com.orange.signsatwork.biz.persistence.model.MessageServerDB;
import com.orange.signsatwork.biz.persistence.repository.MessageServerRepository;
import com.orange.signsatwork.biz.persistence.service.MessageServerService;
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
  public long addMessageServer(MessageServer messageServer) {
    MessageServerDB messageServerDB = messageServerRepository.save(MessageServerDBFrom(messageServer));
    return messageServerDB.getId();
  }

  @Override
  public MessagesServer messagesServerAllAsc() {
    return messagesServerFrom(messageServerRepository.findMessagesServerAllAsc());
  }

  @Override
  public MessagesServer messagesServerAllDesc() {
    return messagesServerFrom(messageServerRepository.findMessagesServerAllDesc());
  }

  @Override
  public MessagesServer messagesServerCreateUserAsc() {
    return messagesServerFrom(messageServerRepository.findMessagesServerCreateUserAsc());
  }

  @Override
  public MessagesServer messagesServerCreateUserDesc() {
    return messagesServerFrom(messageServerRepository.findMessagesServerCreateUserDesc());
  }

  @Override
  public MessagesServer messagesServerChangeUserLoginAsc() {
    return messagesServerFrom(messageServerRepository.findMessagesServerChangeUserLoginAsc());
  }

  @Override
  public MessagesServer messagesServerChangeUserLoginDesc() {
    return messagesServerFrom(messageServerRepository.findMessagesServerChangeUserLoginDesc());
  }

  @Override
  public MessagesServer messagesServerUserProfilActionAsc() {
    return messagesServerFrom(messageServerRepository.findMessagesServerUserProfilActionAsc());
  }

  @Override
  public MessagesServer messagesServerUserProfilActionDesc() {
    return messagesServerFrom(messageServerRepository.findMessagesServerUserProfilActionDesc());
  }
  @Override
  public MessagesServer messagesServerCommunityActionAsc() {
    return messagesServerFrom(messageServerRepository.findMessagesServerCommunityActionAsc());
  }

  @Override
  public MessagesServer messagesServerCommunityActionDesc() {
    return messagesServerFrom(messageServerRepository.findMessagesServerCommunityActionDesc());
  }

  @Override
  public MessagesServer messagesServerRequestAsc() {
    return messagesServerFrom(messageServerRepository.findMessagesServerRequestAsc());
  }

  @Override
  public MessagesServer messagesServerRequestDesc() {
    return messagesServerFrom(messageServerRepository.findMessagesServerRequestDesc());
  }

  @Override
  public MessagesServer messagesServerShareFavoriteAsc() {
    return messagesServerFrom(messageServerRepository.findMessagesServerShareFavoriteAsc());
  }

  @Override
  public MessagesServer messagesServerShareFavoriteDesc() {
    return messagesServerFrom(messageServerRepository.findMessagesServerShareFavoriteDesc());
  }

  @Override
  public MessagesServer messagesServerDataSignAsc() {
    return messagesServerFrom(messageServerRepository.findMessagesServerDataSignAsc());
  }

  @Override
  public MessagesServer messagesServerDataSignDesc() {
    return messagesServerFrom(messageServerRepository.findMessagesServerDataSignDesc());
  }

  @Override
  public MessagesServer messagesServerCreateUserChangeEmailToDoAsc() {
    return messagesServerFrom(messageServerRepository.findMessagesServerCreateUserChangeEmailToDoAsc());
  }

  @Override
  public MessagesServer messagesServerCreateUserChangeEmailWithId(long id) {
    return messagesServerFrom(messageServerRepository.findMessagesServerCreateUserChangeEmailWithId(id));
  }

  @Override
  public void updateMessageServerAction(long messageServerId, ActionType action) {
    MessageServerDB messageServerDB = messageServerRepository.findOne(messageServerId);
    messageServerDB.setAction(action);
    messageServerRepository.save(messageServerDB);
  }

  @Override
  public MessagesServer messagesServerCreateUserWithUserName(String userName) {
    return messagesServerFrom(messageServerRepository.findMessagesServerCreateUserWithUserName(userName));
  }

  @Override
  public MessagesServer messagesServerChangeEmailWithUserName(String userName) {
    return messagesServerFrom(messageServerRepository.findMessagesServerChangeEmailWithUserName(userName));
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
