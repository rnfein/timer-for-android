package com.apprise.toggl;

import com.apprise.toggl.remote.TogglWebApi;
import com.apprise.toggl.storage.DatabaseAdapter;
import com.apprise.toggl.storage.models.User;

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
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    app = (Toggl) getApplication();
    webApi = new TogglWebApi(app.getAPIToken());

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
    getMenuInflater().inflate(R.menu.account_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }   

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.account_menu_log_out:
        app.logOut();
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
    if (app.getCurrentUser() != null) {
      emailEditText.setText(app.getCurrentUser().email);
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
  
  public void saveCurrentUser(User user) {
    DatabaseAdapter dbAdapter = new DatabaseAdapter(this);
    dbAdapter.open();
    User fetchedUser = dbAdapter.findUserByRemoteId(user.id);
    if(fetchedUser == null) {
      dbAdapter.createUser(user); 
    } else {
      user._id = fetchedUser._id;
      dbAdapter.updateUser(user);
    }
    dbAdapter.close();
  }
  
  protected Runnable authenticateInBackground = new Runnable() {
    
    public void run() {
      String email = emailEditText.getText().toString();
      String password = passwordEditText.getText().toString();
      User user = webApi.authenticateWithCredentials(email, password);

      if (user != null) {
        app.logIn(user);
        saveCurrentUser(user);
        Log.d(TAG, "CurrentUser: " + app.getCurrentUser().toString());        
        startTasksActivity();
      } else {
        Toast.makeText(AccountActivity.this, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();        
      }
    }
  };
  
}