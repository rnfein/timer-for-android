package com.apprise.toggl;

import com.apprise.toggl.remote.TogglWebApi;
import com.apprise.toggl.storage.CurrentUser;
import com.apprise.toggl.storage.User;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class TasksActivity extends ApplicationActivity {

  private static final String TAG = "Tasks";
  
  public static final int DEFAULT_CATEGORY = 0;
  public static final int REFRESH_TASKS_OPTION = Menu.FIRST;
  public static final int TO_ACCOUNT_OPTION = Menu.FIRST + 1; 
  
  private User user;
  private Toggl app;
  private TogglWebApi webApi;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    app = (Toggl) getApplication();
    webApi = new TogglWebApi(handler);
    
    getUserAndPopulateList();
    
    setContentView(R.layout.tasks);
  }
  
  public void getUserAndPopulateList() {
    if (CurrentUser.isLoggedIn()) {
      this.user = CurrentUser.getInstance();
      Log.d(TAG, "***getUserAndPopulate: User is logged in");
      populateList();
    } else if (app.getAPIToken() != null) {
      String apiToken = app.getAPIToken();
      Log.d(TAG, "***getUserAndPopulate: Token is provided");      
      webApi.authenticateWithToken(apiToken); //background thread
      //populateList is called in handler      
    } else {
      Log.d(TAG, "***getUserAndPopulate: Redirect to login");      
      startActivity(new Intent(this, AccountActivity.class));      
    }
  }
  
  public void populateList() {
    Log.d(TAG, "*** populateList");
    //TODO: fetch and populate list
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
        user = CurrentUser.getInstance();
        Log.d(TAG, "user:" + user);
        populateList();
      }
    }
  };  
  
}
