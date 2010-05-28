package com.apprise.toggl.storage.models;

public class PlannedTask {
  
  public long _id;
  public String name;
  public Workspace workspace;
  public long id;    
  public Project project;
  public User user;
  public int estimated_workhours;
  
  public PlannedTask(long _id, String name, Workspace workspace, long remote_id,
      Project project, User user, int estimatedWorkhours) {
    this._id = _id;
    this.name = name;
    this.workspace = workspace;
    this.id = remote_id;
    this.project = project;
    this.user = user;
    this.estimated_workhours = estimatedWorkhours;
  }
  
}
