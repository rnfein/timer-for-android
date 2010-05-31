package com.apprise.toggl.storage.models;

public interface Syncable {

  public boolean identicalTo(Model other);
  public void updateAttributes(Model other);

}
