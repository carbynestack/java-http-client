/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/java-http-client.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.httpclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Optional;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import lombok.experimental.UtilityClass;

@UtilityClass
class X509TrustManagerUtils {

  Optional<X509TrustManager> getX509TrustManager(KeyStore keyStore)
      throws NoSuchAlgorithmException, KeyStoreException {
    TrustManagerFactory tmf =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(keyStore);
    return Arrays.stream(tmf.getTrustManagers())
        .filter(tm -> (tm instanceof X509TrustManager))
        .map(X509TrustManager.class::cast)
        .findFirst();
  }

  Optional<X509TrustManager> getDefaultX509TrustManager()
      throws NoSuchAlgorithmException, KeyStoreException {
    return getX509TrustManager((KeyStore) null);
  }

  Optional<X509TrustManager> getX509TrustManager(File file)
      throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
    try (FileInputStream fis = new FileInputStream(file)) {
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(fis, null);
      return getX509TrustManager(keyStore);
    }
  }
}
