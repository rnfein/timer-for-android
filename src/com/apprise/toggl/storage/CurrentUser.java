package com.apprise.toggl.storage;

import com.apprise.toggl.storage.models.User;

public class CurrentUser {

  private static User currentUserInstance = new User(); 
  
  protected CurrentUser() {
  }
  
  public static User getInstance() {
    return currentUserInstance;
  }
  
  public static boolean isLoggedIn() {
    return (currentUserInstance.api_token != null);
  }

  public static void logIn(User user) {
    currentUserInstance = user;
  }
  
  public static void logOut() {
    currentUserInstance = new User();
  }
  
}
