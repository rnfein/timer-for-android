package com.apprise.toggl;

import java.util.Date;
import java.util.Calendar;

import com.apprise.toggl.storage.DatabaseAdapter;
import com.apprise.toggl.storage.DatabaseAdapter.Projects;
import com.apprise.toggl.storage.models.Task;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class TaskActivity extends ApplicationActivity {

  public static final String TASK_ID = "TASK_ID";
  private static final String TAG = "TaskActivity";
  
  private static final int DATE_DIALOG_ID = 0;  
  
  DatabaseAdapter dbAdapter;
  Task task;
  EditText descriptionView;
  Spinner projectSpinner;
  TextView dateView;
  TextView plannedTasksView;
  TextView tagsView;
  LinearLayout plannedTasksArea;
  CheckBox billableCheckBox;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.task);
    
    init();
    initViews();
    attachEvents();
  } 
  
  protected void init() {
    dbAdapter = new DatabaseAdapter(this, (Toggl) getApplication());
    dbAdapter.open();
    long _id = getIntent().getLongExtra(TASK_ID, -1);
    task = dbAdapter.findTask(_id);
  }

  protected void initViews() {
    descriptionView = (EditText) findViewById(R.id.task_description);
    dateView = (TextView) findViewById(R.id.task_date);
    projectSpinner = (Spinner) findViewById(R.id.task_project);    
    plannedTasksArea = (LinearLayout) findViewById(R.id.task_planned_tasks_area);
    plannedTasksView = (TextView) findViewById(R.id.task_planned_tasks);
    tagsView = (TextView) findViewById(R.id.task_tags);
    billableCheckBox = (CheckBox) findViewById(R.id.task_billable_cb);
    
    descriptionView.setText(task.description);
    billableCheckBox.setChecked(task.billable);
    initDateView();
    initProjectSpinner();
    
    initPlannedTasks();
  }

  private void initProjectSpinner() {
    Cursor projectsCursor = dbAdapter.findAllProjectsForSpinner();
    startManagingCursor(projectsCursor);
    
    String[] from = new String[]{Projects.CLIENT_PROJECT_NAME};
    int[] to = new int[]{R.id.project_item_project_name};
    
    SimpleCursorAdapter projectsAdapter =
      new SimpleCursorAdapter(this, R.layout.project_item, projectsCursor, from, to);
    projectsAdapter.setDropDownViewResource(R.layout.project_dropdown_item);
    projectSpinner.setAdapter(projectsAdapter);    
    projectSpinner.setOnItemSelectedListener(new OnProjectItemSelectedListener());

    if (task.project != null) {
      for (int i = -1; i < projectsCursor.getCount(); i++) {
        if (task.project._id == projectsAdapter.getItemId(i)) {
          projectSpinner.setSelection(i);
        }
      }
    }   
  }

  private void initDateView() {
    dateView.setText(Util.smallDateString(Util.parseStringToDate(task.start)));
  }
  
  private void initPlannedTasks() {
    if (task.project != null) {
      long project_remote_id = task.project.id;
      Cursor cursor = dbAdapter.findPlannedTasksByProjectId(project_remote_id);
      if (cursor != null) {
        if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
          plannedTasksArea.setVisibility(LinearLayout.GONE);
        }
      }
    } else {
      plannedTasksArea.setVisibility(LinearLayout.GONE);

    }
  }
  
  protected void attachEvents() {
    findViewById(R.id.task_project_area).setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Log.d(TAG, "clicked project name");        
        projectSpinner.performClick();
      }
    });
    
    findViewById(R.id.task_date_area).setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Log.d(TAG, "clicked date");        
        showDialog(DATE_DIALOG_ID);
      }
    });
    
    billableCheckBox.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Log.d(TAG, "clicked billable cb");
        task.billable = billableCheckBox.isChecked();
        saveTask();
      }
    });
    
    findViewById(R.id.task_tags_area).setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Log.d(TAG, "clicked tags");
        //TODO: tags
      }
    });
    
    findViewById(R.id.task_planned_tasks_area).setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Log.d(TAG, "clicked planned tasks");
        //TODO: planned tasks
      }
    });
  }
  
  protected void saveTask() {
    Log.d(TAG, "saving task: " + task);
    task.sync_dirty = true;
    if (!dbAdapter.updateTask(task))
      dbAdapter.createTask(task);
  }
  
  private void setDate(int year, int month, int date) {
    Date start = Util.parseStringToDate(task.start);
    Date stop = Util.parseStringToDate(task.stop);
    
    Calendar cal = (Calendar) Calendar.getInstance().clone();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month);
    cal.set(Calendar.DATE, date);
    cal.set(Calendar.HOUR_OF_DAY, start.getHours());
    cal.set(Calendar.MINUTE, start.getMinutes());
    cal.set(Calendar.SECOND, start.getSeconds());
    task.start = Util.formatDateToString(cal.getTime());

    cal.set(Calendar.HOUR_OF_DAY, stop.getHours());
    cal.set(Calendar.MINUTE, stop.getMinutes());
    cal.set(Calendar.SECOND, stop.getSeconds());
    task.stop = Util.formatDateToString(cal.getTime());
    
    saveTask();
    initDateView();
  }
  
  private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

    public void onDateSet(DatePicker view, int year, int month,
        int date) {
      setDate(year, month, date);
    }
  };
  
  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
    case DATE_DIALOG_ID:
      Calendar cal = Util.parseStringToCalendar(task.start);
      int mYear = cal.get(Calendar.YEAR);
      int mMonth = cal.get(Calendar.MONTH);
      int mDay = cal.get(Calendar.DATE);
      return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
    }
    return null;
  }
  
  public class OnProjectItemSelectedListener implements OnItemSelectedListener {

    public void onItemSelected(AdapterView<?> parent,
        View view, int pos, long id) {
      Log.d(TAG, parent.getItemAtPosition(pos).toString());
    }

    public void onNothingSelected(AdapterView parent) {
      // Do nothing.
    }
}  
}
