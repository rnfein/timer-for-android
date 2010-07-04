package com.apprise.toggl.googleauth;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class GoogleAuth {
  
  private static final String TAG = "GoogleAuth";
  public static final String AUTH_COMPLETED = "com.apprise.toggl.remote.GoogleAuth.AUTH_COMPLETED";  
  public static final String RESULT = "com.apprise.toggl.remote.GoogleAuth.RESULT";  
  public static final String SUCCESSFUL = "com.apprise.toggl.remote.GoogleAuth.SUCCESSFUL";  
  public static final String FAILED = "com.apprise.toggl.remote.GoogleAuth.FAILED";  
  private static final String GOOGLE_AUTH_URL = "https://www.google.com/accounts/ServiceLoginAuth?auth=";
  
  DefaultHttpClient httpClient;

  private AccountManager accountManager;
  private Context context;

  public GoogleAuth(Context context) {
    this.context = context;
  }

  public boolean getGoogleSessionFor(DefaultHttpClient httpClient) {
    this.httpClient = httpClient;
    Account account = getAccountFromDevice(context);
    Log.d(TAG, "Google account from device: " + account);
    accountManager.getAuthToken(account, "ah", false, new GetAuthTokenCallback(), null);
    return true;
  }

  private Account getAccountFromDevice(Context context) {
    accountManager = AccountManager.get(context);
    Account[] accounts = accountManager.getAccountsByType("com.google");
    // TODO: should we consider choosing from multiple accounts?
    return accounts[0];
  }

  protected void onGetAuthToken(Bundle bundle) {
    String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
    Log.d(TAG, "authToken: " + authToken);
    new GetCookieTask().execute(authToken);
  }

  private class GetAuthTokenCallback implements AccountManagerCallback<Bundle> {
    public void run(AccountManagerFuture<Bundle> result) {
      Bundle bundle;
      try {
        bundle = result.getResult();
        Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);
        if (intent != null) {
          // User input required
          context.startActivity(intent);
        } else {
          onGetAuthToken(bundle);
        }
      } catch (OperationCanceledException e) {
        Log.e(TAG, "OperationCanceledException when getting AuthToken", e);
      } catch (AuthenticatorException e) {
        Log.e(TAG, "AuthenticationException when getting AuthToken", e);
      } catch (IOException e) {
        Log.e(TAG, "IOException when getting AuthToken", e);
      }
    }
  };

  private class GetCookieTask extends AsyncTask<String, Void, Boolean> {
    protected Boolean doInBackground(String... tokens) {
      try {
        // Don't follow redirects
        httpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        
        HttpGet httpGet = new HttpGet(GOOGLE_AUTH_URL + tokens[0]);
        httpClient.execute(httpGet);
        
        Intent intent = new Intent(AUTH_COMPLETED);
        for (Cookie cookie : httpClient.getCookieStore().getCookies()) {
          if (cookie.getName().equals("GALX")) {
            intent.putExtra(RESULT, SUCCESSFUL);
            context.sendBroadcast(intent);
            return true;
          }
        }
        
        intent.putExtra(RESULT, FAILED);
        context.sendBroadcast(intent);
        
      } catch (ClientProtocolException e) {
        Log.e(TAG, "ClientProtocolException when getting Google Cookie", e);
      } catch (IOException e) {
        Log.e(TAG, "IOException when getting Google Cookie", e);
      } finally {
        httpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
      }
      return false;
    }

  }

}
