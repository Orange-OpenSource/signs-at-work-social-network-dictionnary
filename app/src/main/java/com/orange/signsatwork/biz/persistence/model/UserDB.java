package com.orange.signsatwork.biz.persistence.model;

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

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

@Entity
// default constructor only exists for the sake of JPA
@NoArgsConstructor
@Getter
@Setter
public class UserDB {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @NotNull
  @Column(unique = true)
  private String username;

  private String firstName;

  private String lastName;

  private String nameVideo;

  private String email;

  private String entity;

  private String job;

  @Column(length = 1000)
  private String jobTextDescription;

  private String jobVideoDescription;

  private Date lastDeconnectionDate;

  @NotNull
  @ManyToMany(fetch = FetchType.EAGER)
  private Set<UserRoleDB> userRoles = new HashSet<>();

  // we use 'fetch = FetchType.EAGER' to be sure to avoid lazy loading
  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinTable(name = "users_communities", joinColumns = @JoinColumn(name = "users_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "communities_id", referencedColumnName = "id"))
  @JsonManagedReference
  private List<CommunityDB> communities = new ArrayList<>();

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name="user_id", referencedColumnName = "id")
  // we put this annotation because findAll and findOne don't have the same result
  @Fetch(value = FetchMode.SUBSELECT)
  private List<RequestDB> requests = new ArrayList<>();

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name="user_id", referencedColumnName = "id")
  // we put this annotation because findAll and findOne don't have the same result
  @Fetch(value = FetchMode.SUBSELECT)
  private List<FavoriteDB> favorites = new ArrayList<>();

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name="user_id", referencedColumnName = "id")
  // we put this annotation because findAll and findOne don't have the same result
  @Fetch(value = FetchMode.SUBSELECT)
  private List<VideoDB> videos = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  private List<CommentDB> comments = new ArrayList<>();

  @NotNull
  private String passwordHash;

  public UserDB(String username, String passwordHash, String firstName, String lastName, String nameVideo, String email, String entity, String job, String jobTextDescription, String jobVideoDescription) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.firstName = firstName;
    this.lastName = lastName;
    this.nameVideo = nameVideo;
    this.email = email;
    this.entity = entity;
    this.job = job;
    this.jobTextDescription = jobTextDescription;
    this.jobVideoDescription = jobVideoDescription;
  }
}
