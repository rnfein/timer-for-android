package com.apprise.toggl.tracking;


import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.apprise.toggl.storage.models.Task;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class TimeTrackingService extends Service {

  public static final String BROADCAST_SECOND_ELAPSED = "com.apprise.toggl.tracking.BROADCAST_SECOND_ELAPSED";
  
  private Timer timer;
  private Task task;
  private boolean isTracking = false;
  
  @Override
  public void onCreate() {
    super.onCreate();
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
  
  public void startTracking(Task task) {
    this.task = task;
    
    timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        TimeTrackingService.this.task.duration += 1l;
        Intent intent = new Intent(BROADCAST_SECOND_ELAPSED);
        sendBroadcast(intent);
      }
    }, new Date(System.currentTimeMillis() + 1000), 1000);

    isTracking = true;
  }
  
  public void stopTracking() {
    timer.cancel();
    task = null;
    isTracking = false;
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
    return isTracking && task.id == this.task.id; // TODO: task.equals(this.task);
  }
  
}
