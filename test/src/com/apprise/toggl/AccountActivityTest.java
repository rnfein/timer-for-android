package com.apprise.toggl;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;

public class AccountActivityTest extends ActivityInstrumentationTestCase2<AccountActivity>{

  public AccountActivityTest() {
    super("com.apprise.toggl", AccountActivity.class);
  }
  
  public void testStoreEditTextsOnKill() {
    Activity activity = getActivity();
    
    final Activity uiRef = activity;
    activity.runOnUiThread(new Runnable() {
      
      public void run() {
        EditText email = (EditText) uiRef.findViewById(R.id.email);
        EditText password = (EditText) uiRef.findViewById(R.id.password);

        email.setText("ted@toggl.com");
        password.setText("tog..");                
      }
    });
    
    // kill activity
    activity.finish();
    
    // revive activity
    activity = getActivity();
    EditText email = (EditText) activity.findViewById(R.id.email);
    EditText password = (EditText) activity.findViewById(R.id.password);

    assertEquals("ted@toggl.com", email.getText().toString());
    assertEquals("tog..", password.getText().toString());
  }
  
}
