/*
 * Copyright (c) 2021-2024 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/java-http-client.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.httpclient;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class X509TrustManagerUtils {

  public static Optional<X509TrustManager> getX509TrustManager(KeyStore keyStore)
      throws NoSuchAlgorithmException, KeyStoreException {
    TrustManagerFactory tmf =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(keyStore);
    return Arrays.stream(tmf.getTrustManagers())
        .filter(tm -> (tm instanceof X509TrustManager))
        .map(X509TrustManager.class::cast)
        .findFirst();
  }

  public static Optional<X509TrustManager> getDefaultX509TrustManager()
      throws NoSuchAlgorithmException, KeyStoreException {
    return getX509TrustManager((KeyStore) null);
  }

  public static Optional<X509TrustManager> getX509TrustManager(List<File> certs)
      throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
      keyStore.load(null);
      for (File certificate : certs) {
        X509Certificate x509Certificate =
                (X509Certificate)
                        certificateFactory.generateCertificate(Files.newInputStream(certificate.toPath()));
        keyStore.setCertificateEntry(
                x509Certificate.getSubjectX500Principal().getName(), x509Certificate);
      }
      return getX509TrustManager(keyStore);
    }
}
