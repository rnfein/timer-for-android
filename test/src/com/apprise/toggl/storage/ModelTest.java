package com.apprise.toggl;

import com.apprise.toggl.storage.models.*;

import android.test.AndroidTestCase;
import android.util.Log;


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
