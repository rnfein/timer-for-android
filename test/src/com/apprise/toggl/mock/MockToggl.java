package com.apprise.toggl.mock;

import com.apprise.toggl.Toggl;

public class MockToggl extends Toggl {

  private String apiTokenStorage;
  
  public void storeAPIToken(String apiToken) {
    apiTokenStorage = apiToken;
  }

  public String getAPIToken() {
    return apiTokenStorage;
  }
  
  public void initSyncSchedule() {
    
  }

}
