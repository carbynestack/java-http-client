/*
 * Copyright (c) 2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/carbynestack/java-http-client.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.carbynestack.httpclient;

import java.io.IOException;

/** Exception thrown in case a client operation fails. */
public class CsHttpClientException extends IOException {
  /**
   * Constructs a new exception with the specified detail message and an empty status map.
   *
   * @param message The detail message (which is saved for later retrieval by the {@link
   *     Throwable#getMessage()} method)
   */
  public CsHttpClientException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message, cause and an empty status map.
   *
   * @param message The detail message (which is saved for later retrieval by the {@link
   *     Throwable#getMessage()} method)
   * @param cause The cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public CsHttpClientException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new exception with the specified cause and an empty status map.
   *
   * @param cause The cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public CsHttpClientException(Throwable cause) {
    super(cause);
  }
}
