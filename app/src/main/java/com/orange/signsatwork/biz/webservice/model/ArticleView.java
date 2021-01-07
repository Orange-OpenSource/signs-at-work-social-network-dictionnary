package com.orange.signsatwork.biz.webservice.model;

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

import com.orange.signsatwork.biz.domain.Article;
import com.orange.signsatwork.biz.domain.ArticleType;
import com.orange.signsatwork.biz.domain.Sign;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArticleView {
  private String name;
  private String descriptionText;
  private String descriptionPicture;
  private String descriptionVideo;

  public ArticleView(Article article) {
    this(article.name, article.descriptionText, article.descriptionPicture, article.descriptionVideo);
  }
}
