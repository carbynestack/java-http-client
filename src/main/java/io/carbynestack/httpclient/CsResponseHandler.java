/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/java-http-client.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.httpclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class CsResponseHandler<L, R> implements ResponseHandler<CsResponseEntity<L, R>> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final Class<? extends L> failureType;
  private final Class<? extends R> successType;

  @Override
  public CsResponseEntity<L, R> handleResponse(HttpResponse response) throws IOException {
    int status = response.getStatusLine().getStatusCode();
    HttpEntity entity = response.getEntity();
    String contentString = entity != null ? EntityUtils.toString(entity) : null;
    if (status >= 200 && status < 300) {
      return CsResponseEntity.success(
          status,
          contentString != null && !contentString.isEmpty()
              ? OBJECT_MAPPER.readValue(contentString, successType)
              : null);
    } else {
      log.debug(String.format("Request failed with status code <%s>: %s", status, contentString));
      return CsResponseEntity.failed(
          status,
          contentString != null && !contentString.isEmpty()
              ? asFailureInstance(contentString)
              : null);
    }
  }

  private L asFailureInstance(String content) throws IOException {
    return failureType.equals(String.class)
        ? failureType.cast(content)
        : OBJECT_MAPPER.readValue(content, failureType);
  }
}
