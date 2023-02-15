package com.orange.signsatwork.biz.persistence.service;

import com.orange.signsatwork.biz.domain.*;

public interface MessageServerService {

  long addMessageServer(MessageServer messageServer);

  MessagesServer messagesServerAllAsc();

  MessagesServer messagesServerAllDesc();

  MessagesServer messagesServerCreateUserAsc();

  MessagesServer messagesServerCreateUserDesc();

  MessagesServer messagesServerChangeUserLoginAsc();

  MessagesServer messagesServerChangeUserLoginDesc();

  MessagesServer messagesServerUserProfilActionAsc();

  MessagesServer messagesServerUserProfilActionDesc();

  MessagesServer messagesServerCommunityActionAsc();

  MessagesServer messagesServerCommunityActionDesc();

  MessagesServer messagesServerDataRequestAsc();

  MessagesServer messagesServerDataRequestDesc();

  MessagesServer messagesServerShareFavoriteAsc();

  MessagesServer messagesServerShareFavoriteDesc();

  MessagesServer messagesServerDataSignAsc();

  MessagesServer messagesServerDataSignDesc();

  MessagesServer messagesServerCreateUserChangeEmailToDoAsc();

  MessagesServer messagesServerCreateUserChangeEmailWithId(long id);

  MessagesServer messagesServerCreateUserWithUserName(String userName);

  MessagesServer messagesServerChangeEmailWithUserName(String userName);

  void updateMessageServerAction(long messageServerId, ActionType action);
}
