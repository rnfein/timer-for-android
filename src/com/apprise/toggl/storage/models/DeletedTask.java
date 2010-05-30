package com.apprise.toggl.storage.models;

public class DeletedTask extends DeletedModel {
  public long taskRemoteId;

  public DeletedTask(long _id, long taskRemoteId) {
    this._id = _id;
    this.taskRemoteId = taskRemoteId;
  }
}
