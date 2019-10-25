package com.orange.signsatwork.biz.persistence.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Calendar;
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

@Table(name = "password_reset_tokens")
@Entity
// default constructor only exists for the sake of JPA
@NoArgsConstructor
@Getter
@Setter
public class PasswordResetTokenDB implements Serializable {

    private static final int EXPIRATION = 60 * 24;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String token;

    @OneToOne(targetEntity = UserDB.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private UserDB user;


    private Date expiryDate;


    public PasswordResetTokenDB(final String token) {
      super();

      this.token = token;
      this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    public PasswordResetTokenDB(final String token, final UserDB user) {
      super();

      this.token = token;
      this.user = user;
      this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    private Date calculateExpiryDate(final int expiryTimeInMinutes) {
      final Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(new Date().getTime());
      cal.add(Calendar.MINUTE, expiryTimeInMinutes);
      return new Date(cal.getTime().getTime());
    }

}
