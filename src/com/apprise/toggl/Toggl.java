package com.apprise.toggl;

import com.apprise.toggl.remote.SyncAlarmReceiver;
import com.apprise.toggl.remote.SyncService;
import com.apprise.toggl.storage.models.User;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.util.Log;

public class Toggl extends Application {

  public static final String PREF_API_TOKEN = "PREF_API_TOKEN";
  public static final String TOGGL_PREFS = "TOGGL_PREFS";

  private Toggl singleton;
  private SharedPreferences settings;
  private ConnectivityManager connectivityManager;
  private User currentUser;

  private AlarmManager am;
  private PendingIntent syncAlarmIntent;  
  
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
    clearSyncSchedule();
    clearCurrentUser();
    storeAPIToken(null);
  }
  
  public boolean isConnected() {
    return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting() ||
        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
  }

  public void initSyncSchedule() {
    Log.d("Toggl", "initing schedule");
    Intent intent = new Intent(SyncAlarmReceiver.ACTION_SYNC_ALARM);
    syncAlarmIntent = PendingIntent.getBroadcast(this,
            0, intent, 0);

    long timeToRefresh = SystemClock.elapsedRealtime() + 1000;

    // Schedule the alarm!
    am = (AlarmManager)getSystemService(ALARM_SERVICE);
    am.setRepeating(AlarmManager.ELAPSED_REALTIME,
                    timeToRefresh, 60*1000, syncAlarmIntent);    
  }
  
  public void clearSyncSchedule() {
    am.cancel(syncAlarmIntent);
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
    connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
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
