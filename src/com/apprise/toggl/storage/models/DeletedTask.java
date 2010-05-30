package com.apprise.toggl.storage.models;

public class DeletedTask extends DeletedModel {
  public long taskRemoteId;

  public DeletedTask(long id, long taskRemoteId) {
    this.id = id;
    this.taskRemoteId = taskRemoteId;
  }
}
