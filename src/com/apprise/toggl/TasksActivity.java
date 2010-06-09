package com.apprise.toggl;

import java.util.Calendar;

import com.apprise.toggl.remote.SyncService;
import com.apprise.toggl.storage.DatabaseAdapter;
import com.apprise.toggl.storage.DatabaseAdapter.Projects;
import com.apprise.toggl.storage.DatabaseAdapter.Tasks;
import com.apprise.toggl.storage.models.User;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TasksActivity extends ApplicationListActivity {

  private DatabaseAdapter dbAdapter;
  private SyncService syncService;
  private Toggl app;
  private User currentUser;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setProgressBarIndeterminate(true);
    setContentView(R.layout.tasks);
    
    app = (Toggl) getApplication();    
    dbAdapter = new DatabaseAdapter(this, app);
    dbAdapter.open();
    currentUser = app.getCurrentUser();
    Intent intent = new Intent(this, SyncService.class);
    bindService(intent, syncConnection, BIND_AUTO_CREATE);
  }
  
  @Override
  protected void onResume() {
    IntentFilter filter = new IntentFilter(SyncService.SYNC_COMPLETED);
    registerReceiver(updateReceiver, filter);
    adapter.clearSections();
    populateList();
    super.onResume();
  }
  
  @Override
  protected void onPause() {
    unregisterReceiver(updateReceiver);
    super.onPause();
  }
  
  @Override
  protected void onDestroy() {
    dbAdapter.close();
    unbindService(syncConnection);
    super.onDestroy();
  }

  public void populateList() {
    int taskRetentionDays = currentUser.task_retention_days;
    Calendar queryCal = (Calendar) Calendar.getInstance().clone();
    String[] fieldsToShow = { Tasks.DURATION, Tasks.DESCRIPTION, Projects.CLIENT_PROJECT_NAME };
    int[] viewsToFill = { R.id.task_item_duration, R.id.task_item_description, R.id.task_item_client_project_name };
    
    for (int i = 0; i <= taskRetentionDays; i++) {
      Cursor tasksCursor = dbAdapter.findTasksForListByDate(queryCal.getTime());

      if (tasksCursor.getCount() > 0) {
        startManagingCursor(tasksCursor);
        TasksCursorAdapter cursorAdapter = new TasksCursorAdapter(this, R.layout.task_item, tasksCursor, fieldsToShow, viewsToFill);
        String date = Util.smallDateString(queryCal.getTime());
        String headerText = date + " (" + Util.secondsToHM(getDurationTotal(tasksCursor)) + " h)";
        adapter.addSection(headerText, cursorAdapter);
      }
      else {
        tasksCursor.close();
      }

      queryCal.add(Calendar.DATE, -1);
    }
    
    setListAdapter(adapter);
  }
  
  private long getDurationTotal(Cursor tasksCursor) {
    long duration_total = 0;
    long duration = 0;
    while (tasksCursor.moveToNext()) {
      duration = tasksCursor.getLong(tasksCursor.getColumnIndex(Tasks.DURATION));
      if (duration > 0) duration_total += duration; 
    }
    return duration_total;
  }
  
  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    Intent intent = new Intent(TasksActivity.this, TaskActivity.class);

    Cursor cursor = (Cursor) adapter.getItem(position);
    long clickedTaskId = cursor.getLong(cursor.getColumnIndex(Tasks._ID));
    cursor.close();
    
    intent.putExtra(TaskActivity.TASK_ID, clickedTaskId);
    startActivity(intent);    
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.tasks_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }   

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.tasks_menu_refresh:
        setProgressBarIndeterminateVisibility(true);
        new Thread(syncAllInBackground).start();
        return true;
      case R.id.tasks_menu_account:
        startActivity(new Intent(this, AccountActivity.class));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }  
  
  protected ServiceConnection syncConnection = new ServiceConnection() {
    
    public void onServiceDisconnected(ComponentName name) {}
    
    public void onServiceConnected(ComponentName name, IBinder serviceBinding) {
      SyncService.SyncBinder binding = (SyncService.SyncBinder) serviceBinding;
      syncService = binding.getService();
    }

  };
  
  protected Runnable refreshTasksInBackground = new Runnable() {
    
    public void run() {
      syncService.syncTasks();
    }
  };
  
  protected Runnable syncAllInBackground = new Runnable() {
    
    public void run() {
      syncService.syncAll();
    }
  };
  
  protected BroadcastReceiver updateReceiver = new BroadcastReceiver() {
    
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getStringExtra(SyncService.COLLECTION).equals(SyncService.ALL_COMPLETED)) {
        adapter.clearSections();
        populateList();
        setProgressBarIndeterminateVisibility(false);      
      }
    }
  };

  SectionedAdapter adapter = new SectionedAdapter() {
    protected View getHeaderView(String caption, int index, View convertView, ViewGroup parent) {
      LinearLayout result = (LinearLayout) convertView;

      if (convertView == null) {
        result = (LinearLayout) getLayoutInflater().inflate(R.layout.tasks_group_header, null);
      }

      TextView header = (TextView) result.findViewById(R.id.task_list_header_text);      
      header.setText(caption);

      return result;
    }
  };
  
  public final class TasksCursorAdapter extends SimpleCursorAdapter {

    public TasksCursorAdapter(Context context, int layout, Cursor c,
        String[] from, int[] to) {
      super(context, layout, c, from, to);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
      TextView durationView = (TextView) view.findViewById(R.id.task_item_duration);
      long seconds = cursor.getLong(cursor.getColumnIndex(Tasks.DURATION));
      durationView.setText(Util.secondsToHMS(seconds));

      TextView descriprionView = (TextView) view.findViewById(R.id.task_item_description);
      String description = cursor.getString(cursor.getColumnIndex(Tasks.DESCRIPTION));
      if (description == null) {
        descriprionView.setText(R.string.no_description);
        descriprionView.setTextColor(R.color.light_gray);
      } else {
        descriprionView.setText(description);  
      }
      
      TextView clientProjectNameView = (TextView) view.findViewById(R.id.task_item_client_project_name);
      String clientProjectName = cursor.getString(cursor.getColumnIndex(Projects.CLIENT_PROJECT_NAME));
      clientProjectNameView.setText(clientProjectName);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
      View view = getLayoutInflater().inflate(R.layout.task_item, null);
      bindView(view, context, cursor);
      return view;
    }
  }  

}
