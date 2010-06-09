package com.apprise.toggl;

import com.apprise.toggl.remote.TogglWebApi;
import com.apprise.toggl.storage.DatabaseAdapter;
import com.apprise.toggl.storage.models.User;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class WelcomeActivity extends Activity {

  private static final String TAG = "WelcomeActivity";
  
  private Toggl app;
  private TogglWebApi webApi;
  private String apiToken;
  private DatabaseAdapter dbAdapter;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.welcome);
    app = (Toggl) getApplication();
    apiToken = app.getAPIToken();
    dbAdapter = new DatabaseAdapter(this, app);
    dbAdapter.open();
    
    if (apiToken != null) {
      User currentUser = getCurrentUserFromDb();
      if (currentUser != null) {
        app.logIn(currentUser);
        startTasksActivity();
      } else {
        new Thread(authenticateInBackground).start(); 
      }
    } else {
      startAccountActivity();
    }
  }
 
  @Override
  protected void onDestroy() {
    dbAdapter.close();
    super.onDestroy();
  }

  private User getCurrentUserFromDb() {
    User currentUser = dbAdapter.findUserByApiToken(apiToken);
    Log.d(TAG, "Found user: " + currentUser);
    return currentUser;
  }
  
  private void startAccountActivity() {
    startActivity(new Intent(WelcomeActivity.this, AccountActivity.class));
    finish();
  }
  
  private void startTasksActivity() {
    startActivity(new Intent(WelcomeActivity.this, TasksActivity.class));
    finish();
  }

  private Runnable authenticateInBackground = new Runnable() {
    
    public void run() {
      webApi = new TogglWebApi(apiToken);
      User currentUser = webApi.authenticateWithToken(apiToken);

      if (currentUser != null) {
        app.logIn(currentUser);
        dbAdapter.createUser(currentUser);
        startTasksActivity();
      } else {
        startAccountActivity();
      }
    }
  };  

}
