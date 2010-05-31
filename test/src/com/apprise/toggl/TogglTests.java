package com.apprise.toggl;

import junit.framework.Test;
import junit.framework.TestSuite;

import android.test.suitebuilder.TestSuiteBuilder;

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
    return new TestSuiteBuilder(TogglTests.class).includeAllPackagesUnderHere().build();
  }
}
