package com.orange.signsatwork.biz.domain;

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

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class Community {
    public final long id;
    public final String name;
    public final String descriptionText;
    public final String descriptionVideo;
    public final Users users;
    public final CommunityType type;
    public final User user;


    public static Community create(String name, CommunityType type) {
        return new Community(-1, name, null, null, null, type, null);
    }

    public List<Long> usersIds() {
    return users != null ? users.ids() : null;
  }

}
