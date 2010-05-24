package com.apprise.toggl.storage;

public class User {

	public String jqueryTimeofdayFormat;
	public String apiToken;
	public String taskRetentionDays;
	public String jqueryDateFormat;
	public String dateFormat;
	public String defaultWorkspaceId;
	public boolean newTasksStartAutomatically;
	public String fullname;
	public String language;
	public long id;
	public int beginningOfWeek;
	public String timeofdayFormat;
	public String email;
	
	public User() {
		
	}
	
	public User(String jqueryTimeofdayFormat, String apiToken,
      String taskRetentionDays, String jqueryDateFormat, String dateFormat,
      String defaultWorkspaceId, boolean newTasksStartAutomatically,
      String fullname, String language, long id, int beginningOfWeek,
      String timeofdayFormat, String email) {
	  this.jqueryTimeofdayFormat = jqueryTimeofdayFormat;
	  this.apiToken = apiToken;
	  this.taskRetentionDays = taskRetentionDays;
	  this.jqueryDateFormat = jqueryDateFormat;
	  this.dateFormat = dateFormat;
	  this.defaultWorkspaceId = defaultWorkspaceId;
	  this.newTasksStartAutomatically = newTasksStartAutomatically;
	  this.fullname = fullname;
	  this.language = language;
	  this.id = id;
	  this.beginningOfWeek = beginningOfWeek;
	  this.timeofdayFormat = timeofdayFormat;
	  this.email = email;
  }
	
	@Override
	public String toString() {
		return this.email;
	}
	
}
