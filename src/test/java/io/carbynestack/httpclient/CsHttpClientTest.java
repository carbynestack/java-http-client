/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/java-http-client.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.httpclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
    assertThatThrownBy(() -> csHttpClient.getForObject(uri, String.class))
        .isExactlyInstanceOf(CsHttpClientException.class)
        .hasCause(cause);
    verify(closeableHttpClient, times(1)).execute(any(HttpGet.class), any(CsResponseHandler.class));
  }

  @Test
  public void givenSuccessfulRequest_whenGettingObject_thenReturnsExpectedContent()
      throws IOException, URISyntaxException {
    URI uri = new URI(DUMMY_URL);
    CsResponseEntity<Void, String> response =
        CsResponseEntity.success(HttpStatus.SC_OK, DUMMY_CONTENT);
    when(closeableHttpClient.execute(any(HttpGet.class), any(CsResponseHandler.class)))
        .thenReturn(response);
    assertThat(csHttpClient.getForObject(uri, String.class)).isEqualTo(DUMMY_CONTENT);
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
    assertThatThrownBy(() -> csHttpClient.getForObject(uri, String.class))
        .isExactlyInstanceOf(CsHttpClientException.class)
        .hasMessageContaining("Request has failed")
        .hasMessageContaining(Integer.toString(HttpStatus.SC_INTERNAL_SERVER_ERROR))
        .hasMessageContaining(DUMMY_CONTENT);
    verify(closeableHttpClient, times(1)).execute(any(HttpGet.class), any(CsResponseHandler.class));
  }

  @Test
  public void givenSuccessfulRequest_whenGettingEntity_thenReturnsExpectedContent()
      throws IOException, URISyntaxException {
    URI uri = new URI(DUMMY_URL);
    CsResponseEntity<Void, String> response =
        CsResponseEntity.success(HttpStatus.SC_OK, DUMMY_CONTENT);
    when(closeableHttpClient.execute(any(HttpGet.class), any(CsResponseHandler.class)))
        .thenReturn(response);
    assertThat(csHttpClient.getForEntity(uri, String.class)).isEqualTo(response);
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
    assertThatThrownBy(() -> csHttpClient.getForEntity(uri, String.class))
        .isExactlyInstanceOf(CsHttpClientException.class)
        .hasCause(cause);
    verify(closeableHttpClient, times(1)).execute(any(HttpGet.class), any(CsResponseHandler.class));
  }

  @Test
  public void
      givenThrowingRequest_whenPostingEntity_thenThrowsCsHttpClientExceptionWithUnderlyingCause()
          throws URISyntaxException, IOException {
    URI uri = new URI(DUMMY_URL);
    IOException cause = new IOException(EXCEPTION_MESSAGE);
    when(closeableHttpClient.execute(any(HttpPost.class), any(CsResponseHandler.class)))
        .thenThrow(cause);
    assertThatThrownBy(() -> csHttpClient.postForEntity(uri, DUMMY_BODY, String.class))
        .isExactlyInstanceOf(CsHttpClientException.class)
        .hasCause(cause);
    verify(closeableHttpClient, times(1))
        .execute(any(HttpPost.class), any(CsResponseHandler.class));
  }

  @Test
  public void givenSuccessfulRequest_whenPostingObject_thenReturnsExpectedContent()
      throws IOException, URISyntaxException {
    URI uri = new URI(DUMMY_URL);
    CsResponseEntity<Void, String> response =
        CsResponseEntity.success(HttpStatus.SC_OK, DUMMY_CONTENT);
    when(closeableHttpClient.execute(any(HttpPost.class), any(CsResponseHandler.class)))
        .thenReturn(response);
    assertThat(csHttpClient.postForObject(uri, DUMMY_BODY, String.class)).isEqualTo(DUMMY_CONTENT);
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
    assertThat(csHttpClient.postForEntity(uri, DUMMY_BODY, String.class)).isEqualTo(response);
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
    assertThatThrownBy(() -> csHttpClient.put(uri, DUMMY_BODY))
        .isExactlyInstanceOf(CsHttpClientException.class)
        .hasCause(cause);
    verify(closeableHttpClient, times(1)).execute(any(HttpPut.class), any(CsResponseHandler.class));
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
    assertThatThrownBy(() -> csHttpClient.put(uri, DUMMY_BODY))
        .isExactlyInstanceOf(CsHttpClientException.class)
        .hasMessageContaining("PUT request failed")
        .getCause()
        .hasMessageContaining(Integer.toString(HttpStatus.SC_INTERNAL_SERVER_ERROR));
    verify(closeableHttpClient, times(1)).execute(any(HttpPut.class), any(CsResponseHandler.class));
  }

  @Test
  public void
      givenThrowingRequest_whenInvokingDelete_thenThrowsCsHttpClientExceptionWithUnderlyingCause()
          throws URISyntaxException, IOException {
    URI uri = new URI(DUMMY_URL);
    IOException cause = new IOException(EXCEPTION_MESSAGE);
    when(closeableHttpClient.execute(any(HttpDelete.class), any(CsResponseHandler.class)))
        .thenThrow(cause);
    assertThatThrownBy(() -> csHttpClient.delete(uri))
        .isExactlyInstanceOf(CsHttpClientException.class)
        .hasCause(cause);
    verify(closeableHttpClient, times(1))
        .execute(any(HttpDelete.class), any(CsResponseHandler.class));
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
    assertThatThrownBy(() -> csHttpClient.delete(uri))
        .isExactlyInstanceOf(CsHttpClientException.class)
        .hasMessageContaining("DELETE request failed")
        .getCause()
        .hasMessageContaining(Integer.toString(HttpStatus.SC_INTERNAL_SERVER_ERROR));
    verify(closeableHttpClient, times(1))
        .execute(any(HttpDelete.class), any(CsResponseHandler.class));
  }
}
