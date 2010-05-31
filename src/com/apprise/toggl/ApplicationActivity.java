package com.apprise.toggl;

import android.app.Activity;
import android.util.Log;

public class ApplicationActivity extends Activity {

  private static final String TAG = "ApplicationActivity";
  
  @Override
  protected void onResume() {
    Log.d(TAG, "***onResume***");
    super.onResume();
  }
  
}
