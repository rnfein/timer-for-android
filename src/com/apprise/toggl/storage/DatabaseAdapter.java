package com.apprise.toggl.storage;

import java.util.Date;

import com.apprise.toggl.Toggl;
import com.apprise.toggl.Util;
import com.apprise.toggl.storage.models.Client;
import com.apprise.toggl.storage.models.DeletedTask;
import com.apprise.toggl.storage.models.PlannedTask;
import com.apprise.toggl.storage.models.Project;
import com.apprise.toggl.storage.models.Task;
import com.apprise.toggl.storage.models.User;
import com.apprise.toggl.storage.models.Workspace;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseAdapter {

  public static final String DEFAULT_DATABASE_NAME = "toggl.db";
  private static final String TAG = "DatabaseAdapter";
  private static final int DATABASE_VERSION = 1;
  
  private SQLiteDatabase db;
  private Context context;
  private DatabaseOpenHelper dbHelper;
  private String databaseName;
  private Toggl app;

  public DatabaseAdapter(Context context, Toggl app) {
    this.context = context;
    this.app = app; 
  }
  
  public void close() {
    db.close();
  }
  
  public void open() throws SQLiteException {
    this.dbHelper = new DatabaseOpenHelper(this.context, getDatabaseName(), null, DATABASE_VERSION);
    try {
      db = dbHelper.getWritableDatabase();
    } catch (SQLiteException e) {
      Log.e(TAG, "Couldn't open a writable DB, returning a readable.", e);
      db = dbHelper.getReadableDatabase();
    }
  }

  public User createUser(User user) {
    ContentValues values = setUserValues(user);
    
    long _id = db.insert(Users.TABLE_NAME, Users.EMAIL, values);
    return findUser(_id);
  }  
  
  public User findUser(long _id) {
    Cursor cursor = getMovedCursorWithoutOwner(Users.TABLE_NAME, Users._ID, _id);
    User user = ORM.mapUser(cursor);
    safeClose(cursor);
    return user;
  }
  
  public boolean updateUser(User user) {
    ContentValues values = setUserValues(user);
    
    int affectedRows = db.update(Users.TABLE_NAME, values, Users._ID + " = " + user._id, null);
    return affectedRows == 1;
  }  
  
  public User findUserByRemoteId(long remoteId) {
    Cursor cursor = getMovedCursorWithoutOwner(Users.TABLE_NAME, Users.REMOTE_ID, remoteId);
    User user = ORM.mapUser(cursor);
    safeClose(cursor);
    return user;
  }  
  
  public User findUserByApiToken(String apiToken) {
    Cursor cursor = getMovedCursorByStringWithoutOwner(Users.TABLE_NAME, Users.API_TOKEN, apiToken);
    User user = ORM.mapUser(cursor);
    safeClose(cursor);
    return user;
  }  
  
  public Cursor findAllUsers() {
    return db.query(Users.TABLE_NAME, null, null, null, null, null, null);
  }  

  public int deleteUser(long _id) {
    return delete(Users.TABLE_NAME, Users._ID, _id);
  }
  
  private ContentValues setUserValues(User user) {
    ContentValues values = new ContentValues();
    values.put(Users.JQUERY_TIMEOFDAY_FORMAT, user.jquery_timeofday_format);
    values.put(Users.API_TOKEN, user.api_token);
    values.put(Users.TASK_RETENTION_DAYS, user.task_retention_days);
    values.put(Users.JQUERY_DATE_FORMAT, user.jquery_date_format);
    values.put(Users.DATE_FORMAT, user.date_format);
    values.put(Users.DEFAULT_WORKSPACE_ID, user.default_workspace_id);
    values.put(Users.NEW_TASKS_START_AUTOMATICALLY, user.new_tasks_start_automatically);
    values.put(Users.FULLNAME, user.fullname);
    values.put(Users.LANGUAGE, user.language);
    values.put(Users.REMOTE_ID, user.id);
    values.put(Users.BEGINNING_OF_WEEK, user.beginning_of_week);
    values.put(Users.TIMEODAY_FORMAT, user.timeofday_format);
    values.put(Users.EMAIL, user.email);
    
    return values;
  }
  
  public Workspace createWorkspace(Workspace workspace) {
    ContentValues values = setWorkspaceValues(workspace);

    long _id = db.insert(Workspaces.TABLE_NAME, Workspaces.NAME, values);
    return findWorkspace(_id);
  }

  public Workspace findWorkspace(long _id) {
    Cursor cursor = getMovedCursor(Workspaces.TABLE_NAME, Workspaces._ID, _id);
    Workspace workspace = ORM.mapWorkspace(cursor);
    safeClose(cursor);
    return workspace;
  }
  
  public boolean updateWorkspace(Workspace workspace) {
    ContentValues values = setWorkspaceValues(workspace);
    
    int affectedRows = db.update(Workspaces.TABLE_NAME, values, Workspaces._ID + " = " + workspace._id, null);
    return affectedRows == 1;    
  }
  
  public Workspace findWorkspaceByRemoteId(long remoteId) {
    Cursor cursor = getMovedCursor(Workspaces.TABLE_NAME, Workspaces.REMOTE_ID, remoteId);
    Workspace workspace = ORM.mapWorkspace(cursor);
    safeClose(cursor);
    return workspace;
  }
  
  public Cursor findAllWorkspaces() {
    return db.query(Workspaces.TABLE_NAME, null, " owner_user_id = ? ", new String[]{ String.valueOf(app.getCurrentUser()._id)}, null, null, null);    
  }
  
  public int deleteWorkspace(long _id) {
    return delete(Workspaces.TABLE_NAME, Workspaces._ID, _id);
  }  

  private ContentValues setWorkspaceValues(Workspace workspace) {
    ContentValues values = new ContentValues();
    values.put(Workspaces.OWNER_USER_ID, app.getCurrentUser()._id);
    values.put(Workspaces.NAME, workspace.name);
    values.put(Workspaces.REMOTE_ID, workspace.id);
    
    return values;
  }
  
  public Project createProject(Project project) {
    ContentValues values = setProjectValues(project);
    
    long _id = db.insert(Projects.TABLE_NAME, Projects.NAME, values);
    return findProject(_id);    
  }
  
  public Project createDirtyProject() {
    Project dirtyProject = new Project();
    dirtyProject.sync_dirty = true;
    return createProject(dirtyProject);
  }
  
  public Project findProject(long _id) {
    Cursor cursor = getMovedCursor(Projects.TABLE_NAME, Projects._ID, _id);
    Project project = ORM.mapProject(cursor, this);
    safeClose(cursor);
    return project;
  }
  
  public Project findProjectByRemoteId(long remoteId) {
    Cursor cursor = getMovedCursor(Projects.TABLE_NAME, Projects.REMOTE_ID, remoteId);
    Project project = ORM.mapProject(cursor, this);
    safeClose(cursor);
    return project;
  }  
  
  public boolean updateProject(Project project) {
    ContentValues values = setProjectValues(project);
    
    int affectedRows = db.update(Projects.TABLE_NAME, values, Projects._ID + " = " + project._id, null);
    return affectedRows == 1;    
  } 
  
  public Cursor findAllProjects() {
    return db.query(Projects.TABLE_NAME, null, " owner_user_id = ? ", new String[]{ String.valueOf(app.getCurrentUser()._id)}, null, null, null);        
  }
  
  public int deleteProject(long _id) {
    return delete(Projects.TABLE_NAME, Projects._ID, _id);    
  }
  
  private ContentValues setProjectValues(Project project) {
    ContentValues values = new ContentValues();
    values.put(Projects.OWNER_USER_ID, app.getCurrentUser()._id);    
    values.put(Projects.BILLABLE, project.billable);
    values.put(Projects.CLIENT_PROJECT_NAME, project.client_project_name);
    values.put(Projects.ESTIMATED_WORKHOURS, project.estimated_workhours);
    values.put(Projects.FIXED_FEE, project.fixed_fee);
    values.put(Projects.HOURLY_RATE, project.hourly_rate);
    values.put(Projects.IS_FIXED_FEE, project.is_fixed_fee);
    values.put(Projects.NAME, project.name);
    values.put(Projects.REMOTE_ID, project.id);
    values.put(Projects.SYNC_DIRTY, project.sync_dirty);

    if (project.workspace != null) values.put(Projects.WORKSPACE_REMOTE_ID, project.workspace.id);
    
    return values;
  }

  public Client createClient(Client client) {
    ContentValues values = setClientValues(client);
    
    long _id = db.insert(Clients.TABLE_NAME, Clients.NAME, values);
    return findClient(_id);    
  }
  
  public Client findClient(long _id) {
    Cursor cursor = getMovedCursor(Clients.TABLE_NAME, Clients._ID, _id);
    Client client = ORM.mapClient(cursor, this);
    safeClose(cursor);
    return client;
  }
  
  public Client findClientByRemoteId(long remoteId) {
    Cursor cursor = getMovedCursor(Clients.TABLE_NAME, Clients.REMOTE_ID, remoteId);
    Client client = ORM.mapClient(cursor, this);
    safeClose(cursor);
    return client;
  }  
  
  public boolean updateClient(Client client) {
    ContentValues values = setClientValues(client);
    
    int affectedRows = db.update(Clients.TABLE_NAME, values, Clients._ID + " = " + client._id, null);
    return affectedRows == 1;    
  } 
  
  public Cursor findAllClients() {
    return db.query(Clients.TABLE_NAME, null, " owner_user_id = ? ", new String[]{ String.valueOf(app.getCurrentUser()._id)}, null, null, null);        
  }
  
  public int deleteClient(long _id) {
    return delete(Clients.TABLE_NAME, Clients._ID, _id);    
  }  
  
  private ContentValues setClientValues(Client client) {
    ContentValues values = new ContentValues();
    values.put(Clients.OWNER_USER_ID, app.getCurrentUser()._id);    
    values.put(Clients.NAME, client.name);
    values.put(Clients.HOURLY_RATE, client.hourly_rate);
    values.put(Clients.CURRENCY, client.currency);
    
    if (client.workspace != null) values.put(Clients.WORKSPACE_REMOTE_ID, client.workspace.id);
    
    return values;
  }
  
  public Task createTask(Task task) {
    ContentValues values = setTaskValues(task);
    
    long _id = db.insert(Tasks.TABLE_NAME, Tasks.DESCRIPTION, values);
    return findTask(_id);
  }
  
  public Task createDirtyTask() {
    Task dirtyTask = new Task();
    dirtyTask.sync_dirty = true;
    return createTask(dirtyTask);
  }
  
  public Task findTask(long _id) {
    Cursor cursor = getMovedCursor(Tasks.TABLE_NAME, Tasks._ID, _id);
    Task task = ORM.mapTask(cursor, this);
    safeClose(cursor);
    return task;
  }
  
  public Task findTaskByRemoteId(long remoteId) {
    Cursor cursor = getMovedCursor(Tasks.TABLE_NAME, Tasks.REMOTE_ID, remoteId);
    Task task = ORM.mapTask(cursor, this);
    safeClose(cursor);
    return task;
  }
  
  public Cursor findTasksForListByDate(Date date) {
    String dateString = Util.formatDateToString(date); 
    Cursor cursor = db.rawQuery("SELECT " 
        + Tasks.TABLE_NAME + "." + Tasks._ID + ", "
        + Tasks.TABLE_NAME + "." + Tasks.DESCRIPTION + ", "
        + Tasks.TABLE_NAME + "." + Tasks.DURATION + ", "
        + Projects.TABLE_NAME + "." + Projects.CLIENT_PROJECT_NAME
        + " FROM " + Tasks.TABLE_NAME 
        + " LEFT OUTER JOIN projects ON " + Tasks.TABLE_NAME + "." + Tasks.PROJECT_REMOTE_ID + " = " + Projects.TABLE_NAME + "." + Projects.REMOTE_ID + 
        " WHERE strftime('%Y-%m-%d', " + Tasks.START + ", 'localtime') = strftime('%Y-%m-%d', ?, 'localtime')" +
        " AND " + Tasks.TABLE_NAME + "." + Tasks.OWNER_USER_ID + " = ? ORDER BY start", new String[] { String.valueOf(dateString), String.valueOf(app.getCurrentUser()._id) });

    return cursor;
  }  
  
  public Cursor findAllTasks() {
    return db.query(Tasks.TABLE_NAME, null, " owner_user_id = ? ", new String[]{ String.valueOf(app.getCurrentUser()._id)}, null, null, null);        
  }  
  
  public boolean updateTask(Task task) {
    ContentValues values = setTaskValues(task);
    
    int affectedRows = db.update(Tasks.TABLE_NAME, values, Tasks._ID + " = " + task._id, null);
    return affectedRows == 1;    
  }   
  
  public void deleteTask(Task task) {
    long remoteId = task.id;
    int rowsAffected = deleteTaskHard(task._id);
    if(rowsAffected > 0 && remoteId > 0) {
      // if task was successfully deleted, create an entry in the deleted table
      // with the remote id
      ContentValues values = new ContentValues();
      values.put(DeletedTasks.TASK_REMOTE_ID, remoteId);
      db.insert(DeletedTasks.TABLE_NAME, null, values);
    }
  }
  
  public int deleteTaskHard(long _id) {
    return delete(Tasks.TABLE_NAME, Tasks._ID, _id);    
  }
    
  private ContentValues setTaskValues(Task task) {
    ContentValues values = new ContentValues();
    values.put(Tasks.OWNER_USER_ID, app.getCurrentUser()._id);    
    values.put(Tasks.REMOTE_ID, task.id);
    values.put(Tasks.DESCRIPTION, task.description);    
    values.put(Tasks.BILLABLE, task.billable);
    values.put(Tasks.DURATION, task.duration);
    values.put(Tasks.START, task.start);
    values.put(Tasks.STOP, task.stop);
    values.put(Tasks.SYNC_DIRTY, task.sync_dirty);
//    values.put(Tasks.TAG_NAMES, "tag_names"); //TODO
//
    if (task.workspace != null) values.put(Tasks.WORKSPACE_REMOTE_ID, task.workspace.id);
    if (task.project != null) values.put(Tasks.PROJECT_REMOTE_ID, task.project.id); 
    
    return values;
  }
  
  public DeletedTask findDeletedTask(long taskRemoteId) {
    Cursor cursor = getMovedCursor(DeletedTasks.TABLE_NAME, DeletedTasks.TASK_REMOTE_ID, taskRemoteId);
    DeletedTask deletedTask = ORM.mapDeletedTask(cursor);
    safeClose(cursor);
    return deletedTask;
  }
  
  public Cursor findAllDeletedTasks() {
    return db.query(DeletedTasks.TABLE_NAME, null, " owner_user_id = ? ", new String[]{ String.valueOf(app.getCurrentUser()._id)}, null, null, null);     
  }
  
  public void deleteDeletedTask(long _id) {
    delete(DeletedTasks.TABLE_NAME, DeletedTasks._ID, _id);    
  }
  
  public PlannedTask createPlannedTask(PlannedTask plannedTask) {
    ContentValues values = setPlannedTaskValues(plannedTask);

    long _id = db.insert(PlannedTasks.TABLE_NAME, PlannedTasks.NAME, values);
    return findPlannedTask(_id);
  }
  
  public PlannedTask findPlannedTask(long _id) {
    Cursor cursor = getMovedCursor(PlannedTasks.TABLE_NAME, PlannedTasks._ID, _id);
    PlannedTask plannedTask = ORM.mapPlannedTask(cursor, this);
    safeClose(cursor);
    return plannedTask;    
  }
  
  public PlannedTask findPlannedTaskByRemoteId(long remoteId) {
    Cursor cursor = getMovedCursor(PlannedTasks.TABLE_NAME, PlannedTasks.REMOTE_ID, remoteId);
    PlannedTask plannedTask = ORM.mapPlannedTask(cursor, this);
    safeClose(cursor);
    return plannedTask;    
  }
  
  /*
   * finds planned tasks by project remote id
   */
  public Cursor findPlannedTasksByProjectId(long projectRemoteId) {
    return getMovedCursor(PlannedTasks.TABLE_NAME, PlannedTasks.PROJECT_REMOTE_ID, projectRemoteId);
  }
  
  public Cursor findAllPlannedTasks() {
    return db.query(PlannedTasks.TABLE_NAME, null, " owner_user_id = ? ", new String[]{ String.valueOf(app.getCurrentUser()._id)}, null, null, null);     
  }
  
  public boolean updatePlannedTask(PlannedTask plannedTask) {
    ContentValues values = setPlannedTaskValues(plannedTask);
    
    int affectedRows = db.update(PlannedTasks.TABLE_NAME, values, PlannedTasks._ID + " = " + plannedTask._id, null);
    return affectedRows == 1;    
  } 
  
  public void deletePlannedTask(long _id) {
    delete(PlannedTasks.TABLE_NAME, PlannedTasks._ID, _id);    
  }  
  
  private ContentValues setPlannedTaskValues(PlannedTask plannedTask) {
    ContentValues values = new ContentValues();
    values.put(PlannedTasks.OWNER_USER_ID, app.getCurrentUser()._id);    
    values.put(PlannedTasks.REMOTE_ID, plannedTask.id);
    values.put(PlannedTasks.ESTIMATED_WORKHOURS, plannedTask.estimated_workhours);
    values.put(PlannedTasks.NAME, plannedTask.name);
    
    if(plannedTask.project != null) values.put(PlannedTasks.PROJECT_REMOTE_ID, plannedTask.project.id);
    if(plannedTask.workspace != null) values.put(PlannedTasks.WORKSPACE_REMOTE_ID, plannedTask.workspace.id);
    if(plannedTask.user != null) values.put(PlannedTasks.USER_ID, plannedTask.user._id);

    return values;
  }
  
  private Cursor getMovedCursor(String tableName, String columnName, long value) {
    Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE owner_user_id = ? AND " 
        + columnName + " = ?", new String[] { String.valueOf(app.getCurrentUser()._id), String.valueOf(value) });
    
    if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
      cursor.close();
      return null;
    }
    return cursor;
  }
  
  private Cursor getMovedCursorWithoutOwner(String tableName, String columnName, long value) {
    Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE " 
        + columnName + " = ?", new String[] { String.valueOf(value) });
    
    if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
      cursor.close();
      return null;
    }
    return cursor;
  }
  
  private Cursor getMovedCursorByStringWithoutOwner(String tableName, String columnName, String value) {
    Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE " 
        + columnName + " = ?", new String[] { String.valueOf(value) });
    
    if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
      cursor.close();
      return null;
    }
    return cursor;
  }
  
  private int delete(String tableName, String columnName, long value) {
    return db.delete(tableName, columnName + " = ?", new String[] { String.valueOf(value) });
  }
  
  private void safeClose(Cursor cursor) {
    if(cursor != null) cursor.close();
  }
  
  public String getDatabaseName() {
    if(databaseName != null) {
      return databaseName;
    }
    return DEFAULT_DATABASE_NAME;
  }
  
  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName; 
  }
  
  public static class ORM {

    public static User mapUser(Cursor cursor) {
      if(cursor == null) return null;

      long _id = cursor.getLong(cursor.getColumnIndex(Users._ID));
      String jqueryTimeofdayFormat = cursor.getString(cursor.getColumnIndex(Users.JQUERY_TIMEOFDAY_FORMAT));
      String apiToken = cursor.getString(cursor.getColumnIndex(Users.API_TOKEN));
      int taskRetentionDays = cursor.getInt(cursor.getColumnIndex(Users.TASK_RETENTION_DAYS));
      String jqueryDateFormat = cursor.getString(cursor.getColumnIndex(Users.JQUERY_DATE_FORMAT));
      String dateFormat = cursor.getString(cursor.getColumnIndex(Users.DATE_FORMAT));
      long defaultWorkspaceId = cursor.getLong(cursor.getColumnIndex(Users.DEFAULT_WORKSPACE_ID));
      boolean newTasksStartAutomatically = (cursor.getInt(cursor.getColumnIndex(Users.NEW_TASKS_START_AUTOMATICALLY)) == 1);
      String fullname = cursor.getString(cursor.getColumnIndex(Users.FULLNAME));
      String language = cursor.getString(cursor.getColumnIndex(Users.LANGUAGE));
      long remote_id = cursor.getLong(cursor.getColumnIndex(Users.REMOTE_ID));
      int beginningOfWeek = cursor.getInt(cursor.getColumnIndex(Users.BEGINNING_OF_WEEK));
      String timeofdayFormat = cursor.getString(cursor.getColumnIndex(Users.TIMEODAY_FORMAT));
      String email = cursor.getString(cursor.getColumnIndex(Users.EMAIL));
      
      return new User(_id, jqueryTimeofdayFormat, apiToken, taskRetentionDays, jqueryDateFormat, dateFormat, defaultWorkspaceId, 
          newTasksStartAutomatically, fullname, language, remote_id, beginningOfWeek, timeofdayFormat, email);      
    }
    
    public static Workspace mapWorkspace(Cursor cursor) {
      if(cursor == null) return null;
      
      long _id = cursor.getLong(cursor.getColumnIndex(Workspaces._ID));
      String name = cursor.getString(cursor.getColumnIndex(Workspaces.NAME));
      long remote_id = cursor.getLong(cursor.getColumnIndex(Workspaces.REMOTE_ID));
      
      return new Workspace(_id, name, remote_id);      
    }
    
    public static Project mapProject(Cursor cursor, DatabaseAdapter dbAdapter) {
      if (cursor == null) return null;
      
      long _id = cursor.getLong(cursor.getColumnIndex(Projects._ID));
      long fixedFee = cursor.getLong(cursor.getColumnIndex(Projects.FIXED_FEE));
      boolean billable = (cursor.getInt(cursor.getColumnIndex(Projects.BILLABLE)) == 1);
      String clientProjectName = cursor.getString(cursor.getColumnIndex(Projects.CLIENT_PROJECT_NAME));
      long estimatedWorkhours = cursor.getLong(cursor.getColumnIndex(Projects.ESTIMATED_WORKHOURS));
      boolean isFixedFee = (cursor.getLong(cursor.getColumnIndex(Projects.IS_FIXED_FEE)) == 1);
      long hourlyRate = cursor.getLong(cursor.getColumnIndex(Projects.HOURLY_RATE));
      String name = cursor.getString(cursor.getColumnIndex(Projects.NAME));
      long remote_id = cursor.getLong(cursor.getColumnIndex(Projects.REMOTE_ID));
      long workspaceRemoteId = cursor.getLong(cursor.getColumnIndex(Projects.WORKSPACE_REMOTE_ID));
      boolean syncDirty = (cursor.getInt(cursor.getColumnIndex(Projects.SYNC_DIRTY)) == 1);
      
      return new Project(_id, fixedFee, estimatedWorkhours, isFixedFee, dbAdapter.findWorkspaceByRemoteId(workspaceRemoteId), billable, clientProjectName, hourlyRate, name, remote_id, syncDirty);
    }
    
    public static Client mapClient(Cursor cursor, DatabaseAdapter dbAdapter) {
      if (cursor == null) return null;
      
      long _id = cursor.getLong(cursor.getColumnIndex(Clients._ID));
      String name = cursor.getString(cursor.getColumnIndex(Clients.NAME));
      String hourlyRate = cursor.getString(cursor.getColumnIndex(Clients.HOURLY_RATE));
      String currency = cursor.getString(cursor.getColumnIndex(Clients.CURRENCY));
      long workspaceRemoteId = cursor.getLong(cursor.getColumnIndex(Clients.WORKSPACE_REMOTE_ID));
      
      return new Client(_id, name, dbAdapter.findWorkspaceByRemoteId(workspaceRemoteId), hourlyRate, currency);
    }
    
    public static Task mapTask(Cursor cursor, DatabaseAdapter dbAdapter) {
      if (cursor == null) return null;
      
      long _id = cursor.getLong(cursor.getColumnIndex(Tasks._ID));
      boolean billable = (cursor.getInt(cursor.getColumnIndex(Tasks.BILLABLE)) == 1);
      String description = cursor.getString(cursor.getColumnIndex(Tasks.DESCRIPTION));
      long duration = cursor.getLong(cursor.getColumnIndex(Tasks.DURATION));
      long projectRemoteId = cursor.getLong(cursor.getColumnIndex(Tasks.PROJECT_REMOTE_ID));
      long workspaceRemoteId = cursor.getLong(cursor.getColumnIndex(Tasks.WORKSPACE_REMOTE_ID));
      String start = cursor.getString(cursor.getColumnIndex(Tasks.START));
      String stop = cursor.getString(cursor.getColumnIndex(Tasks.STOP));
      long remote_id = cursor.getLong(cursor.getColumnIndex(Tasks.REMOTE_ID));
      boolean syncDirty = (cursor.getInt(cursor.getColumnIndex(Tasks.SYNC_DIRTY)) == 1);

      //TODO: String tagNames = cursor.getString(cursor.getColumnIndex(Tasks.TAG_NAMES));
      String[] tagNames = null;
      
      return new Task(_id, dbAdapter.findProjectByRemoteId(projectRemoteId), dbAdapter.findWorkspaceByRemoteId(workspaceRemoteId),
          duration, start, billable, description, stop, tagNames, remote_id, syncDirty);
    }
    
    public static DeletedTask mapDeletedTask(Cursor cursor) {
      if (cursor == null) return null;
      
      long _id = cursor.getLong(cursor.getColumnIndex(DeletedTasks._ID));
      long taskId = cursor.getLong(cursor.getColumnIndex(DeletedTasks.TASK_REMOTE_ID));
      
      return new DeletedTask(_id, taskId);
    }
    
    public static PlannedTask mapPlannedTask(Cursor cursor, DatabaseAdapter dbAdapter) {
      if(cursor == null) return null;
      
      long _id = cursor.getLong(cursor.getColumnIndex(PlannedTasks._ID));
      long remote_id = cursor.getLong(cursor.getColumnIndex(PlannedTasks.REMOTE_ID));
      String name = cursor.getString(cursor.getColumnIndex(PlannedTasks.NAME));
      long workspaceRemoteId = cursor.getLong(cursor.getColumnIndex(PlannedTasks.WORKSPACE_REMOTE_ID));
      long projectRemoteId = cursor.getLong(cursor.getColumnIndex(PlannedTasks.PROJECT_REMOTE_ID));
      long userId = cursor.getLong(cursor.getColumnIndex(PlannedTasks.USER_ID));
      long estimatedWorkhours = cursor.getLong(cursor.getColumnIndex(PlannedTasks.ESTIMATED_WORKHOURS));

      return new PlannedTask(_id, name, dbAdapter.findWorkspaceByRemoteId(workspaceRemoteId), remote_id, 
          dbAdapter.findProjectByRemoteId(projectRemoteId), dbAdapter.findUser(userId), estimatedWorkhours);
    }
    
  }
  
  private static class DatabaseOpenHelper extends SQLiteOpenHelper {

    public DatabaseOpenHelper(Context context, String name, CursorFactory factory, int version) {
      super(context, name, factory, version);
    }

    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + Users.TABLE_NAME + " ("
      + Users._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
      + Users.REMOTE_ID + " INTEGER NOT NULL,"      
      + Users.JQUERY_TIMEOFDAY_FORMAT + " TEXT,"
      + Users.API_TOKEN + " TEXT,"
      + Users.TASK_RETENTION_DAYS + " INTEGER,"
      + Users.JQUERY_DATE_FORMAT + " TEXT,"
      + Users.DATE_FORMAT + " TEXT,"
      + Users.DEFAULT_WORKSPACE_ID + " INTEGER,"
      + Users.NEW_TASKS_START_AUTOMATICALLY + " TEXT,"
      + Users.FULLNAME + " TEXT,"
      + Users.LANGUAGE + " TEXT,"
      + Users.BEGINNING_OF_WEEK + " INTEGER,"
      + Users.TIMEODAY_FORMAT + " TEXT,"
      + Users.EMAIL + " TEXT"
      + ");";

    private static final String CREATE_WORKSPACES_TABLE = "CREATE TABLE " + Workspaces.TABLE_NAME + " ("
      + Workspaces._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
      + Workspaces.OWNER_USER_ID + " INTEGER NOT NULL,"
      + Workspaces.REMOTE_ID + " INTEGER NOT NULL,"      
      + Workspaces.NAME + " TEXT"
      + ");";
    
    private static final String CREATE_PROJECTS_TABLE = "CREATE TABLE " + Projects.TABLE_NAME + " ("
      + Projects._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
      + Projects.OWNER_USER_ID + " INTEGER NOT NULL,"      
      + Projects.REMOTE_ID + " INTEGER NOT NULL,"
      + Projects.SYNC_DIRTY + " INTEGER NOT NULL,"      
      + Projects.FIXED_FEE + " INTEGER,"
      + Projects.ESTIMATED_WORKHOURS + " INTEGER,"
      + Projects.IS_FIXED_FEE + " INTEGER,"
      + Projects.WORKSPACE_REMOTE_ID + " INTEGER,"
      + Projects.BILLABLE + " INTEGER,"
      + Projects.CLIENT_PROJECT_NAME + " TEXT,"
      + Projects.HOURLY_RATE + " INTEGER,"
      + Projects.NAME + " TEXT"
      + ");";
    
    private static final String CREATE_CLIENTS_TABLE = "CREATE TABLE " + Clients.TABLE_NAME + " ("
    + Clients._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
    + Clients.OWNER_USER_ID + " INTEGER NOT NULL,"      
    + Clients.REMOTE_ID + " INTEGER NOT NULL,"
    + Clients.NAME + " TEXT,"      
    + Clients.HOURLY_RATE + " TEXT,"
    + Clients.CURRENCY + " TEXT"
    + ");";
    
    private static final String CREATE_TASKS_TABLE = "CREATE TABLE " + Tasks.TABLE_NAME + " ("
      + Tasks._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
      + Tasks.OWNER_USER_ID + " INTEGER NOT NULL,"      
      + Tasks.REMOTE_ID + " INTEGER NOT NULL,"  
      + Tasks.SYNC_DIRTY + " INTEGER NOT NULL,"  
      + Tasks.PROJECT_REMOTE_ID + " INTEGER,"
      + Tasks.WORKSPACE_REMOTE_ID + " INTEGER,"
      + Tasks.DURATION + " INTEGER,"
      + Tasks.START + " TEXT,"
      + Tasks.BILLABLE + " INTEGER,"
      + Tasks.DESCRIPTION + " TEXT,"
      + Tasks.STOP + " TEXT"
//      + Tasks.TAG_NAMES + " TEXT"
      + ");";
    
    private static final String CREATE_DELETED_TASKS_TABLE = "CREATE TABLE " + DeletedTasks.TABLE_NAME + " ("
    + DeletedTasks._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
    + DeletedTasks.OWNER_USER_ID + " INTEGER NOT NULL,"    
    + DeletedTasks.TASK_REMOTE_ID + " INTEGER NOT NULL"
    + ");";
    
    private static final String CREATE_PLANNED_TASKS_TABLE = "CREATE TABLE " + PlannedTasks.TABLE_NAME + " ("
      + PlannedTasks._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
      + PlannedTasks.OWNER_USER_ID + " INTEGER NOT NULL,"      
      + PlannedTasks.REMOTE_ID + " INTEGER NOT NULL,"      
      + PlannedTasks.NAME + " TEXT,"
      + PlannedTasks.WORKSPACE_REMOTE_ID + " INTEGER,"
      + PlannedTasks.PROJECT_REMOTE_ID + " INTEGER,"
      + PlannedTasks.USER_ID + " INTEGER,"
      + PlannedTasks.ESTIMATED_WORKHOURS + " INTEGER"
    + ");";


    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL(CREATE_USERS_TABLE);
      db.execSQL(CREATE_WORKSPACES_TABLE);      
      db.execSQL(CREATE_PROJECTS_TABLE);
      db.execSQL(CREATE_CLIENTS_TABLE);
      db.execSQL(CREATE_TASKS_TABLE);
      db.execSQL(CREATE_DELETED_TASKS_TABLE);
      db.execSQL(CREATE_PLANNED_TASKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.d(TAG, "Upgrading from version " + oldVersion + " to " + newVersion);
    }
  }

  public static final class Users implements BaseColumns {
    public static final String TABLE_NAME = "users";
    
    public static final String REMOTE_ID = "remote_id";    
    public static final String JQUERY_TIMEOFDAY_FORMAT = "jquery_timeofday_format";
    public static final String API_TOKEN = "api_token";
    public static final String TASK_RETENTION_DAYS = "task_retention_days";
    public static final String JQUERY_DATE_FORMAT = "jquery_date_format";
    public static final String DATE_FORMAT = "date_format";
    public static final String DEFAULT_WORKSPACE_ID = "default_workspace_id";
    public static final String NEW_TASKS_START_AUTOMATICALLY = "new_tasks_start_automatically";
    public static final String FULLNAME = "fullname";
    public static final String LANGUAGE = "language";
    public static final String BEGINNING_OF_WEEK = "beginning_of_week";
    public static final String TIMEODAY_FORMAT = "timeofday_format";
    public static final String EMAIL = "email";
    
  }  
  
  public static final class Workspaces implements BaseColumns {
    public static final String TABLE_NAME = "workspaces";
    
    public static final String OWNER_USER_ID = "owner_user_id";    
    public static final String REMOTE_ID = "remote_id";    
    public static final String NAME = "name";
  }
  
  public static final class Projects implements BaseColumns {
    public static final String TABLE_NAME = "projects";
    
    public static final String OWNER_USER_ID = "owner_user_id";    
    public static final String REMOTE_ID = "remote_id";
    public static final String SYNC_DIRTY = "sync_dirty";     
    public static final String FIXED_FEE = "fixed_fee";
    public static final String ESTIMATED_WORKHOURS = "estimated_workhours";
    public static final String IS_FIXED_FEE = "is_fixed_fee";
    public static final String WORKSPACE_REMOTE_ID = "workspace_remote_id";
    public static final String BILLABLE = "billable";
    public static final String CLIENT_PROJECT_NAME = "client_project_name";
    public static final String HOURLY_RATE = "hourly_rate";
    public static final String NAME = "name";   
  }
  
  public static final class Clients implements BaseColumns {
    public static final String TABLE_NAME = "clients";

    public static final String OWNER_USER_ID = "owner_user_id";
    public static final String REMOTE_ID = "remote_id";
    public static final String NAME = "name";
    public static final String WORKSPACE_REMOTE_ID = "workspace_remote_id";
    public static final String HOURLY_RATE = "hourly_rate";
    public static final String CURRENCY = "currency";
  }
  
  public static final class Tasks implements BaseColumns {
    public static final String TABLE_NAME = "tasks";

    public static final String OWNER_USER_ID = "owner_user_id";
    public static final String REMOTE_ID = "remote_id";
    public static final String SYNC_DIRTY = "sync_dirty";     
    public static final String PROJECT_REMOTE_ID = "project_remote_id";
    public static final String WORKSPACE_REMOTE_ID = "workspace_remote_id";
    public static final String DURATION = "duration";
    public static final String START = "start";
    public static final String BILLABLE = "billable";
    public static final String DESCRIPTION = "description";
    public static final String STOP = "stop";
    public static final String TAG_NAMES = "tag_names";
  }
  
  public static final class DeletedTasks implements BaseColumns {
    public static final String TABLE_NAME = "deleted_tasks";

    public static final String OWNER_USER_ID = "owner_user_id";
    public static final String TASK_REMOTE_ID = "task_remote_id";
  }
  
  public static final class PlannedTasks implements BaseColumns {
    public static final String TABLE_NAME = "planned_tasks";
    
    public static final String OWNER_USER_ID = "owner_user_id";
    public static final String REMOTE_ID = "remote_id";      
    public static final String NAME = "name";
    public static final String WORKSPACE_REMOTE_ID = "workspace_remote_id";  
    public static final String PROJECT_REMOTE_ID = "project_remote_id";
    public static final String USER_ID = "user_id";
    public static final String ESTIMATED_WORKHOURS = "estimated_workhours";
  }
  
}