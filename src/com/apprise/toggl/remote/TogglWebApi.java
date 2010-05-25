package com.apprise.toggl.remote;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.apprise.toggl.Util;
import com.apprise.toggl.storage.User;
import com.google.gson.Gson;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class TogglWebApi {

  private DefaultHttpClient httpClient;
  private Handler handler;
  
  public static final int HANDLER_AUTH_PASSED = 1;

  private static final int CONNECTION_TIMEOUT = 30 * 1000; // ms

  private static final String TAG = "TogglWebApi";
  private static final String BASE_URL = "http://www.toggl.com";
  private static final String API_URL = BASE_URL + "/api/v1";
  private static final String SESSIONS_URL = API_URL + "/sessions.json";
  private static final String EMAIL = "email";
  private static final String PASSWORD = "password";
  private static final String API_TOKEN = "api_token";

  public TogglWebApi() {
    init();
  }
  
  public TogglWebApi(Handler handler) {
    this.handler = handler;
    init();
  }

  protected void init() {
    createHttpClient();
  }

  public void AuthenticateWithCredentials(final String email, final String password) {
    runInBackground(new Runnable() {
      
      public void run() {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(EMAIL, email));
        params.add(new BasicNameValuePair(PASSWORD, password));

        userAuthenticationRequest(params);
      }
    });
  }

  public void AuthenticateWithToken(final String apiToken) {
    runInBackground(new Runnable() {
      
      public void run() {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(API_TOKEN, apiToken));

        userAuthenticationRequest(params);
      }
    });
  }
  
  private void userAuthenticationRequest(ArrayList<NameValuePair> params) {
    HttpResponse response = executePostRequest(SESSIONS_URL, params);
    String responseContent = null;

    if (ok(response)) {
      Message msg = Message.obtain();
      msg.what = HANDLER_AUTH_PASSED;          
      try {            
        responseContent = Util.inputStreamToString(response.getEntity().getContent());
        Gson gson = new Gson();
        User user = gson.fromJson(responseContent, User.class);            
        Log.d(TAG,
            "TogglWebApi#AuthenticationRequest got a successful response body: "
                + responseContent);
        msg.obj = user;
        handler.sendMessage(msg);
      } catch (IOException e) {
        // TODO: Error handling
      }
    } else {
      Log.e(TAG,
          "TogglWebApi#AuthenticateWithToken got a failed request: "
              + response.getStatusLine().getStatusCode());
    }
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
  
  protected void runInBackground(Runnable runnable) {
    new Thread(runnable).start();
  }  

  protected boolean ok(HttpResponse response) {
    return response != null
        && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
  }
}
