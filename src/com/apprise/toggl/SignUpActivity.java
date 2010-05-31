package com.apprise.toggl;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class SignUpActivity extends ApplicationActivity {
  
  private static final String SIGNUP_URL = "https://www.toggl.com/signup";
  private static final int DEFAULT_CATEGORY = 0;
  private static final int BACK_TO_ACCOUNT_OPTION = Menu.FIRST;

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
    menu.add(DEFAULT_CATEGORY, BACK_TO_ACCOUNT_OPTION, Menu.NONE, R.string.account).setIcon(android.R.drawable.ic_menu_preferences);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    switch (item.getItemId()) {
      case BACK_TO_ACCOUNT_OPTION:
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
