package com.apprise.toggl;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;

public class Toggl extends Application {

	public static final String PREF_API_TOKEN = "PREF_API_TOKEN";
	public static final String TOGGL_PREFS = "TOGGL_PREFS";
	
	private Toggl singleton;
  private SharedPreferences settings;	
	
	public Toggl getInstance() {
		return singleton;
	}
	
	public void storeAPIToken(String apiToken) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PREF_API_TOKEN, apiToken);
		editor.commit();
	}
	
	public String getAPIToken() {
    return settings.getString(PREF_API_TOKEN, null);		
	}
	
	@Override
  public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
  }

	@Override
  public void onCreate() {
	  super.onCreate();
    singleton = this;
    settings = getSharedPreferences(TOGGL_PREFS, MODE_PRIVATE);	  
  }

	@Override
  public void onLowMemory() {
	  super.onLowMemory();
  }

	@Override
  public void onTerminate() {
	  super.onTerminate();
  }

	
	
}
