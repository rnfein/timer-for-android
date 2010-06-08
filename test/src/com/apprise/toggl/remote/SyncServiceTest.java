package com.apprise.toggl.remote;

import java.util.LinkedList;

import com.apprise.toggl.Toggl;
import com.apprise.toggl.TogglTests;
import com.apprise.toggl.mock.MockModel;
import com.apprise.toggl.mock.MockToggl;
import com.apprise.toggl.storage.DatabaseAdapter;
import com.apprise.toggl.storage.models.DeletedModel;
import com.apprise.toggl.storage.models.Model;
import com.apprise.toggl.storage.models.Task;
import com.apprise.toggl.storage.models.User;

import android.content.Intent;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.test.ServiceTestCase;
import android.util.Log;

public class SyncServiceTest extends ServiceTestCase<SyncService> {
  
  public SyncServiceTest() {
    super(SyncService.class);
  }

  private static final String TAG = "SyncServiceTest";
  private DatabaseAdapter dbAdapter;
  
  private SyncService service;
  
  @Override
  protected void setUp() throws Exception {
    Toggl app = new MockToggl();
    setApplication(app);
    
    SyncService.SyncBinder binder = (SyncService.SyncBinder) bindService(new Intent(getContext(), SyncService.class));
    service = binder.getService();

    dbAdapter = new DatabaseAdapter(getContext(), app);
    dbAdapter.setDatabaseName(TogglTests.TEST_DATABASE_NAME);
    dbAdapter.open();
    
    User user = new User();
    user.id = 123;
    user.email = "syncuser@toggl.com";
    User createdUser = dbAdapter.createUser(user);
    app.logIn(createdUser);
    
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    dbAdapter.close();
    getContext().deleteDatabase(TogglTests.TEST_DATABASE_NAME);
    super.tearDown();
  }
  
  /**
   * 1, 2, 2.1, b
   */
  public void testSyncScenarioOne() {
    LinkedList<Model> remoteEntries = new LinkedList<Model>();
    Model remoteEntry = new MockModel();
    remoteEntry.id = 3l;
    remoteEntries.add(remoteEntry);
    
    final int[] steps = new int[3];
    
    service.sync(dbAdapter.findAllTasks(), remoteEntries, new SyncProxy() {
      
      public void updateRemoteEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#updateRemoteEntry invoked.");
      }
      
      public void updateLocalEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#updateLocalEntry invoked.");
      }
      
      public Model mapEntryFromCursor(Cursor cursor) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#mapEntryFromCursor invoked.");
        return null;
      }
      
      public Model getLocalEntry(long remoteId) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#getLocalEntry invoked.");
        steps[0] = 1;
        assertEquals(3l, remoteId);
        return null;
      }
      
      public DeletedModel getLocalDeletedEntry(long remoteId) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#getLocalDeletedEntry invoked.");
        steps[1] = 2;
        assertEquals(3l, remoteId);
        return null;
      }
      
      public void deleteRemoteEntry(long id) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#deleteRemoteEntry invoked.");  
      }
      
      public void deleteLocalEntry(long _id) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#deleteLocalEntry invoked.");
      }
      
      public void deleteLocalDeletedEntry(long _id) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#deleteLocalDeletedEntry invoked.");
      }
      
      public void createRemoteEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#createRemoteEntry invoked.");
      }
      
      public Model createLocalEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#createLocalEntry invoked.");
        steps[2] = 3;
        assertEquals(0l, model._id);
        assertEquals(3l, model.id);
        // random, to avoid nullpointer
        model._id = 5l;
        return model;
      }
      
      public void broadcastSyncCompleted() {
        Log.d(TAG, "SyncServiceTest#SyncProxy#broadcastSyncCompleted invoked.");        
      };
    });
    
    assertEquals(1, steps[0]);
    assertEquals(2, steps[1]);
    assertEquals(3, steps[2]);
    Log.d(TAG, "SyncServiceTest#testSync done.");
  }

  /**
   * 1, 2, 2.1, a
   */
  public void testSyncScenarioTwo() {
    LinkedList<Model> remoteEntries = new LinkedList<Model>();
    Model remoteEntry = new MockModel();
    remoteEntry.id = 3l;
    remoteEntries.add(remoteEntry);
    
    final int[] steps = new int[4];
    
    service.sync(dbAdapter.findAllTasks(), remoteEntries, new SyncProxy() {
      
      public void updateRemoteEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#updateRemoteEntry invoked.");
      }
      
      public void updateLocalEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#updateLocalEntry invoked.");
      }
      
      public Model mapEntryFromCursor(Cursor cursor) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#mapEntryFromCursor invoked.");
        return null;
      }
      
      public Model getLocalEntry(long remoteId) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#getLocalEntry invoked.");
        steps[0] = 1;
        assertEquals(3l, remoteId);
        return null;
      }
      
      public DeletedModel getLocalDeletedEntry(long remoteId) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#getLocalDeletedEntry invoked.");
        steps[1] = 2;
        assertEquals(3l, remoteId);
        // dummy, found a deleted entry!
        DeletedModel deleted = new DeletedModel() {};
        deleted._id = 6l;
        return deleted;
      }
      
      public void deleteRemoteEntry(long id) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#deleteRemoteEntry invoked.");
        steps[2] = 3;
        assertEquals(3l, id);
      }
      
      public void deleteLocalEntry(long _id) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#deleteLocalEntry invoked.");
      }
      
      public void deleteLocalDeletedEntry(long _id) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#deleteLocalDeletedEntry invoked.");
        steps[3] = 4;
        assertEquals(6l, _id);
      }
      
      public void createRemoteEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#createRemoteEntry invoked.");
      }
      
      public Model createLocalEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#createLocalEntry invoked.");
        return null;
      }
      
      public void broadcastSyncCompleted() {
        Log.d(TAG, "SyncServiceTest#SyncProxy#broadcastSyncCompleted invoked.");        
      };      
    });
    
    assertEquals(1, steps[0]);
    assertEquals(2, steps[1]);
    assertEquals(3, steps[2]);
    assertEquals(4, steps[3]);
    Log.d(TAG, "SyncServiceTest#testSync done.");
  }
  
  /**
   * 1, 2, 2.2, clean
   */
  public void testSyncScenarioThree() {
    LinkedList<Model> remoteEntries = new LinkedList<Model>();
    Model remoteEntry = new MockModel();
    remoteEntry.id = 3l;
    remoteEntries.add(remoteEntry);
    
    final int[] steps = new int[2];
    
    service.sync(dbAdapter.findAllTasks(), remoteEntries, new SyncProxy() {
      
      public void updateRemoteEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#updateRemoteEntry invoked.");
      }
      
      public void updateLocalEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#updateLocalEntry invoked.");
        steps[1] = 2;
        assertFalse(model.sync_dirty);
      }
      
      public Model mapEntryFromCursor(Cursor cursor) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#mapEntryFromCursor invoked.");
        return null;
      }
      
      public Model getLocalEntry(long remoteId) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#getLocalEntry invoked.");
        steps[0] = 1;
        assertEquals(3l, remoteId);
        Model localEntry = new MockModel();
        localEntry.id = remoteId;
        localEntry.sync_dirty = false;
        return localEntry;
      }
      
      public DeletedModel getLocalDeletedEntry(long remoteId) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#getLocalDeletedEntry invoked.");
        return null;
      }
      
      public void deleteRemoteEntry(long id) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#deleteRemoteEntry invoked.");
      }
      
      public void deleteLocalEntry(long _id) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#deleteLocalEntry invoked.");
      }
      
      public void deleteLocalDeletedEntry(long _id) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#deleteLocalDeletedEntry invoked.");
      }
      
      public void createRemoteEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#createRemoteEntry invoked.");
      }
      
      public Model createLocalEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#createLocalEntry invoked.");
        return null;
      }
      
      public void broadcastSyncCompleted() {
        Log.d(TAG, "SyncServiceTest#SyncProxy#broadcastSyncCompleted invoked.");        
      };      
    });
    
    assertEquals(1, steps[0]);
    assertEquals(2, steps[1]);
    Log.d(TAG, "SyncServiceTest#testSync done.");
  }
  
  /**
   * 1, 2, 2.2, clean
   * Skip sync of already processed local entry
   */
  public void testSyncScenarioFour() {
    Task task = dbAdapter.createTask(new Task());
    final long existingTaskId = task._id;
    Cursor localCursor = dbAdapter.findAllTasks();
    
    LinkedList<Model> remoteEntries = new LinkedList<Model>();
    Model remoteEntry = new MockModel();
    remoteEntry.id = 3l;
    remoteEntries.add(remoteEntry);
    
    final int[] steps = new int[2];
    
    service.sync(localCursor, remoteEntries, new SyncProxy() {
      
      public void updateRemoteEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#updateRemoteEntry invoked.");
      }
      
      public void updateLocalEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#updateLocalEntry invoked.");
        steps[1] = 2;
        assertFalse(model.sync_dirty);
      }
      
      public Model mapEntryFromCursor(Cursor cursor) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#mapEntryFromCursor invoked.");
        // should not be here!
        assertTrue(false);
        return null;
      }
      
      public Model getLocalEntry(long remoteId) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#getLocalEntry invoked.");
        steps[0] = 1;
        assertEquals(3l, remoteId);
        Model localEntry = new MockModel();
        localEntry._id = existingTaskId;
        localEntry.id = remoteId;
        localEntry.sync_dirty= false;
        return localEntry;
      }
      
      public DeletedModel getLocalDeletedEntry(long remoteId) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#getLocalDeletedEntry invoked.");
        return null;
      }
      
      public void deleteRemoteEntry(long id) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#deleteRemoteEntry invoked.");
      }
      
      public void deleteLocalEntry(long _id) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#deleteLocalEntry invoked.");
      }
      
      public void deleteLocalDeletedEntry(long _id) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#deleteLocalDeletedEntry invoked.");
      }
      
      public void createRemoteEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#createRemoteEntry invoked.");
      }
      
      public Model createLocalEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#createLocalEntry invoked.");
        return null;
      }
      
      public void broadcastSyncCompleted() {
        Log.d(TAG, "SyncServiceTest#SyncProxy#broadcastSyncCompleted invoked.");        
      };      
    });
    
    assertEquals(1, steps[0]);
    assertEquals(2, steps[1]);
    Log.d(TAG, "SyncServiceTest#testSync done.");
  }
  
  /**
   * Sync local entry that was not included in remote entries set.
   * Local entry is dirty, should thereby be deleted from remote.
   */
  public void testSyncScenarioFive() {
    Task task = dbAdapter.createTask(new Task());
    final long existingTaskId = task._id;
    Cursor localCursor = dbAdapter.findAllTasks();
    
    final int[] steps = new int[3];
    
    service.sync(localCursor, new LinkedList<Model>(), new SyncProxy() {
      
      public void updateRemoteEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#updateRemoteEntry invoked.");
      }
      
      public void updateLocalEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#updateLocalEntry invoked.");
        steps[2] = 3;
        assertFalse(model.sync_dirty);
        assertEquals(existingTaskId, model._id);
      }
      
      public Model mapEntryFromCursor(Cursor cursor) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#mapEntryFromCursor invoked.");
        steps[0] = 1;
        long _id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        assertEquals(existingTaskId, _id);
        
        // return dirty mock, with the passed in cursor id
        MockModel entryModel = new MockModel();
        entryModel._id = _id;
        entryModel.sync_dirty = true;
        return entryModel;
      }
      
      public Model getLocalEntry(long remoteId) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#getLocalEntry invoked.");
        return null;
      }
      
      public DeletedModel getLocalDeletedEntry(long remoteId) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#getLocalDeletedEntry invoked.");
        return null;
      }
      
      public void deleteRemoteEntry(long id) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#deleteRemoteEntry invoked.");
      }
      
      public void deleteLocalEntry(long _id) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#deleteLocalEntry invoked.");
      }
      
      public void deleteLocalDeletedEntry(long _id) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#deleteLocalDeletedEntry invoked.");
      }
      
      public void createRemoteEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#createRemoteEntry invoked.");
        steps[1] = 2;
        assertTrue(model.sync_dirty);
        assertEquals(existingTaskId, model._id);
      }
      
      public Model createLocalEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#createLocalEntry invoked.");
        return null;
      }
      
      public void broadcastSyncCompleted() {
        Log.d(TAG, "SyncServiceTest#SyncProxy#broadcastSyncCompleted invoked.");        
      };      
    });
    
    assertEquals(1, steps[0]);
    assertEquals(2, steps[1]);
    assertEquals(3, steps[2]);
    Log.d(TAG, "SyncServiceTest#testSync done.");
  }
  
  /**
   * Sync local entry that was not included in remote entries set.
   * Local entry is clean, should thereby be deleted.
   */
  public void testSyncScenarioSix() {
    Task task = dbAdapter.createTask(new Task());
    final long existingTaskId = task._id;
    Cursor localCursor = dbAdapter.findAllTasks();
    
    final int[] steps = new int[2];
    
    service.sync(localCursor, new LinkedList<Model>(), new SyncProxy() {
      
      public void updateRemoteEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#updateRemoteEntry invoked.");
      }
      
      public void updateLocalEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#updateLocalEntry invoked.");
      }
      
      public Model mapEntryFromCursor(Cursor cursor) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#mapEntryFromCursor invoked.");
        steps[0] = 1;
        long _id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        assertEquals(existingTaskId, _id);
        
        // return dirty mock, with the passed in cursor id
        MockModel entryModel = new MockModel();
        entryModel._id = _id;
        entryModel.sync_dirty = false;
        return entryModel;
      }
      
      public Model getLocalEntry(long remoteId) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#getLocalEntry invoked.");
        return null;
      }
      
      public DeletedModel getLocalDeletedEntry(long remoteId) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#getLocalDeletedEntry invoked.");
        return null;
      }
      
      public void deleteRemoteEntry(long id) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#deleteRemoteEntry invoked.");
      }
      
      public void deleteLocalEntry(long _id) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#deleteLocalEntry invoked.");
        steps[1] = 2;
        assertEquals(existingTaskId, _id);
      }
      
      public void deleteLocalDeletedEntry(long _id) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#deleteLocalDeletedEntry invoked.");
      }
      
      public void createRemoteEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#createRemoteEntry invoked.");
      }
      
      public Model createLocalEntry(Model model) {
        Log.d(TAG, "SyncServiceTest#SyncProxy#createLocalEntry invoked.");
        return null;
      }
      
      public void broadcastSyncCompleted() {
        Log.d(TAG, "SyncServiceTest#SyncProxy#broadcastSyncCompleted invoked.");        
      };      
    });
    
    assertEquals(1, steps[0]);
    assertEquals(2, steps[1]);
    Log.d(TAG, "SyncServiceTest#testSync done.");
  }
  
}
