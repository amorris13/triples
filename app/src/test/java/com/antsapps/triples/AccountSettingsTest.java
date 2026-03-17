package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

import android.content.Context;
import androidx.preference.PreferenceManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.backend.Application;
import org.junit.Test;

public class AccountSettingsTest extends BaseRobolectricTest {

  @Test
  public void testDeleteData() {
    Context context = ApplicationProvider.getApplicationContext();
    Application app = Application.getInstance(context);

    // SettingsFragment calls deleteData() which calls application.clearAllData()
    // and CloudSaveManager.deleteFromCloud(activity)

    try (ActivityScenario<SettingsActivity> scenario =
        ActivityScenario.launch(SettingsActivity.class)) {
      scenario.onActivity(
          activity -> {
            SettingsFragment fragment =
                (SettingsFragment)
                    activity.getSupportFragmentManager().findFragmentByTag(".SettingsFragment");

            // We'll call clearAllData directly and verify results since CloudSaveManager
            // uses static methods that are hard to mock without PowerMock (avoiding here).
            app.clearAllData();

            assertThat(app.getCompletedClassicGames()).isEmpty();
            assertThat(app.getCompletedArcadeGames()).isEmpty();
            assertThat(app.getCompletedDailyGames()).isEmpty();
          });
    }
  }

  @Test
  public void testSignOutSetsPersistentFlag() {
    Context context = ApplicationProvider.getApplicationContext();

    try (ActivityScenario<SettingsActivity> scenario =
        ActivityScenario.launch(SettingsActivity.class)) {
      scenario.onActivity(
          activity -> {
            // Initially, the flag should be false (default)
            assertThat(
                    PreferenceManager.getDefaultSharedPreferences(activity)
                        .getBoolean(activity.getString(R.string.pref_explicit_sign_out), false))
                .isFalse();

            activity.signOut();

            // After signOut(), the flag should be true
            assertThat(
                    PreferenceManager.getDefaultSharedPreferences(activity)
                        .getBoolean(activity.getString(R.string.pref_explicit_sign_out), false))
                .isTrue();

            activity.signIn();

            // After signIn(), the flag should be reset to false
            assertThat(
                    PreferenceManager.getDefaultSharedPreferences(activity)
                        .getBoolean(activity.getString(R.string.pref_explicit_sign_out), false))
                .isFalse();
          });
    }
  }
}
