package com.apprise.toggl;

import com.apprise.toggl.remote.SyncService;
import com.apprise.toggl.remote.TogglWebApi;
import com.apprise.toggl.remote.exception.FailedResponseException;
import com.apprise.toggl.storage.DatabaseAdapter;
import com.apprise.toggl.storage.models.User;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AccountActivity extends Activity {
  
  private static final String STATE_EMAIL = "com.apprise.toggl.STATE_EMAIL";
  private static final String STATE_PASSWORD = "com.apprise.toggl.STATE_PASSWORD";
  private static final int DIALOG_LOGGING_IN = 1;
  private static final int DIALOG_SYNCING = 2;

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
    attachEvents();
  }
  
  @Override
  protected void onResume() {
    initFields();    
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
        // hide keyboard
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);

        if (app.isConnected()) {
          showDialog(DIALOG_LOGGING_IN);
          new Thread(authenticateInBackground).start();
        } else {
          showNoConnectionDialog();
        }
      }
    });

    createNewAccount.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {
        Intent intent = new Intent(AccountActivity.this, SignUpActivity.class);
        startActivity(intent);
      }
    });
  }
  
  @Override
  protected Dialog onCreateDialog(int id) {
    ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setIndeterminate(true);
    progressDialog.setCancelable(false);
    
    switch (id) {
    case DIALOG_LOGGING_IN:
      progressDialog.setMessage(getString(R.string.logging_in));
      break;
    case DIALOG_SYNCING:
      progressDialog.setMessage(getString(R.string.syncing));
      break;
    }
    return progressDialog;
  }

  private void showNoConnectionDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
    builder.setTitle(R.string.no_internet_connection);
    builder.setMessage(R.string.no_internet_msg);
    builder.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
      
      public void onClick(DialogInterface dialog, int which) {
        Intent settingsIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(settingsIntent);
      }
    });
    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) { }
    });
    builder.setCancelable(false);
    builder.show();    
  }
  
  private void startTasksActivity() {
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
  
  private Runnable authenticateInBackground = new Runnable() {
    
    public void run() {
      String email = emailEditText.getText().toString();
      String password = passwordEditText.getText().toString();
      User user = webApi.authenticateWithCredentials(email, password);

      dismissDialog(DIALOG_LOGGING_IN);

      if (user != null) {
        user = saveCurrentUser(user);        
        app.logIn(user);
        webApi.setApiToken(user.api_token);
        Log.d(TAG, "CurrentUser: " + app.getCurrentUser().toString());

        runOnUiThread(new Runnable() {
          public void run() {
            showDialog(DIALOG_SYNCING);
          }
        });

        new Thread(syncAllInBackground).start();        
      } else {
        runOnUiThread(new Runnable() {
          public void run() {
            Toast.makeText(AccountActivity.this, getString(R.string.authentication_failed),
                Toast.LENGTH_SHORT).show(); 
          }
        });
      }
    }
  };
  
  private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
    
    @Override
    public void onReceive(Context context, Intent intent) {
      Log.d(TAG, "sync completed on: " + intent.getStringExtra(SyncService.COLLECTION));
      if (intent.getStringExtra(SyncService.COLLECTION).equals(SyncService.ALL_COMPLETED)) {
        dismissDialog(DIALOG_SYNCING);
        startTasksActivity();
      }
    }
  };  
  
  private ServiceConnection syncConnection = new ServiceConnection() {
    
    public void onServiceDisconnected(ComponentName name) {}
    
    public void onServiceConnected(ComponentName name, IBinder serviceBinding) {
      SyncService.SyncBinder binding = (SyncService.SyncBinder) serviceBinding;
      syncService = binding.getService();
    }

  };
  
  private Runnable syncAllInBackground = new Runnable() {
    
    public void run() {
      syncService.setApiToken(app.getAPIToken());
      try {
        boolean scheduled = false;
        syncService.syncAll(scheduled);
      } catch (FailedResponseException e) {
        Log.e(TAG, "FailedResponseException", e);
        runOnUiThread(new Runnable() {
          public void run() {
            Toast.makeText(AccountActivity.this, getString(R.string.sync_failed),
                Toast.LENGTH_SHORT).show(); 
            dismissDialog(DIALOG_SYNCING);            
          }
        });    
      }
    }
  };  
  
}