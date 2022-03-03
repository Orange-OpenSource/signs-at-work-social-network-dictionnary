package com.orange.signsatwork.biz.persistence.service;

import com.orange.signsatwork.biz.domain.*;

public interface MessageServerService {

  void addMessageServer(MessageServer messageServer);

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

  MessagesServer messagesServerRequestAsc();

  MessagesServer messagesServerRequestDesc();

  MessagesServer messagesServerShareFavoriteAsc();

  MessagesServer messagesServerShareFavoriteDesc();

  MessagesServer messagesServerCreateUserToDoAsc();

  MessagesServer messagesServerCreateUserWithId(long id);

  void updateMessageServerAction(long messageServerId, ActionType action);
}
