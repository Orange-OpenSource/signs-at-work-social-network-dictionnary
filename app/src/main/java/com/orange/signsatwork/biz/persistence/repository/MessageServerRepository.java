package com.orange.signsatwork.biz.persistence.repository;

import com.orange.signsatwork.biz.persistence.model.MessageServerDB;
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

  @Query("select distinct m FROM MessageServerDB m where m.type in ('CreatePasswordMessage', 'CreatePasswordMessageAfterChangeEmailMessage', 'SavePasswordMessage', 'UserDeleteMessage', 'UserLockMessage', 'UserUnLockMessage','UserChangeFirstLastNameMessage', 'UserChangeFirstNameMessage', 'UserChangeLastNameMessage', 'UserChangeEntityMessage', 'UserChangeJobMessage', 'UserChangeJobDescriptionTextMessage', 'UserChangeJobDescriptionLsfMessage', 'UserDeleteJobDescriptionLsfMessage', 'UserChangeNameLsfMessage', 'UserDeleteNameLsfMessage') order by m.date asc")
  List<MessageServerDB> findMessagesServerUserProfilActionAsc();

  @Query("select distinct m FROM MessageServerDB m where m.type in ('CreatePasswordMessage', 'CreatePasswordMessageAfterChangeEmailMessage', 'SavePasswordMessage','UserDeleteMessage', 'UserLockMessage', 'UserUnLockMessage','UserChangeFirstLastNameMessage', 'UserChangeFirstNameMessage', 'UserChangeLastNameMessage', 'UserChangeEntityMessage', 'UserChangeJobMessage', 'UserChangeJobDescriptionTextMessage', 'UserChangeJobDescriptionLsfMessage', 'UserDeleteJobDescriptionLsfMessage', 'UserChangeNameLsfMessage', 'UserDeleteNameLsfMessage') order by m.date desc")
  List<MessageServerDB> findMessagesServerUserProfilActionDesc();

  @Query("select distinct m FROM MessageServerDB m where m.type in ('CreateProjectCommunityMessage', 'CreateProjectCommunitySendEmailMessage', 'CreateJobCommunityMessage', 'CommunityCreateMessage', 'CommunityRenameMessage', 'CommunityDeleteSendEmailMessage', 'CommunityDeleteMessage', 'DeleteCommunityMessageWithoutUsers', 'DeleteCommunityMessage', 'DeleteCommunitySendEmailMessage','CommunityAddDescriptionMessage', 'CommunityRemoveMessage', 'CommunityRemoveMeMessage', 'CommunityAddMessage') order by m.date asc")
  List<MessageServerDB> findMessagesServerCommunityActionAsc();

  @Query("select distinct m FROM MessageServerDB m where m.type in ('CreateProjectCommunityMessage', 'CreateProjectCommunitySendEmailMessage', 'CreateJobCommunityMessage', 'CommunityCreateMessage', 'CommunityRenameMessage', 'CommunityDeleteSendEmailMessage', 'CommunityDeleteMessage', 'DeleteCommunityMessageWithoutUsers', 'DeleteCommunityMessage', 'DeleteCommunitySendEmailMessage', 'CommunityAddDescriptionMessage', 'CommunityRemoveMessage', 'CommunityRemoveMeMessage', 'CommunityAddMessage') order by m.date desc")
  List<MessageServerDB> findMessagesServerCommunityActionDesc();

  @Query("select distinct m FROM MessageServerDB m where m.type in ('UpdateRequestDescriptionMessage','UpdateRequestDescriptionSendEmailMessage', 'AddRequestDescriptionMessage', 'AddRequestDescriptionSendEmailMessage', 'DeleteRequestDescriptionMessage', 'DeleteRequestDescriptionSendEmailMessage', 'AddRequestDescriptionTextSendEmailMessage', 'UpdateRequestDescriptionTextSendEmailMessage', 'DeleteRequestSendEmailMessage', 'DeleteRequestMessage', 'PrioriseRequestSendEmailMessage', 'PrioriseRequestMessage', 'CreateSignForRequestSendEmailMessage', 'CreateSignForRequestMessage')  order by m.date asc")
  List<MessageServerDB> findMessagesServerDataRequestAsc();

  @Query("select distinct m FROM MessageServerDB m where m.type in ('UpdateRequestDescriptionMessage','UpdateRequestDescriptionSendEmailMessage', 'AddRequestDescriptionMessage', 'AddRequestDescriptionSendEmailMessage', 'DeleteRequestDescriptionMessage', 'DeleteRequestDescriptionSendEmailMessage', 'AddRequestDescriptionTextSendEmailMessage', 'UpdateRequestDescriptionTextSendEmailMessage', 'DeleteRequestSendEmailMessage', 'DeleteRequestMessage', 'PrioriseRequestSendEmailMessage', 'PrioriseRequestMessage', 'CreateSignForRequestSendEmailMessage', 'CreateSignForRequestMessage')  order by m.date desc")
  List<MessageServerDB> findMessagesServerDataRequestDesc();

  @Query("select distinct m FROM MessageServerDB m where m.type='FavoriteShareMessage' order by m.date asc")
  List<MessageServerDB> findMessagesServerShareFavoriteAsc();

  @Query("select distinct m FROM MessageServerDB m where m.type='FavoriteShareMessage' order by m.date desc")
  List<MessageServerDB> findMessagesServerShareFavoriteDesc();

  @Query("select distinct m FROM MessageServerDB m where m.type in ('CommentDeleteMessage', 'CommentDeleteSendEmailMessage', 'UpdateSignDefinitionMessage', 'UpdateSignDefinitionSendEmailMessage', 'AddSignDefinitionMessage', 'AddSignDefinitionSendEmailMessage', 'DeleteSignDefinitionMessage', 'DeleteSignDefinitionSendEmailMessage', 'AddSignDefinitionTextMessage', 'UpdateSignDefinitionTextMessage', 'AddSignDefinitionTextSendEmailMessage', 'UpdateSignDefinitionTextSendEmailMessage', 'DeleteVideoSendEmailMessage', 'DeleteVideoMessage', 'DeleteSignSendEmailMessage', 'DeleteSignMessage', 'UpdateVideoSendEmailMessage', 'UpdateVideoMessage', 'RenameSignMessage', 'RenameSignSendEmailMessage') order by m.date asc")
  List<MessageServerDB> findMessagesServerDataSignAsc();

  @Query("select distinct m FROM MessageServerDB m where m.type in ('CommentDeleteMessage', 'CommentDeleteSendEmailMessage', 'UpdateSignDefinitionMessage','UpdateSignDefinitionSendEmailMessage', 'AddSignDefinitionMessage', 'AddSignDefinitionSendEmailMessage', 'DeleteSignDefinitionMessage', 'DeleteSignDefinitionSendEmailMessage', 'AddSignDefinitionTextSendEmailMessage', 'UpdateSignDefinitionTextSendEmailMessage', 'DeleteVideoSendEmailMessage', 'DeleteVideoMessage', 'DeleteSignSendEmailMessage', 'DeleteSignMessage', 'UpdateVideoSendEmailMessage', 'UpdateVideoMessage', 'RenameSignMessage', 'RenameSignSendEmailMessage') order by m.date desc")
  List<MessageServerDB> findMessagesServerDataSignDesc();

  @Query("select distinct m FROM MessageServerDB m where m.type in ('RequestCreateUserMessage','RequestChangeEmailMessage') and m.action='TODO' order by m.date asc")
  List<MessageServerDB> findMessagesServerCreateUserChangeEmailToDoAsc();

  @Query("select distinct m FROM MessageServerDB m where m.type in ('RequestCreateUserMessage','RequestChangeEmailMessage') and m.id = :id")
  List<MessageServerDB> findMessagesServerCreateUserChangeEmailWithId(@Param("id") long id);

  @Query("select distinct m FROM MessageServerDB m where m.type='RequestCreateUserMessage' and m.action='TODO' and m.val like concat('%',:userName,'%')")
  List<MessageServerDB> findMessagesServerCreateUserWithUserName(@Param("userName") String userName);

  @Query("select distinct m FROM MessageServerDB m where m.type='RequestChangeEmailMessage' and m.action='TODO' and m.val like concat('%',:userName,'%')")
  List<MessageServerDB> findMessagesServerChangeEmailWithUserName(@Param("userName") String userName);

  public default MessageServerDB findOne(long id) {
    return findById(id).orElse(null);
  }
}

