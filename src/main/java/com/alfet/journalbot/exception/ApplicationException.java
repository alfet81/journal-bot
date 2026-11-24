package com.alfet.journalbot.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ApplicationException extends RuntimeException {

  public ApplicationException(String message) {
    super(message);
  }

  public ApplicationException(String message, Throwable cause) {
    super(message, cause);
  }
}
