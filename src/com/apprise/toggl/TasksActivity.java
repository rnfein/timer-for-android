package com.apprise.toggl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class TasksActivity extends Activity {

  public static final int DEFAULT_CATEGORY = 0;
  public static final int REFRESH_TASKS_OPTION = Menu.FIRST;
  public static final int TO_ACCOUNT_OPTION = Menu.FIRST + 1;  
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.tasks);
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(DEFAULT_CATEGORY, REFRESH_TASKS_OPTION, Menu.NONE, R.string.refresh).setIcon(android.R.drawable.ic_menu_preferences);
    menu.add(DEFAULT_CATEGORY, TO_ACCOUNT_OPTION, Menu.NONE, R.string.account).setIcon(android.R.drawable.ic_menu_preferences);
    return true;
  }   

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case REFRESH_TASKS_OPTION:
        //TODO: refresh tasks
        return true;
      case TO_ACCOUNT_OPTION:
        startActivity(new Intent(this, AccountActivity.class));
        return true;
    }

    return super.onOptionsItemSelected(item);
  }  
  
}
