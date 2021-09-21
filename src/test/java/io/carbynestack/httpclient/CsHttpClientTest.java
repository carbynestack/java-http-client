/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/java-http-client.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.httpclient;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CsHttpClientTest {

  private static final String DUMMY_URL = "https://cs-virtual-cloud.com/";
  private static final String DUMMY_BODY = "payload";
  private static final String DUMMY_CONTENT = "response";
  private static final String EXCEPTION_MESSAGE = "exception message";

  @Mock private CloseableHttpClient closeableHttpClient;

  private final CsHttpClient<Void> csHttpClient =
      new CsHttpClient<>(() -> closeableHttpClient, Void.class);

  @Test
  public void
      givenThrowingRequest_whenGettingObject_thenThrowsCsHttpClientExceptionWithUnderlyingCause()
          throws IOException, URISyntaxException {
    URI uri = new URI(DUMMY_URL);
    IOException cause = new IOException(EXCEPTION_MESSAGE);
    when(closeableHttpClient.execute(any(HttpGet.class), any(CsResponseHandler.class)))
        .thenThrow(cause);
    CsHttpClientException sce =
        assertThrows(
            CsHttpClientException.class, () -> csHttpClient.getForObject(uri, String.class));
    verify(closeableHttpClient, times(1)).execute(any(HttpGet.class), any(CsResponseHandler.class));
    assertEquals(sce.getCause(), cause);
  }

  @Test
  public void givenSuccessfulRequest_whenGettingObject_thenReturnsExpectedContent()
      throws IOException, URISyntaxException {
    URI uri = new URI(DUMMY_URL);
    CsResponseEntity<Void, String> response =
        CsResponseEntity.success(HttpStatus.SC_OK, DUMMY_CONTENT);
    when(closeableHttpClient.execute(any(HttpGet.class), any(CsResponseHandler.class)))
        .thenReturn(response);
    assertEquals(DUMMY_CONTENT, csHttpClient.getForObject(uri, String.class));
    verify(closeableHttpClient, times(1)).execute(any(HttpGet.class), any(CsResponseHandler.class));
  }

  @Test
  public void
      givenRequestFailingWithInternalServerError_whenGettingObject_thenThrowsCsHttpClientExceptionWithExpectedMessage()
          throws IOException, URISyntaxException {
    URI uri = new URI(DUMMY_URL);
    CsResponseEntity<String, String> response =
        CsResponseEntity.failed(HttpStatus.SC_INTERNAL_SERVER_ERROR, DUMMY_CONTENT);
    when(closeableHttpClient.execute(any(HttpGet.class), any(CsResponseHandler.class)))
        .thenReturn(response);
    CsHttpClientException sce =
        assertThrows(
            CsHttpClientException.class, () -> csHttpClient.getForObject(uri, String.class));
    verify(closeableHttpClient, times(1)).execute(any(HttpGet.class), any(CsResponseHandler.class));
    assertThat(sce.getMessage(), containsString("Request has failed"));
    assertThat(
        sce.getMessage(), containsString(Integer.toString(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
    assertThat(sce.getMessage(), containsString(DUMMY_CONTENT));
  }

  @Test
  public void givenSuccessfulRequest_whenGettingEntity_thenReturnsExpectedContent()
      throws IOException, URISyntaxException {
    URI uri = new URI(DUMMY_URL);
    CsResponseEntity<Void, String> response =
        CsResponseEntity.success(HttpStatus.SC_OK, DUMMY_CONTENT);
    when(closeableHttpClient.execute(any(HttpGet.class), any(CsResponseHandler.class)))
        .thenReturn(response);
    assertEquals(response, csHttpClient.getForEntity(uri, String.class));
    verify(closeableHttpClient, times(1)).execute(any(HttpGet.class), any(CsResponseHandler.class));
  }

  @Test
  public void
      givenThrowingRequest_whenGettingEntity_thenThrowsCsHttpClientExceptionWithUnderlyingCause()
          throws URISyntaxException, IOException {
    URI uri = new URI(DUMMY_URL);
    IOException cause = new IOException(EXCEPTION_MESSAGE);
    when(closeableHttpClient.execute(any(HttpGet.class), any(CsResponseHandler.class)))
        .thenThrow(cause);
    CsHttpClientException sce =
        assertThrows(
            CsHttpClientException.class, () -> csHttpClient.getForEntity(uri, String.class));
    verify(closeableHttpClient, times(1)).execute(any(HttpGet.class), any(CsResponseHandler.class));
    assertEquals(sce.getCause(), cause);
  }

  @Test
  public void
      givenThrowingRequest_whenPostingEntity_thenThrowsCsHttpClientExceptionWithUnderlyingCause()
          throws URISyntaxException, IOException {
    URI uri = new URI(DUMMY_URL);
    IOException cause = new IOException(EXCEPTION_MESSAGE);
    when(closeableHttpClient.execute(any(HttpPost.class), any(CsResponseHandler.class)))
        .thenThrow(cause);
    CsHttpClientException sce =
        assertThrows(
            CsHttpClientException.class,
            () -> csHttpClient.postForEntity(uri, DUMMY_BODY, String.class));
    verify(closeableHttpClient, times(1))
        .execute(any(HttpPost.class), any(CsResponseHandler.class));
    assertEquals(sce.getCause(), cause);
  }

  @Test
  public void givenSuccessfulRequest_whenPostingObject_thenReturnsExpectedContent()
      throws IOException, URISyntaxException {
    URI uri = new URI(DUMMY_URL);
    CsResponseEntity<Void, String> response =
        CsResponseEntity.success(HttpStatus.SC_OK, DUMMY_CONTENT);
    when(closeableHttpClient.execute(any(HttpPost.class), any(CsResponseHandler.class)))
        .thenReturn(response);
    assertEquals(DUMMY_CONTENT, csHttpClient.postForObject(uri, DUMMY_BODY, String.class));
    verify(closeableHttpClient, times(1))
        .execute(any(HttpPost.class), any(CsResponseHandler.class));
  }

  @Test
  public void givenSuccessfulRequest_whenPostingEntity_thenReturnsExpectedContent()
      throws URISyntaxException, IOException {
    URI uri = new URI(DUMMY_URL);
    CsResponseEntity<Void, String> response =
        CsResponseEntity.success(HttpStatus.SC_OK, DUMMY_CONTENT);
    when(closeableHttpClient.execute(any(HttpPost.class), any(CsResponseHandler.class)))
        .thenReturn(response);
    assertEquals(response, csHttpClient.postForEntity(uri, DUMMY_BODY, String.class));
    verify(closeableHttpClient, times(1))
        .execute(any(HttpPost.class), any(CsResponseHandler.class));
  }

  @Test
  public void
      givenThrowingRequest_whenInvokingPut_thenThrowsCsHttpClientExceptionWithUnderlyingCause()
          throws URISyntaxException, IOException {
    URI uri = new URI(DUMMY_URL);
    IOException cause = new IOException(EXCEPTION_MESSAGE);
    when(closeableHttpClient.execute(any(HttpPut.class), any(CsResponseHandler.class)))
        .thenThrow(cause);
    CsHttpClientException sce =
        assertThrows(CsHttpClientException.class, () -> csHttpClient.put(uri, DUMMY_BODY));
    verify(closeableHttpClient, times(1)).execute(any(HttpPut.class), any(CsResponseHandler.class));
    assertEquals(sce.getCause(), cause);
  }

  @Test
  public void givenSuccessfulRequest_whenInvokingPut_thenEmitsPutRequest()
      throws URISyntaxException, IOException {
    URI uri = new URI(DUMMY_URL);
    CsResponseEntity<String, Void> response = CsResponseEntity.success(HttpStatus.SC_OK, null);
    when(closeableHttpClient.execute(any(HttpPut.class), any(CsResponseHandler.class)))
        .thenReturn(response);
    csHttpClient.put(uri, DUMMY_BODY);
    verify(closeableHttpClient, times(1)).execute(any(HttpPut.class), any(CsResponseHandler.class));
  }

  @Test
  public void
      givenRequestFailingWithInternalServerError_whenInvokingPut_thenThrowsCsHttpClientExceptionWithExpectedMessage()
          throws URISyntaxException, IOException {
    URI uri = new URI(DUMMY_URL);
    CsResponseEntity<String, Void> response =
        CsResponseEntity.failed(HttpStatus.SC_INTERNAL_SERVER_ERROR, null);
    when(closeableHttpClient.execute(any(HttpPut.class), any(CsResponseHandler.class)))
        .thenReturn(response);
    CsHttpClientException sce =
        assertThrows(CsHttpClientException.class, () -> csHttpClient.put(uri, DUMMY_BODY));
    verify(closeableHttpClient, times(1)).execute(any(HttpPut.class), any(CsResponseHandler.class));
    assertThat(sce.getMessage(), containsString("PUT request failed"));
    assertThat(
        sce.getCause().getMessage(),
        containsString(Integer.toString(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
  }

  @Test
  public void
      givenThrowingRequest_whenInvokingDelete_thenThrowsCsHttpClientExceptionWithUnderlyingCause()
          throws URISyntaxException, IOException {
    URI uri = new URI(DUMMY_URL);
    IOException cause = new IOException(EXCEPTION_MESSAGE);
    when(closeableHttpClient.execute(any(HttpDelete.class), any(CsResponseHandler.class)))
        .thenThrow(cause);
    CsHttpClientException sce =
        assertThrows(CsHttpClientException.class, () -> csHttpClient.delete(uri));
    verify(closeableHttpClient, times(1))
        .execute(any(HttpDelete.class), any(CsResponseHandler.class));
    assertEquals(sce.getCause(), cause);
  }

  @Test
  public void givenSuccessfulRequest_whenInvokingDelete_thenEmitsDeleteRequest()
      throws URISyntaxException, IOException {
    URI uri = new URI(DUMMY_URL);
    CsResponseEntity<Void, Void> response = CsResponseEntity.success(HttpStatus.SC_OK, null);
    when(closeableHttpClient.execute(any(HttpDelete.class), any(CsResponseHandler.class)))
        .thenReturn(response);
    csHttpClient.delete(uri);
    verify(closeableHttpClient, times(1))
        .execute(any(HttpDelete.class), any(CsResponseHandler.class));
  }

  @Test
  public void
      givenRequestFailingWithInternalServerError_whenInvokingDelete_thenThrowsCsHttpClientExceptionWithExpectedMessage()
          throws URISyntaxException, IOException {
    URI uri = new URI(DUMMY_URL);
    CsResponseEntity<Void, Void> response =
        CsResponseEntity.failed(HttpStatus.SC_INTERNAL_SERVER_ERROR, null);
    when(closeableHttpClient.execute(any(HttpDelete.class), any(CsResponseHandler.class)))
        .thenReturn(response);
    CsHttpClientException sce =
        assertThrows(CsHttpClientException.class, () -> csHttpClient.delete(uri));
    verify(closeableHttpClient, times(1))
        .execute(any(HttpDelete.class), any(CsResponseHandler.class));
    assertThat(sce.getMessage(), containsString("DELETE request failed"));
    assertThat(
        sce.getCause().getMessage(),
        containsString(Integer.toString(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
  }
}
