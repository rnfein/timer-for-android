package com.apprise.toggl;

import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class SignUpActivity extends ApplicationActivity {
  
  private static final String SIGNUP_URL = "https://www.toggl.com/signup";

  private WebView webView;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_PROGRESS);
    webView = new WebView(this);
    setContentView(webView);
    
    initWebClient();
  }
  
  private void initWebClient() {
    webView.getSettings().setJavaScriptEnabled(true);
    
    webView.setWebChromeClient(new WebChromeClient() {
      public void onProgressChanged(WebView view, int progress) {
        // progress measures 0 - 10000
        SignUpActivity.this.setProgress(progress * 1000);
      }
    });
    webView.loadUrl(SIGNUP_URL);
  }

}
