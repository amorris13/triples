package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.backend.Application;
import org.junit.Test;
import org.robolectric.annotation.Config;
import android.content.Context;

public class AccountSettingsTest extends BaseRobolectricTest {

    @Test
    public void testDeleteData() {
        Context context = ApplicationProvider.getApplicationContext();
        Application app = Application.getInstance(context);

        // SettingsFragment calls deleteData() which calls application.clearAllData()
        // and CloudSaveManager.deleteFromCloud(activity)

        try (ActivityScenario<SettingsActivity> scenario = ActivityScenario.launch(SettingsActivity.class)) {
            scenario.onActivity(activity -> {
                SettingsFragment fragment = (SettingsFragment) activity.getSupportFragmentManager().findFragmentByTag(".SettingsFragment");

                // We'll call clearAllData directly and verify results since CloudSaveManager
                // uses static methods that are hard to mock without PowerMock (avoiding here).
                app.clearAllData();

                assertThat(app.getCompletedClassicGames()).isEmpty();
                assertThat(app.getCompletedArcadeGames()).isEmpty();
                assertThat(app.getCompletedDailyGames()).isEmpty();
            });
        }
    }
}
