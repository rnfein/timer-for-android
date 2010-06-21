package com.apprise.toggl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.apprise.toggl.remote.SyncService;
import com.apprise.toggl.storage.DatabaseAdapter;
import com.apprise.toggl.storage.DatabaseAdapter.Projects;
import com.apprise.toggl.storage.DatabaseAdapter.Tasks;
import com.apprise.toggl.storage.models.User;
import com.apprise.toggl.tracking.TimeTrackingService;
import com.apprise.toggl.widget.SectionedAdapter;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TasksActivity extends ListActivity {
 
  private static final String TAG = "TasksActivity";
  
  private DatabaseAdapter dbAdapter;
  private SyncService syncService;
  private Toggl app;
  private User currentUser;
  private SectionedTasksAdapter adapter;
  private LinkedList<TasksCursorAdapter> taskAdapters;
  private Button newTaskButton;
  
  private long trackedTaskId;
  private long trackedTaskDurationSeconds;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setProgressBarIndeterminate(true);
    setContentView(R.layout.tasks);

    adapter = new SectionedTasksAdapter();
    taskAdapters = new LinkedList<TasksCursorAdapter>();
    
    app = (Toggl) getApplication();
    dbAdapter = new DatabaseAdapter(this, app);
    dbAdapter.open();

    Intent intent = new Intent(this, SyncService.class);
    bindService(intent, syncConnection, BIND_AUTO_CREATE);
    
    initViews();
    attachEvents();
  }
  
  @Override
  protected void onResume() {
    super.onResume();    
    currentUser = app.getCurrentUser();
    trackedTaskId = -1;

    IntentFilter syncFilter = new IntentFilter(SyncService.SYNC_COMPLETED);
    registerReceiver(updateReceiver, syncFilter);
    
    IntentFilter timerFilter = new IntentFilter(TimeTrackingService.BROADCAST_SECOND_ELAPSED);
    registerReceiver(updateReceiver, timerFilter);

    adapter.clearSections();
    populateList();
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

  private void initViews() {
    newTaskButton = (Button) findViewById(R.id.new_task_button);
  }
  
  private void attachEvents() {
    newTaskButton.setOnClickListener(new View.OnClickListener() {
      
      public void onClick(View v) {
        Intent intent = new Intent(TasksActivity.this, TaskActivity.class);
        startActivity(intent);
      }
    });
  }
  
  private void populateList() {
    for (TasksCursorAdapter taskAdapter : taskAdapters) {
      stopManagingCursor(taskAdapter.getCursor());
      taskAdapter.getCursor().close();
    }
    taskAdapters.clear();
    
    int taskRetentionDays;
    if (currentUser != null) {
      taskRetentionDays = currentUser.task_retention_days;
    } else {
      taskRetentionDays = -1; // never touch the db
    }
    
    Calendar queryCal = (Calendar) Calendar.getInstance().clone();
    
    for (int i = 0; i <= taskRetentionDays; i++) {
      Cursor tasksCursor = dbAdapter.findTasksForListByDate(queryCal.getTime());

      if (tasksCursor.getCount() > 0) {
        startManagingCursor(tasksCursor);
        TasksCursorAdapter cursorAdapter = new TasksCursorAdapter(this, tasksCursor);
        taskAdapters.add(cursorAdapter);
        adapter.addSection(queryCal.getTime(), cursorAdapter);
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
    long id = 0;

    // TODO shouldn't be here if cursor is closed
    if (!tasksCursor.isClosed()) {
      tasksCursor.moveToFirst();

      while (!tasksCursor.isAfterLast()) {
        id = tasksCursor.getLong(tasksCursor.getColumnIndex(Tasks._ID));
        
        if (id == trackedTaskId) {
          duration = Util.convertIfRunningTime(trackedTaskDurationSeconds);
        } else {
          duration = tasksCursor.getLong(tasksCursor.getColumnIndex(Tasks.DURATION));  
        }

        duration_total += duration;
        tasksCursor.moveToNext();
      }      
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
  
  
  
  protected Runnable syncAllInBackground = new Runnable() {

    public void run() {
      if (app.isConnected()) {
        try {
          syncService.syncAll();
        } catch (Exception e) {
          Log.e(TAG, "Exception", e);
          runOnUiThread(new Runnable() {
            public void run() {
              Toast.makeText(TasksActivity.this,
                  getString(R.string.sync_failed), Toast.LENGTH_SHORT).show();
              setProgressBarIndeterminateVisibility(false);
            }
          });
        }
      } else {
        runOnUiThread(new Runnable() {
          public void run() {
            Toast.makeText(TasksActivity.this,
                getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
            setProgressBarIndeterminateVisibility(false);
          }
        });
      }
    }
  };
  
  protected BroadcastReceiver updateReceiver = new BroadcastReceiver() {
    
    @Override
    public void onReceive(Context context, Intent intent) {
      if (SyncService.SYNC_COMPLETED.equals(intent.getAction())) {
        String syncedHeader = intent.getStringExtra(SyncService.COLLECTION); 
        if (SyncService.ALL_COMPLETED.equals(syncedHeader) ||
            SyncService.ALL_COMPLETED_SCHEDULED.equals(syncedHeader)) {
          Log.d(TAG, "Received Broadcast: sync all completed");

          adapter.clearSections();
          populateList();

          setProgressBarIndeterminateVisibility(false);      
        }        
      }
      else if (TimeTrackingService.BROADCAST_SECOND_ELAPSED.equals(intent.getAction())) {
        trackedTaskId = intent.getLongExtra(TimeTrackingService.TRACKED_TASK_ID, -1);
        trackedTaskDurationSeconds = intent.getLongExtra(TimeTrackingService.TRACKED_TASK_DURATION, -1);
        
        for (TasksCursorAdapter taskAdapter : taskAdapters) {
          TextView durationView = taskAdapter.getDurationView(trackedTaskId);
          if (durationView != null) {
            durationView.setText(Util.secondsToHMS(trackedTaskDurationSeconds));
            durationView.setTextColor(getResources().getColor(R.color.red));            
            durationView.setTextSize(getResources().getDimension(R.dimen.listitem_running_duration_text_size));            

            SectionedHeader header = adapter.getHeader(taskAdapters.indexOf(taskAdapter));
            header.cursor = taskAdapter.getCursor();
            header.update();
            break;
          }
        }
      }
    }
  };
  
  private class SectionedTasksAdapter extends SectionedAdapter {
    
    private LinkedList<SectionedHeader> headers = new LinkedList<SectionedHeader>();
    
    public void addSection(Date tasksDate, CursorAdapter adapter) {
      SectionedHeader header = new SectionedHeader(tasksDate, adapter.getCursor());

      headers.add(header);
      addSection(header.toString(), adapter);
    }

    protected View getHeaderView(String caption, int index, View convertView, ViewGroup parent) {
      LinearLayout result = (LinearLayout) convertView;

      if (convertView == null) {
        result = (LinearLayout) getLayoutInflater().inflate(R.layout.tasks_group_header, null);
      }

      TextView headerView = (TextView) result.findViewById(R.id.task_list_header_text);      
      headerView.setText(caption);
      headers.get(index).textView = headerView;

      return result;
    }
    
    public SectionedHeader getHeader(int index) {
      return headers.get(index);
    }
    
  };
  
  private class SectionedHeader {

    public TextView textView;
    public Date tasksDate;
    public Cursor cursor;
    
    public SectionedHeader(Date tasksDate, Cursor cursor) {
      this.tasksDate = tasksDate;
      this.cursor = cursor;
    }

    public void update() {
      textView.setText(toString());
    }
    
    public String toString() {
      String prettyDate = Util.smallDateString(tasksDate);
      String prettyTotal = Util.secondsToHM(getDurationTotal(cursor)); 
      return prettyDate + " (" + prettyTotal + " h)";
    }
  }
  
  private class TasksCursorAdapter extends CursorAdapter {

    private Map<Long, TextView> durationViewsMap;
    
    public TasksCursorAdapter(Context context, Cursor cursor) {
      super(context, cursor);
      durationViewsMap = new HashMap<Long, TextView>();
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
      TextView durationView = (TextView) view.findViewById(R.id.task_item_duration);
      long id = cursor.getLong(cursor.getColumnIndex(Tasks._ID));
      
      if (id == trackedTaskId) {
        durationView.setText(Util.secondsToHMS(trackedTaskDurationSeconds));
      } else {
        long seconds = cursor.getLong(cursor.getColumnIndex(Tasks.DURATION));
        durationView.setText(Util.secondsToHMS(seconds));        
      }

      durationViewsMap.put(id, durationView);

      TextView descriprionView = (TextView) view.findViewById(R.id.task_item_description);
      String description = cursor.getString(cursor.getColumnIndex(Tasks.DESCRIPTION));
      if (description == null || description.equals("")) {
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
    
    public TextView getDurationView(long taskId) {
      return durationViewsMap.get(new Long(taskId));
    }

  }  

}
