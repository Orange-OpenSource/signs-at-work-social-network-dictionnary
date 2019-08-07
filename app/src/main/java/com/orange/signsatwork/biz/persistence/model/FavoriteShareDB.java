package com.orange.signsatwork.biz.persistence.model;

import com.orange.signsatwork.biz.domain.Rating;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

@Table(name = "favorite_shares")
@Entity
@AssociationOverrides({
        @AssociationOverride(name = "primaryKey.favorite",
                joinColumns = @JoinColumn(name = "favorite_id")),
        @AssociationOverride(name = "primaryKey.community",
                joinColumns = @JoinColumn(name = "community_id")) })
// default constructor only exists for the sake of JPA
@NoArgsConstructor
@Getter
@Setter
public class FavoriteShareDB implements Serializable {
    @EmbeddedId
    private FavoriteShareDBId primaryKey = new FavoriteShareDBId();

    @Transient
    public CommunityDB getCommunity() {
        return  getPrimaryKey().getCommunity();
    }

    public void setCommunity(CommunityDB community) {
        getPrimaryKey().setCommunity(community);
    }

    @Transient
    public FavoriteDB getFavorite() {
        return  getPrimaryKey().getFavorite();
    }

    public void setFavorite(FavoriteDB favorite) {
        getPrimaryKey().setFavorite(favorite);
    }

    @NotNull
    private Date shareDate;


}
