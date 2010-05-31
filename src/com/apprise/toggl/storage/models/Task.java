package com.apprise.toggl.storage.models;

public class Task extends Model {

  public Project project;
  public Workspace workspace;
  public long duration;
  public String start;
  public boolean billable;
  public String description;
  public String stop;
  public String[] tag_names;

  public Task() {}
  
  public Task(long _id, Project project, Workspace workspace, long duration,
      String start, boolean billable, String description, String stop,
      String[] tagNames, long remote_id, boolean syncDirty) {
    this._id = _id;
    this.project = project;
    this.workspace = workspace;
    this.duration = duration;
    this.start = start;
    this.billable = billable;
    this.description = description;
    this.stop = stop;
    this.tag_names = tagNames;
    this.id = remote_id;
    this.sync_dirty = syncDirty;
  }

  public static Task dirty(Project project, Workspace workspace, long duration,
      String start, boolean billable, String description, String stop,
      String[] tagNames, long remote_id) {
    Task task = new Task();
    task.project = project;
    task.workspace = workspace;
    task.duration = duration;
    task.start = start;
    task.billable = billable;
    task.description = description;
    task.stop = stop;
    task.tag_names = tagNames;
    task.id = remote_id;
    task.sync_dirty = true;
    return task;
  }

  public boolean identicalTo(Model other) {
    Task otherTask = (Task) other;

    boolean diff = project.id == otherTask.project.id &&
    workspace.id == otherTask.id &&
    duration == otherTask.duration &&
    start.equals(otherTask.start) &&
    billable == otherTask.billable &&
    description.equals(otherTask.description) &&
    stop.equals(otherTask.description) &&
    tag_names.equals(otherTask.tag_names);
    
    return diff;    
  }

  public void updateAttributes(Model other) {
    Task otherTask = (Task) other;
    project = otherTask.project;
    workspace = otherTask.workspace;
    duration = otherTask.duration;
    start = otherTask.start;
    billable = otherTask.billable;
    description = otherTask.description;
    stop = otherTask.stop;
    tag_names = otherTask.tag_names;
  }
    
}
