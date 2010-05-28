package com.apprise.toggl.storage;

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

  public DatabaseAdapter(Context context) {
    this.context = context;
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

  
  private Cursor getCursorById(String tableName, long id) {
    Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE _ID = ?", new String[] { String.valueOf(id) });
    
    if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
      return null;
    }
    return cursor;
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
  
  private static class DatabaseOpenHelper extends SQLiteOpenHelper {

    public DatabaseOpenHelper(Context context, String name, CursorFactory factory, int version) {
      super(context, name, factory, version);
    }

    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + Users.TABLE_NAME + " ("
      + Users._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
      + Users.JQUERY_TIMEOFDAY_FORMAT + " TEXT,"
      + Users.API_TOKEN + " TEXT,"
      + Users.TASK_RETENTION_DAYS + " INTEGER,"
      + Users.JQUERY_DATE_FORMAT + " TEXT,"
      + Users.DATE_FORMAT + " TEXT,"
      + Users.DEFAULT_WORKSPACE_ID + " INTEGER,"
      + Users.NEW_TASKS_START_AUTOMATICALLY + " TEXT,"
      + Users.FULLNAME + " TEXT,"
      + Users.LANGUAGE + " TEXT,"
      + Users.REMOTE_ID + " INTEGER NOT NULL,"
      + Users.BEGINNING_OF_WEEK + " INTEGER,"
      + Users.TIMEODAY_FORMAT + " TEXT,"
      + Users.EMAIL + " TEXT"
      + ");";
    
    private static final String CREATE_PROJECTS_TABLE = "CREATE TABLE " + Projects.TABLE_NAME + " ("
      + Projects._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
      + Projects.FIXED_FEE + " INTEGER,"
      + Projects.ESTIMATED_WORKHOURS + " INTEGER,"
      + Projects.IS_FIXED_FEE + " INTEGER NOT NULL,"
      + Projects.WORKSPACE_REMOTE_ID + " INTEGER NOT NULL,"
      + Projects.BILLABLE + " INTEGER NOT NULL,"
      + Projects.CLIENT_PROJECT_NAME + " TEXT,"
      + Projects.HOURLY_RATE + " INTEGER NOT NULL,"
      + Projects.NAME + " TEXT,"
      + Projects.REMOTE_ID + " INTEGER NOT NULL,"
      + Projects.SYNC_DIRTY + " INTEGER NOT NULL"
      + ");";

    
    private static final String CREATE_TASKS_TABLE = "CREATE TABLE " + Tasks.TABLE_NAME + " ("
      + Tasks._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
      + Tasks.PROJECT_REMOTE_ID + " INTEGER NOT NULL,"
      + Tasks.WORKSPACE_REMOTE_ID + " INTEGER NOT NULL,"
      + Tasks.DURATION + " INTEGER NOT NULL,"
      + Tasks.START + " TEXT,"
      + Tasks.BILLABLE + " INTEGER NOT NULL,"
      + Tasks.DESCRIPTION + " TEXT,"
      + Tasks.STOP + " TEXT,"
      + Tasks.TAG_NAMES + " TEXT,"
      + Tasks.REMOTE_ID + " INTEGER NOT NULL"
      + ");";
    
    private static final String CREATE_DELETED_TASKS_TABLE = "CREATE TABLE " + DeletedTasks.TABLE_NAME + " ("
    + DeletedTasks._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
    + DeletedTasks.TASK_ID + " INTEGER NOT NULL"
    + ");";

    private static final String CREATE_WORKSPACES_TABLE = "CREATE TABLE " + Workspaces.TABLE_NAME + " ("
      + Workspaces._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
      + Workspaces.NAME + " TEXT,"
      + Workspaces.REMOTE_ID + " INTEGER NOT NULL"
      + ");";
    
    private static final String CREATE_PLANNED_TASKS_TABLE = "CREATE TABLE " + PlannedTasks.TABLE_NAME + " ("
      + PlannedTasks._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
      + PlannedTasks.NAME + " TEXT,"
      + PlannedTasks.WORKSPACE_REMOTE_ID + " INTEGER NOT NULL,"
      + PlannedTasks.REMOTE_ID + " INTEGER NOT NULL,"
      + PlannedTasks.PROJECT_REMOTE_ID + " INTEGER NOT NULL,"
      + PlannedTasks.USER_REMOTE_ID + " INTEGER NOT NULL,"
      + PlannedTasks.ESTIMATED_WORKHOURS + " INTEGER NOT NULL"
    + ");";


    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL(CREATE_USERS_TABLE);
      db.execSQL(CREATE_PROJECTS_TABLE);
      db.execSQL(CREATE_WORKSPACES_TABLE);
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
    
    public static final String JQUERY_TIMEOFDAY_FORMAT = "jquery_timeofday_format";
    public static final String API_TOKEN = "api_token";
    public static final String TASK_RETENTION_DAYS = "task_retention_days";
    public static final String JQUERY_DATE_FORMAT = "jquery_date_format";
    public static final String DATE_FORMAT = "date_format";
    public static final String DEFAULT_WORKSPACE_ID = "default_workspace_id";
    public static final String NEW_TASKS_START_AUTOMATICALLY = "new_tasks_start_automatically";
    public static final String FULLNAME = "fullname";
    public static final String LANGUAGE = "language";
    public static final String REMOTE_ID = "remote_id";
    public static final String BEGINNING_OF_WEEK = "beginning_of_week";
    public static final String TIMEODAY_FORMAT = "timeofday_format";
    public static final String EMAIL = "email";
    
  }  
  
  public static final class Projects implements BaseColumns {
    public static final String TABLE_NAME = "tasks";
    
    public static final String FIXED_FEE = "fixed_fee";
    public static final String ESTIMATED_WORKHOURS = "estimated_workhours";
    public static final String IS_FIXED_FEE = "is_fixed_fee";
    public static final String WORKSPACE_REMOTE_ID = "workspace_remote_id";
    public static final String BILLABLE = "billable";
    public static final String CLIENT_PROJECT_NAME = "client_project_name";
    public static final String HOURLY_RATE = "hourly_rate";
    public static final String NAME = "name";
    public static final String REMOTE_ID = "remote_id";
    public static final String SYNC_DIRTY = "sync_dirty";    
  }
  
  public static final class Workspaces implements BaseColumns {
    public static final String TABLE_NAME = "workspaces";
    
    public static final String NAME = "name";
    public static final String REMOTE_ID = "remote_id";
  }
  
  public static final class Tasks implements BaseColumns {
    public static final String TABLE_NAME = "tasks";

    public static final String PROJECT_REMOTE_ID = "project_remote_id";
    public static final String WORKSPACE_REMOTE_ID = "workspace_remote_id";
    public static final String DURATION = "duration";
    public static final String START = "start";
    public static final String BILLABLE = "billable";
    public static final String DESCRIPTION = "description";
    public static final String STOP = "stop";
    public static final String TAG_NAMES = "tag_names";
    public static final String REMOTE_ID = "remote_id";
    public static final String SYNC_DIRTY = "sync_dirty";    
  }
  
  public static final class DeletedTasks implements BaseColumns {
    public static final String TABLE_NAME = "deleted_tasks";

    public static final String TASK_ID = "task_id";
  }
  
  public static final class PlannedTasks implements BaseColumns {
    public static final String TABLE_NAME = "planned_tasks";
    
    public static final String NAME = "name";
    public static final String WORKSPACE_REMOTE_ID = "workspace_remote_id";
    public static final String REMOTE_ID = "remote_id";    
    public static final String PROJECT_REMOTE_ID = "project_remote_id";
    public static final String USER_REMOTE_ID = "user_remote_id";
    public static final String ESTIMATED_WORKHOURS = "estimated_workhours";
  }
  
}