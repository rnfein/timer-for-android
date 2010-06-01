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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

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
  private static final String WORKSPACES_URL = API_URL + "/workspaces.json";
  private static final String PROJECTS_URL = API_URL + "/projects.json";
  private static final String PLANNED_TASKS_URL = API_URL + "/planned_tasks.json";
  private static final String EMAIL = "email";
  private static final String PASSWORD = "password";
  private static final String API_TOKEN = "api_token";

  private DefaultHttpClient httpClient;
  public String apiToken;
  
  public TogglWebApi(String apiToken) {
    this.apiToken = apiToken;
    createHttpClient();
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
      User user = gson.fromJson(getResponseReader(response), User.class);            
      Log.d(TAG, "TogglWebApi#AuthenticationRequest got a successful response for user: " + user.toString());
      this.apiToken = user.api_token;
      return user;
    } else {
      int statusCode = response.getStatusLine().getStatusCode();
      Log.e(TAG, "TogglWebApi#AuthenticateWithToken got a failed request: " + statusCode);
    }
    return null;
  }
  
  public LinkedList<Workspace> fetchWorkspaces() {
    Type collectionType = new TypeToken<LinkedList<Workspace>>() {}.getType();
    return (LinkedList<Workspace>) makeApiGetRequest(collectionType, WORKSPACES_URL);    
  }
  
  public LinkedList<Project> fetchProjects() {
    Type collectionType = new TypeToken<LinkedList<Project>>() {}.getType();
    return (LinkedList<Project>) makeApiGetRequest(collectionType, PLANNED_TASKS_URL);
  }
  
  public LinkedList<PlannedTask> fetchPlannedTasks() {
    Type collectionType = new TypeToken<LinkedList<PlannedTask>>() {}.getType();
    return (LinkedList<PlannedTask>) makeApiGetRequest(collectionType, PROJECTS_URL);
  }

  public LinkedList<Task> fetchTasks() {
    Type collectionType = new TypeToken<LinkedList<Task>>() {}.getType();
    return (LinkedList<Task>) makeApiGetRequest(collectionType, TASKS_URL);    
  }
  
  private LinkedList<? extends Model> makeApiGetRequest(Type collectionType, String url) {
    if (getSession()) {
      HttpResponse response = executeGetRequest(url);
      
      if (ok(response)) {
        Gson gson = new Gson();
        Log.d(TAG, "TogglWebApi#fetchPlannedTasks got a successful response");
        return gson.fromJson(getResponseReader(response), collectionType);
      } else {
        Log.e(TAG, "TogglWebApi#fetchProjects got a failed request: "
            + response.getStatusLine().getStatusCode());
        return null;
      }
    } else {
      return null;
    }
  }
  
  /*
   * Authenticate if session cookies are missing
   * */
  private boolean getSession() {
    for (Cookie cookie : httpClient.getCookieStore().getCookies()) {
      if (cookie.getName().equals("_toggl_session"))
        return true;
    }
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
      Log.d(TAG, "IOException when performing remote request", e);
      return null;
    }
  }
  
  protected void createHttpClient() {
    httpClient = new DefaultHttpClient();
    final HttpParams params = httpClient.getParams();

    HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
    HttpConnectionParams.setSoTimeout(params, CONNECTION_TIMEOUT);
    ConnManagerParams.setTimeout(params, CONNECTION_TIMEOUT);
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
}
