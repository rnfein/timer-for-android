import com.apprise.toggl.storage.DatabaseAdapter;

import android.test.AndroidTestCase;


public class DatabaseAdapterTest extends AndroidTestCase {
  
  private final String TEST_DATABASE_NAME = "toggl_test.db";
  private DatabaseAdapter dbAdapter;  

  @Override
  protected void setUp() throws Exception {
    dbAdapter = new DatabaseAdapter(getContext());
    dbAdapter.setDatabaseName(TEST_DATABASE_NAME);
    dbAdapter.open();
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    dbAdapter.close();
    getContext().deleteDatabase(TEST_DATABASE_NAME);
    super.tearDown();
  }
  
  
}
