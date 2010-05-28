package com.apprise.toggl.storage.models;

public class User {

  public long _id;
  public String jquery_timeofday_format;
  public String api_token;
  public int task_retention_days;
  public String jquery_date_format;
  public String date_format;
  public long default_workspace_id;
  public boolean new_tasks_start_automatically;
  public String fullname;
  public String language;
  public long id;
  public int beginning_of_week;
  public String timeofday_format;
  public String email;
  
  public User() {
    
  }

  public User(long _id, String jqueryTimeofdayFormat, String apiToken,
      int taskRetentionDays, String jqueryDateFormat, String dateFormat,
      long defaultWorkspaceId, boolean newTasksStartAutomatically,
      String fullname, String language, long remote_id, int beginningOfWeek,
      String timeofdayFormat, String email) {
    this._id = _id;
    this.jquery_timeofday_format = jqueryTimeofdayFormat;
    this.api_token = apiToken;
    this.task_retention_days = taskRetentionDays;
    this.jquery_date_format = jqueryDateFormat;
    this.date_format = dateFormat;
    this.default_workspace_id = defaultWorkspaceId;
    this.new_tasks_start_automatically = newTasksStartAutomatically;
    this.fullname = fullname;
    this.language = language;
    this.id = remote_id;
    this.beginning_of_week = beginningOfWeek;
    this.timeofday_format = timeofdayFormat;
    this.email = email;
  }

  @Override
  public String toString() {
    return this.email;
  }

}
