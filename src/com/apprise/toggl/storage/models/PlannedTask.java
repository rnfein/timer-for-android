package com.apprise.toggl.storage.models;

public class PlannedTask extends Model {
  
  public String name;
  public Workspace workspace;
  public Project project;
  public User user;
  public long estimated_workhours;
  
  public PlannedTask() {}
  
  public PlannedTask(long _id, String name, Workspace workspace, long remote_id,
      Project project, User user, long estimatedWorkhours) {
    this._id = _id;
    this.name = name;
    this.workspace = workspace;
    this.id = remote_id;
    this.project = project;
    this.user = user;
    this.estimated_workhours = estimatedWorkhours;
  }

  public String toString() {
    Long workspace_remote_id = null;
    Long project_remote_id = null;
    Long user_remote_id = null;
    
    if (workspace != null)
      workspace_remote_id = new Long(workspace.id);
    
    if (project != null)
      project_remote_id = new Long(project.id);
    
    if (user != null)
      user_remote_id = new Long(user.id);
    
    return "workspace_id: " + workspace_remote_id
      + ", project_remote_id: " + project_remote_id
      + ", user_remote_id: " + user_remote_id
      + ", name: " + name
      + ", estimated_workhours: " + estimated_workhours;
  }  
  
  public boolean identicalTo(Model other) {
    PlannedTask otherPlannedTask = (PlannedTask) other;
    
    return this.toString().equals(otherPlannedTask.toString());
  }

  public void updateAttributes(Model other) {
    PlannedTask otherPlannedTask = (PlannedTask) other;
    
    workspace = otherPlannedTask.workspace;
    name = otherPlannedTask.name;
    project = otherPlannedTask.project;
    user = otherPlannedTask.user;
    estimated_workhours = otherPlannedTask.estimated_workhours;
  }
  
}
