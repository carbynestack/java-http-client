/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/java-http-client.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.httpclient;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import lombok.SneakyThrows;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class CsResponseHandlerTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final Random RANDOM = new Random();
  private static final CsResponseHandler<String, String> RESPONSE_HANDLER =
      CsResponseHandler.of(String.class, String.class);

  @Test
  public void givenSuccessfulRequestWithValidBody_whenHandlingResponse_thenReturnSuccessful()
      throws IOException {
    String content = String.valueOf(RANDOM.nextLong());
    int httpStatus = HttpStatus.SC_OK;
    HttpResponse httpResponse = getHttpResponseForObject(httpStatus, content);
    CsResponseEntity<String, String> actualResponse = RESPONSE_HANDLER.handleResponse(httpResponse);
    assertThat(actualResponse.isSuccess(), is(true));
    assertThat(actualResponse.isFailure(), is(false));
    assertThat(actualResponse.getContent(), equalTo(Either.right(content)));
    assertEquals(httpStatus, actualResponse.getHttpStatus());
    assertEquals(content, actualResponse.get());
    CsHttpClientException sce = assertThrows(CsHttpClientException.class, actualResponse::getError);
    assertThat(
        sce.getMessage(),
        CoreMatchers.startsWith("Expected failure but response was successful with status"));
  }

  @Test
  public void givenSuccessfulRequestWithNullEntity_whenHandlingResponse_thenReturnSuccessful()
      throws IOException {
    int httpStatus = HttpStatus.SC_OK;
    HttpResponse httpResponse = getHttpResponseForObject(httpStatus, null);
    CsResponseEntity<String, String> actualResponse = RESPONSE_HANDLER.handleResponse(httpResponse);
    assertThat(actualResponse.isSuccess(), is(true));
    assertThat(actualResponse.getContent(), equalTo(Either.right(null)));
    assertEquals(httpStatus, actualResponse.getHttpStatus());
    assertNull(actualResponse.get());
  }

  @Test
  public void
      givenSuccessfulRequestWithEmptyContentEntity_whenHandlingResponse_thenReturnSuccessful()
          throws IOException {
    int httpStatus = HttpStatus.SC_OK;
    HttpResponse httpResponse = getHttpResponseForObject(httpStatus, "");
    CsResponseEntity<String, String> actualResponse = RESPONSE_HANDLER.handleResponse(httpResponse);
    assertThat(actualResponse.isSuccess(), is(true));
    assertThat(actualResponse.getContent(), equalTo(Either.right(null)));
    assertEquals(httpStatus, actualResponse.getHttpStatus());
    assertNull(actualResponse.get());
  }

  @Test
  public void givenRequestWithHttpFailureCode_whenHandlingResponse_thenReturnFailedResponseEntity()
      throws IOException {
    String failureResponse = "Something went wrong.";
    int httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
    HttpResponse httpResponse = getHttpResponseForObject(httpStatus, failureResponse);
    CsResponseEntity<String, String> actualResponse = RESPONSE_HANDLER.handleResponse(httpResponse);
    assertThat(actualResponse.isFailure(), is(true));
    assertThat(actualResponse.isSuccess(), is(false));
    assertThat(actualResponse.getContent(), equalTo(Either.left(failureResponse)));
    assertEquals(httpStatus, actualResponse.getHttpStatus());
    assertEquals(failureResponse, actualResponse.getError());
    CsHttpClientException sce = assertThrows(CsHttpClientException.class, actualResponse::get);
    assertThat(sce.getMessage(), CoreMatchers.startsWith("Request has failed with status"));
  }

  @Test
  public void
      givenRequestWithHttpFailureCodeWithNullEntity_whenHandlingResponse_thenReturnFailedResponseEntity()
          throws IOException {
    int httpStatus = 100;
    HttpResponse httpResponse = getHttpResponseForObject(httpStatus, null);
    CsResponseEntity<String, String> actualResponse = RESPONSE_HANDLER.handleResponse(httpResponse);
    assertThat(actualResponse.isFailure(), is(true));
    assertThat(actualResponse.getContent(), equalTo(Either.left(null)));
    assertEquals(httpStatus, actualResponse.getHttpStatus());
    assertNull(actualResponse.getError());
  }

  @Test
  public void
      givenRequestWithHttpFailureCodeWithEmptyEntity_whenHandlingResponse_thenReturnFailedResponseEntity()
          throws IOException {
    int httpStatus = 100;
    HttpResponse httpResponse = getHttpResponseForObject(httpStatus, "");
    CsResponseEntity<String, String> actualResponse = RESPONSE_HANDLER.handleResponse(httpResponse);
    assertThat(actualResponse.isFailure(), is(true));
    assertThat(actualResponse.getContent(), equalTo(Either.left(null)));
    assertEquals(httpStatus, actualResponse.getHttpStatus());
    assertNull(actualResponse.getError());
  }

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

  @SneakyThrows
  private static String writeObjectAsJsonString(Object obj) {
    return OBJECT_MAPPER.writeValueAsString(obj);
  }
}
