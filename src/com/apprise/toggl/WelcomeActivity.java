package com.apprise.toggl;

import com.apprise.toggl.remote.TogglWebApi;
import com.apprise.toggl.storage.models.User;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class WelcomeActivity extends Activity {

  Toggl app;
  TogglWebApi webApi;
  String apiToken;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.welcome);
    app = (Toggl) getApplication();
    apiToken = app.getAPIToken();
    
    if (apiToken != null) {
      webApi = new TogglWebApi(apiToken);
      new Thread(authenticateInBackground).start();      
    } else {
      startAccountActivity();
    }
  }
  
  private void startAccountActivity() {
    startActivity(new Intent(WelcomeActivity.this, AccountActivity.class));
  }
  
  private void startTasksActivity() {
    startActivity(new Intent(WelcomeActivity.this, TasksActivity.class));
  }

  protected Runnable authenticateInBackground = new Runnable() {
    
    public void run() {
      User user = webApi.authenticateWithToken(apiToken);

      if (user != null) {
        app.logIn(user);
        startTasksActivity();
      } else {
        startAccountActivity();
      }
    }
  };  

}
