package com.apprise.toggl.storage.models;

public class DeletedTasks extends DeletedModel {
  public long taskId;

  public DeletedTasks(long id, long taskId) {
    this.id = id;
    this.taskId = taskId;
  }
}
