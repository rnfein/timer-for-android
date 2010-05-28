package com.apprise.toggl.storage.models;

public abstract class Model implements Syncable {

  public long _id;
  public boolean sync_dirty;
  public long id;

}
