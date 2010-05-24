package com.apprise.toggl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class Util {
  
  public static String inputStreamToString(InputStream is) {
    StringBuilder out = new StringBuilder();
    try {
      final char[] buffer = new char[0x10000];
      Reader in = new InputStreamReader(is, "UTF-8");
      int read;
      do {
        read = in.read(buffer, 0, buffer.length);
        if (read > 0) {
          out.append(buffer, 0, read);
        }
      } while (read >= 0);
    } catch(IOException e) {
      // ignore
    }
    return out.length() > 0 ? out.toString() : null;
  }

}
