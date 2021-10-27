/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/java-http-client.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.httpclient;

import static org.assertj.core.api.Assertions.assertThat;

import net.bytebuddy.utility.RandomString;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;

public class BearerTokenUtilsTest {

  @Test
  public void givenToken_whenCreating_thenReturnCorrectHeader() {
    String token = RandomString.make();
    Header h = BearerTokenUtils.createBearerToken(token);
    assertThat(h.getName())
        .as("incorrect authorization header type")
        .isEqualTo(HttpHeaders.AUTHORIZATION);
    String expectedHeaderValue = String.format("Bearer %s", token);
    assertThat(h.getValue())
        .as("incorrect authorization header value")
        .isEqualTo(expectedHeaderValue);
  }
}
