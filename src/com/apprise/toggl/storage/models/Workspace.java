package com.apprise.toggl.storage.models;

public class Workspace extends Model {

  public long _id;
  public String name;
  public long id;
  
  public Workspace() {}
  
  public Workspace(long _id, String name, long remote_id) {
    this._id = _id;
    this.name = name;
    this.id = remote_id;
  }

  public boolean identicalTo(Model other) {
    return false;
  }

  public void updateAttributes(Model other) {
  }
  
}
