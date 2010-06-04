package com.apprise.toggl.remote;

import com.apprise.toggl.storage.models.DeletedModel;
import com.apprise.toggl.storage.models.Model;

import android.database.Cursor;

public interface SyncProxy {

  abstract Model getLocalEntry(long remoteId);
  abstract DeletedModel getLocalDeletedEntry(long remoteId);
  
  abstract void deleteLocalEntry(long id);
  abstract void updateLocalEntry(Model model);
  abstract Model createLocalEntry(Model model);
  
  abstract void deleteLocalDeletedEntry(long id);
  
  abstract void deleteRemoteEntry(long id);
  abstract void updateRemoteEntry(Model model);
  abstract void createRemoteEntry(Model model);
 
  abstract Model mapEntryFromCursor(Cursor cursor);
  
  abstract void broadcastSyncCompleted();
}
