package com.apprise.toggl;

import android.app.ListActivity;

public class ApplicationListActivity extends ListActivity {

  Toggl app;
  
  @Override
  protected void onResume() {
    app = (Toggl) getApplication();
    if (app.getCurrentUser() == null)
      finish();
    super.onResume();
  }  
}
