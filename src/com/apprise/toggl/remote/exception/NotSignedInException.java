package com.apprise.toggl.remote.exception;

public class NotSignedInException extends ApiException {

  public NotSignedInException(String cause) {
    super(cause);
  }

  public NotSignedInException() {}
  
  private static final long serialVersionUID = -3369237428094070711L;

}
