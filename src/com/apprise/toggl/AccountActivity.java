package com.apprise.toggl;

import com.apprise.toggl.remote.TogglWebApi;
import com.apprise.toggl.storage.CurrentUser;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AccountActivity extends ApplicationActivity {

  private TogglWebApi webApi;

  private EditText emailEditText;
  private EditText passwordEditText;
  private TextView createNewAccount;
  private Button loginButton;
  private Toggl app;

  private static final String TAG = "AccountActivity";
  
  public static final int DEFAULT_CATEGORY = 0;
  public static final int LOG_OUT_OPTION = Menu.FIRST;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    app = (Toggl) getApplication();
    webApi = new TogglWebApi(currentUser().api_token);

    setContentView(R.layout.account);

    initViews();
    initFields();
    attachEvents();
  } 
  
  @Override
  protected void onResume() {
    super.onResume();
    initFields();
  }  

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(DEFAULT_CATEGORY, LOG_OUT_OPTION, Menu.NONE, R.string.log_out).setIcon(android.R.drawable.ic_menu_preferences);
    return true;
  }   

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case LOG_OUT_OPTION:
        CurrentUser.logOut();
        app.storeAPIToken(null);
        initFields();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }    
  
  protected void initViews() {
    emailEditText = (EditText) findViewById(R.id.email);
    passwordEditText = (EditText) findViewById(R.id.password);
    loginButton = (Button) findViewById(R.id.login);
    createNewAccount = (TextView) findViewById(R.id.create_account);
  }
  
  private void initFields() {
    passwordEditText.setText(null);
    if (CurrentUser.isLoggedIn()) {
      emailEditText.setText(currentUser().email);
    } else {
      emailEditText.setText(null);      
    }
  }  

  protected void attachEvents() {
    loginButton.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {
        new Thread(authenticateInBackground).start();
      }
    });

    createNewAccount.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {
        Intent intent = new Intent(AccountActivity.this, SignUpActivity.class);
        startActivity(intent);
      }
    });
  }
  
  public void startTasksActivity() {
    startActivity(new Intent(AccountActivity.this, TasksActivity.class));
  }
  
  protected Runnable authenticateInBackground = new Runnable() {
    
    public void run() {
      String email = emailEditText.getText().toString();
      String password = passwordEditText.getText().toString();
      boolean success = webApi.authenticateWithCredentials(email, password);

      if (success) {
        Log.d(TAG, "CurrentUser: " + currentUser().toString());
        app.storeAPIToken(currentUser().api_token);
        startTasksActivity();
      } else {
        Toast.makeText(AccountActivity.this, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();        
      }
    }
  };
  
}