package com.apprise.toggl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.res.Resources;

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
    } catch (IOException e) {
      // ignore
    }
    return out.length() > 0 ? out.toString() : null;
  }

  public static Date parseStringToDate(String dateString) {
    SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    Date date = null;
    try {
      date = iso8601Format.parse(dateString);
    } catch (java.text.ParseException e) {
      e.printStackTrace();
    }
    return date;
  }
  
  public static Calendar parseStringToCalendar(String string) {
    Date date = parseStringToDate(string);
    Calendar cal = (Calendar) Calendar.getInstance().clone();
    cal.set(Calendar.YEAR, date.getYear() + 1900);
    cal.set(Calendar.MONTH, date.getMonth());
    cal.set(Calendar.DATE, date.getDate());
    
    return cal;
  }

  public static String formatDateToString(Date date) {
    SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    String s = null;
    String dateString = null;
    s = iso8601Format.format(date);
    //FIXME: hax to get the timezone from "+0300" to "+03:00"
    dateString = s.substring(0, s.length() - 2) + ":" + s.substring(s.length() - 2, s.length());
    return dateString;
  }
  
  public static String smallDateString(Date date) {
    SimpleDateFormat smallFormat = new SimpleDateFormat("dd. MMM, EEE");    
    return smallFormat.format(date);
  }
  
  public static Date currentDate() {
    return Calendar.getInstance().getTime();
  } 
  
  public static String secondsToHM(long time){
    int minutes = getMinutesFromSeconds(time);
    int hours = getHoursFromSeconds(time);
    String minutesStr = (minutes<10 ? "0" : "")+ minutes;
    String hoursStr = (hours<10 ? "0" : "")+ hours;
    return new String(hoursStr + ":" + minutesStr);
  }  
  
  public static String secondsToHMS(long time){
    time = convertIfRunningTime(time);
    
    int seconds = (int)(time % 60);    
    int minutes = getMinutesFromSeconds(time);
    int hours = getHoursFromSeconds(time);
    String secondsStr = (seconds<10 ? "0" : "")+ seconds;    
    String minutesStr = (minutes<10 ? "0" : "")+ minutes;
    String hoursStr = (hours<10 ? "0" : "")+ hours;
    return new String(hoursStr + ":" + minutesStr + ":" + secondsStr);
  }
  
  public static int getMinutesFromSeconds(long time) {
    time = convertIfRunningTime(time);
    return (int) ((time/60) % 60);
  }

  public static int getHoursFromSeconds(long time) {
    time = convertIfRunningTime(time);    
    return (int) ((time/3600) % 24);
  }

  /*
   *  neg. value provided means "running time's start" in seconds since epoch * -1
   *  this function gives back seconds since "running time's start"
   */
  public static long convertIfRunningTime(long time) {
    if (time < 0) {
      long currentTimeSeconds = System.currentTimeMillis() / 1000;
      time = currentTimeSeconds + time;
    }
    return time;
  }  
  
  /*
   * Calculates the start of "running time" in seconds since epoch.
   * Makes it a neg. value to distinguish its meaning.
   */
  public static long getRunningTimeStart(long time) {
    long currentTimeSeconds = System.currentTimeMillis() / 1000;
    long trackingStartSinceEpoch = currentTimeSeconds - time;
    return -(trackingStartSinceEpoch);
  }  
  
  public static String joinStringArray(String[] array, String separator){
    if (array != null) {
      Arrays.sort(array);
      
      List<String> list = Arrays.asList(array);
      JSONArray jsonArray = new JSONArray(list);
      String string = null;    
      try {
        string = jsonArray.join(separator);
        string = string.replaceAll("\"", "");
      } catch (JSONException e) {
        e.printStackTrace();
      }
      return string;
    } else {
      return null;
    }
  }
  
  public static String hoursMinutesSummary(int hours, int minutes, Resources r) {
    int hoursResource = hours == 1 ? R.string.hour : R.string.hours;
    int minutesResource = minutes == 1 ? R.string.minute : R.string.minutes;
    StringBuilder s = new StringBuilder();
    s.append(hours).
      append(" ").
      append(r.getString(hoursResource)).
      append(", ").
      append(minutes).
      append(" ").
      append(r.getString(minutesResource));

    return s.toString();
  }

}
