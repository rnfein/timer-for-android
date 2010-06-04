package com.apprise.toggl;

import com.apprise.toggl.storage.DatabaseAdapter;
import com.apprise.toggl.storage.models.Task;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TaskActivity extends ApplicationActivity {

  public static final String TASK_ID = "TASK_ID";
  private static final String TAG = "TaskActivity";
  
  DatabaseAdapter dbAdapter;
  Task task;
  EditText descriptionView;
  TextView clientProjectNameView;
  TextView dateView;
  LinearLayout plannedTasksArea;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.task);
    
    init();
    initViews();
    
  } 
  
  protected void init() {
    dbAdapter = new DatabaseAdapter(this);
    dbAdapter.open();
    long _id = getIntent().getLongExtra(TASK_ID, -1);
    task = dbAdapter.findTask(_id);
  }

  protected void initViews() {
    descriptionView = (EditText) findViewById(R.id.task_project_description);
    dateView = (TextView) findViewById(R.id.task_date);
    clientProjectNameView = (TextView) findViewById(R.id.task_client_project_name);    
    plannedTasksArea = (LinearLayout) findViewById(R.id.task_planned_tasks_area);
    
    dateView.setText(Util.smallDateString(Util.parseStringToDate(task.start)));
    descriptionView.setText(task.description);
    
    if(task.project != null) {
      clientProjectNameView.setText(task.project.client_project_name); 
    } else {
      clientProjectNameView.setText(R.string.choose);
      clientProjectNameView.setTextColor(R.color.light_gray);
    }
    
    initPlannedTasks();
  }
  
  private void initPlannedTasks() {
    if (task.project != null) {
      long project_remote_id = task.project.id;
      Cursor cursor = dbAdapter.findPlannedTasksByProjectId(project_remote_id);
      if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
        plannedTasksArea.setVisibility(LinearLayout.GONE);
      }
    } else {
      plannedTasksArea.setVisibility(LinearLayout.GONE);      
    }
  }
}
