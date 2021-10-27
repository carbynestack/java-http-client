/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/java-http-client.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.httpclient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Objects;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class SslValidationIT {
  private static final String KEY_STORE_A_PATH =
      Objects.requireNonNull(SslValidationIT.class.getClassLoader().getResource("keyStoreA.jks"))
          .getPath();
  private static final String KEY_STORE_A_PASSWORD = "verysecure";
  private static final String GOOGLE_DIRECTIONS_REST_URI =
      "https://maps.googleapis.com/maps/api/directions/json";
  private static final String TEST_ENDPOINT = "/test";
  private static final String SUCCESS_RESPONSE_STRING = "success";

  @RegisterExtension
  static WireMockExtension wireMockExtension =
      WireMockExtension.newInstance()
          .options(
              wireMockConfig()
                  .dynamicPort()
                  .dynamicHttpsPort()
                  .keystorePath(KEY_STORE_A_PATH)
                  .keystorePassword(KEY_STORE_A_PASSWORD)
                  .keyManagerPassword(KEY_STORE_A_PASSWORD))
          .build();

  private final ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  public void initialize() throws Exception {
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, null, null);
    SSLContext.setDefault(sslContext);
    if (initialized) {
      return;
    }
    wireMockExtension.stubFor(
        get(urlPathEqualTo(TEST_ENDPOINT))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mapper.writeValueAsString(SUCCESS_RESPONSE_STRING))));
    testUri =
        new URI(
            String.format(
                "https://localhost:%s%s",
                wireMockExtension.getRuntimeInfo().getHttpsPort(), TEST_ENDPOINT));
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
    assertThat(csHttpClient.getForObject(testUri, String.class)).isEqualTo(SUCCESS_RESPONSE_STRING);
  }

  @Test
  public void givenSslValidationDisabled_whenGettingObject_thenSucceeds()
      throws CsHttpClientException {
    CsHttpClient<String> csHttpClient =
        CsHttpClient.<String>builder()
            .withFailureType(String.class)
            .withoutSslValidation(true)
            .build();
    assertThat(csHttpClient.getForObject(testUri, String.class)).isEqualTo(SUCCESS_RESPONSE_STRING);
  }

  @Test
  public void givenNonExistingTrustStore_whenBuildingClient_thenThrows() {
    File nonExistingFile = new File("");
    assertThatThrownBy(
            () ->
                CsHttpClient.<String>builder()
                    .withFailureType(String.class)
                    .withTrustedCertificates(Collections.singletonList(nonExistingFile))
                    .build())
        .isExactlyInstanceOf(CsHttpClientException.class)
        .hasCauseInstanceOf(IOException.class);
  }

  @Test
  public void givenUntrustedCertificate_whenGettingObject_thenThrows() {
    CsHttpClient<String> csHttpClient = CsHttpClient.createDefault();
    assertThatThrownBy(() -> csHttpClient.getForObject(testUri, String.class))
        .isExactlyInstanceOf(CsHttpClientException.class)
        .hasCauseExactlyInstanceOf(SSLHandshakeException.class);
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
    assertThat(csResponseEntity.isFailure())
        .as("Although the certificate was trustworthy, the request is supposed to return an error.")
        .isTrue();
    assertThat(csResponseEntity.getError()).isNotNull();
    assertThat(csResponseEntity.getHttpStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
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
    assertThat(csResponseEntity.isFailure())
        .as("Although the certificate was trustworthy, the request is supposed to return an error.")
        .isTrue();
    assertThat(csResponseEntity.getError()).isNotNull();
    assertThat(csResponseEntity.getHttpStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class GoogleDirectionsResponse {
    String error_message;
  }
}
