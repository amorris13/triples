package com.antsapps.triples;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

import android.content.Intent;
import android.view.View;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.backend.Application;
import com.github.takahirom.roborazzi.RoborazziOptions;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

@RunWith(RobolectricTestRunner.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = 36)
public class SnackbarScreenshotTest extends BaseRobolectricTest {

  @Before
  public void setUp() {
    RuntimeEnvironment.setQualifiers("w412dp-h915dp-notnight-420dpi");
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

    Application.sSeed = 12345L;
    HelpActivity.sRandom = new Random(12345L);
  }

  private void capture(String screenName) {
    onView(isRoot())
        .perform(
            new androidx.test.espresso.ViewAction() {
              @Override
              public org.hamcrest.Matcher<View> getConstraints() {
                return isRoot();
              }

              @Override
              public String getDescription() {
                return "capture screenshot";
              }

              @Override
              public void perform(androidx.test.espresso.UiController uiController, View view) {
                com.github.takahirom.roborazzi.RoborazziKt.captureRoboImage(
                    view,
                    "src/test/screenshots/" + screenName + ".png",
                    new RoborazziOptions());
              }
            });
  }

  @Test
  public void testIncorrectTriplesSnackbar() {
    Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ZenGameActivity.class);
    try (ActivityScenario<ZenGameActivity> scenario = ActivityScenario.launch(intent)) {
      scenario.onActivity(
          activity -> {
            // First incorrect triple (3 cards that don't form a triple)
            // Using indices 0, 1, 3 for simplicity, assuming they don't form a triple.
            // With seed 12345L, we can be reasonably sure.
            activity.mCardsView.getChildAt(0).performClick();
            activity.mCardsView.getChildAt(1).performClick();
            activity.mCardsView.getChildAt(3).performClick();

            // Second incorrect triple
            activity.mCardsView.getChildAt(0).performClick();
            activity.mCardsView.getChildAt(1).performClick();
            activity.mCardsView.getChildAt(3).performClick();
          });
      // Wait a bit for snackbar to appear if necessary, though Robolectric is usually sync
      capture("incorrect_triples_snackbar");
    }
  }
}
