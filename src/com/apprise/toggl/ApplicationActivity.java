package com.apprise.toggl;

import com.apprise.toggl.storage.models.Model;

import android.app.Activity;
import android.database.Cursor;
import android.provider.BaseColumns;

public class ApplicationActivity extends Activity {

  private Toggl app;

  /**
   * Helper method to get the index of the cursor item that matches:
   *   model._id == item._id
   *
   * Returns -1 if no match, or if model is null. 
   */
  protected int getCheckedItem(Cursor cursor, Model model) {
    int checked = -1;
    if (model != null) {
      while(cursor.moveToNext()) {
        long clientId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        if (model._id == clientId) {
          checked = cursor.getPosition();
          break;
        }
      }
    }
    return checked;
  }

}
