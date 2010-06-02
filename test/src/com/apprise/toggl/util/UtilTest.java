package com.apprise.toggl;

import java.util.Calendar;
import java.util.Date;

import android.test.AndroidTestCase;
import android.util.Log;
import android.webkit.DateSorter;

import com.apprise.toggl.Util;


public class UtilTest extends AndroidTestCase {
  
  public void testParseStringToDate() {
    String dateString = "2010-05-26T06:06:18+03:00";

    Date date = Util.parseStringToDate(dateString);

    
    Log.d("UtilTest", "************");
    Log.d("UtilTest", "parsedStringDate" + date);
    Log.d("UtilTest", "************");    
    
    assertNotNull(date);
    assertEquals(2010, 1900 + date.getYear());
    assertEquals(05, date.getMonth() + 1);
    assertEquals(26, date.getDate());
    assertEquals(06, date.getHours());
    assertEquals(06, date.getMinutes());
    assertEquals(18, date.getSeconds());
  }  
  
  public void testParseDateToString() {
    Date date = new Date();
    date.setYear(110);
    date.setMonth(04);
    date.setDate(20);
    date.setHours(10);
    date.setMinutes(25);
    date.setSeconds(30);
    
    String expectedDateString = "2010-05-20T10:25:30+03:00"; // +xx:xx depends on timezone
    
    String parsedDateString = Util.parseDateToString(date); 
    
    Log.d("UtilTest", "************");
    Log.d("UtilTest", "expectedDateString" + expectedDateString);
    Log.d("UtilTest", "parsedDateString" + parsedDateString);
    Log.d("UtilTest", "************");
    
    assertNotNull(parsedDateString);
    assertEquals(expectedDateString, parsedDateString);
  }  
}


