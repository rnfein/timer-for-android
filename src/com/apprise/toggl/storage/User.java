package com.apprise.toggl.storage;

public class User {

  public String jquery_timeofday_format;
  public String api_token;
  public String task_retention_days;
  public String jquery_date_format;
  public String date_format;
  public String default_workspace_id;
  public boolean new_tasks_start_automatically;
  public String fullname;
  public String language;
  public long id;
  public int beginning_of_week;
  public String timeofday_format;
  public String email;

  private static User theInstance = new User();
  
  static public User getInstance() {
    return theInstance;
  }
  
  protected User() {
    
  }

  protected User(String jqueryTimeofdayFormat, String apiToken,
      String taskRetentionDays, String jqueryDateFormat, String dateFormat,
      String defaultWorkspaceId, boolean newTasksStartAutomatically,
      String fullname, String language, long id, int beginningOfWeek,
      String timeofdayFormat, String email) {
    this.jquery_timeofday_format = jqueryTimeofdayFormat;
    this.api_token = apiToken;
    this.task_retention_days = taskRetentionDays;
    this.jquery_date_format = jqueryDateFormat;
    this.date_format = dateFormat;
    this.default_workspace_id = defaultWorkspaceId;
    this.new_tasks_start_automatically = newTasksStartAutomatically;
    this.fullname = fullname;
    this.language = language;
    this.id = id;
    this.beginning_of_week = beginningOfWeek;
    this.timeofday_format = timeofdayFormat;
    this.email = email;
  }

  @Override
  public String toString() {
    return this.email;
  }
  
  public static boolean isLoggedIn() {
    return (theInstance.api_token != null);
  }

}
