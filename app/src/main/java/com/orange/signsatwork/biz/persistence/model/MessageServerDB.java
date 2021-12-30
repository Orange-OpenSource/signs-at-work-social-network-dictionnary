package com.orange.signsatwork.biz.persistence.model;

import com.orange.signsatwork.biz.domain.ActionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

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

@Table(name = "messages_server")
@Entity
// default constructor only exists for the sake of JPA
@NoArgsConstructor
@Getter
@Setter
public class MessageServerDB implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private Date date;

    @NotNull
    private String type;

    private String val;

    @Enumerated(EnumType.STRING)
    private ActionType action;

    public MessageServerDB(Date date, String type, String values, ActionType action) {
        this.date = date;this.type = type;this.val = values;this.action=action;
    }

}
