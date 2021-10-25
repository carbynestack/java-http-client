/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/java-http-client.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.httpclient;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.net.ssl.X509TrustManager;
import org.apache.http.Header;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsHttpClient<L> {
  private static final Logger log = LoggerFactory.getLogger(CsHttpClient.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final HttpClientFactory clientFactory;
  private final Class<? extends L> failureType;

  CsHttpClient(HttpClientFactory clientFactory, Class<? extends L> failureType) {
    this.clientFactory = clientFactory;
    this.failureType = requireNonNull(failureType);
  }

  // @Builder
  public CsHttpClient(
      int withNumberOfRetries,
      Class<? extends L> withFailureType,
      boolean withoutSslValidation,
      List<File> withTrustedCertificates)
      throws CsHttpClientException {
    this.failureType = requireNonNull(withFailureType);
    if (withNumberOfRetries < 0) {
      log.debug(
          "Number of retries must not be negative (was {}). Using default value \"0\".",
          withNumberOfRetries);
      withNumberOfRetries = 0;
    }
    HttpClientBuilder builder = HttpClients.custom().useSystemProperties();
    if (withNumberOfRetries > 0) {
      builder.setRetryHandler(new DefaultHttpRequestRetryHandler(withNumberOfRetries, false));
    }
    if (withoutSslValidation
        || (withTrustedCertificates != null && !withTrustedCertificates.isEmpty())) {
      try {
        SSLConnectionSocketFactory sslConnectionSocketFactory;
        if (withoutSslValidation) {
          SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();
          sslContextBuilder.loadTrustMaterial(TrustAllStrategy.INSTANCE);
          sslConnectionSocketFactory =
              new SSLConnectionSocketFactory(sslContextBuilder.build(), new NoopHostnameVerifier());
        } else {
          List<Optional<X509TrustManager>> custom = new ArrayList<>();
          for (File f : withTrustedCertificates) {
            custom.add(X509TrustManagerUtils.getX509TrustManager(f));
          }
          List<X509TrustManager> allTrustManagers =
              Stream.concat(
                      custom.stream(),
                      Stream.of(X509TrustManagerUtils.getDefaultX509TrustManager()))
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .collect(toList());
          SSLContextBuilder sslContextBuilder =
              ExtendedSSLContextBuilder.create(new CompositeX509TrustManager(allTrustManagers));
          sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContextBuilder.build());
        }
        builder.setSSLSocketFactory(sslConnectionSocketFactory);
      } catch (Exception e) {
        throw new CsHttpClientException("Unable to configure certificate management", e);
      }
    }
    this.clientFactory = builder::build;
  }

  public static <L> CsHttpClient<L> createWithRetries(
      int numberOfRetries, Class<? extends L> failureType) {
    try {
      return new CsHttpClient<>(numberOfRetries, failureType, false, Collections.emptyList());
    } catch (CsHttpClientException e) {
      log.error(
          "This shouldn't have happened as the builder never throws in case no trusted"
              + " certificates are given");
      return null;
    }
  }

  public static CsHttpClient<String> createDefault() {
    return createDefaultWithRetries(0);
  }

  public static CsHttpClient<String> createDefaultWithRetries(int numberOfRetries) {
    return createWithRetries(numberOfRetries, String.class);
  }

  public static <L> CsHttpClientBuilder<L> builder() {
    return new CsHttpClientBuilder<>();
  }

  public <R> R getForObject(URI url, Class<R> responseType) throws CsHttpClientException {
    return getForObject(url, Collections.emptyList(), responseType);
  }

  public <R> R getForObject(URI url, List<Header> headers, Class<R> responseType)
      throws CsHttpClientException {
    return getForEntity(url, headers, responseType).get();
  }

  public <R> CsResponseEntity<L, R> getForEntity(URI url, Class<R> responseType)
      throws CsHttpClientException {
    return getForEntity(url, Collections.emptyList(), responseType);
  }

  public <R> CsResponseEntity<L, R> getForEntity(
      URI url, List<Header> headers, Class<R> responseType) throws CsHttpClientException {
    try (CloseableHttpClient httpClient = clientFactory.create()) {
      HttpGet httpGet = new HttpGet(url);
      headers.forEach(httpGet::addHeader);
      return httpClient.execute(httpGet, CsResponseHandler.of(failureType, responseType));
    } catch (IOException ioe) {
      throw new CsHttpClientException("GET request failed.", ioe);
    }
  }

  public <R> R postForObject(URI url, Object body, Class<R> responseType)
      throws CsHttpClientException {
    return postForObject(url, Collections.emptyList(), body, responseType);
  }

  public <R> R postForObject(URI url, List<Header> headers, Object body, Class<R> responseType)
      throws CsHttpClientException {
    return postForEntity(url, headers, body, responseType).get();
  }

  public <R> CsResponseEntity<L, R> postForEntity(URI url, Object content, Class<R> responseType)
      throws CsHttpClientException {
    return postForEntity(url, Collections.emptyList(), content, responseType);
  }

  public <R> CsResponseEntity<L, R> postForEntity(
      URI url, List<Header> headers, Object content, Class<R> responseType)
      throws CsHttpClientException {
    try (CloseableHttpClient httpClient = clientFactory.create()) {
      HttpPost httpPost = attachContentToRequest(new HttpPost(url), content);
      headers.forEach(httpPost::addHeader);
      return httpClient.execute(httpPost, CsResponseHandler.of(failureType, responseType));
    } catch (IOException ioe) {
      throw new CsHttpClientException("POST request failed.", ioe);
    }
  }

  public void put(URI url, Object content) throws CsHttpClientException {
    put(url, Collections.emptyList(), content);
  }

  public void put(URI url, List<Header> headers, Object content) throws CsHttpClientException {
    try (CloseableHttpClient httpClient = clientFactory.create()) {
      HttpPut httpPut = attachContentToRequest(new HttpPut(url), content);
      headers.forEach(httpPut::addHeader);
      httpClient.execute(httpPut, CsResponseHandler.of(failureType, Void.class)).get();
    } catch (IOException ioe) {
      throw new CsHttpClientException("PUT request failed.", ioe);
    }
  }

  public void delete(URI url) throws CsHttpClientException {
    delete(url, Collections.emptyList());
  }

  public void delete(URI url, List<Header> headers) throws CsHttpClientException {
    try (CloseableHttpClient httpClient = clientFactory.create()) {
      HttpDelete httpDelete = new HttpDelete(url);
      headers.forEach(httpDelete::addHeader);
      httpClient.execute(httpDelete, CsResponseHandler.of(failureType, Void.class)).get();
    } catch (IOException ioe) {
      throw new CsHttpClientException("DELETE request failed.", ioe);
    }
  }

  private <R extends HttpEntityEnclosingRequestBase> R attachContentToRequest(
      R request, Object content) throws IOException {
    StringEntity entity = new StringEntity(OBJECT_MAPPER.writeValueAsString(content));
    request.setEntity(entity);
    request.setHeader("Accept", "application/json");
    request.setHeader("Content-type", "application/json");
    return request;
  }

  public static class CsHttpClientBuilder<L> {
    private int withNumberOfRetries;
    private Class<? extends L> withFailureType;
    private boolean withoutSslValidation;
    private List<File> withTrustedCertificates;

    CsHttpClientBuilder() {}

    public CsHttpClient.CsHttpClientBuilder<L> withNumberOfRetries(int withNumberOfRetries) {
      this.withNumberOfRetries = withNumberOfRetries;
      return this;
    }

    public CsHttpClient.CsHttpClientBuilder<L> withFailureType(Class<? extends L> withFailureType) {
      this.withFailureType = requireNonNull(withFailureType);
      return this;
    }

    public CsHttpClient.CsHttpClientBuilder<L> withoutSslValidation(boolean withoutSslValidation) {
      this.withoutSslValidation = withoutSslValidation;
      return this;
    }

    public CsHttpClient.CsHttpClientBuilder<L> withTrustedCertificates(
        List<File> withTrustedCertificates) {
      this.withTrustedCertificates = withTrustedCertificates;
      return this;
    }

    public CsHttpClient<L> build() throws CsHttpClientException {
      return new CsHttpClient<>(
          this.withNumberOfRetries,
          this.withFailureType,
          this.withoutSslValidation,
          this.withTrustedCertificates);
    }

    public String toString() {
      return "CsHttpClient.CsHttpClientBuilder(withNumberOfRetries="
          + this.withNumberOfRetries
          + ", withFailureType="
          + this.withFailureType
          + ", withoutSslValidation="
          + this.withoutSslValidation
          + ", withTrustedCertificates="
          + this.withTrustedCertificates
          + ")";
    }
  }
}
