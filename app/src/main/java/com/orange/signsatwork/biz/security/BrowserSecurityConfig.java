package com.orange.signsatwork.biz.security;

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

import com.orange.signsatwork.AppProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

import java.util.Arrays;

@Slf4j
@Configuration
public class BrowserSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private AppProfile appProfile;
  @Autowired
  private Environment environment;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    configureRoutes(http);
  }

  private void configureRoutes(HttpSecurity http) throws Exception {
    String fileDirectory =  environment.getProperty("app.file");

    disableSecurityOnWebJars(http);
    disableSecurityOnAssets(http);
    disableSecForDBConsole(http);
    securyFilterChain(http);

    http.csrf().disable()
            // configure the HttpSecurity to only be invoked when matching the provided ant pattern
            .antMatcher("/**")
            // configure restricting access
            .authorizeRequests()
            // open api is... opened
            .antMatchers(fileDirectory+ "/**", "/signs/**","/articles", "/cgu", "/about-cgu-lsf", "/personal-data-lsf", "/sendMail", "/forgetPassword", "/user/createPassword*", "/user/changePassword*", "/user/*/savePassword").permitAll()
//            .antMatchers("/", "/signs/**", "/sign/**").permitAll()
            // admin api restricted to... ADMIN
            .antMatchers("/sec/admin/**").hasRole("ADMIN")
            // and the rest is allowed by any authenticated user
            .anyRequest().authenticated()
            .and()
            // setup login & logout
            .formLogin()
            .loginPage("/login")
            // always redirect to home after login   .defaultSuccessUrl("/", true)
            .permitAll()
            .and()
            .logout()
            .logoutSuccessUrl("/")
            .permitAll();
  }

  private void disableSecurityOnWebJars(HttpSecurity http) throws Exception {
    http.authorizeRequests().antMatchers("/webjars/**").permitAll();
  }

  private void disableSecurityOnAssets(HttpSecurity http) throws Exception {
    http.authorizeRequests().antMatchers("/doc/**", "/css/**", "/js/**","/img/**", "/font/**","/files/**", "/.well-known/**", "/manifest/**", "/pwabuilder-sw.js").permitAll();
  }

  private void disableSecForDBConsole(HttpSecurity http) throws Exception {
    if (appProfile.isDevProfile()) {
      log.warn("Disable security to allow H2 console");
      String url = "/h2-console/**";
      http.csrf().ignoringAntMatchers(url);
      http.authorizeRequests().antMatchers(url).permitAll();
      http.headers().frameOptions().disable();
    }
  }

  private void securyFilterChain(HttpSecurity http) throws Exception {
    http.headers()
      .xssProtection()
      .and()
      .httpStrictTransportSecurity()
      .includeSubDomains(true)
      .maxAgeInSeconds(31536000)
      .and()
      .contentSecurityPolicy("script-src 'self' 'unsafe-inline' 'report-sample'  https://*.googleapis.com https://*.gstatic.com *.google.com *.googleusercontent.com data: blob: https://cdn.jsdelivr.net https://code.jquery.com");
  }
}
