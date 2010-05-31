package com.apprise.toggl;

import com.apprise.toggl.storage.models.User;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;

public class Toggl extends Application {

  public static final String PREF_API_TOKEN = "PREF_API_TOKEN";
  public static final String TOGGL_PREFS = "TOGGL_PREFS";

  private Toggl singleton;
  private SharedPreferences settings;
  private User currentUser;

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
  
  public User getCurrentUser() {
    return currentUser;
  }
  
  public void setCurrentUser(User user) {
    currentUser = user;
  }
  
  public void clearCurrentUser() {
    currentUser = null;
  }
  
  public void logIn(User user) {
    setCurrentUser(user);
    storeAPIToken(user.api_token);
  }
  
  public void logOut() {
    clearCurrentUser();
    storeAPIToken(null);
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
