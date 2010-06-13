package com.apprise.toggl.tracking;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.util.Log;

/**
 * Service compatibility for pre-2 Android builds.
 * Taken from Android documentation.
 */
public abstract class ServiceCompat extends Service {

  private static final String TAG = "ServiceCompat";
  
  @SuppressWarnings("unchecked")
  private static final Class[] mStartForegroundSignature = new Class[] {
      int.class, Notification.class };

  @SuppressWarnings("unchecked")
  private static final Class[] mStopForegroundSignature = new Class[] { boolean.class };

  private NotificationManager mNM;
  private Method mStartForeground;
  private Method mStopForeground;
  private Object[] mStartForegroundArgs = new Object[2];
  private Object[] mStopForegroundArgs = new Object[1];

  /**
   * This is a wrapper around the new startForeground method, using the older
   * APIs if it is not available.
   */
  void startForegroundCompat(int id, Notification notification) {
    // If we have the new startForeground API, then use it.
    if (mStartForeground != null) {
      mStartForegroundArgs[0] = Integer.valueOf(id);
      mStartForegroundArgs[1] = notification;
      try {
        mStartForeground.invoke(this, mStartForegroundArgs);
      } catch (InvocationTargetException e) {
        // Should not happen.
        Log.w(TAG, "Unable to invoke startForeground", e);
      } catch (IllegalAccessException e) {
        // Should not happen.
        Log.w(TAG, "Unable to invoke startForeground", e);
      }
      return;
    }

    // Fall back on the old API.
    setForeground(true);
    mNM.notify(id, notification);
  }

  /**
   * This is a wrapper around the new stopForeground method, using the older
   * APIs if it is not available.
   */
  void stopForegroundCompat(int id) {
    // If we have the new stopForeground API, then use it.
    if (mStopForeground != null) {
      mStopForegroundArgs[0] = Boolean.TRUE;
      try {
        mStopForeground.invoke(this, mStopForegroundArgs);
      } catch (InvocationTargetException e) {
        // Should not happen.
        Log.w(TAG, "Unable to invoke stopForeground", e);
      } catch (IllegalAccessException e) {
        // Should not happen.
        Log.w(TAG, "Unable to invoke stopForeground", e);
      }
      return;
    }

    // Fall back on the old API. Note to cancel BEFORE changing the
    // foreground state, since we could be killed at that point.
    mNM.cancel(id);
    setForeground(false);
  }

  protected void initCompat() {
    mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    try {
      mStartForeground = getClass().getMethod("startForeground",
          mStartForegroundSignature);
      mStopForeground = getClass().getMethod("stopForeground",
          mStopForegroundSignature);
    } catch (NoSuchMethodException e) {
      // Running on an older platform.
      mStartForeground = mStopForeground = null;
    }
  }

}
