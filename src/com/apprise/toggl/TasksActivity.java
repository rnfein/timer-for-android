package com.apprise.toggl;

import com.apprise.toggl.remote.TogglWebApi;
import com.apprise.toggl.storage.CurrentUser;
import com.apprise.toggl.storage.DatabaseAdapter;
import com.apprise.toggl.storage.DatabaseAdapter.Tasks;
import com.apprise.toggl.storage.models.User;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;

public class TasksActivity extends ListActivity {

  private static final String TAG = "TasksActivity";
  
  public static final int DEFAULT_CATEGORY = 0;
  public static final int REFRESH_TASKS_OPTION = Menu.FIRST;
  public static final int TO_ACCOUNT_OPTION = Menu.FIRST + 1; 
  
  private Toggl app;
  private TogglWebApi webApi;
  private DatabaseAdapter dbAdapter;
  private SimpleCursorAdapter cursorAdapter;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.tasks);

    app = (Toggl) getApplication();
    webApi = new TogglWebApi(handler, currentUser().api_token);
    dbAdapter = new DatabaseAdapter(this);
    
    getUserAndPopulateList();
  }
  
  public void getUserAndPopulateList() {
    if (CurrentUser.isLoggedIn()) {
      Log.d(TAG, "***getUserAndPopulate: User is logged in");
      populateList();
    } else if (app.getAPIToken() != null) {
      String apiToken = app.getAPIToken();
      Log.d(TAG, "***getUserAndPopulate: Token is provided");      
      webApi.authenticateWithToken(apiToken); //background thread, populateList is called in handler      
    } else {
      Log.d(TAG, "***getUserAndPopulate: Redirect to login");      
      startActivity(new Intent(this, AccountActivity.class));      
    }
  }
  
  public void populateList() {
    Log.d(TAG, "*** populateList");
    dbAdapter.open();
    Cursor tasksCursor = dbAdapter.findAllTasks();
    
    String[] fieldsToShow = { Tasks.DURATION, Tasks.DESCRIPTION };
    int[] viewsToFill = { R.id.task_item_duration, R.id.task_item_description };
    
    cursorAdapter = new SimpleCursorAdapter(this, R.layout.task_item, tasksCursor, fieldsToShow, viewsToFill);
    setListAdapter(cursorAdapter);
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(DEFAULT_CATEGORY, REFRESH_TASKS_OPTION, Menu.NONE, R.string.refresh).setIcon(android.R.drawable.ic_menu_preferences);
    menu.add(DEFAULT_CATEGORY, TO_ACCOUNT_OPTION, Menu.NONE, R.string.account).setIcon(android.R.drawable.ic_menu_preferences);
    return true;
  }   

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case REFRESH_TASKS_OPTION:
        //TODO: refresh tasks
        return true;
      case TO_ACCOUNT_OPTION:
        startActivity(new Intent(this, AccountActivity.class));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }  
  
  
  protected Handler handler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      switch(msg.what) {
      case TogglWebApi.HANDLER_AUTH_PASSED:
        Log.d(TAG, "user:" + currentUser());
        populateList();
        break;
      case TogglWebApi.HANDLER_TASKS_FETCHED:
        Log.d(TAG, "" + msg.obj);
        break;
      }
    }
  };
  
  protected User currentUser() {
    return CurrentUser.getInstance();
  }
  
}
