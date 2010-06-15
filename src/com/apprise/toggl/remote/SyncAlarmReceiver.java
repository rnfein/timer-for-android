package com.apprise.toggl.remote;

import com.apprise.toggl.Toggl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SyncAlarmReceiver extends BroadcastReceiver {

  public static final String ACTION_SYNC_ALARM = "com.apprise.toggl.remote.ACTION_SYNC_ALARM";
  public static final String TAG = "SyncAlarmReceiver";
  
  Toggl app;

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d(TAG, "Alarm Recieved!");
    Intent syncServiceIntent = new Intent(context, SyncService.class);
    context.startService(syncServiceIntent);
  }

}
