package com.orange.signsatwork.biz.persistence.service;

/*
 * #%L
 * Signs at work
 * %%
 * Copyright (C) 2016 Orange
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import com.orange.signsatwork.biz.domain.CommunityType;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public interface EmailService {

  public void sendSimpleMessage(String to, String subject, String text, String type, String values);

  public void sendRequestMessage(String[] to, String subject, String userName, String requestName, String url, Locale locale);

  public void sendFavoriteShareMessage(String[] to, String subject, String userName, String favoriteName, String url, Locale locale);

  public void sendCommunityCreateMessage(String[] to, String subject, String userName, String communityName, List<String> names, String url, Locale locale);

  public void sendCommunityAddMessage(String[] to, String subject, String userName, List<String> names, CommunityType communityType, String communityName, String url, Locale locale);

  public void sendCommunityDeleteMessage(String[] to, String subject, String userName, CommunityType communityType, String communityName, List<String> names, Locale locale);

  public void sendCommunityRemoveMessage(String[] to, String subject, String userName, List<String> names, CommunityType communityType, String communityName, Locale locale);

  public void sendCommunityRenameMessage(String[] to, String subject, String userName, CommunityType communityType, String oldName, String newName, String url, Locale locale);

  public void sendResetPasswordMessage(String to, String subject, String userName, String url, Locale locale);

  public void sendCreatePasswordMessage(String to, String subject, String username, String url, Locale locale);

  public void sendCommunityDescriptionMessage(String[] to, String subject, String body, String userName, CommunityType communityType, String communityName, String url, String messageType, Locale locale);

  public void sendCreatePasswordMessageAfterChangeEmail(String to, String subject,  String username, String url, Locale locale);

  public void sendCreateUserMessage(String to, String subject, Date date, String username, String url, Locale locale);

  public void sendChangeEmailMessage(String to, String subject, Date date, String name, String username, String url, Locale locale);

  public void sendCanceledCreateUserChangeEmailMessage(String to, String subject, String bodyMail, Locale locale);

  public void sendDeleteLockUnLockUserMessage(String to, String subject, String userName, String username, String body1, String body2, String messServer, Locale locale);

  public void sendUpdateProfilUserByAdminMessage(String to, String subject, String username, String body, String messageServerType, Locale locale);

  public void sendCommentDeleteMessage(String[] to, String subject, String userNameDeleted, String userName, Date commentDate, String url, String videoName, Locale locale);

  public void sendSignDefinitionMessage(String[] to, String subject, String body, String signName, String messageType, Locale locale);

  public void sendVideoMessage(String[] to, String subject, String body, String videoName, String messageType, Locale locale);

  public void sendRenameSignMessage(String[] to, String subject, String body, String name, String oldName, String messageType, Locale locale);

  public void sendRequestDescriptionMessage(String to, String subject, String body, String requestName, String messageType, Locale locale);
}
