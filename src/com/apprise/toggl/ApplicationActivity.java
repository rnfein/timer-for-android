package com.apprise.toggl;

import com.apprise.toggl.storage.CurrentUser;

import android.app.Activity;
import android.util.Log;

public class ApplicationActivity extends Activity {

  private static final String TAG = "ApplicationActivity";
  
  @Override
  protected void onResume() {
    Log.d(TAG, "***onResume***");
    Log.d(TAG, "***User logged in:" + CurrentUser.isLoggedIn());
    super.onResume();
  }
  
}
