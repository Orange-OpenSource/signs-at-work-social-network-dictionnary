package com.orange.signsatwork.biz.view.model;

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

import com.orange.signsatwork.biz.domain.Comment;
import com.orange.signsatwork.biz.domain.Comments;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.persistence.model.CommentData;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentAdminView {
  private long id;
  private Date commentDate;
  private String text;
  private String name;
  private String messageInDeleteCommentModal;
  public Long userId;

  @Autowired
  static
  MessageByLocaleService messageByLocaleService;

  public static CommentAdminView from(CommentData commentData) {
    String message = messageByLocaleService.getMessage("comment_from_user_delete_message", new Object[]{commentData.name()});
    return new CommentAdminView(commentData.id, commentData.commentDate, commentData.text, commentData.name(), message, commentData.userId);
  }

}
