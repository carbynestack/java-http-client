/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/java-http-client.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.httpclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.Test;

public class CsResponseHandlerTest {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final Random RANDOM = new Random();
  private static final CsResponseHandler<String, String> RESPONSE_HANDLER =
      CsResponseHandler.of(String.class, String.class);

  private static HttpResponse getHttpResponseForObject(int httpStatus, Object obj) {
    StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, httpStatus, null);
    HttpResponse response = new BasicHttpResponse(statusLine);
    if (obj != null) {
      BasicHttpEntity httpEntity = new BasicHttpEntity();
      String contentJsonString =
          obj instanceof String ? obj.toString() : writeObjectAsJsonString(obj);
      InputStream contentStream =
          obj == ""
              ? new ByteArrayInputStream(new byte[0])
              : new ByteArrayInputStream(contentJsonString.getBytes());
      httpEntity.setContent(contentStream);
      httpEntity.setContentLength(contentJsonString.length());
      response.setEntity(httpEntity);
    }
    return response;
  }

  @SuppressWarnings("unchecked")
  private static <E extends Throwable> String writeObjectAsJsonString(Object obj) throws E {
    try {
      return OBJECT_MAPPER.writeValueAsString(obj);
    } catch (Throwable throwable) {
      throw (E) throwable;
    }
  }

  @Test
  public void givenSuccessfulRequestWithValidBody_whenHandlingResponse_thenReturnSuccessful()
      throws IOException {
    String content = String.valueOf(RANDOM.nextLong());
    int httpStatus = HttpStatus.SC_OK;
    HttpResponse httpResponse = getHttpResponseForObject(httpStatus, content);
    CsResponseEntity<String, String> actualResponse = RESPONSE_HANDLER.handleResponse(httpResponse);
    assertThat(actualResponse.isSuccess()).isTrue();
    assertThat(actualResponse.isFailure()).isFalse();
    assertThat(actualResponse.getContent()).isEqualTo(Either.right(content));
    assertThat(actualResponse.getHttpStatus()).isEqualTo(httpStatus);
    assertThat(actualResponse.get()).isEqualTo(content);
    assertThatThrownBy(actualResponse::getError)
        .isExactlyInstanceOf(CsHttpClientException.class)
        .hasMessageStartingWith("Expected failure but response was successful with status");
  }

  @Test
  public void givenSuccessfulRequestWithNullEntity_whenHandlingResponse_thenReturnSuccessful()
      throws IOException {
    int httpStatus = HttpStatus.SC_OK;
    HttpResponse httpResponse = getHttpResponseForObject(httpStatus, null);
    CsResponseEntity<String, String> actualResponse = RESPONSE_HANDLER.handleResponse(httpResponse);
    assertThat(actualResponse.isSuccess()).isTrue();
    assertThat(actualResponse.getContent()).isEqualTo(Either.right(null));
    assertThat(actualResponse.getHttpStatus()).isEqualTo(httpStatus);
    assertThat(actualResponse.get()).isNull();
  }

  @Test
  public void
      givenSuccessfulRequestWithEmptyContentEntity_whenHandlingResponse_thenReturnSuccessful()
          throws IOException {
    int httpStatus = HttpStatus.SC_OK;
    HttpResponse httpResponse = getHttpResponseForObject(httpStatus, "");
    CsResponseEntity<String, String> actualResponse = RESPONSE_HANDLER.handleResponse(httpResponse);
    assertThat(actualResponse.isSuccess()).isTrue();
    assertThat(actualResponse.getContent()).isEqualTo(Either.right(null));
    assertThat(actualResponse.getHttpStatus()).isEqualTo(httpStatus);
    assertThat(actualResponse.get()).isNull();
  }

  @Test
  public void givenRequestWithHttpFailureCode_whenHandlingResponse_thenReturnFailedResponseEntity()
      throws IOException {
    String failureResponse = "Something went wrong.";
    int httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
    HttpResponse httpResponse = getHttpResponseForObject(httpStatus, failureResponse);
    CsResponseEntity<String, String> actualResponse = RESPONSE_HANDLER.handleResponse(httpResponse);
    assertThat(actualResponse.isFailure()).isTrue();
    assertThat(actualResponse.isSuccess()).isFalse();
    assertThat(actualResponse.getContent()).isEqualTo(Either.left(failureResponse));
    assertThat(actualResponse.getHttpStatus()).isEqualTo(httpStatus);
    assertThat(actualResponse.getError()).isEqualTo(failureResponse);
    assertThatThrownBy(actualResponse::get)
        .isExactlyInstanceOf(CsHttpClientException.class)
        .hasMessageStartingWith("Request has failed with status");
  }

  @Test
  public void
      givenRequestWithHttpFailureCodeWithNullEntity_whenHandlingResponse_thenReturnFailedResponseEntity()
          throws IOException {
    int httpStatus = 100;
    HttpResponse httpResponse = getHttpResponseForObject(httpStatus, null);
    CsResponseEntity<String, String> actualResponse = RESPONSE_HANDLER.handleResponse(httpResponse);
    assertThat(actualResponse.isFailure()).isTrue();
    assertThat(actualResponse.getContent()).isEqualTo(Either.left(null));
    assertThat(actualResponse.getHttpStatus()).isEqualTo(httpStatus);
    assertThat(actualResponse.getError()).isNull();
  }

  @Test
  public void
      givenRequestWithHttpFailureCodeWithEmptyEntity_whenHandlingResponse_thenReturnFailedResponseEntity()
          throws IOException {
    int httpStatus = 100;
    HttpResponse httpResponse = getHttpResponseForObject(httpStatus, "");
    CsResponseEntity<String, String> actualResponse = RESPONSE_HANDLER.handleResponse(httpResponse);
    assertThat(actualResponse.isFailure()).isTrue();
    assertThat(actualResponse.getContent()).isEqualTo(Either.left(null));
    assertThat(actualResponse.getHttpStatus()).isEqualTo(httpStatus);
    assertThat(actualResponse.getError()).isNull();
  }
}
