package com.apprise.toggl.remote.exception;

public class ApiException extends RuntimeException {

  private static final long serialVersionUID = -2902576046201081387L;

  private String cause;

  public ApiException(String cause) {
    super(cause);
    this.cause = cause;
  }

  public ApiException() {
    super();
  }
  
  public String getError() {
    return cause;
  }

}
