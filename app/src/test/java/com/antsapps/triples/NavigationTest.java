package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.widget.Button;
import androidx.test.core.app.ActivityScenario;
import org.junit.Test;
import org.robolectric.shadows.ShadowActivity;

public class NavigationTest extends BaseRobolectricTest {

  @Test
  public void testNavigateToClassicStatistics() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      scenario.onActivity(
          activity -> {
            Button statsButton = activity.findViewById(R.id.classic_statistics_button);
            assertThat(statsButton).isNotNull();

            statsButton.performClick();

            ShadowActivity shadowActivity = shadowOf(activity);
            Intent nextIntent = shadowActivity.getNextStartedActivity();
            assertThat(nextIntent.getComponent().getClassName())
                .isEqualTo(StatisticsActivity.class.getName());
            assertThat(nextIntent.getStringExtra(StatisticsActivity.GAME_TYPE))
                .isEqualTo("Classic");
          });
    }
  }

  @Test
  public void testNavigateToArcadeStatistics() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      scenario.onActivity(
          activity -> {
            Button statsButton = activity.findViewById(R.id.arcade_statistics_button);
            assertThat(statsButton).isNotNull();

            statsButton.performClick();

            ShadowActivity shadowActivity = shadowOf(activity);
            Intent nextIntent = shadowActivity.getNextStartedActivity();
            assertThat(nextIntent.getComponent().getClassName())
                .isEqualTo(StatisticsActivity.class.getName());
            assertThat(nextIntent.getStringExtra(StatisticsActivity.GAME_TYPE)).isEqualTo("Arcade");
          });
    }
  }

  @Test
  public void testNavigateToNewClassicGame() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      scenario.onActivity(
          activity -> {
            Button newGameButton = activity.findViewById(R.id.classic_new_game_button);
            assertThat(newGameButton).isNotNull();

            newGameButton.performClick();

            ShadowActivity shadowActivity = shadowOf(activity);
            Intent nextIntent = shadowActivity.getNextStartedActivity();
            assertThat(nextIntent.getComponent().getClassName())
                .isEqualTo(ClassicGameActivity.class.getName());
          });
    }
  }

  @Test
  public void testNavigateToZenGame() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      scenario.onActivity(
          activity -> {
            Button zenButton = activity.findViewById(R.id.zen_button);
            assertThat(zenButton).isNotNull();

            zenButton.performClick();

            ShadowActivity shadowActivity = shadowOf(activity);
            Intent nextIntent = shadowActivity.getNextStartedActivity();
            assertThat(nextIntent.getComponent().getClassName())
                .isEqualTo(ZenGameActivity.class.getName());
            assertThat(nextIntent.getBooleanExtra(ZenGameActivity.IS_BEGINNER, true)).isFalse();
          });
    }
  }

  @Test
  public void testNavigateToBeginnerTutorial() {
    try (ActivityScenario<HelpActivity> scenario = ActivityScenario.launch(HelpActivity.class)) {
      scenario.onActivity(
          activity -> {
            Button beginnerButton = activity.findViewById(R.id.beginner_tutorial_button);
            assertThat(beginnerButton).isNotNull();

            beginnerButton.performClick();

            ShadowActivity shadowActivity = shadowOf(activity);
            Intent nextIntent = shadowActivity.getNextStartedActivity();
            assertThat(nextIntent.getComponent().getClassName())
                .isEqualTo(ZenGameActivity.class.getName());
            assertThat(nextIntent.getBooleanExtra(ZenGameActivity.IS_BEGINNER, false)).isTrue();
          });
    }
  }

  @Test
  public void testNavigateToDailyGame() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      scenario.onActivity(
          activity -> {
            Button dailyButton = activity.findViewById(R.id.daily_play_button);
            assertThat(dailyButton).isNotNull();

            dailyButton.performClick();

            ShadowActivity shadowActivity = shadowOf(activity);
            Intent nextIntent = shadowActivity.getNextStartedActivity();
            assertThat(nextIntent.getComponent().getClassName())
                .isEqualTo(DailyGameActivity.class.getName());
            assertThat(nextIntent.hasExtra(com.antsapps.triples.backend.Game.ID_TAG)).isTrue();
          });
    }
  }
}
