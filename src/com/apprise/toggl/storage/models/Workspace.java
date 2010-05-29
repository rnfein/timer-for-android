package com.apprise.toggl.storage.models;

public class Workspace {

  public long _id;
  public String name;
  public long id;
  
  public Workspace() {}
  
  public Workspace(long _id, String name, long remote_id) {
    this._id = _id;
    this.name = name;
    this.id = remote_id;
  }
  
}
