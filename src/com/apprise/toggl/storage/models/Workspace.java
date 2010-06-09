package com.apprise.toggl.storage.models;

public class Workspace extends Model {

  public String name;
  
  public Workspace() {}
  
  public Workspace(long _id, String name, long remote_id) {
    this._id = _id;
    this.name = name;
    this.id = remote_id;
  }

  public String toString() {
    return "name: " + name
    + ", remote_id: " + id;
  }
  
  public boolean identicalTo(Model other) {
    Workspace otherWorkspace = (Workspace) other;
    
    return this.toString().equals(otherWorkspace.toString());
  }

  public void updateAttributes(Model other) {
    Workspace otherWorkspace = (Workspace) other;
    name = otherWorkspace.name;
  }
  
}
