package com.apprise.toggl.tracking;

import com.apprise.toggl.Util;
import com.apprise.toggl.storage.models.Task;
import com.apprise.toggl.tracking.TimeTrackingService;

import android.content.Intent;
import android.test.ServiceTestCase;

public class TimeTrackingServiceTest extends ServiceTestCase<TimeTrackingService> {

  private TimeTrackingService service;
  
  public TimeTrackingServiceTest() {
    super(TimeTrackingService.class);
  }
  
  @Override
  protected void setUp() throws Exception {
    TimeTrackingService.TimeTrackingBinder binder = (TimeTrackingService.TimeTrackingBinder)
      bindService(new Intent(getContext(), TimeTrackingService.class));
    service = binder.getService();
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testStartTracking() throws Exception {
    Task task = new Task();
    task._id = 3l;
    service.startTracking(task);
    
    Thread.sleep(2100);
    long duration = Util.convertIfRunningTime(service.getCurrentDuration());
    
    assertEquals(2l, duration);
  }

  public void testStopTracking() throws Exception {
    Task task = new Task();
    task._id = 3l;
    task.duration = 440l;
    service.startTracking(task);
    
    Thread.sleep(1100);
    
    task.duration = service.stopTracking();
    assertEquals(441l, task.duration);
  }
  
  public void testIsTracking() {
    Task task = new Task();
    task._id = 3l;
    Task otherTask = new Task();
    otherTask._id = 4l;
    service.startTracking(task);
    
    assertTrue(service.isTracking());
    assertTrue(service.isTracking(task));
    assertFalse(service.isTracking(otherTask));

    service.stopTracking();
    assertFalse(service.isTracking());
    assertFalse(service.isTracking(task));
    assertFalse(service.isTracking(otherTask));
  }

  
}
