package com.apprise.toggl.mock;

import com.apprise.toggl.storage.models.Model;

public class MockModel extends Model {

  public boolean identicalTo(Model other) {
    return false;
  }

  public void updateAttributes(Model other) {

  }

}
