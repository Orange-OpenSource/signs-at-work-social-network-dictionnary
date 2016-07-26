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


import com.orange.signsatwork.biz.domain.Sign;
import com.orange.signsatwork.biz.persistence.service.SignService;
import com.orange.signsatwork.biz.domain.Request;
import com.orange.signsatwork.biz.persistence.service.RequestService;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RequestProfileView {
  private Request request;
  private Long requestSignId;
  private List<Sign> allSigns;

  private RequestService requestService;

  public RequestProfileView(Request request, SignService signService) {
    this.request = request;
    if (request.sign != null) {
      this.requestSignId = request.sign.id;
    }
    this.allSigns = signService.all().list();
  }
}
