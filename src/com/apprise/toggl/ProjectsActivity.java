package com.apprise.toggl;

import com.apprise.toggl.TasksActivity.TasksCursorAdapter;
import com.apprise.toggl.storage.DatabaseAdapter;
import com.apprise.toggl.storage.DatabaseAdapter.Projects;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public class ProjectsActivity extends ApplicationListActivity {

  DatabaseAdapter dbAdapter;
  Toggl app;
  Cursor projectsCursor;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.projects);
    
    init();
    populateList();
  }

  protected void init() {
    app = (Toggl) getApplication();
    dbAdapter = new DatabaseAdapter(this, app);
  }
  
  private void populateList() {
    String[] fieldsToShow = { Projects.CLIENT_PROJECT_NAME };
    int[] viewsToFill = { R.id.project_item_project_name };
    
    dbAdapter.open();
    projectsCursor = dbAdapter.findAllProjects();
    SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, R.layout.project_item,
        projectsCursor, fieldsToShow, viewsToFill);    

    setListAdapter(cursorAdapter);
    dbAdapter.close();
  }
  
}
