/*
 * Copyright (c) 2021-2024 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/java-http-client.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.httpclient;

import java.security.KeyManagementException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.ssl.SSLContextBuilder;

public class ExtendedSSLContextBuilder extends SSLContextBuilder {
  private final X509TrustManager trustManager;

  private ExtendedSSLContextBuilder(X509TrustManager trustManager) {
    this.trustManager = trustManager;
  }

  public static ExtendedSSLContextBuilder create(X509TrustManager trustManager) {
    return new ExtendedSSLContextBuilder(trustManager);
  }

  @Override
  protected void initSSLContext(
      SSLContext sslContext,
      Collection<KeyManager> keyManagers,
      Collection<TrustManager> trustManagers,
      SecureRandom secureRandom)
      throws KeyManagementException {
    super.initSSLContext(
        sslContext, keyManagers, Collections.singletonList(trustManager), secureRandom);
  }
}
