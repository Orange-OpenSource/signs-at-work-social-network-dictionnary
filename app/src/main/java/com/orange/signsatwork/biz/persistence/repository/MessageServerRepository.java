package com.orange.signsatwork.biz.persistence.repository;

import com.orange.signsatwork.biz.persistence.model.MessageServerDB;
import com.orange.signsatwork.biz.persistence.model.RequestDB;
import com.orange.signsatwork.biz.persistence.model.UserDB;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageServerRepository extends CrudRepository<MessageServerDB, Long> {

  @Query("select distinct m FROM MessageServerDB m order by m.date asc")
  List<MessageServerDB> findMessagesServerAllAsc();

  @Query("select distinct m FROM MessageServerDB m order by m.date desc")
  List<MessageServerDB> findMessagesServerAllDesc();

  @Query("select distinct m FROM MessageServerDB m where m.type='RequestCreateUserMessage' order by m.date asc")
  List<MessageServerDB> findMessagesServerCreateUserAsc();

  @Query("select distinct m FROM MessageServerDB m where m.type='RequestCreateUserMessage' order by m.date desc")
  List<MessageServerDB> findMessagesServerCreateUserDesc();

  @Query("select distinct m FROM MessageServerDB m where m.type='RequestChangeEmailMessage' order by m.date asc")
  List<MessageServerDB> findMessagesServerChangeUserLoginAsc();

  @Query("select distinct m FROM MessageServerDB m where m.type='RequestChangeEmailMessage' order by m.date desc")
  List<MessageServerDB> findMessagesServerChangeUserLoginDesc();

  @Query("select distinct m FROM MessageServerDB m where m.type in ('CreatePasswordMessage', 'CreatePasswordMessageAfterChangeEmailMessage', 'SavePasswordMessage') order by m.date asc")
  List<MessageServerDB> findMessagesServerUserProfilActionAsc();

  @Query("select distinct m FROM MessageServerDB m where m.type in ('CreatePasswordMessage', 'CreatePasswordMessageAfterChangeEmailMessage', 'SavePasswordMessage') order by m.date desc")
  List<MessageServerDB> findMessagesServerUserProfilActionDesc();

  @Query("select distinct m FROM MessageServerDB m where m.type in ('CommunityCreateMessage', 'CommunityRenameMessage', 'CommunityDeleteMessage', 'CommunityAddDescriptionMessage', 'CommunityRemoveMessage', 'CommunityRemoveMeMessage', 'CommunityAddMessage') order by m.date asc")
  List<MessageServerDB> findMessagesServerCommunityActionAsc();

  @Query("select distinct m FROM MessageServerDB m where m.type in ('CommunityCreateMessage', 'CommunityRenameMessage', 'CommunityDeleteMessage', 'CommunityAddDescriptionMessage', 'CommunityRemoveMessage', 'CommunityRemoveMeMessage', 'CommunityAddMessage') order by m.date desc")
  List<MessageServerDB> findMessagesServerCommunityActionDesc();

  @Query("select distinct m FROM MessageServerDB m where m.type='RequestMessage' order by m.date asc")
  List<MessageServerDB> findMessagesServerRequestAsc();

  @Query("select distinct m FROM MessageServerDB m where m.type='RequestMessage' order by m.date desc")
  List<MessageServerDB> findMessagesServerRequestDesc();

  @Query("select distinct m FROM MessageServerDB m where m.type='FavoriteShareMessage' order by m.date asc")
  List<MessageServerDB> findMessagesServerShareFavoriteAsc();

  @Query("select distinct m FROM MessageServerDB m where m.type='FavoriteShareMessage' order by m.date desc")
  List<MessageServerDB> findMessagesServerShareFavoriteDesc();

}

