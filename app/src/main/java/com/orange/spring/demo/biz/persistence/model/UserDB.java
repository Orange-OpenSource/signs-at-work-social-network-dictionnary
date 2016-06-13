package com.orange.spring.demo.biz.persistence.model;

/*
 * #%L
 * Spring demo
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

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
// default constructor only exists for the sake of JPA
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserDB {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @NotNull
  @Column(unique = true)
  private String username;

  @NotNull
  private String firstName;

  @NotNull
  private String lastName;

  @NotNull
  private String email;

  // entity in the company: OLPS/SOFT for instance
  private String entity;

  // for instance: developer, designer, etc
  private String activity;

  private Date lastConnectionDate;

  @NotNull
  @ManyToMany(fetch = FetchType.EAGER)
  private Set<UserRoleDB> userRoles = new HashSet<>();

  @NotNull
  private String passwordHash;

  public UserDB(String username, String passwordHash) {
    this.username = username;
    this.passwordHash = passwordHash;
  }

  public UserDB(String username, String passwordHash, String firstName, String lastName, String email, String entity, String activity) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.entity = entity;
    this.activity = activity;
  }
}
