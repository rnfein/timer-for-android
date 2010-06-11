package com.apprise.toggl.remote;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.apprise.toggl.Toggl;
import com.apprise.toggl.remote.exception.FailedResponseException;
import com.apprise.toggl.remote.exception.NotSignedInException;
import com.apprise.toggl.storage.models.Client;
import com.apprise.toggl.storage.models.Model;
import com.apprise.toggl.storage.models.PlannedTask;
import com.apprise.toggl.storage.models.Project;
import com.apprise.toggl.storage.models.Task;
import com.apprise.toggl.storage.models.User;
import com.apprise.toggl.storage.models.Workspace;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.util.Log;

public class TogglWebApi {

  private static final int CONNECTION_TIMEOUT = 30 * 1000; // ms

  private static final String TAG = "TogglWebApi";
  private static final String BASE_URL = "http://www.toggl.com";
  private static final String API_URL = BASE_URL + "/api/v1";
  private static final String SESSIONS_URL = API_URL + "/sessions.json";
  private static final String TASKS_URL = API_URL + "/tasks.json";
  private static final String TASKS_URL_BASE = API_URL + "/tasks/";
  private static final String WORKSPACES_URL = API_URL + "/workspaces.json";
  private static final String PROJECTS_URL = API_URL + "/projects.json";
  private static final String PLANNED_TASKS_URL = API_URL + "/planned_tasks.json";
  private static final String CLIENTS_URL = API_URL + "/clients.json";
  private static final String EMAIL = "email";
  private static final String PASSWORD = "password";
  private static final String API_TOKEN = "api_token";

  private static DefaultHttpClient httpClient;

  private String apiToken;
  private boolean restartSession = true; 
  
  public TogglWebApi(String apiToken) {
    this.apiToken = apiToken;
    maybeCreateHttpClient();
  }
  
  public void setApiToken(String apiToken) {
    this.apiToken = apiToken;
    this.restartSession = true;
  }

  public User authenticateWithCredentials(final String email, final String password) {
    ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair(EMAIL, email));
    params.add(new BasicNameValuePair(PASSWORD, password));

    return userAuthentication(params);
  }

  public User authenticateWithToken(final String apiToken) {
    this.apiToken = apiToken;
    return userAuthentication(paramsWithApiToken());
  }
  
  private User userAuthentication(ArrayList<NameValuePair> params) {
    HttpResponse response = executePostRequest(SESSIONS_URL, params);
    
    if (ok(response)) {
      Gson gson = new Gson();
      InputStreamReader reader = null;
      try {
        reader = getResponseReader(response);
        User user = gson.fromJson(reader, User.class);            
        Log.d(TAG, "TogglWebApi#userAuthentication got a successful response for user: " + user.toString());
        this.apiToken = user.api_token;
        return user;          
      } finally {
        try {
          reader.close();
        } catch (IOException e) {}
      }
    } else {
      int statusCode = response.getStatusLine().getStatusCode();
      Log.w(TAG, "TogglWebApi#userAuthentication got a failed request: " + statusCode);
    }
    return null;
  }
  
  @SuppressWarnings("unchecked")
  public LinkedList<Workspace> fetchWorkspaces() {
    Type collectionType = new TypeToken<LinkedList<Workspace>>() {}.getType();
    return (LinkedList<Workspace>) fetchCollection(collectionType, WORKSPACES_URL);    
  }
  
  @SuppressWarnings("unchecked")
  public LinkedList<Project> fetchProjects() {
    Type collectionType = new TypeToken<LinkedList<Project>>() {}.getType();
    return (LinkedList<Project>) fetchCollection(collectionType, PROJECTS_URL);
  }
  
  public Project createProject(Project project, Toggl app) {
    Type type = new TypeToken<Project>() {}.getType();
    String jsonString = project.apiJsonString(app.getCurrentUser());
    String url = PROJECTS_URL;
    Gson gson = new Gson();    
    InputStreamReader reader = postJSON(jsonString, url);    

    try {
      return gson.fromJson(reader, type);
    } finally {
      try {
        reader.close();
      } catch (IOException e) {}
    }
  }
  
  @SuppressWarnings("unchecked")
  public LinkedList<PlannedTask> fetchPlannedTasks() {
    Type collectionType = new TypeToken<LinkedList<PlannedTask>>() {}.getType();
    return (LinkedList<PlannedTask>) fetchCollection(collectionType, PLANNED_TASKS_URL);
  }

  @SuppressWarnings("unchecked")
  public LinkedList<Task> fetchTasks() {
    Type collectionType = new TypeToken<LinkedList<Task>>() {}.getType();
    return (LinkedList<Task>) fetchCollection(collectionType, TASKS_URL);    
  }
  
  public Task createTask(Task task, Toggl app) {
    Type type = new TypeToken<Task>() {}.getType();
    String jsonString = task.apiJsonString(app.getCurrentUser());
    String url = TASKS_URL;
    Gson gson = new Gson();    
    InputStreamReader reader = postJSON(jsonString, url);
    try {
      return gson.fromJson(reader, type);
    } finally {
      try {
        reader.close();
      } catch (IOException e) {}
    }    
  }  
  
  public Task updateTask(Task task, Toggl app) {
    Type type = new TypeToken<Task>() {}.getType();
    String jsonString = task.apiJsonString(app.getCurrentUser());
    String url = TASKS_URL_BASE + task.id + ".json";
    Gson gson = new Gson();
    Log.d(TAG, "update task url: " + url);
    InputStreamReader reader = putJSON(jsonString, url);
    try {
      return gson.fromJson(reader, type);
    } finally {
      try {
        reader.close();
      } catch (IOException e) {}
    } 
  }  
  
  public boolean deleteTask(long id) {
    String url = TASKS_URL_BASE + id + ".json";
    HttpResponse response = executeDeleteRequest(url);
    if (ok(response)) {    
      Log.d(TAG, "TogglWebApi#deleteTask got a successful response");
      return true;
    }
    return false;
  }  
  
  @SuppressWarnings("unchecked")
  public LinkedList<Client> fetchClients() {
    Type collectionType = new TypeToken<LinkedList<Client>>() {}.getType();
    return (LinkedList<Client>) fetchCollection(collectionType, CLIENTS_URL);    
  }
  
  private LinkedList<? extends Model> fetchCollection(Type collectionType, String url) {
    if (getSession()) {
      HttpResponse response = executeGetRequest(url);
      if (ok(response)) {
        Gson gson = new Gson();
        Log.d(TAG, "TogglWebApi#fetchCollection got a successful response");
        InputStreamReader reader = null;
        try {
          reader = getResponseReader(response);
          return gson.fromJson(reader, collectionType);          
        } finally {
          try {
            reader.close();
          } catch (IOException e) {}
        }
      } else {
        Log.e(TAG, "TogglWebApi#fetchCollection got a failed request: "
            + response.getStatusLine().getStatusCode());
        throw new FailedResponseException();
      }
    } else {
      throw new NotSignedInException();
    }
  }
  
  private InputStreamReader putJSON(String jsonString, String url) {      
    if (getSession()) {
      Log.d(TAG, "put JSON: " + jsonString);
      HttpResponse response = executeJSONPutRequest(url, jsonString);    
      if (ok(response)) {
        Log.d(TAG, "TogglWebApi#putJSON got a successful response.");
        try {
          InputStreamReader reader = null;          
          reader = getResponseReader(response);
          return reader;
        } catch (Exception e) {
        }
      } else {
        Log.w(TAG, "TogglWebApi#putJSON got a failed request: "
            + response.getStatusLine().getStatusCode());
        throw new FailedResponseException();
      }
    } else {
      throw new NotSignedInException();
    }
    return null;
  }  
  
  private InputStreamReader postJSON(String jsonString, String url) {      
    if (getSession()) {
      Log.d(TAG, "post JSON: " + jsonString);
      HttpResponse response = executeJSONPostRequest(url, jsonString);    
      if (ok(response)) {
        Log.d(TAG, "TogglWebApi#postJSON got a successful response");
        return getResponseReader(response);
      } else {
        Log.w(TAG, "TogglWebApi#postJSON got a failed request: "
            + response.getStatusLine().getStatusCode());
        throw new FailedResponseException();
      }
    } else {
      throw new NotSignedInException();
    }
  }  
  
  /*
   * Authenticate if session cookies are missing
   * */
  private boolean getSession() {
    if (!restartSession) { 
      for (Cookie cookie : httpClient.getCookieStore().getCookies()) {
        Log.d(TAG, "cookie: " + cookie.getName());
        if (cookie.getName().equals("_toggl_session"))
          return true;
      }
    } 
    restartSession = false;
    return (userAuthentication(paramsWithApiToken()) != null);    
  }

  protected HttpResponse executeGetRequest(String url) {
    return executeGetRequest(url, new ArrayList<NameValuePair>());
  }
  
  protected HttpResponse executeGetRequest(String url,
      ArrayList<NameValuePair> params) {
    StringBuilder uriBuilder = new StringBuilder(url);

    for (NameValuePair param : params) {
      uriBuilder.append(params.indexOf(param) == 0 ? "?" : "&");
      uriBuilder.append(param.getName() + "=" + param.getValue());
    }
    HttpGet request = new HttpGet(uriBuilder.toString());
    return execute(request);
  }

  protected HttpResponse executePostRequest(String url,
      ArrayList<NameValuePair> params) {
    HttpEntity entity = initEntity(params);
    HttpPost request = new HttpPost(url);
    request.addHeader(entity.getContentType());
    request.setEntity(entity);
    return execute(request);
  }
  
  protected HttpResponse executeJSONPostRequest(String url,
      String data) {
    HttpPost request = new HttpPost(url);
    StringEntity entity = null;
    try {
      entity = new StringEntity(data);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    request.addHeader("Content-type", "application/json");
    request.setEntity(entity);
    return execute(request);
  }
  
  protected HttpResponse executeJSONPutRequest(String url,
      String data) {
    HttpPut request = new HttpPut(url);
    StringEntity entity = null;
    try {
      entity = new StringEntity(data);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    request.addHeader("Content-type", "application/json");
    request.setEntity(entity);
    return execute(request);
  }
  
  protected HttpResponse executeDeleteRequest(String url) {
    HttpDelete request = new HttpDelete(url);
    return execute(request);
  }

  protected HttpEntity initEntity(ArrayList<NameValuePair> params) {
    try {
      return new UrlEncodedFormEntity(params, "UTF-8");
    } catch (final UnsupportedEncodingException e) {
      // this should never happen.
      throw new AssertionError(e);
    }
  }

  protected HttpResponse execute(HttpRequestBase request) {
    try {
      return httpClient.execute(request);
    } catch (final IOException e) {
      Log.e(TAG, "IOException when performing remote request", e);
      return null;
    }
  }
  
  protected InputStreamReader getResponseReader(HttpResponse response) {
    try {
      return new InputStreamReader(response.getEntity().getContent());
    } catch(IOException e) {
      Log.e(TAG, "TogglWebApi#getResponseReader couldn't read response content.", e);
      return null;
    }
  }

  protected ArrayList<NameValuePair> paramsWithApiToken() {
    ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair(API_TOKEN, apiToken));
    return params;
  }
  
  protected boolean ok(HttpResponse response) {
    return response != null
        && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
  }
  
  protected static void maybeCreateHttpClient() {
    if (httpClient == null) {
      httpClient = new DefaultHttpClient();
      final HttpParams params = httpClient.getParams();
  
      HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
      HttpConnectionParams.setSoTimeout(params, CONNECTION_TIMEOUT);
      ConnManagerParams.setTimeout(params, CONNECTION_TIMEOUT);      
    }
  }
  
}
