package com.apprise.toggl.tracking;


import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.apprise.toggl.R;
import com.apprise.toggl.TaskActivity;
import com.apprise.toggl.Util;
import com.apprise.toggl.storage.models.Task;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class TimeTrackingService extends ServiceCompat {

  public static final String BROADCAST_SECOND_ELAPSED = "com.apprise.toggl.tracking.BROADCAST_SECOND_ELAPSED";
  public static final String TRACKED_TASK_DURATION = "com.apprise.toggl.TRACKED_TASK_DURATION";
  public static final String TRACKED_TASK_ID = "com.apprise.toggl.tracking.TRACKED_TASK_ID";
  
  public static final int NOTIFICATION_ID = 1;
  private static boolean isAlive = false; 
  
  private Timer timer;
  private Task task;
  private long seconds = 0l;
  private boolean isTracking = false;

  public static boolean isAlive() {
    return isAlive;
  }
  
  @Override
  public void onCreate() {
    isAlive = true;
    initCompat();
    super.onCreate();
  }

  @Override
  public void onDestroy() {
    isAlive = false;
    super.onDestroy();
  }
  
  @Override
  public IBinder onBind(Intent intent) {
    return new TimeTrackingBinder(this);
  }
  
  public static class TimeTrackingBinder extends Binder {

    private final TimeTrackingService service;

    public TimeTrackingBinder(TimeTrackingService service) {
      this.service = service;
    }

    public TimeTrackingService getService() {
      return service;
    }

  }
  
  public long startTracking(Task task) {
    this.task = task;
    setCurrentDuration(this.task.duration);
    
    timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        seconds += 1l;
        Intent intent = new Intent(BROADCAST_SECOND_ELAPSED);
        intent.putExtra(TRACKED_TASK_ID, TimeTrackingService.this.task._id);
        intent.putExtra(TRACKED_TASK_DURATION, seconds);
        sendBroadcast(intent);
      }
    }, new Date(System.currentTimeMillis() + 1000), 1000);

    pushToForeground();
    isTracking = true;
    return Util.getRunningTimeStart(seconds);
  }
  
  public long stopTracking() {
    timer.cancel();
    long endDuration = seconds;
    pullFromForeground();
    task = null;
    seconds = 0;
    isTracking = false;
    return endDuration;
  }

  public Task getTrackedTask() {
    return task;
  }
  
  public long getCurrentDuration() {
    if (isTracking) {
      return Util.getRunningTimeStart(seconds);
    } else {
      return seconds;
    }
  }
  
  public void setCurrentDuration(long seconds) {
    this.seconds = Util.convertIfRunningTime(seconds);
  }

  /**
   * Returns true if any task is currently being tracked. 
   */
  public boolean isTracking() {
    return isTracking;
  }
  
  /**
   * Returns true if the given task is currently being tracked. 
   */
  public boolean isTracking(Task task) {
    return isTracking && task._id == this.task._id;
  }
  
  private void pushToForeground() {
    // icon, title and ticker in minified status bar
    int icon = R.drawable.trigger_active;
    String ticker = getString(R.string.notification_ticker);
    long when = System.currentTimeMillis();
    Notification notification = new Notification(icon, ticker, when);
    notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
    
    // activity to launch when notification is clicked
    Intent intent = new Intent(this, TaskActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(TaskActivity.TASK_ID, task._id);
    
    PendingIntent launchIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    
    // event shown in expanded status bar
    notification.setLatestEventInfo(getApplicationContext(),
      getString(R.string.notification_expanded_title),
      getString(R.string.notification_expanded_content),
      launchIntent);
    
    startForegroundCompat(NOTIFICATION_ID, notification);
  }
  
  private void pullFromForeground() {
    stopForegroundCompat(NOTIFICATION_ID);
  }
  
}
