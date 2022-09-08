package com.korovko.reactive.exception;

public class EntityNotFoundException extends RuntimeException {

  public EntityNotFoundException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
