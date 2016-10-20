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

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpsRedirect {

  @Bean
  @Autowired
  public EmbeddedServletContainerFactory servletContainer(AppProfile appProfile) {
    boolean https = appProfile.isHttps();

    TomcatEmbeddedServletContainerFactory tomcat =
      new TomcatEmbeddedServletContainerFactory() {
        @Override
        protected void postProcessContext(Context context) {
          if (https) {
            addSecurityConstraint(context);
          }
        }
      };
    if (https) {
      tomcat.addAdditionalTomcatConnectors(createHttpConnector());
    }
    return tomcat;
  }

  private void addSecurityConstraint(Context context) {
    SecurityConstraint securityConstraint = new SecurityConstraint();
    securityConstraint.setUserConstraint("CONFIDENTIAL");
    SecurityCollection collection = new SecurityCollection();
    collection.addPattern("/*");
    securityConstraint.addCollection(collection);
    context.addConstraint(securityConstraint);
  }

  private Connector createHttpConnector() {
    Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
    connector.setScheme("http");
    connector.setSecure(false);
    connector.setPort(8080);
    connector.setRedirectPort(8443);
    return connector;
  }
}
