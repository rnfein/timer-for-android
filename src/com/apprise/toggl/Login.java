package com.apprise.toggl;

import com.apprise.toggl.remote.TogglWebApi;
import com.apprise.toggl.storage.User;
import com.google.gson.Gson;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Login extends Activity {
	
	private TogglWebApi webApi;
	
	private EditText emailEditText;
	private EditText passwordEditText;
	private Button loginButton;
	private Toggl app;
	
	private static final String TAG = "Login";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		app = (Toggl) getApplication();
		webApi = new TogglWebApi();
		initViews();
		attachEvents();
	}
	
	protected void initViews() {
		emailEditText = (EditText) findViewById(R.id.email);
		passwordEditText = (EditText) findViewById(R.id.password);
		loginButton = (Button) findViewById(R.id.login);
	}
	
	protected void attachEvents() {
		loginButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				String email = emailEditText.getText().toString();
				String password = passwordEditText.getText().toString();
				String response = webApi.AuthenticateWithCredentials(email, password);
				
				Gson gson = new Gson();
				User user = gson.fromJson(response, User.class);
				Log.d(TAG, "user:" + user);
				
				app.storeAPIToken(user.apiToken);
			}
		});
	}
}