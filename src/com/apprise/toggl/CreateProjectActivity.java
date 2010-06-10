package com.apprise.toggl;


import com.apprise.toggl.storage.DatabaseAdapter;
import com.apprise.toggl.storage.DatabaseAdapter.Clients;
import com.apprise.toggl.storage.models.Client;
import com.apprise.toggl.storage.models.Project;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
  private String clientName;  

  static final String CREATED_PROJECT_LOCAL_ID = "created_project_local_id";
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.create_project);
    
    app = (Toggl) getApplication();
    dbAdapter = new DatabaseAdapter(this, app);
    dbAdapter.open();
    
    
    initViews();
    attachEvents();
  }
  
  @Override
  protected void onResume() {
    if (project == null) {
      project = dbAdapter.createDirtyProject();
    }
    super.onResume();
  }

  @Override
  protected void onPause() {
    if ("".equals(String.valueOf(projectNameView.getText()))) {
      dbAdapter.deleteProject(project._id);
      project = null;
    }
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    dbAdapter.close();
    super.onDestroy();
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
        project.name = new String(String.valueOf(projectNameView.getText()));
        project.client_project_name = clientName + " - " + projectNameView.getText();
        dbAdapter.updateProject(project);
        Intent intent = getIntent();
        intent.putExtra(CREATED_PROJECT_LOCAL_ID, project._id);
        setResult(RESULT_OK, intent);
        finish();          
      }
    });
    
    cancelButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        dbAdapter.deleteProject(project._id);
        setResult(RESULT_CANCELED);
        finish();
      }
    });
    
    findViewById(R.id.new_project_client_area).setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        showChooseClientDialog();
      }
    });    
  }

  private void showChooseClientDialog() {
    final Cursor clientsCursor = dbAdapter.findAllClients();
    startManagingCursor(clientsCursor);

    AlertDialog.Builder builder = new AlertDialog.Builder(CreateProjectActivity.this);
    builder.setTitle(R.string.choose_client);
    
    builder.setSingleChoiceItems(clientsCursor, getCheckedItem(clientsCursor, project.client), Clients.NAME, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        clientsCursor.moveToPosition(which);
        long clickedId = clientsCursor.getLong(clientsCursor.getColumnIndex(Clients._ID));;        
        Client client = dbAdapter.findClient(clickedId); 
        clientName = client.name;
        project.client = client;
        project.client_project_name = clientName + " - " + projectNameView.getText();
        dbAdapter.updateProject(project);
        projectClientView.setText(clientName);
        dialog.dismiss();
      }
    });
    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {}
    });    
    builder.show();
  }  
}
