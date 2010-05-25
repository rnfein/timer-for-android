package com.apprise.toggl;

import com.apprise.toggl.remote.TogglWebApi;
import com.apprise.toggl.storage.User;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {

  private TogglWebApi webApi;

  private EditText emailEditText;
  private EditText passwordEditText;
  private TextView createNewAccount;
  private Button loginButton;
  private Toggl app;

  private static final String TAG = "Login";
  private static final String CREATE_NEW_ACCOUNT_URL = "https://www.toggl.com/signup";

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    app = (Toggl) getApplication();
    String apiToken = app.getAPIToken();
    webApi = new TogglWebApi(handler);
    
    Log.d(TAG, "apiToken: " + apiToken);
    if (apiToken != null) {
      webApi.authenticateWithToken(apiToken);
    } else {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.login);

      initViews();
      attachEvents();
    }
  }

  protected void initViews() {
    emailEditText = (EditText) findViewById(R.id.email);
    passwordEditText = (EditText) findViewById(R.id.password);
    loginButton = (Button) findViewById(R.id.login);
    createNewAccount = (TextView) findViewById(R.id.create_account);
  }

  protected void attachEvents() {
    loginButton.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        webApi.authenticateWithCredentials(email, password);
      }
    });

    createNewAccount.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {
        Uri uri = Uri.parse(CREATE_NEW_ACCOUNT_URL);
        Intent i = new Intent("android.intent.action.VIEW", uri);
        startActivity(i);
      }
    });
  }
  
  private void startTasksActivity() {
    Intent i = new Intent(LoginActivity.this, TasksActivity.class);
    startActivity(i);       
  }
  
  protected Handler handler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      switch(msg.what) {
      case TogglWebApi.HANDLER_AUTH_PASSED:
        User user = (User) msg.obj;
        Log.d(TAG, "user:" + user);
        app.storeAPIToken(user.api_token);
        startTasksActivity();
      }
    }
    
  };  
}