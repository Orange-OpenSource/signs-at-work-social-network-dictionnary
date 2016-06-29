package com.orange.spring.demo.biz.persistence.model;

import com.orange.spring.demo.biz.domain.Rate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/*
 * #%L
 * Telsigne
 * %%
 * Copyright (C) 2016 Orange
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
// we want to save 'Rating' objects in the 'ratings' DB table
@Table(name = "ratings")
@Entity
@AssociationOverrides({
        @AssociationOverride(name = "primaryKey.video",
                joinColumns = @JoinColumn(name = "video_id")),
        @AssociationOverride(name = "primaryKey.user",
                joinColumns = @JoinColumn(name = "user_id")) })
// default constructor only exists for the sake of JPA
@NoArgsConstructor
@Getter
@Setter
@ToString
public class RatingDB implements Serializable {


    @EmbeddedId
    private RatingDBId primaryKey = new RatingDBId();

    @Transient
    public UserDB getUser() {
        return  getPrimaryKey().getUser();
    }

    public void setUser(UserDB user) {
        getPrimaryKey().setUser(user);
    }

    @Transient
    public VideoDB getVideo() {
        return  getPrimaryKey().getVideo();
    }

    public void setVideo(VideoDB video) {
        getPrimaryKey().setVideo(video);
    }

    @NotNull
    private Date ratingDate;

    @Enumerated(EnumType.STRING)
    private Rate rate;


}
