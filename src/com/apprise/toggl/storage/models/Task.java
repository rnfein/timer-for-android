package com.apprise.toggl.storage.models;

import com.apprise.toggl.Util;
import com.google.mygson.Gson;
import com.google.mygson.JsonObject;


public class Task extends Model {

  public Project project;
  public Workspace workspace;
  public PlannedTask planned_task;
  public long duration;
  public String start;
  public boolean billable;
  public String description;
  public String stop;
  public String[] tag_names;

  public Task() {}
  
  public Task(long _id, Project project, Workspace workspace, PlannedTask plannedTask, long duration,
      String start, boolean billable, String description, String stop,
      String[] tagNames, long remote_id, boolean syncDirty) {
    this._id = _id;
    this.project = project;
    this.workspace = workspace;
    this.planned_task = plannedTask;
    this.duration = duration;
    this.start = start;
    this.billable = billable;
    this.description = description;
    this.stop = stop;
    this.tag_names = tagNames;
    this.id = remote_id;
    this.sync_dirty = syncDirty;
  }

  public static Task dirty(Project project, Workspace workspace , PlannedTask plannedTask, long duration,
      String start, boolean billable, String description, String stop,
      String[] tagNames, long remote_id) {
    Task task = new Task();
    task.project = project;
    task.workspace = workspace;
    task.planned_task = plannedTask;    
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
  
  public String toString() {
    Long project_id = null;
    Long workspace_id = null;
    Long planned_task_id = null;
    
    if (project != null)
      project_id = new Long(project.id);
    if (workspace != null)
      workspace_id = new Long(workspace.id);
    if (planned_task != null)
      planned_task_id = new Long(planned_task.id);

    String tagNames = null;
    if (this.tag_names != null) {
      tagNames = Util.joinStringArray(this.tag_names, ";");
    }
    
    return "project_id: " + project_id
      + ", workspace_id: " + workspace_id
      + ", planned_task_id: " + planned_task_id
      + ", duration: " + duration
      + ", start: " + start
      + ", billable: " + billable
      + ", description: " + description
      + ", remote_id: " + id
      + ", stop: " + stop
      + ", tag_names: " + tagNames;
  }

  public boolean identicalTo(Model other) {
    Task otherTask = (Task) other;
    
    return this.toString().equals(otherTask.toString());
  }
    
  public void updateAttributes(Model other) {
    Task otherTask = (Task) other;
    id = otherTask.id;
    project = otherTask.project;
    workspace = otherTask.workspace;
    planned_task = otherTask.planned_task;
    duration = otherTask.duration;
    start = otherTask.start;
    billable = otherTask.billable;
    description = otherTask.description;
    stop = otherTask.stop;
    tag_names = otherTask.tag_names;
  }
  
  public String apiJsonString(User currentUser) {
    Gson gson = new Gson();
    JsonObject workspaceObj = new JsonObject();
    JsonObject projectObj = new JsonObject();
    JsonObject rootObj = new JsonObject();
    JsonObject taskObj = gson.toJsonTree(this).getAsJsonObject();
    
    if (this.project != null) {
      projectObj.addProperty("id", project.id);
      taskObj.add("project", projectObj);
    }
    
    if (this.workspace == null) {
      workspaceObj.addProperty("id", currentUser.default_workspace_id);
    } else {
      workspaceObj.addProperty("id", this.workspace.id);      
    }
    
    taskObj.add("workspace", workspaceObj);
    taskObj.remove("tag_names");
    taskObj.addProperty("created_with", "Toggl Android Client.");
    rootObj.add("task", taskObj);
    
    String requestJson = rootObj.toString();

    // FIXME: workaround to add tag_names array to JSON since taskObj.addProperty takes only String, Boolean, Number or Char
    if (this.tag_names != null) {
      String tagJson = "\"tag_names\":[";
      for (int i = 0; i < tag_names.length; i++) {
        tagJson += "\"" + tag_names[i] + "\"";
        if (i < (tag_names.length - 1)) {
          tagJson += ",";
        }
      }
      tagJson += "],";
      String working = requestJson.substring(0, (requestJson.indexOf(":") + 2))
          + tagJson
          + requestJson.substring((requestJson.indexOf(":") + 2), requestJson.length());
      requestJson = working;
    }
    
    return requestJson;
  }  
    
}
