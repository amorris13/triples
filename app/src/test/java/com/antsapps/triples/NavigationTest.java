package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.widget.ListView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.robolectric.shadows.ShadowActivity;

public class NavigationTest extends BaseRobolectricTest {

    @Test
    public void testNavigateToArcade() {
        try (ActivityScenario<ClassicGameListActivity> scenario = ActivityScenario.launch(ClassicGameListActivity.class)) {
            scenario.onActivity(activity -> {
                ListView drawerList = activity.findViewById(R.id.mode_list);
                assertThat(drawerList).isNotNull();

                // Click on Arcade (position 1)
                shadowOf(drawerList).performItemClick(1);

                ShadowActivity shadowActivity = shadowOf(activity);
                Intent nextIntent = shadowActivity.getNextStartedActivity();
                assertThat(nextIntent.getComponent().getClassName()).isEqualTo(ArcadeGameListActivity.class.getName());
            });
        }
    }
}
