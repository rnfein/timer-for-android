package com.apprise.toggl.storage.models;

public class Tag extends Model {

  public String name;
  public Workspace workspace;
  
  public Tag() {};
  
  public Tag(long _id, long remoteId, String name, Workspace workspace) {
    this._id = _id;
    this.id = remoteId;
    this.name = name;
    this.workspace = workspace;
  }

  public String toString() {
    Long workspace_remote_id = null;
    
    if (workspace != null)
      workspace_remote_id = new Long(workspace.id);
    
    return "workspace_id: " + workspace_remote_id
    + ", remote_id: " + id
    + ", name: " + name;
  }
  
  public boolean identicalTo(Model other) {
    Tag otherTag = (Tag) other;
    
    return this.toString().equals(otherTag.toString());
  }

  public void updateAttributes(Model other) {
    Tag otherTag = (Tag) other;
    workspace = otherTag.workspace;
    name = otherTag.name;
    id = otherTag.id;
  }

}
