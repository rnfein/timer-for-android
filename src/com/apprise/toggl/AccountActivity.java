package com.apprise.toggl;

import com.apprise.toggl.remote.SyncService;
import com.apprise.toggl.remote.TogglWebApi;
import com.apprise.toggl.storage.DatabaseAdapter;
import com.apprise.toggl.storage.models.User;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AccountActivity extends Activity {
  
  private static final String STATE_EMAIL = "com.apprise.toggl.STATE_EMAIL";
  private static final String STATE_PASSWORD = "com.apprise.toggl.STATE_PASSWORD";

  private TogglWebApi webApi;
  private SyncService syncService;

  private EditText emailEditText;
  private EditText passwordEditText;
  private TextView createNewAccount;
  private Button loginButton;
  private Toggl app;

  private static final String TAG = "AccountActivity";
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    init();

    setContentView(R.layout.account);
    
    initViews();
    initFields();
    attachEvents();
  }
  
  @Override
  protected void onResume() {    
    IntentFilter filter = new IntentFilter(SyncService.SYNC_COMPLETED);
    registerReceiver(updateReceiver, filter);    
    super.onResume();
  }  
  
  @Override
  protected void onPause() {
    unregisterReceiver(updateReceiver);
    super.onPause();
  }
  
  @Override
  protected void onDestroy() {
    unbindService(syncConnection);
    super.onDestroy();
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    emailEditText.setText(savedInstanceState.getString(STATE_EMAIL));
    passwordEditText.setText(savedInstanceState.getString(STATE_PASSWORD));
    super.onRestoreInstanceState(savedInstanceState);
  }

  @Override
  public void onSaveInstanceState(Bundle saveInstanceState) {
    saveInstanceState.putString(STATE_EMAIL, emailEditText.getText().toString());
    saveInstanceState.putString(STATE_PASSWORD, passwordEditText.getText().toString());
    super.onSaveInstanceState(saveInstanceState);
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
  
  protected void init() {    
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setProgressBarIndeterminate(true);    
    app = (Toggl) getApplication();
    webApi = new TogglWebApi(app.getAPIToken());
    Intent intent = new Intent(this, SyncService.class);
    bindService(intent, syncConnection, BIND_AUTO_CREATE);     
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
        setProgressBarIndeterminateVisibility(true);
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
  
  private User saveCurrentUser(User user) {
    DatabaseAdapter dbAdapter = new DatabaseAdapter(this, app);
    dbAdapter.open();
    User fetchedUser = dbAdapter.findUserByRemoteId(user.id);
    if(fetchedUser == null) {
      user = dbAdapter.createUser(user); 
    } else {
      user._id = fetchedUser._id;
      dbAdapter.updateUser(user);
    }
    dbAdapter.close();
    return user;
  }
  
  protected Runnable authenticateInBackground = new Runnable() {
    
    public void run() {
      String email = emailEditText.getText().toString();
      String password = passwordEditText.getText().toString();
      User user = webApi.authenticateWithCredentials(email, password);

      if (user != null) {
        user = saveCurrentUser(user);        
        app.logIn(user);
        webApi.setApiToken(user.api_token);
        Log.d(TAG, "CurrentUser: " + app.getCurrentUser().toString());
        new Thread(syncAllInBackground).start();        
      } else {
        Toast.makeText(AccountActivity.this, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();        
      }
    }
  };
  
  protected BroadcastReceiver updateReceiver = new BroadcastReceiver() {
    
    @Override
    public void onReceive(Context context, Intent intent) {
      Log.d(TAG, "sync completed on: " + intent.getStringExtra(SyncService.COLLECTION));
      if (intent.getStringExtra(SyncService.COLLECTION).equals(SyncService.ALL_COMPLETED)) {
        setProgressBarIndeterminateVisibility(false);        
        startTasksActivity();      
      }
    }
  };  
  
  protected ServiceConnection syncConnection = new ServiceConnection() {
    
    public void onServiceDisconnected(ComponentName name) {}
    
    public void onServiceConnected(ComponentName name, IBinder serviceBinding) {
      SyncService.SyncBinder binding = (SyncService.SyncBinder) serviceBinding;
      syncService = binding.getService();
    }

  };
  
  protected Runnable syncAllInBackground = new Runnable() {
    
    public void run() {
      syncService.setApiToken(app.getAPIToken());
      syncService.syncAll();
    }
  };  
  
}