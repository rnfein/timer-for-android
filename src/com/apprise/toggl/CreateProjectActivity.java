package com.apprise.toggl;

import java.util.Vector;

import com.apprise.toggl.storage.DatabaseAdapter;
import com.apprise.toggl.storage.models.Project;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CreateProjectActivity extends ApplicationActivity {

  private Project project;
  private Toggl app;
  private DatabaseAdapter dbAdapter;
  private EditText projectNameView;
  private TextView projectClientView;
  private Button createButton;
  private Button cancelButton;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.create_project);
    
    init();
    attachEvents();
  }
  
  private void init() {
    app = (Toggl) getApplication();
    dbAdapter = new DatabaseAdapter(this, app);
    project = dbAdapter.createDirtyProject();    
  }
  
  private void initViews() {
    projectNameView = (EditText) findViewById(R.id.project_name);
    projectClientView = (TextView) findViewById(R.id.project_client);
    createButton = (Button) findViewById(R.id.create_project_create);
    cancelButton = (Button) findViewById(R.id.create_project_cancel);
  }
  
  private void attachEvents() {
    createButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        project.name = new String( String.valueOf(projectNameView.getText()));
        dbAdapter.updateProject(project);
      }
    });
    
    cancelButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        dbAdapter.deleteProject(project._id);
      }
    });
    
    findViewById(R.id.new_project_client_area).setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        //TODO: build dialog for choosing client
      }
    });    
  }

}
