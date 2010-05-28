package com.apprise.toggl.storage.models;

public class Project {

  public long _id;
  public int fixed_fee;
  public long estimated_workhours;
  public boolean is_fixed_fee;
  public Workspace workspace;
  public boolean billable;
  public String client_project_name;
  public long hourly_rate;
  public String name;
  public long id;
  public boolean sync_dirty;
  
  public Project() {}
  
  public Project(long _id, int fixedFee, long estimatedWorkhours,
      boolean isFixedFee, Workspace workspace, boolean billable,
      String clientProjectName, long hourlyRate, String name, long remote_id,
      boolean syncDirty) {
    this._id = _id;
    this.fixed_fee = fixedFee;
    this.estimated_workhours = estimatedWorkhours;
    this.is_fixed_fee = isFixedFee;
    this.workspace = workspace;
    this.billable = billable;
    this.client_project_name = clientProjectName;
    this.hourly_rate = hourlyRate;
    this.name = name;
    this.id = remote_id;
    this.sync_dirty = syncDirty;
  }
  
  public static Project dirty(int fixedFee, long estimatedWorkhours,
      boolean isFixedFee, Workspace workspace, boolean billable,
      String clientProjectName, long hourlyRate, String name, long remote_id) {
    Project project = new Project();
    project.fixed_fee = fixedFee;
    project.estimated_workhours = estimatedWorkhours;
    project.is_fixed_fee = isFixedFee;
    project.workspace = workspace;
    project.billable = billable;
    project.client_project_name = clientProjectName;
    project.hourly_rate = hourlyRate;
    project.name = name;
    project.id = remote_id;
    project.sync_dirty = true;
    return project;
  }
  
}
