/**
 * Sync ideology:
 * 
 * (1) retrieve list of all entries from remote server
 * for all records:
 *     (2) fetch local db entry
 *     local entry found?
 *         (2.1) no:
 *                 a: entry is in the removed table => delete from remote
 *                 b: entry is not in removed table => save locally
 *         (2.2) yes: check if local entry is sync dirty?
 *                 dirty: upload local changes to remote
 *                 clean: updating local
 *     (5) remember message as processed
 * (6) for all unprocessed local items
 *     a: skip already processed from server
 *     b: found in local db:
 *         dirty: create entry on server
 *         clean: deleting locally
 */

package com.apprise.toggl.remote;

import java.util.LinkedList;
import java.util.List;

import com.apprise.toggl.Toggl;
import com.apprise.toggl.storage.DatabaseAdapter;
import com.apprise.toggl.storage.DatabaseAdapter.ORM;
import com.apprise.toggl.storage.models.Client;
import com.apprise.toggl.storage.models.DeletedModel;
import com.apprise.toggl.storage.models.Model;
import com.apprise.toggl.storage.models.PlannedTask;
import com.apprise.toggl.storage.models.Project;
import com.apprise.toggl.storage.models.Tag;
import com.apprise.toggl.storage.models.Task;
import com.apprise.toggl.storage.models.Workspace;
import com.apprise.toggl.tracking.TimeTrackingService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.util.Log;

public class SyncService extends Service {

  public static final String SYNC_COMPLETED = "com.apprise.toggl.remote.SYNC_COMPLETED";
  public static final String COLLECTION = "com.apprise.toggl.remote.COLLECTION";
  public static final String ALL_COMPLETED = "com.apprise.toggl.remote.ALL_COMPLETED";
  public static final String ALL_COMPLETED_SCHEDULED = "com.apprise.toggl.remote.ALL_COMPLETED_SCHEDULED";
  public static final String PROJECTS_COMPLETED = "com.apprise.toggl.remote.PROJECTS_COMPLETED";
  public static final String TASKS_COMPLETED = "com.apprise.toggl.remote.TASKS_COMPLETED";
  public static final String CLIENTS_COMPLETED = "com.apprise.toggl.remote.CLIENTS_COMPLETED";
  public static final String TAGS_COMPLETED = "com.apprise.toggl.remote.TAGS_COMPLETED";
  public static final String WORKSPACES_COMPLETED = "com.apprise.toggl.remote.WORKSPACES_COMPLETED";
  public static final String PLANNED_TASKS_COMPLETED = "com.apprise.toggl.remote.PLANNED_TASKS_COMPLETED";
  
  public static final String TAG = "SyncService";
  
  public static boolean isSyncingAll = false;
  
  private TimeTrackingService trackingService;
  private Toggl app;
  private TogglWebApi api;
  private DatabaseAdapter dbAdapter;
  
  AlarmManager alarms;
  PendingIntent alarmIntent;
  
  @Override
  public void onCreate() {
    super.onCreate();

    Intent intent = new Intent(this, TimeTrackingService.class);
    if (!TimeTrackingService.isAlive()) {
      startService(intent);
    }
    bindService(intent, trackingConnection, BIND_AUTO_CREATE);
    
    app = (Toggl) getApplication();
    api = new TogglWebApi(app.getAPIToken());

    dbAdapter = new DatabaseAdapter(this, app);
    dbAdapter.open();
    app.initSyncSchedule();
  }
  
  @Override
  public void onDestroy() {
    dbAdapter.close();
    unbindService(trackingConnection);
    super.onDestroy();
  }
  
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    // explicitly started, not bound by an activity,
    // hence start complete sync immediately

    // to avoid interfering with explicitly started sync
    if (!isSyncingAll) {
      isSyncingAll = true;

      new Thread(new Runnable() {
        public void run() {
          try {

            syncAllModels();

            Intent intent = new Intent(SYNC_COMPLETED);
            intent.putExtra(COLLECTION, ALL_COMPLETED_SCHEDULED);
            sendBroadcast(intent);
          } catch (Exception e) {
            Log.e(TAG, "Error while syncing implicitly.", e);
          } finally {
            isSyncingAll = false;
          }
        }
      }).start();
    }

    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return new SyncBinder(this);
  }
  
  public static class SyncBinder extends Binder {

    private final SyncService service;

    public SyncBinder(SyncService service) {
      this.service = service;
    }

    public SyncService getService() {
      return service;
    }

  }
  
  public void setApiToken(String apiToken) {
    api.setApiToken(apiToken);
  }

  public void syncAll() {
    Log.d(TAG, "isSyncingAll: " + isSyncingAll);
    if (app.isConnected() && !isSyncingAll) {
      isSyncingAll = true;
      Log.d(TAG, "connection found, starting sync.");
      
      try {
        syncAllModels();

        Intent intent = new Intent(SYNC_COMPLETED);
        intent.putExtra(COLLECTION, ALL_COMPLETED);        
        sendBroadcast(intent);        
      } finally {
        isSyncingAll = false;
      }
    }
  }


  
  public void createOrUpdateRemoteTask(Task task) {
    if (app.isConnected()) {    
      Log.d(TAG, "connection found, #createOrUpdateRemoteTask starting.");
      if (task.id > 0) {
        Task updatedTask = api.updateTask(task, app);
        task.updateAttributes(updatedTask);
        task.sync_dirty = false;
        dbAdapter.updateTask(task);
      } else {
        Task createdTask = api.createTask(task, app);
        task.updateAttributes(createdTask);
        task.sync_dirty = false;
        dbAdapter.updateTask(task);        
      }
      // sync projects in case user created one
      syncProjects();
    }
  }

  public void syncTasks() {
    Log.d(TAG, "#syncTasks starting to sync.");
    
    sync(dbAdapter.findAllTasks(), api.fetchTasks(), new SyncProxy() {
      
      public void updateRemoteEntry(Model model) {
        Log.d(TAG, "updating remote task with: " + ((Task) model));
        Task createdTask = api.updateTask((Task) model, app);
        Log.d(TAG, "updated remote task: " + createdTask);
      }
      
      public void updateLocalEntry(Model model) {
        Task task = (Task) model;
        dbAdapter.updateTask(task);
        
        // negative number means that the task is currently
        // being tracked
        if (task.duration < 0) {
          triggerTracking(task);
        }
        else if (trackingService.isTracking(task)) {
          // tracking has been stopped from the api
          // close it locally as well
          trackingService.stopTracking();
        }
      }
      
      public Model getLocalEntry(long remoteId) {
        return dbAdapter.findTaskByRemoteId(remoteId);
      }
      
      public DeletedModel getLocalDeletedEntry(long remoteId) {
        return dbAdapter.findDeletedTask(remoteId);
      }
      
      public void deleteRemoteEntry(long id) {
        api.deleteTask(id);
      }
      
      public void deleteLocalEntry(long _id) {
        dbAdapter.deleteTaskHard(_id);
      }
      
      public void deleteLocalDeletedEntry(long _id) {
        dbAdapter.deleteDeletedTask(_id);
      }
      
      public void createRemoteEntry(Model model) {
        Task task = (Task) model;
        Task createdTask = api.createTask(task, app);
        task.updateAttributes(createdTask);
        dbAdapter.updateTask(task);
      }
      
      public Model createLocalEntry(Model model) {
        Task task = (Task) model;
        Log.d(TAG, "creating local task: " + task.description);

        task = dbAdapter.createTask(task);
        
        // if is tracking remotely
        if (task.duration < 0) {
          triggerTracking(task);
        }
        
        return task;
      }
      
      public Model mapEntryFromCursor(Cursor cursor) {
        return ORM.mapTask(cursor, dbAdapter);
      }
      
      public void broadcastSyncCompleted() {
        Intent intent = new Intent(SYNC_COMPLETED);
        intent.putExtra(COLLECTION, TASKS_COMPLETED);
        sendBroadcast(intent);
      }          

    });
  }
  
  public void syncProjects() {
    Log.d(TAG, "#syncProjects starting to sync.");
    
    sync(dbAdapter.findAllProjects(), api.fetchProjects(), new SyncProxy() {
      
      public void updateRemoteEntry(Model model) { }
      
      public void updateLocalEntry(Model model) {
        dbAdapter.updateProject((Project) model);
      }
      
      public Model getLocalEntry(long remoteId) {
        return dbAdapter.findProjectByRemoteId(remoteId);
      }
      
      public DeletedModel getLocalDeletedEntry(long remoteId) {
        return null;
      }
      
      public void deleteRemoteEntry(long id) { }
      
      public void deleteLocalEntry(long _id) {
        dbAdapter.deleteProject(_id);
      }
      
      public void deleteLocalDeletedEntry(long _id) { }
      
      /*
       * Create new remote Project based on dirty record and save it locally.
       * Switch old dirty project record relations in tasks to newly created project.
       * Delete dirty project record.   
       */
      public void createRemoteEntry(Model model) {
        Project dirtyProject = (Project) model;
        Project remoteProject = api.createProject(dirtyProject, app);
        Project newProject = dbAdapter.createProject(remoteProject);
        
        Cursor tasks = dbAdapter.findAllTasksByProjectLocalId(dirtyProject._id);
        while (tasks.moveToNext()) {
          Task task = ORM.mapTask(tasks, dbAdapter);
          task.project = newProject;
          dbAdapter.updateTask(task);
        }
        dbAdapter.deleteProject(dirtyProject._id);
        tasks.close();
      }
      
      public Model createLocalEntry(Model model) {
        Log.d(TAG, "creating local project: " + ((Project) model).client_project_name);
        return dbAdapter.createProject((Project) model);
      }
      
      public Model mapEntryFromCursor(Cursor cursor) {
        return ORM.mapProject(cursor, dbAdapter);
      }
      
      public void broadcastSyncCompleted() {
        Intent intent = new Intent(SYNC_COMPLETED);
        intent.putExtra(COLLECTION, PROJECTS_COMPLETED);
        sendBroadcast(intent);
      }      
      
    });
  }
  
  public void syncClients() {
    Log.d(TAG, "#syncClients starting to sync.");
    
    sync(dbAdapter.findAllClients(), api.fetchClients(), new SyncProxy() {
      
      public void updateRemoteEntry(Model model) {

      }
      
      public void updateLocalEntry(Model model) {
        dbAdapter.updateClient((Client) model);
      }
      
      public Model getLocalEntry(long remoteId) {
        return dbAdapter.findClientByRemoteId(remoteId);
      }
      
      public DeletedModel getLocalDeletedEntry(long remoteId) {
        return null;
      }
      
      public void deleteRemoteEntry(long id) { }
      
      public void deleteLocalEntry(long _id) {
        dbAdapter.deleteClient(_id);
      }
      
      public void deleteLocalDeletedEntry(long _id) { }
      
      public void createRemoteEntry(Model model) { }
      
      public Model createLocalEntry(Model model) {
        Log.d(TAG, "creating local client: " + ((Client) model).name);        
        return dbAdapter.createClient((Client) model);
      }
      
      public Model mapEntryFromCursor(Cursor cursor) {
        return ORM.mapClient(cursor, dbAdapter);
      }
      
      public void broadcastSyncCompleted() {
        Intent intent = new Intent(SYNC_COMPLETED);
        intent.putExtra(COLLECTION, CLIENTS_COMPLETED);
        sendBroadcast(intent);
      }      
      
    });
  }
  
  public void syncTags() {
    Log.d(TAG, "#syncTags starting to sync.");
    
    sync(dbAdapter.findAllTags(), api.fetchTags(), new SyncProxy() {
      
      public void updateRemoteEntry(Model model) {
        
      }
      
      public void updateLocalEntry(Model model) {
        dbAdapter.updateTag((Tag) model);
      }
      
      public Model getLocalEntry(long remoteId) {
        return dbAdapter.findTagByRemoteId(remoteId);
      }
      
      public DeletedModel getLocalDeletedEntry(long remoteId) {
        return null;
      }
      
      public void deleteRemoteEntry(long id) { }
      
      public void deleteLocalEntry(long _id) {
        dbAdapter.deleteTag(_id);
      }
      
      public void deleteLocalDeletedEntry(long _id) { }
      
      public void createRemoteEntry(Model model) { }
      
      public Model createLocalEntry(Model model) {
        Log.d(TAG, "creating local tag: " + ((Tag) model).name);        
        return dbAdapter.createTag((Tag) model);
      }
      
      public Model mapEntryFromCursor(Cursor cursor) {
        return ORM.mapTag(cursor, dbAdapter);
      }
      
      public void broadcastSyncCompleted() {
        Intent intent = new Intent(SYNC_COMPLETED);
        intent.putExtra(COLLECTION, TAGS_COMPLETED);
        sendBroadcast(intent);
      }      
      
    });
  }
  
  public void syncWorkspaces() {
    Log.d(TAG, "#syncWorkspaces starting to sync.");
    
    sync(dbAdapter.findAllWorkspaces(), api.fetchWorkspaces(), new SyncProxy() {
      
      public void updateRemoteEntry(Model model) { }
      
      public void updateLocalEntry(Model model) {
        dbAdapter.updateWorkspace((Workspace) model);
      }
      
      public Model getLocalEntry(long remoteId) {
        return dbAdapter.findWorkspaceByRemoteId(remoteId);
      }
      
      public DeletedModel getLocalDeletedEntry(long remoteId) {
        return null;
      }
      
      public void deleteRemoteEntry(long id) { }
      
      public void deleteLocalEntry(long _id) {
        dbAdapter.deleteWorkspace(_id);
      }
      
      public void deleteLocalDeletedEntry(long _id) { }
      
      public void createRemoteEntry(Model model) { }
      
      public Model createLocalEntry(Model model) {
        Log.d(TAG, "creating local workspace: " + ((Workspace) model).name);
        Workspace created = dbAdapter.createWorkspace((Workspace) model);
        Log.d(TAG, "created workspace:" + created);
        return created;
      }
      
      public Model mapEntryFromCursor(Cursor cursor) {
        return ORM.mapWorkspace(cursor);
      }
      
      public void broadcastSyncCompleted() {
        Intent intent = new Intent(SYNC_COMPLETED);
        intent.putExtra(COLLECTION, WORKSPACES_COMPLETED);
        sendBroadcast(intent);
      }      
      
    });
  }
  
  public void syncPlannedTasks() {
    Log.d(TAG, "#syncPlannedTasks starting to sync.");
    
    sync(dbAdapter.findAllPlannedTasks(), api.fetchPlannedTasks(), new SyncProxy() {
      
      public void updateRemoteEntry(Model model) { }
      
      public void updateLocalEntry(Model model) {
        dbAdapter.updatePlannedTask((PlannedTask) model);
      }
      
      public Model getLocalEntry(long remoteId) {
        return dbAdapter.findPlannedTaskByRemoteId(remoteId);
      }
      
      public DeletedModel getLocalDeletedEntry(long remoteId) {
        return null;
      }
      
      public void deleteRemoteEntry(long id) { }
      
      public void deleteLocalEntry(long _id) {
        dbAdapter.deletePlannedTask(_id);
      }
      
      public void deleteLocalDeletedEntry(long _id) { }
      
      public void createRemoteEntry(Model model) { }
      
      public Model createLocalEntry(Model model) {
        return dbAdapter.createPlannedTask((PlannedTask) model);
      }
      
      public Model mapEntryFromCursor(Cursor cursor) {
        return ORM.mapPlannedTask(cursor, dbAdapter);
      }
      
      public void broadcastSyncCompleted() {
        Intent intent = new Intent(SYNC_COMPLETED);
        intent.putExtra(COLLECTION, PLANNED_TASKS_COMPLETED);
        sendBroadcast(intent);
      }      
      
    });
  }
  
  public void sync(Cursor localCursor, List<? extends Model> remoteEntries, SyncProxy proxy) {
    LinkedList<Long> processedEntries = new LinkedList<Long>();
    
    for(Model remoteEntry : remoteEntries) {
      Model localEntry = proxy.getLocalEntry(remoteEntry.id);
      
      if(localEntry == null) { // 2.1: local entry not found
        DeletedModel deletedEntry = proxy.getLocalDeletedEntry(remoteEntry.id); 
        
        if(deletedEntry != null) { // entry has been deleted locally
          proxy.deleteRemoteEntry(remoteEntry.id);
          proxy.deleteLocalDeletedEntry(deletedEntry._id);
        }
        else { // entry does not exist, nor is deleted
          localEntry = proxy.createLocalEntry(remoteEntry);
          processedEntries.add(localEntry._id);
        }
      }
      else { // 2.2: local entry found
        if(localEntry.sync_dirty) {
          proxy.updateRemoteEntry(localEntry);

          localEntry.sync_dirty = false;
          proxy.updateLocalEntry(localEntry);
        }
        else {
          if(!localEntry.identicalTo(remoteEntry)) {
            // remote entry has changed, updating local entry
            localEntry.updateAttributes(remoteEntry);
            proxy.updateLocalEntry(localEntry);
          }
        }
        processedEntries.add(localEntry._id);
      }
    }
    
    // iterate all local entries
    while(localCursor.moveToNext()) {
      // only id for efficiency
      long _id = localCursor.getLong(localCursor.getColumnIndex(BaseColumns._ID));

      // only process entries that has not yet been processed
      if(!processedEntries.contains(_id)) {
        // two options: either deleted from server, or created locally
        Model localEntry = proxy.mapEntryFromCursor(localCursor);
        
        if(localEntry.sync_dirty) { // created locally
          proxy.createRemoteEntry(localEntry);
          localEntry.sync_dirty = false;
          proxy.updateLocalEntry(localEntry);
        }
        else { // deleted on server 
          proxy.deleteLocalEntry(_id);
        }
      }
    }

    localCursor.close();
    proxy.broadcastSyncCompleted();
  }
  
  private ServiceConnection trackingConnection = new ServiceConnection() {

    public void onServiceConnected(ComponentName name, IBinder service) {
      TimeTrackingService.TimeTrackingBinder binding = (TimeTrackingService.TimeTrackingBinder) service;
      trackingService = binding.getService();
    }

    public void onServiceDisconnected(ComponentName name) {}

  };
  
  private void syncAllModels() {
    syncWorkspaces();
    syncClients();
    syncTags();
    syncProjects();
    syncTasks();
    syncPlannedTasks();    
  }
  
  /**
   * Only starts tracking if timer service is not already tracking
   * a task, any task. 
   */
  private void triggerTracking(Task task) {
    if (!trackingService.isTracking()) {
      trackingService.startTracking(task);
    }
  }
  
}

