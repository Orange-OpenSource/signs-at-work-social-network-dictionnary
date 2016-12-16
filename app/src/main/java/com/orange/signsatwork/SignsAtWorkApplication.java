package com.orange.signsatwork;

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

import com.orange.signsatwork.biz.storage.StorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@Slf4j
@Configuration
@EnableAutoConfiguration
@ComponentScan
@EnableConfigurationProperties(StorageProperties.class)
public class SignsAtWorkApplication extends WebMvcConfigurerAdapter {

  public static void main(String[] args) {
    SpringApplication.run(SignsAtWorkApplication.class, args);
  }

  @Bean
  /** Use the user's browser preferred language for translation */
  public LocaleResolver localeResolver() {
    AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
    localeResolver.setDefaultLocale(Locale.UK);
    return localeResolver;
  }

  @Override
  /** Enable static resources cache control: images, css, fonts, js (app & libraries) */
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
      .addResourceHandler(
        "/**",
        "/webjars/**")
      .addResourceLocations(
        "classpath:/public/",
        "classpath:/META-INF/resources/webjars/")
      .setCacheControl(
        CacheControl.
          maxAge(1, TimeUnit.DAYS)
          .cachePublic()
          .mustRevalidate()
      );
  }
}
