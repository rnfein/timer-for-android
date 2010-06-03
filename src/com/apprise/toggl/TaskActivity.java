package com.apprise.toggl;

import android.os.Bundle;

public class TaskActivity extends ApplicationActivity {

  public static final String TASK_ID = "TASK_ID";
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.task);    
  }   
  
}
