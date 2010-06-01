package com.apprise.toggl;

import com.apprise.toggl.remote.SyncServiceTest;
import com.apprise.toggl.storage.DatabaseAdapterTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * A test suite containing all tests for runner.
 *
 * To run all suites found in this apk:
 * $ adb shell am instrument -w com.apprise.toggl.tests/android.test.InstrumentationTestRunner
 *  
 *
 * To run just this suite from the command line:
 * $ adb shell am instrument -w \
 *   -e class com.apprise.toggl.TogglTests \
 *   com.apprise.toggl.tests/android.test.InstrumentationTestRunner
 *
 * To run an individual test case, e.g. {@link com.example.android.apis.os.MorseCodeConverterTest}:
 * $ adb shell am instrument -w \
 *   -e class com.example.android.apis.os.MorseCodeConverterTest \
 *   com.example.android.apis.tests/android.test.InstrumentationTestRunner
 *
 * To run an individual test, e.g. {@link com.example.android.apis.os.MorseCodeConverterTest#testCharacterS()}:
 * $ adb shell am instrument -w \
 *   -e class com.example.android.apis.os.MorseCodeConverterTest#testCharacterS \
 *   com.example.android.apis.tests/android.test.InstrumentationTestRunner
 */
public class TogglTests extends TestSuite {

  public static final String TEST_DATABASE_NAME = "toggl_test.db";
  
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(DatabaseAdapterTest.class);
    suite.addTestSuite(SyncServiceTest.class);
    suite.addTestSuite(AccountActivityTests.class);
    return suite;
  }
}
