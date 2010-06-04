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
  
  Toggl app;
  TogglWebApi webApi;
  String apiToken;
  DatabaseAdapter dbAdapter;
  User currentUser = null;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.welcome);
    app = (Toggl) getApplication();
    apiToken = app.getAPIToken();
    
    if (apiToken != null) {
      getCurrentUserFromDb();
      if (currentUser != null) {
        app.logIn(currentUser);
        startTasksActivity();
      } else {
        webApi = new TogglWebApi(apiToken);        
        new Thread(authenticateInBackground).start(); 
      }
    } else {
      startAccountActivity();
    }
  }
  
  @Override
  protected void onPause() {
    super.onPause();
    finish();
  }

  private void getCurrentUserFromDb() {
    Log.d(TAG, "get user from db");
    dbAdapter = new DatabaseAdapter(this);
    dbAdapter.open();
    currentUser = dbAdapter.findUserByApiToken(apiToken);
    Log.d(TAG, "fetched user: " + currentUser);
  }
  
  private void startAccountActivity() {
    startActivity(new Intent(WelcomeActivity.this, AccountActivity.class));
  }
  
  private void startTasksActivity() {
    startActivity(new Intent(WelcomeActivity.this, TasksActivity.class));
  }

  protected Runnable authenticateInBackground = new Runnable() {
    
    public void run() {
      currentUser = webApi.authenticateWithToken(apiToken);

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
