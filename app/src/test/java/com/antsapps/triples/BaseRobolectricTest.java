package com.antsapps.triples;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.SystemTimeProvider;
import com.google.firebase.FirebaseApp;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 36)
public abstract class BaseRobolectricTest {

  @Before
  public void setupBase() {
    MockitoAnnotations.openMocks(this);
    Context context = ApplicationProvider.getApplicationContext();
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context);
    }
    Application.setTimeProvider(new SystemTimeProvider());
    Application.getInstance(context).clearAllData();
  }
}
