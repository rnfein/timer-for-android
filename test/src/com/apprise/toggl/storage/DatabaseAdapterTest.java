package com.apprise.toggl.storage;

import java.util.Date;

import com.apprise.toggl.Toggl;
import com.apprise.toggl.TogglTests;
import com.apprise.toggl.Util;
import com.apprise.toggl.mock.MockToggl;
import com.apprise.toggl.storage.DatabaseAdapter;
import com.apprise.toggl.storage.DatabaseAdapter.DeletedTasks;
import com.apprise.toggl.storage.DatabaseAdapter.PlannedTasks;
import com.apprise.toggl.storage.DatabaseAdapter.Projects;
import com.apprise.toggl.storage.DatabaseAdapter.Tasks;
import com.apprise.toggl.storage.DatabaseAdapter.Users;
import com.apprise.toggl.storage.DatabaseAdapter.Workspaces;
import com.apprise.toggl.storage.models.DeletedTask;
import com.apprise.toggl.storage.models.PlannedTask;
import com.apprise.toggl.storage.models.Project;
import com.apprise.toggl.storage.models.Task;
import com.apprise.toggl.storage.models.User;
import com.apprise.toggl.storage.models.Workspace;

import android.database.Cursor;
import android.test.AndroidTestCase;
import android.util.Log;


public class DatabaseAdapterTest extends AndroidTestCase {
  
  private Toggl app;
  private DatabaseAdapter dbAdapter;  

  @Override
  protected void setUp() throws Exception {
    app = new MockToggl();
    
    dbAdapter = new DatabaseAdapter(getContext(), app);
    dbAdapter.setDatabaseName(TogglTests.TEST_DATABASE_NAME);
    dbAdapter.open();
    
    User user = new User();
    user.id = 123;
    user.email = "user@toggl.com";
    User createdUser = dbAdapter.createUser(user);
    app.logIn(createdUser);
    
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    dbAdapter.close();
    getContext().deleteDatabase(TogglTests.TEST_DATABASE_NAME);
    super.tearDown();
  }
  
  public void testCreateUser() {
    User userContents = new User();
    userContents.api_token = "tokitoki123";
    userContents.email = "bat@man.com";
    User createdUser = dbAdapter.createUser(userContents);
    assertNotNull(createdUser);
    assertEquals("tokitoki123", createdUser.api_token);
    assertEquals("bat@man.com", createdUser.email);
  }
  
  public void testFindUser() {
    User user = dbAdapter.createUser(new User());
    user.email = "cat@woman.com";
    dbAdapter.updateUser(user);
    User foundUser = dbAdapter.findUser(user._id);
    
    assertNotNull(foundUser);
    assertEquals(user.id, foundUser.id);
    assertEquals("cat@woman.com", foundUser.email);
  }
  
  public void testFindUserByApiToken() {
    User user = dbAdapter.createUser(new User());
    user.api_token = "APIAPIAPPI";
    dbAdapter.updateUser(user);
    User foundUser = dbAdapter.findUserByApiToken("APIAPIAPPI");
    
    assertNotNull(foundUser);
    assertEquals(user.id, foundUser.id);
    assertEquals("APIAPIAPPI", foundUser.api_token);
  }
  
  public void testFindUserByRemoteId() {
    User user = dbAdapter.createUser(new User());
    user.id = 345;
    user.email = "aqua@man.com";
    dbAdapter.updateUser(user);
    
    User foundUser = dbAdapter.findUserByRemoteId(345);
    assertNotNull(foundUser);
    assertEquals(345, foundUser.id);
    assertEquals("aqua@man.com", foundUser.email);
    assertEquals(user.id, foundUser.id);
  }
  
  public void testUpdateUser() {
    User user = dbAdapter.createUser(new User());
    assertEquals(0l, user.id);
    assertEquals(null, user.api_token);
    
    user.beginning_of_week = 2;
    user.date_format = "%m/%d/%Y";
    user.default_workspace_id = 555;
    user.fullname = "Robin Hood Loxley";
    user.id = 123;
    user.jquery_date_format = "m/d/Y";
    user.jquery_timeofday_format = "H:i";
    user.language = "en_GB";
    user.new_tasks_start_automatically = false;
    user.task_retention_days = 5;
    user.timeofday_format = "%H:%M";
    user.email = "robin@hood.com";
    user.api_token = "jibberish";
    dbAdapter.updateUser(user);

    User foundUser = dbAdapter.findUser(user._id);
    assertEquals(2, foundUser.beginning_of_week);
    assertEquals("%m/%d/%Y", foundUser.date_format);
    assertEquals(555, foundUser.default_workspace_id);
    assertEquals("Robin Hood Loxley", foundUser.fullname);
    assertEquals(123, foundUser.id);
    assertEquals("m/d/Y", foundUser.jquery_date_format);
    assertEquals("H:i", foundUser.jquery_timeofday_format);
    assertEquals("en_GB", foundUser.language);
    assertEquals(false, foundUser.new_tasks_start_automatically);
    assertEquals(5, foundUser.task_retention_days);
    assertEquals("%H:%M", foundUser.timeofday_format);
    assertEquals("robin@hood.com", foundUser.email);
    assertEquals("jibberish", foundUser.api_token);
  }
  
  public void testFindAllUsers() {
    User user1 = dbAdapter.createUser(new User());
    User user2 = dbAdapter.createUser(new User());
    
    Cursor allUsers = dbAdapter.findAllUsers();
    assertNotNull(allUsers);
    assertEquals(3, allUsers.getCount()); //consider current user created in setUp
    allUsers.moveToFirst();
    allUsers.moveToNext(); //step over current user
    assertEquals(user1._id, allUsers.getLong(allUsers.getColumnIndex(Users._ID)));
    allUsers.moveToNext();
    assertEquals(user2._id, allUsers.getLong(allUsers.getColumnIndex(Users._ID)));
    allUsers.close();
  }
  
  public void testDeleteUser() {
    User user = dbAdapter.createUser(new User());
    int deletedCount = dbAdapter.deleteUser(user._id);
    
    assertEquals(1, deletedCount);
    assertNull(dbAdapter.findUser(user._id));
  }
  
  public void testCreateWorkspace() {
    Workspace workspaceContents = new Workspace();
    workspaceContents.name = "work@space.com";
    Workspace createdWorkspace = dbAdapter.createWorkspace(workspaceContents);
    
    assertNotNull(createdWorkspace);
    assertEquals("work@space.com", createdWorkspace.name);
  }
  
  public void testFindWorkspace() {
    Workspace workspace = new Workspace();
    workspace.name = "jaha@baha.fa";
    Workspace createdWorkspace = dbAdapter.createWorkspace(workspace);

    Workspace foundWorkspace = dbAdapter.findWorkspace(createdWorkspace._id);
    assertNotNull(foundWorkspace);
    assertEquals(createdWorkspace._id, foundWorkspace._id);
    assertEquals("jaha@baha.fa", foundWorkspace.name);
  }
  
  public void testFindWorkspaceByRemoteId() {
    Workspace workspace = new Workspace();
    workspace.name = "jaha@baha.fa";
    Workspace createdWorkspace = dbAdapter.createWorkspace(workspace);
    
    Workspace foundWorkspace = dbAdapter.findWorkspaceByRemoteId(createdWorkspace.id);
    assertNotNull(foundWorkspace);
    assertEquals(createdWorkspace._id, foundWorkspace._id);
    assertEquals("jaha@baha.fa", foundWorkspace.name);
  }
  
  public void testFindAllWorkspaces() {
    Workspace workspace1 = dbAdapter.createWorkspace(new Workspace());
    Workspace workspace2 = dbAdapter.createWorkspace(new Workspace());
    
    Cursor allWorkspaces = dbAdapter.findAllWorkspaces();
    assertNotNull(allWorkspaces);
    assertEquals(2, allWorkspaces.getCount());
    allWorkspaces.moveToFirst();
    assertEquals(workspace1._id, allWorkspaces.getLong(allWorkspaces.getColumnIndex(Workspaces._ID)));
    allWorkspaces.moveToNext();
    assertEquals(workspace2._id, allWorkspaces.getLong(allWorkspaces.getColumnIndex(Workspaces._ID)));
    allWorkspaces.close();
  }
  
  public void testUpdateWorkspace() {
    Workspace workspace = dbAdapter.createWorkspace(new Workspace());
    assertEquals(0l, workspace.id);
    assertEquals(null, workspace.name);
    
    workspace.id = 2;
    workspace.name = "asd@asd.ee";
    dbAdapter.updateWorkspace(workspace);
    
    Workspace foundWorkspace = dbAdapter.findWorkspace(workspace._id);
    assertEquals(2, foundWorkspace.id);
    assertEquals("asd@asd.ee", foundWorkspace.name);
  }  
  
  public void testDeleteWorkspace() {
    Workspace workspace = dbAdapter.createWorkspace(new Workspace());
    int deletedId = dbAdapter.deleteWorkspace(workspace._id);
    
    assertEquals(workspace._id, deletedId);
    assertNull(dbAdapter.findWorkspace(workspace._id));
  }  
  
  public void testCreateProject() {
    Project projectContents = new Project();
    Workspace workspace = dbAdapter.createWorkspace(new Workspace());
    projectContents.name = "Big Project.";
    projectContents.workspace = workspace;
    Project createdProject = dbAdapter.createProject(projectContents);
    
    assertNotNull(createdProject);
    assertEquals("Big Project.", createdProject.name);    
    assertFalse(createdProject.sync_dirty);    
    assertEquals(workspace._id, createdProject.workspace._id);    
  }
  
  public void testCreateDirtyProject() {
    Project dirtyProject = dbAdapter.createDirtyProject();
    assertNotNull(dirtyProject);
    assertTrue(dirtyProject.sync_dirty);
  }
  
  public void testFindProject() {
    Project project = new Project();
    project.name = "Toggl Android Client";
    Project createdProject = dbAdapter.createProject(project);

    Project foundProject = dbAdapter.findProject(createdProject._id);
    assertNotNull(foundProject);
    assertEquals(createdProject._id, foundProject._id);
    assertEquals("Toggl Android Client", foundProject.name);
  }
  
  public void testFindProjectByRemoteId() {
    Project project = new Project();
    project.name = "Toggl Android Client";
    Project createdProject = dbAdapter.createProject(project);
    
    Project foundProject = dbAdapter.findProjectByRemoteId(createdProject.id);
    assertNotNull(foundProject);
    assertEquals(createdProject._id, foundProject._id);
    assertEquals("Toggl Android Client", foundProject.name);
  }
  
  public void testFindAllProjects() {
    Project project1 = dbAdapter.createProject(new Project());
    Project project2 = dbAdapter.createProject(new Project());
    
    Cursor allProjects = dbAdapter.findAllProjects();
    assertNotNull(allProjects);
    assertEquals(2, allProjects.getCount());
    allProjects.moveToFirst();
    assertEquals(project1._id, allProjects.getLong(allProjects.getColumnIndex(Projects._ID)));
    allProjects.moveToNext();
    assertEquals(project2._id, allProjects.getLong(allProjects.getColumnIndex(Projects._ID)));
    allProjects.close();
  }
  
  public void testUpdateProject() {
    Project project = dbAdapter.createProject(new Project());
    Workspace workspace = dbAdapter.createWorkspace(new Workspace());
    assertEquals(0l, project.id);
    assertEquals(null, project.name);
    
    project.id = 2;
    project.billable = true;
    project.client_project_name = "Big Client - Supersystem";
    project.estimated_workhours = 3200;
    project.fixed_fee = 320000;
    project.hourly_rate = 100;
    project.is_fixed_fee = true;
    project.name = "Supersystem";
    project.workspace = workspace;
    dbAdapter.updateProject(project);
    
    Project foundProject = dbAdapter.findProject(project._id);
    assertEquals(2, foundProject.id);
    assertTrue(foundProject.billable);
    assertEquals("Big Client - Supersystem", foundProject.client_project_name);
    assertEquals(3200, foundProject.estimated_workhours);
    assertEquals(320000F, foundProject.fixed_fee);
    assertEquals(100F, foundProject.hourly_rate);
    assertEquals("Supersystem", foundProject.name);
    assertTrue(foundProject.is_fixed_fee);
    assertEquals(workspace._id, foundProject.workspace._id);
  }   
  
  public void testDeleteProject() {
    Project project = dbAdapter.createProject(new Project());
    long deletedId = dbAdapter.deleteProject(project._id);
    
    assertEquals(project._id, deletedId);
    assertNull(dbAdapter.findProject(project._id));
  }
  
  public void testCreateTask() {
    Task taskContents = new Task();
    Workspace workspace = dbAdapter.createWorkspace(new Workspace());
    Project project = dbAdapter.createProject(new Project());
    taskContents.id = 123;
    taskContents.description = "download the internet";
    taskContents.workspace = workspace;
    taskContents.project = project;
    Task createdTask = dbAdapter.createTask(taskContents);
    
    assertNotNull(createdTask);
    assertEquals(123, createdTask.id);
    assertEquals("download the internet", createdTask.description);
    assertEquals(workspace._id, createdTask.workspace._id);
    assertEquals(project._id, createdTask.project._id);
  }
  
  public void testCreateDirtyTask() {
    Task dirtyTask = dbAdapter.createDirtyTask();
    
    assertNotNull(dirtyTask);
    assertTrue(dirtyTask.sync_dirty);
  }
  
  public void testFindTask() {
    Task task = new Task();
    task.description = "do a backflip";
    Task createdTask = dbAdapter.createTask(task);

    Task foundTask = dbAdapter.findTask(createdTask._id);
    assertNotNull(foundTask);
    assertEquals(createdTask._id, foundTask._id);
    assertEquals("do a backflip", foundTask.description);    
  }
  
  public void testFindTasksByDate() {
    Task task1 = new Task();
    task1.start = "2010-05-12T06:19:45+02:00";
    Task createdTask1 = dbAdapter.createTask(task1);

    Task task2 = new Task();
    task2.start = "2010-05-12T14:19:45+02:00";
    Task createdTask2 = dbAdapter.createTask(task2);
    
    Task task3 = new Task();
    task2.start = "2010-15-12T04:19:45+02:00";
    dbAdapter.createTask(task3);
    
    Date date = new Date();
    date.setYear(110);
    date.setMonth(04);
    date.setDate(12);
    
    Log.d("DatabaseAdapterTest", "date: " + date);
    
    Cursor allTasks = dbAdapter.findTasksForListByDate(date);
    assertNotNull(allTasks);
    assertEquals(2, allTasks.getCount());
    allTasks.moveToFirst();
    assertEquals(createdTask1._id, allTasks.getLong(allTasks.getColumnIndex(Tasks._ID)));
    allTasks.moveToNext();
    assertEquals(createdTask2._id, allTasks.getLong(allTasks.getColumnIndex(Tasks._ID)));
    allTasks.close();   
  }
  
  public void testFindTaskByRemoteId() {
    Task task = new Task();
    task.description = "do a frontflip";
    Task createdTask = dbAdapter.createTask(task);
    
    Task foundTask = dbAdapter.findTaskByRemoteId(createdTask.id);
    assertNotNull(foundTask);
    assertEquals(createdTask._id, foundTask._id);
    assertEquals("do a frontflip", foundTask.description);    
  }
  
  public void testFindRunnintTask() {
    Task task = new Task();
    task.duration = -5213512; //running task with startpoint since epoch
    Task runningTask = dbAdapter.createTask(task);
    
    dbAdapter.createTask(new Task());
    dbAdapter.createTask(new Task());
    
    Task foundTask = dbAdapter.findRunningTask();
    assertNotNull(foundTask);
    assertEquals(runningTask._id, foundTask._id);
    assertEquals(-5213512, foundTask.duration);    
  }
  
  public void testFindAllTasks() {
    Task task1 = dbAdapter.createTask(new Task());
    Task task2 = dbAdapter.createTask(new Task());
    
    Cursor allTasks = dbAdapter.findAllTasks();
    assertNotNull(allTasks);
    assertEquals(2, allTasks.getCount());
    allTasks.moveToFirst();
    assertEquals(task1._id, allTasks.getLong(allTasks.getColumnIndex(Tasks._ID)));
    allTasks.moveToNext();
    assertEquals(task2._id, allTasks.getLong(allTasks.getColumnIndex(Tasks._ID)));
    allTasks.close();
  }
  
  public void testFindTasksByProjectLocalId() {
    Task taskContents = new Task();
    Project project = dbAdapter.createProject(new Project());
    
    taskContents.project = null;
    dbAdapter.createTask(taskContents); //task without project
    
    taskContents.project = project;
    Task task1 = dbAdapter.createTask(taskContents);
    
    taskContents.project = project;
    Task task2 = dbAdapter.createTask(taskContents);
    
    Cursor tasks = dbAdapter.findAllTasksByProjectLocalId(project._id);
    assertNotNull(tasks);
    assertEquals(2, tasks.getCount());
    tasks.moveToFirst();
    assertEquals(task1._id, tasks.getLong(tasks.getColumnIndex(Tasks._ID)));
    tasks.moveToNext();
    assertEquals(task2._id, tasks.getLong(tasks.getColumnIndex(Tasks._ID)));
    tasks.close();
  }
  
  public void testFindTasksForList() {
    Task taskContents = new Task();
    
    taskContents.start = "2010-06-12T06:19:45+02:00";
    dbAdapter.createTask(taskContents); //task with another date
    
    taskContents.start = "2010-05-12T06:19:45+02:00";
    Task task1 = dbAdapter.createTask(taskContents);
    
    taskContents.start = "2010-05-12T10:19:45+02:00";
    Task task2 = dbAdapter.createTask(taskContents);
    
    Date date = new Date(110, 04, 12, 12, 00);
    Log.d("DBtests", "date: " + Util.formatDateToString(date));
    Cursor tasks = dbAdapter.findTasksForListByDate(date);
    assertNotNull(tasks);
    assertEquals(2, tasks.getCount());
    tasks.moveToFirst();
    assertEquals(task1._id, tasks.getLong(tasks.getColumnIndex(Tasks._ID)));
    tasks.moveToNext();
    assertEquals(task2._id, tasks.getLong(tasks.getColumnIndex(Tasks._ID)));
    tasks.close();
  }
  
  public void testFindTasksForAutocomplete() { //TODO: fishi
    Task otherTask = new Task();
    otherTask.description = "ZXCZX";
    dbAdapter.createTask(otherTask); //out of constraint
    
    Task taskContents1 = new Task();    
    taskContents1.description = "aaaaa";
    dbAdapter.createTask(taskContents1);
    
    Project projectContents = new Project();
    projectContents.client_project_name = "aaaaa - AAA";
    Project project = dbAdapter.createProject(projectContents);
    
    Task taskContents2 = new Task();    
    taskContents2.project = project;
    dbAdapter.createTask(taskContents2);
    
    CharSequence constraint = "aa";
    Cursor tasks = dbAdapter.findTasksForAutocomplete(constraint);
    Log.d("DBTests", "count: " + tasks.getCount());
    
    assertNotNull(tasks);
    assertEquals(2, tasks.getCount());
    tasks.close();
  }
  
  public void testUpdateTask() {
    Task task = dbAdapter.createTask(new Task());
    Project project = dbAdapter.createProject(new Project());
    Workspace workspace = dbAdapter.createWorkspace(new Workspace());
    assertEquals(0l, task.id);
    assertEquals(null, task.description);

    String[] tagNamesArray = new String[] {String.valueOf("tag1"), String.valueOf("tag2")};
    
    task.id = 2;
    task.billable = true;
    task.description = "megatask";
    task.duration = 10;
    task.project = project;
    task.start = "2010-02-12T16:19:45+02:00";
    task.stop = null;
    task.workspace = workspace;
    task.tag_names = tagNamesArray;
    dbAdapter.updateTask(task);
    
    Task foundTask = dbAdapter.findTask(task._id);
    assertEquals(2, foundTask.id);
    assertTrue(foundTask.billable);
    assertEquals("megatask", foundTask.description);
    assertEquals(10, foundTask.duration);
    assertEquals(project._id, foundTask.project._id);
    assertEquals("2010-02-12T16:19:45+02:00", foundTask.start);
    assertEquals("tag1;tag2", Util.joinStringArray(foundTask.tag_names, ";"));
    assertNull(foundTask.stop);
    assertEquals(workspace._id, foundTask.workspace._id);
  }
  
  public void testDeleteTask() {
    Task taskContents = new Task(); 
    taskContents.id = 456;
    Task createdTask = dbAdapter.createTask(taskContents);
    long remoteId = createdTask.id;
    dbAdapter.deleteTask(createdTask);
    
    assertNull(dbAdapter.findTask(createdTask._id));

    DeletedTask deletedTask = dbAdapter.findDeletedTask(remoteId);
    assertNotNull(deletedTask);
    assertEquals(remoteId, deletedTask.taskRemoteId);
  }
  
  public void testDeleteDeletedTask() {
    Task taskContents = new Task(); 
    taskContents.id = 456;
    Task createdTask = dbAdapter.createTask(taskContents);
    dbAdapter.deleteTask(createdTask);

    DeletedTask deletedTask = dbAdapter.findDeletedTask(createdTask.id);
    assertNotNull(deletedTask);
    dbAdapter.deleteDeletedTask(deletedTask._id);
    assertNull(dbAdapter.findDeletedTask(deletedTask.taskRemoteId));
  }
  
  public void testFindAllDeletedTasks() {
    Task taskContents1 = new Task(); 
    taskContents1.id = 456;
    Task createdTask1 = dbAdapter.createTask(taskContents1);
    dbAdapter.deleteTask(createdTask1);

    Task taskContents2 = new Task(); 
    taskContents2.id = 789;
    Task createdTask2 = dbAdapter.createTask(taskContents2);
    dbAdapter.deleteTask(createdTask2);
    
    Cursor allDeletedTasks = dbAdapter.findAllDeletedTasks();
    assertNotNull(allDeletedTasks);
    assertEquals(2, allDeletedTasks.getCount());
    allDeletedTasks.moveToFirst();
    assertEquals(createdTask1.id, allDeletedTasks.getLong(allDeletedTasks.getColumnIndex(DeletedTasks.TASK_REMOTE_ID)));
    allDeletedTasks.moveToNext();
    assertEquals(createdTask2.id, allDeletedTasks.getLong(allDeletedTasks.getColumnIndex(DeletedTasks.TASK_REMOTE_ID)));
    allDeletedTasks.close();
  }
  
  public void testCreatePlannedTask() {
    PlannedTask plannedTaskContents = new PlannedTask();
    Workspace workspace = dbAdapter.createWorkspace(new Workspace());
    Project project = dbAdapter.createProject(new Project());
    plannedTaskContents.id = 123;
    plannedTaskContents.workspace = workspace;
    plannedTaskContents.project = project;
    PlannedTask createdPlannedTask = dbAdapter.createPlannedTask(plannedTaskContents);
    
    assertNotNull(createdPlannedTask);
    assertEquals(123, createdPlannedTask.id);
    assertEquals(workspace._id, createdPlannedTask.workspace._id);
    assertEquals(project._id, createdPlannedTask.project._id);    
  }
  
  public void testFindPlannedTask() {
    PlannedTask plannedTaskContents = new PlannedTask();
    PlannedTask createdPlannedTask = dbAdapter.createPlannedTask(plannedTaskContents);
    
    PlannedTask foundPlannedTask = dbAdapter.findPlannedTask(createdPlannedTask._id);
    assertNotNull(foundPlannedTask);
    assertEquals(createdPlannedTask._id, foundPlannedTask._id);
  }
  
  public void testFindPlannedTasksByProjectId() {
    Project projectContents = new Project();
    projectContents.id = 123123l;
    Project project = dbAdapter.createProject(projectContents);
    
    PlannedTask plannedTaskContents1 = new PlannedTask();
    plannedTaskContents1.project = project;
    PlannedTask projectPlannedTask1 = dbAdapter.createPlannedTask(plannedTaskContents1);

    PlannedTask plannedTaskContents2 = new PlannedTask();
    plannedTaskContents2.project = project;
    PlannedTask projectPlannedTask2 = dbAdapter.createPlannedTask(plannedTaskContents2);
    
    dbAdapter.createPlannedTask(new PlannedTask());
    
    Cursor allPlannedTasks = dbAdapter.findPlannedTasksByProjectId(project.id);
    assertNotNull(allPlannedTasks);
    assertEquals(2, allPlannedTasks.getCount());
    allPlannedTasks.moveToFirst();
    assertEquals(projectPlannedTask1._id, allPlannedTasks.getLong(allPlannedTasks.getColumnIndex(PlannedTasks._ID)));
    allPlannedTasks.moveToNext();
    assertEquals(projectPlannedTask2._id, allPlannedTasks.getLong(allPlannedTasks.getColumnIndex(PlannedTasks._ID)));
    allPlannedTasks.close();
  }
  
  public void testFindPlannedTaskByRemoteId() {
    PlannedTask plannedTaskContents = new PlannedTask();
    plannedTaskContents.id = 321;
    PlannedTask createdPlannedTask = dbAdapter.createPlannedTask(plannedTaskContents);
    
    PlannedTask foundPlannedTask = dbAdapter.findPlannedTaskByRemoteId(createdPlannedTask.id);
    assertNotNull(foundPlannedTask);
    assertEquals(createdPlannedTask._id, foundPlannedTask._id);
    assertEquals(createdPlannedTask.id, foundPlannedTask.id);
  }
  
  public void testFindAllPlannedTasks() {
    PlannedTask plannedTask1 = dbAdapter.createPlannedTask(new PlannedTask());
    PlannedTask plannedTask2 = dbAdapter.createPlannedTask(new PlannedTask());
    
    assertNotNull(plannedTask1);
    assertNotNull(plannedTask2);
    
    Cursor allPlannedTasks = dbAdapter.findAllPlannedTasks();
    assertNotNull(allPlannedTasks);
    assertEquals(2, allPlannedTasks.getCount());
    allPlannedTasks.moveToFirst();
    assertEquals(plannedTask1._id, allPlannedTasks.getLong(allPlannedTasks.getColumnIndex(PlannedTasks._ID)));
    allPlannedTasks.moveToNext();
    assertEquals(plannedTask2._id, allPlannedTasks.getLong(allPlannedTasks.getColumnIndex(PlannedTasks._ID)));
    allPlannedTasks.close();
  }  

  public void testUpdatePlannedTask() {
    PlannedTask plannedTask = dbAdapter.createPlannedTask(new PlannedTask());
    Project project = dbAdapter.createProject(new Project());
    Workspace workspace = dbAdapter.createWorkspace(new Workspace());
    User user = dbAdapter.createUser(new User());
    assertEquals(0l, plannedTask.id);
    
    plannedTask.id = 2;
    plannedTask.estimated_workhours = 120;
    plannedTask.name = "refactoring";
    plannedTask.project = project;
    plannedTask.workspace = workspace;
    plannedTask.user = user;
   
    dbAdapter.updatePlannedTask(plannedTask);
    
    PlannedTask foundPlannedTask = dbAdapter.findPlannedTask(plannedTask._id);
    assertEquals(2, foundPlannedTask.id);
    assertEquals(120, foundPlannedTask.estimated_workhours);
    assertEquals("refactoring", foundPlannedTask.name);
    assertEquals(project._id, foundPlannedTask.project._id);
    assertEquals(workspace._id, foundPlannedTask.workspace._id);
    assertEquals(user._id, foundPlannedTask.user._id);
  }
  
  public void testDeletePlannedTask() {
    PlannedTask createdPlannedTask = dbAdapter.createPlannedTask(new PlannedTask());
    dbAdapter.deletePlannedTask(createdPlannedTask._id);
    
    assertNull(dbAdapter.findPlannedTask(createdPlannedTask._id));
  }

}


