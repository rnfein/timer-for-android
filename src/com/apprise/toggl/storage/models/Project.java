package com.apprise.toggl.storage.models;

import org.json.JSONArray;
import org.json.JSONException;

import com.apprise.toggl.Toggl;
import com.google.gson.Gson;


public class Project extends Model {
  
  public float fixed_fee;
  public long estimated_workhours;
  public boolean is_fixed_fee;
  public Workspace workspace;
  public boolean billable;
  public String client_project_name;
  public float hourly_rate;
  public Client client;
  public String name;
  
  public Project() {}
  
  public Project(long _id, float fixedFee, long estimatedWorkhours,
      boolean isFixedFee, Workspace workspace, boolean billable,
      String clientProjectName, float hourlyRate, Client client, String name, long remote_id,
      boolean syncDirty) {
    this._id = _id;
    this.fixed_fee = fixedFee;
    this.estimated_workhours = estimatedWorkhours;
    this.is_fixed_fee = isFixedFee;
    this.workspace = workspace;
    this.billable = billable;
    this.client_project_name = clientProjectName;
    this.hourly_rate = hourlyRate;
    this.client = client;
    this.name = name;
    this.id = remote_id;
    this.sync_dirty = syncDirty;
  }
  
  public static Project dirty(float fixedFee, long estimatedWorkhours,
      boolean isFixedFee, Workspace workspace, boolean billable,
      String clientProjectName, float hourlyRate, Client client, String name, long remote_id) {
    Project project = new Project();
    project.fixed_fee = fixedFee;
    project.estimated_workhours = estimatedWorkhours;
    project.is_fixed_fee = isFixedFee;
    project.workspace = workspace;
    project.billable = billable;
    project.client_project_name = clientProjectName;
    project.hourly_rate = hourlyRate;
    project.client = client;
    project.name = name;
    project.id = remote_id;
    project.sync_dirty = true;
    return project;
  }
  
  public String toString() {
    Long workspace_remote_id = null;
    Long client_remote_id = null;
    
    if (workspace != null)
      workspace_remote_id = new Long(workspace.id);
    if (client != null)
      client_remote_id = new Long(client.id);
    
    return "workspace_id: " + workspace_remote_id
      + ", fixed_fee: " + fixed_fee
      + ", estimated_workhours: " + estimated_workhours
      + ", is_fixed_fee: " + is_fixed_fee
      + ", billable: " + billable
      + ", client_project_name: " + client_project_name
      + ", client_remote_id: " + client_remote_id
      + ", hourly_rate: " + hourly_rate
      + ", name: " + name
      + ", remote_id: " + id; 
  }

  public boolean identicalTo(Model other) {
    Project otherProject = (Project) other;
    
    return this.toString().equals(otherProject.toString());
  }

  public void updateAttributes(Model other) {
    Project otherProject = (Project) other;
    fixed_fee = otherProject.fixed_fee;
    estimated_workhours = otherProject.estimated_workhours;
    is_fixed_fee = otherProject.is_fixed_fee;
    workspace = otherProject.workspace;
    billable = otherProject.billable;
    client_project_name = otherProject.client_project_name;
    client = otherProject.client;
    hourly_rate = otherProject.hourly_rate;
    name = otherProject.name;
  }
  
  public String apiJsonString() {
    JSONArray rootObj = new JSONArray();
    JSONArray projectObj = new JSONArray();
    JSONArray workspaceObj = new JSONArray();
    JSONArray clientObj = new JSONArray();
    
    Toggl app = new Toggl().getInstance();
    
    workspaceObj.put("id:");
    if (this.workspace != null) {      
      workspaceObj.put(this.workspace.id);      
    } else {
      workspaceObj.put(48003);
    }
    
    if (this.client != null) {
      clientObj.put("id:");
      clientObj.put(client.id);
    } 
    
    try {
      projectObj.put("fixed_fee:");
      projectObj.put(this.fixed_fee);
      projectObj.put("estimated_workhours:");
      projectObj.put(this.estimated_workhours);
      projectObj.put("fixed_fee:");
      projectObj.put(this.fixed_fee);
      projectObj.put("is_fixed_fee:");
      projectObj.put(this.is_fixed_fee);
      projectObj.put("workspace:");
      projectObj.put(workspaceObj.toString());
      projectObj.put("billable:");
      projectObj.put(this.billable);
      projectObj.put("client_project_name:");
      projectObj.put(this.client_project_name);
      projectObj.put("hourly_rate:");
      projectObj.put(this.hourly_rate);
      projectObj.put("client:");
      if (this.client != null) {
        projectObj.put(clientObj.toString());
      }
      projectObj.put("name:");
      projectObj.put(this.name);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    rootObj.put("project:");
    rootObj.put(projectObj.toString());
    
    return rootObj.toString();
  }
  
}
