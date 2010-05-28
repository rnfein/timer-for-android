package com.apprise.toggl.storage.models;

public class Task {

  public Project project;
  public Workspace workspace;
  public long duration;
  public String start;
  public boolean billable;
  public String description;
  public String stop;
  public String[] tag_names;
  public long id;
  public boolean sync_dirty;
  
}
