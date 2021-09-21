/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/java-http-client.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.httpclient;

import io.vavr.control.Either;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.http.HttpStatus;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CsResponseEntity<L, R> implements Serializable {

  private static final long serialVersionUID = 6972871863318983069L;

  int httpStatus;
  Either<L, R> content;

  private CsResponseEntity() {
    this.httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
    this.content = Either.left(null);
  }

  public R get() throws CsHttpClientException {
    return content.getOrElseThrow(
        () ->
            new CsHttpClientException(
                String.format(
                    "Request has failed with status <%s>: %s", httpStatus, content.getLeft())));
  }

  public L getError() throws CsHttpClientException {
    return content
        .swap()
        .getOrElseThrow(
            () ->
                new CsHttpClientException(
                    String.format(
                        "Expected failure but response was successful with status <%s>: %s",
                        httpStatus, content.get())));
  }

  public boolean isSuccess() {
    return content.isRight();
  }

  public boolean isFailure() {
    return !isSuccess();
  }

  public static <L, R> CsResponseEntity<L, R> success(int httpStatus, R content) {
    return new CsResponseEntity<>(httpStatus, Either.right(content));
  }

  public static <L, R> CsResponseEntity<L, R> failed(int httpStatus, L failure) {
    return new CsResponseEntity<>(httpStatus, Either.left(failure));
  }
}
