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

public class Proxy {
  public final String proxyHost;
  public final int proxyPort;
  public final boolean noProxy;

  public Proxy(String proxyHost, String proxyPort) {
    this.proxyHost = proxyHost;
    noProxy = proxyHost == null || proxyHost.isEmpty();
    this.proxyPort = noProxy ?
            0 :
            Integer.parseInt(proxyPort);
  }
}
