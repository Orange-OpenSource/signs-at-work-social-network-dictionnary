package com.orange.signsatwork.biz.webservice.controller;

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

public class RestApi {
  public static final String WS_ROOT = "/ws/";
  public static final String WS_OPEN = WS_ROOT + "open/";
  public static final String WS_SEC = WS_ROOT + "sec/";
  public static final String WS_ADMIN = WS_ROOT + "admin/";

  public static final String WS_SEC_GET_USERS = WS_SEC + "users";
  public static final String WS_ADMIN_USER_CREATE = WS_ADMIN + "user/create";

  public static final String WS_SEC_CLOSE = WS_SEC + "close";

  /** API REST For Android and IOS **/
  public static final String WS_SEC_USER = WS_SEC + "users/me";
  public static final String WS_SEC_MY_VIDEOS = WS_SEC + "users/me/videos";
  public static final String WS_SEC_SIGNS = WS_SEC + "signs";
  public static final String WS_SEC_SIGNS_VIDEOS = WS_SEC + "signs/{signId}/videos";
  public static final String WS_SEC_VIDEOS = WS_SEC + "videos";
  public static final String WS_SEC_VIDEO = WS_SEC + "videos/{videoId}";
  public static final String WS_SEC_MY_REQUESTS = WS_SEC + "users/me/requests";
  public static final String WS_SEC_OTHER_REQUESTS = WS_SEC + "users/other/requests";
  public static final String WS_SEC_REQUEST = WS_SEC + "requests/{requestId}";
  public static final String WS_SEC_REQUEST_CREATE = WS_SEC + "requests";
  public static final String WS_SEC_REQUESTS = WS_SEC + "requests";
  public static final String WS_SEC_MY_FAVORITES = WS_SEC + "users/me/favorites";
  public static final String WS_SEC_FAVORITE = WS_SEC + "favorites/{favoriteId}";
  public static final String WS_SEC_FAVORITES_VIDEOS = WS_SEC + "favorites/{favoriteId}/videos";
  public static final String WS_SEC_FAVORITES = WS_SEC + "favorites";
  public static final String WS_SEC_REQUEST_SIGNS = WS_SEC + "requests/{requestId}/signs";
  public static final String WS_SEC_SIGN_VIDEO = WS_SEC + "signs/{signId}/videos/{videoId}";
  public static final String WS_SEC_COMMENTS = WS_SEC + "videos/{videoId}/comments";
  public static final String WS_SEC_RATINGS = WS_SEC + "videos/{videoId}/ratings";
  public static final String WS_SEC_SIGN = WS_SEC + "signs/{signId}";
  /** Fin API REST For Android and IOS **/

  public static final String WS_OPEN_SIGN = WS_OPEN + "sign/";
  public static final String WS_SEC_SIGN_CREATE = WS_SEC + "sign/create";



  public static final String WS_SEC_REQUEST_RENAME = WS_SEC + "request/{requestId}/rename";

  public static final String WS_SEC_REQUEST_PRIORISE = WS_SEC + "request/{requestId}/priorise";
  public static final String WS_SEC_REQUEST_DELETE = WS_SEC + "request/{requestId}/delete";



  public static final String WS_SEC_RECORDED_VIDEO_FILE_UPLOAD = WS_SEC + "uploadRecordedVideoFile";

  public static final String WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FROM_REQUEST = WS_SEC + "uploadRecordedVideoFile/{requestId}";

  public static final String WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FROM_SIGN = WS_SEC + "uploadRecordedVideoFileFromSign/{signId}/{videoId}";

  public static final String WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_NEW_VIDEO = WS_SEC + "uploadRecordedVideoFileForNewVideo/{signId}";

  public static final String WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_JOB_DESCRIPTION = WS_SEC + "uploadRecordedVideoFileForJobDescription";

  public static final String WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_REQUEST_DESCRIPTION = WS_SEC + "uploadRecordedVideoFileForRequestDescription/{requestId}";

  public static final String WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_SIGN_DEFINITION = WS_SEC + "uploadRecordedVideoFileForSignDefinition/{signId}";

  public static final String WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_NAME = WS_SEC + "uploadRecordedVideoFileForName";

  public static final String WS_SEC_SELECTED_VIDEO_FILE_UPLOAD = WS_SEC + "uploadSelectedVideoFile";

  public static final String WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FROM_REQUEST = WS_SEC + "uploadSelectedVideoFile/{requestId}";

  public static final String WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FROM_SIGN = WS_SEC + "uploadSelectedVideoFileFromSign/{signId}/{videoId}";

  public static final String WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_NEW_VIDEO = WS_SEC + "uploadSelectedVideoFileForNewVideo/{signId}";

  public static final String WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_JOB_DESCRIPTION = WS_SEC + "uploadSelectedVideoFileForJobDescription";

  public static final String WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_REQUEST_DESCRIPTION = WS_SEC + "uploadSelectedVideoFileForRequestDescription/{requestId}";

  public static final String WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_SIGN_DEFINITION = WS_SEC + "uploadSelectedVideoFileForSignDefinition/{signId}";

  public static final String WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_NAME = WS_SEC + "uploadSelectedVideoFileForName";

  public static final String WS_SEC_VIDEO_DELETE = WS_SEC + "sign/{signId}/{videoId}/delete";

  public static final String WS_SEC_VIDEO_RATE_POSITIVE = WS_SEC + "sign/{signId}/{videoId}/rate-positive";

  public static final String WS_SEC_VIDEO_RATE_NEUTRAL = WS_SEC + "sign/{signId}/{videoId}/rate-neutral";

  public static final String WS_SEC_VIDEO_RATE_NEGATIVE = WS_SEC + "sign/{signId}/{videoId}/rate-negative";

  public static final String WS_SEC_VIDEO_ASSOCIATE = WS_SEC + "sign/{signId}/{videoId}/associate";

  public static final String WS_SEC_FAVORITE_VIDEO_ASSOCIATE = WS_SEC + "favorite/{favoriteId}/add/videos";
}
