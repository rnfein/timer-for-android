package com.apprise.toggl.widget;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.apprise.toggl.R;

public class DialogCursorAdapter extends CursorAdapter {

  private Block block;
  private String column;
  
  public DialogCursorAdapter(Context context, Cursor cursor, String column, Block block) {
    super(context, cursor);
    this.column = column;
    this.block = block;
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    TextView nameView = (TextView) view.findViewById(R.id.item_name);
    String name = cursor.getString(cursor.getColumnIndex(column));
    nameView.setText(name);

    long projectId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
    
    if (block.isCurrent(projectId)) {
      nameView.setBackgroundResource(R.drawable.arrow);
    } else {
      nameView.setBackgroundResource(0);
    }
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    Activity activity = (Activity) context;
    View view = activity.getLayoutInflater().inflate(R.layout.dialog_list_item, null);
    bindView(view, context, cursor);
    return view;
  }
  
  public static interface Block {
    boolean isCurrent(long entryId);
  }
  
}