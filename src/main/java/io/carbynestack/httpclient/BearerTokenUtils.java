/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/java-http-client.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.httpclient;

import lombok.experimental.UtilityClass;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;

@UtilityClass
public class BearerTokenUtils {

  private final String BEARER_PREFIX = "Bearer";

  public Header createBearerToken(String token) {
    return new BasicHeader(HttpHeaders.AUTHORIZATION, String.format("%s %s", BEARER_PREFIX, token));
  }
}
