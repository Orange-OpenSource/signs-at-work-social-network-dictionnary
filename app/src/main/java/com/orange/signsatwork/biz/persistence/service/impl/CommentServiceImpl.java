package com.orange.signsatwork.biz.persistence.service.impl;

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
import com.orange.signsatwork.biz.persistence.model.CommentDB;
import com.orange.signsatwork.biz.persistence.model.UserDB;
import com.orange.signsatwork.biz.persistence.model.VideoDB;
import com.orange.signsatwork.biz.persistence.repository.CommentRepository;
import com.orange.signsatwork.biz.persistence.repository.VideoRepository;
import com.orange.signsatwork.biz.persistence.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
  private final VideoRepository videoRepository;
  private final CommentRepository commentRepository;

  @Override
  public Comments all() {
    return commentsFrom(commentRepository.findAll());
  }

  @Override
  public Comment withId(long id) {
    return commentFrom(commentRepository.findOne(id));
  }

  @Override
  public Comments forVideo(long videoId) {
    return commentsFrom(
            commentRepository.findByVideo(videoRepository.findOne(videoId))
    );
  }

  @Override
  public long forVideoSignsView(long videoId) {
    return commentRepository.countByVideo(videoRepository.findOne(videoId));
  }


  @Override
  public Comment create(Comment comment) {
    CommentDB commentDB = commentRepository.save(commentDBFrom(comment));
    return commentFrom(commentDB);
  }

  @Override
  public void delete(Comment comment) {
    CommentDB commentDB = commentRepository.findOne(comment.id);
    VideoDB videoDB = commentDB.getVideo();
    UserDB userDB = commentDB.getUser();
    videoDB.getComments().remove(commentDB);
    userDB.getComments().remove(commentDB);
    commentRepository.delete(commentDB);
  }

  Comments commentsFrom(Iterable<CommentDB> commentsDB) {
    List<Comment> comments = new ArrayList<>();
    commentsDB.forEach(commentDB -> comments.add(commentFrom(commentDB)));
    return new Comments(comments);
  }

  static Comment commentFrom(CommentDB commentDB) {
    return new Comment(commentDB.getId(), commentDB.getCommentDate(), commentDB.getText(), UserServiceImpl.userFromSignView(commentDB.getUser()));
  }

  Comments commentsFromSignsView(Iterable<CommentDB> commentsDB) {
    List<Comment> comments = new ArrayList<>();
    commentsDB.forEach(commentDB -> comments.add(commentFromSignsView(commentDB)));
    return new Comments(comments);
  }

  static Comment commentFromSignsView(CommentDB commentDB) {
    return new Comment(commentDB.getId(), commentDB.getCommentDate(), commentDB.getText(), null);
  }

  private CommentDB commentDBFrom(Comment comment) {
    return new CommentDB(comment.commentDate, comment.text);
  }
}
