/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/java-http-client.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.httpclient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.net.ssl.X509TrustManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CompositeX509TrustManagerTest {
  @Mock private X509TrustManager tma, tmb;

  @Test
  public void givenTwoDistrustingTrustProviders_whenCheckClientTrusted_thenThrows()
      throws CertificateException {
    doThrow(new CertificateException())
        .when(tma)
        .checkClientTrusted(any(X509Certificate[].class), any(String.class));
    doThrow(new CertificateException())
        .when(tmb)
        .checkClientTrusted(any(X509Certificate[].class), any(String.class));
    CompositeX509TrustManager ctm = new CompositeX509TrustManager(Arrays.asList(tma, tmb));
    assertThatThrownBy(() -> ctm.checkClientTrusted(new X509Certificate[0], ""))
        .isExactlyInstanceOf(CertificateException.class);
  }

  @Test
  public void givenAtLeastOneTrustingTrustProviders_whenCheckClientTrusted_thenSucceed()
      throws CertificateException {
    CompositeX509TrustManager ctm = new CompositeX509TrustManager(Arrays.asList(tma, tmb));
    ctm.checkClientTrusted(new X509Certificate[0], "");
  }

  @Test
  public void givenTwoDistrustingTrustProviders_whenCheckServerTrusted_thenThrows()
      throws CertificateException {
    doThrow(new CertificateException())
        .when(tma)
        .checkServerTrusted(any(X509Certificate[].class), any(String.class));
    doThrow(new CertificateException())
        .when(tmb)
        .checkServerTrusted(any(X509Certificate[].class), any(String.class));
    CompositeX509TrustManager ctm = new CompositeX509TrustManager(Arrays.asList(tma, tmb));
    assertThatThrownBy(() -> ctm.checkServerTrusted(new X509Certificate[0], ""))
        .isExactlyInstanceOf(CertificateException.class);
  }

  @Test
  public void givenAtLeastOneTrustingTrustProviders_whenCheckServerTrusted_thenSucceed()
      throws CertificateException {
    CompositeX509TrustManager ctm = new CompositeX509TrustManager(Arrays.asList(tma, tmb));
    ctm.checkServerTrusted(new X509Certificate[0], "");
  }
}
