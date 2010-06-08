package com.apprise.toggl.storage;

import com.apprise.toggl.storage.models.Model;
import com.apprise.toggl.storage.models.Project;
import com.apprise.toggl.storage.models.Task;

import android.test.AndroidTestCase;


public class ModelTest extends AndroidTestCase {
  
  public static final String TAG = "ModelTest";
  
  public void testTaskIdenticaTo() {
    Task task = new Task();
    Task similarTask = new Task();
    Task differentTask = new Task();
    
    differentTask.description = "something something darkside";
    
    assertTrue(task.identicalTo((Model) similarTask));
    assertFalse(task.identicalTo(differentTask));
  }
  
  public void testProjectIdenticaTo() {
    Project project = new Project();
    Project similarProject = new Project();
    Project differentProject = new Project();
    
    differentProject.client_project_name = "Darth Vader - Deathstar";
    
    assertTrue(project.identicalTo((Model) similarProject));
    assertFalse(project.identicalTo(differentProject));
  }

}
