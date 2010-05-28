package com.apprise.toggl.storage.models;

public interface Syncable {

  public boolean identicalTo(Syncable other);
  public void updateAttributes(Syncable other);

}
