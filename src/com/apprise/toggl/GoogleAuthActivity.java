package com.apprise.toggl;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class GoogleAuthActivity extends Activity {
  
  private static final String AUTH_URL = "https://www.toggl.com/user/google_login";
  public static final String TOGGL_SESSION_ID = "com.apprise.toggl.googleauth.TOGGL_SESSION_ID";
  
  private WebView webView;
  private CookieManager cookieManager;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_PROGRESS);
    webView = new WebView(this);
    setContentView(webView);
    
    cookieManager = CookieManager.getInstance();     
    
    initWebClient();
  }
  
  private void initWebClient() {
    webView.getSettings().setJavaScriptEnabled(true);
    
    webView.setWebChromeClient(new WebChromeClient() {
      public void onProgressChanged(WebView view, int progress) {
        // progress measures don't match web client progress measures
        GoogleAuthActivity.this.setProgress(progress * 100);
      }
    });
    webView.setWebViewClient(new GoogleAuthWebViewClient());
    
    webView.loadUrl(AUTH_URL);
  }
  
  private class GoogleAuthWebViewClient extends WebViewClient {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      view.loadUrl(url);  
      return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      String cookieString = cookieManager.getCookie(url);
      
      if (cookieString != null && cookieString.indexOf("_toggl_session") > -1) {
        String s = cookieString.substring(cookieString.indexOf("_toggl_session") + 15);
        int end = s.length(); 
        if (s.indexOf(";") > -1) {
          end = s.indexOf(";");
        }
        String togglSession = s.substring(0, end);

        Intent intent = getIntent();
        intent.putExtra(TOGGL_SESSION_ID, togglSession);
        setResult(RESULT_OK, intent);
        view.stopLoading();
        finish();
      }
      super.onPageStarted(view, url, favicon);
    }
    
  }
}
