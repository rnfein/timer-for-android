package com.apprise.toggl;


import com.apprise.toggl.storage.DatabaseAdapter;
import com.apprise.toggl.storage.DatabaseAdapter.Clients;
import com.apprise.toggl.storage.models.Project;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class CreateProjectActivity extends ApplicationActivity {

  private Project project;
  private Toggl app;
  private DatabaseAdapter dbAdapter;
  private EditText projectNameView;
  private TextView projectClientView;
  private Button createButton;
  private Button cancelButton;
  
  static final String CREATED_PROJECT_REMOTE_ID = "created_project_remote_id";
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.create_project);
    
    app = (Toggl) getApplication();
    dbAdapter = new DatabaseAdapter(this, app);
    dbAdapter.open();
    project = dbAdapter.createDirtyProject();
    
    initViews();
    attachEvents();
  }

    
  @Override
  protected void onResume() {
    dbAdapter.open();
    super.onResume();
  }
  
  @Override
  protected void onPause() {
    dbAdapter.close();
    super.onPause();
  }
  
  private void initViews() {
    projectNameView = (EditText) findViewById(R.id.project_name);
    projectClientView = (TextView) findViewById(R.id.project_client);
    createButton = (Button) findViewById(R.id.create_project_create);
    cancelButton = (Button) findViewById(R.id.create_project_cancel);
   
    initClientView();
  }
  
  private void initClientView() {
    projectClientView.setText(project.client_project_name);
  }
  
  private void attachEvents() {
    createButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        project.name = new String( String.valueOf(projectNameView.getText()));
        dbAdapter.updateProject(project);
        Intent intent = getIntent();
        // TODO: Here we need an async api request to create a remote project to get the remote_id
        intent.putExtra(CREATED_PROJECT_REMOTE_ID, project.id);
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
    Cursor clientsCursor = dbAdapter.findAllClients();
    startManagingCursor(clientsCursor);

    String[] from = new String[] { Clients.NAME };
    int[] to = new int[] { R.id.item_name };
    final SimpleCursorAdapter clientsAdapter = new SimpleCursorAdapter(
        CreateProjectActivity.this, R.layout.simple_list_item, clientsCursor, from, to);

    AlertDialog.Builder builder = new AlertDialog.Builder(CreateProjectActivity.this);
    builder.setTitle(R.string.choose_client);
    builder.setAdapter(clientsAdapter, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int pos) {
        long clickedId = clientsAdapter.getItemId(pos);
        String clientName = dbAdapter.findClient(clickedId).name;
        project.client_project_name = clientName + " - " + project.name;
        dbAdapter.updateProject(project);
        initClientView();
      }
    });
    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
      }
    });    
    builder.show();
  }  
  
}
