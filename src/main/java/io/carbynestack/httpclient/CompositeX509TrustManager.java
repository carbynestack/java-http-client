/*
 * Copyright (c) 2021-2024 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/java-http-client.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.httpclient;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.X509TrustManager;

public class CompositeX509TrustManager implements X509TrustManager {
  private final List<X509TrustManager> managers;

  public CompositeX509TrustManager(List<X509TrustManager> managers) {
    this.managers = new ArrayList<>(managers);
  }

  @Override
  public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
      throws CertificateException {
    CertificateException last = null;
    for (X509TrustManager m : managers) {
      try {
        m.checkClientTrusted(x509Certificates, s);
        return;
      } catch (CertificateException ce) {
        last = ce;
      }
    }
    if (last != null) throw last;
  }

  @Override
  public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
      throws CertificateException {
    CertificateException last = null;
    for (X509TrustManager m : managers) {
      try {
        m.checkServerTrusted(x509Certificates, s);
        return;
      } catch (CertificateException ce) {
        last = ce;
      }
    }
    if (last != null) throw last;
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return managers.stream()
        .map(X509TrustManager::getAcceptedIssuers)
        .flatMap(Arrays::stream)
        .toArray(X509Certificate[]::new);
  }
}
