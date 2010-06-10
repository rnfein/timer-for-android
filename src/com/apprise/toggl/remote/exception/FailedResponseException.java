package com.apprise.toggl.remote.exception;

public class FailedResponseException extends ApiException {

  public FailedResponseException(String cause) {
    super(cause);
  }
  
  public FailedResponseException() {}

  private static final long serialVersionUID = 4601698689708766900L;

}
