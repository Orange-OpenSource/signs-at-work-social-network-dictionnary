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

import com.orange.signsatwork.biz.domain.Article;
import com.orange.signsatwork.biz.domain.ArticleType;
import com.orange.signsatwork.biz.domain.Articles;
import com.orange.signsatwork.biz.persistence.model.ArticleDB;
import com.orange.signsatwork.biz.persistence.repository.ArticleRepository;
import com.orange.signsatwork.biz.persistence.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ArticleServiceImpl implements ArticleService {
  private final ArticleRepository articleRepository;

  @Override
  public Articles findByLanguageAndType(String language, ArticleType type) {
    List<ArticleDB> articlesDB = articleRepository.findByLanguageAndType(language, type);
    return articlesFrom(articlesDB);
  }


   private Articles articlesFrom(Iterable<ArticleDB> articlesDB) {
    List<Article> articles = new ArrayList<>();
    articlesDB.forEach(articleDB -> articles.add(articleFrom(articleDB)));
    return new Articles(articles);
  }

  private Article articleFrom(ArticleDB articleDB) {
    if (articleDB == null) {
      return null;
    }
    return new Article(articleDB.getId(), articleDB.getName(), articleDB.getDescriptionText(), articleDB.getDescriptionPicture(), articleDB.getDescriptionVideo(), articleDB.getType());
  }


}
