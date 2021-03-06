package com.orange.signsatwork.biz.domain;

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

public class FileUploadDailymotion {
    public String upload_url;
    public String acodec;
    public String bitrate;
    public String dimension;
    public long duration;
    public String format;
    public String hash;
    public String name;
    public String seal;
    public long size;
    public String streamable;
    public String url;
    public String vcodec;

    public String toString() {
      return "upload_url "+upload_url+ " acodec "+acodec+ " bitrate "+bitrate+" dimension "+dimension+" duration "+duration+" format "+format+" hash "+hash+" name "+name+" seal "+seal+ "size "+size+" stremable "+streamable
        +" url "+url+" vodec "+vcodec;
    }
}
