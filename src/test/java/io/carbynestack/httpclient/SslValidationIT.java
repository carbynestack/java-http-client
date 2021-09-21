/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/java-http-client.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.httpclient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Objects;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import lombok.NoArgsConstructor;
import org.apache.http.HttpStatus;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SslValidationIT {

  private static final String KEY_STORE_A_PATH =
      Objects.requireNonNull(SslValidationIT.class.getClassLoader().getResource("keyStoreA.jks"))
          .getPath();
  private static final String KEY_STORE_A_PASSWORD = "verysecure";
  private static final String GOOGLE_DIRECTIONS_REST_URI =
      "https://maps.googleapis.com/maps/api/directions/json";
  private static final String TEST_ENDPOINT = "/test";
  private static final String SUCCESS_RESPONSE_STRING = "success";

  private final ObjectMapper mapper = new ObjectMapper();
  private boolean initialized = false;
  private URI testUri;

  @Rule
  public WireMockRule wireMockRule =
      new WireMockRule(
          wireMockConfig()
              .dynamicPort()
              .dynamicHttpsPort()
              .keystorePath(KEY_STORE_A_PATH)
              .keystorePassword(KEY_STORE_A_PASSWORD));

  @Before
  public void initialize() throws Exception {
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, null, null);
    SSLContext.setDefault(sslContext);
    if (initialized) {
      return;
    }
    wireMockRule.stubFor(
        get(urlPathEqualTo(TEST_ENDPOINT))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mapper.writeValueAsString(SUCCESS_RESPONSE_STRING))));
    testUri =
        new URI(String.format("https://localhost:%s%s", wireMockRule.httpsPort(), TEST_ENDPOINT));
    initialized = true;
  }

  @Test
  public void givenTrustedCertificate_whenGettingObject_thenSucceeds()
      throws CsHttpClientException {
    CsHttpClient<String> csHttpClient =
        CsHttpClient.<String>builder()
            .withFailureType(String.class)
            .withTrustedCertificates(Collections.singletonList(new File(KEY_STORE_A_PATH)))
            .build();
    assertEquals(SUCCESS_RESPONSE_STRING, csHttpClient.getForObject(testUri, String.class));
  }

  @Test
  public void givenSslValidationDisabled_whenGettingObject_thenSucceeds()
      throws CsHttpClientException {
    CsHttpClient<String> csHttpClient =
        CsHttpClient.<String>builder()
            .withFailureType(String.class)
            .withoutSslValidation(true)
            .build();
    assertEquals(SUCCESS_RESPONSE_STRING, csHttpClient.getForObject(testUri, String.class));
  }

  @Test
  public void givenNonExistingTrustStore_whenBuildingClient_thenThrows() {
    File nonExistingFile = new File("");
    CsHttpClientException sce =
        assertThrows(
            CsHttpClientException.class,
            () ->
                CsHttpClient.<String>builder()
                    .withFailureType(String.class)
                    .withTrustedCertificates(Collections.singletonList(nonExistingFile))
                    .build());
    assertThat(sce.getCause(), CoreMatchers.instanceOf(IOException.class));
  }

  @Test
  public void givenUntrustedCertificate_whenGettingObject_thenThrows() {
    CsHttpClient<String> csHttpClient = CsHttpClient.createDefault();
    CsHttpClientException sce =
        assertThrows(
            CsHttpClientException.class, () -> csHttpClient.getForObject(testUri, String.class));
    assertThat(sce.getCause(), CoreMatchers.instanceOf(SSLHandshakeException.class));
  }

  @Test
  public void givenOfficiallyTrustedTarget_whenGettingEntity_thenSucceedWithBadRequest()
      throws CsHttpClientException, URISyntaxException {
    CsHttpClient<GoogleDirectionsResponse> csHttpClient =
        CsHttpClient.<GoogleDirectionsResponse>builder()
            .withFailureType(GoogleDirectionsResponse.class)
            .build();
    CsResponseEntity<GoogleDirectionsResponse, String> csResponseEntity =
        csHttpClient.getForEntity(new URI(GOOGLE_DIRECTIONS_REST_URI), String.class);
    assertTrue(
        "Although the certificate was trustworthy, the request is supposed to return an error.",
        csResponseEntity.isFailure());
    assertNotNull(csResponseEntity.getError());
    assertEquals(HttpStatus.SC_BAD_REQUEST, csResponseEntity.getHttpStatus());
  }

  @Test
  public void
      givenOfficiallyTrustedTargetAndCustomTruststore_whenGettingEntity_thenSucceedWithBadRequest()
          throws CsHttpClientException, URISyntaxException {
    CsHttpClient<GoogleDirectionsResponse> csHttpClient =
        CsHttpClient.<GoogleDirectionsResponse>builder()
            .withFailureType(GoogleDirectionsResponse.class)
            .withTrustedCertificates(Collections.singletonList(new File(KEY_STORE_A_PATH)))
            .build();
    CsResponseEntity<GoogleDirectionsResponse, String> csResponseEntity =
        csHttpClient.getForEntity(new URI(GOOGLE_DIRECTIONS_REST_URI), String.class);
    assertTrue(
        "Although the certificate was trustworthy, the request is supposed to return an error.",
        csResponseEntity.isFailure());
    assertNotNull(csResponseEntity.getError());
    assertEquals(HttpStatus.SC_BAD_REQUEST, csResponseEntity.getHttpStatus());
  }

  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class GoogleDirectionsResponse {
    String error_message;
  }
}
