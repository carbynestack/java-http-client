/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/java-http-client.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.httpclient;

import static org.junit.Assert.assertEquals;

import net.bytebuddy.utility.RandomString;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.junit.Test;

public class BearerTokenUtilsTest {

  @Test
  public void givenToken_whenCreating_thenReturnCorrectHeader() {
    String token = RandomString.make();
    Header h = BearerTokenUtils.createBearerToken(token);
    assertEquals("incorrect authorization header type", HttpHeaders.AUTHORIZATION, h.getName());
    String expectedHeaderValue = String.format("Bearer %s", token);
    assertEquals("incorrect authorization header value", expectedHeaderValue, h.getValue());
  }
}
