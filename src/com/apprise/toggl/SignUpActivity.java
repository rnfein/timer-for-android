package com.apprise.toggl;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.signup_menu, menu );
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    switch (item.getItemId()) {
      case R.id.signup_menu_return:
        finish();
        return true;
    }

    return super.onMenuItemSelected(featureId, item);
  } 
  
  private void initWebClient() {
    webView.getSettings().setJavaScriptEnabled(true);
    
    webView.setWebChromeClient(new WebChromeClient() {
      public void onProgressChanged(WebView view, int progress) {
        // progress measures don't match web client progress measures
        SignUpActivity.this.setProgress(progress * 100);
      }
    });
    
    webView.loadUrl(SIGNUP_URL);
  }

}
