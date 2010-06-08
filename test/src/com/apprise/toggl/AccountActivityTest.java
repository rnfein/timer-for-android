package com.apprise.toggl;

import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;

public class AccountActivityTest extends ActivityInstrumentationTestCase2<AccountActivity>{

  public AccountActivityTest() {
    super("com.apprise.toggl", AccountActivity.class);
  }
  
  public void testStoreEditTextsOnKill() {
    final EditText email = (EditText) getActivity().findViewById(R.id.email);
    final EditText password = (EditText) getActivity().findViewById(R.id.password);
    
    getActivity().runOnUiThread(new Runnable() {
      public void run() {
        email.setText("ted@toggl.com");
        password.setText("tog..");                
      }
    });

    getInstrumentation().waitForIdleSync();

    getActivity().runOnUiThread(new Runnable() {
      public void run() {
        Bundle savedState = new Bundle();
        getInstrumentation().callActivityOnSaveInstanceState(getActivity(), savedState);
        
        email.setText("");
        password.setText("");

        getInstrumentation().callActivityOnRestoreInstanceState(getActivity(), savedState);
      }
    });

    getInstrumentation().waitForIdleSync();

    assertEquals("ted@toggl.com", email.getText().toString());
    assertEquals("tog..", password.getText().toString());
  }
  
}
