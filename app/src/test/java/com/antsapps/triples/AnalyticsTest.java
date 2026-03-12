package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.os.Bundle;
import android.widget.Button;
import androidx.test.core.app.ActivityScenario;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.ZenGame;
import com.google.firebase.analytics.FirebaseAnalytics;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class AnalyticsTest extends BaseRobolectricTest {

  @Test
  public void testNewClassicGameAnalytics() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      scenario.onActivity(
          activity -> {
            FirebaseAnalytics mockAnalytics = mock(FirebaseAnalytics.class);
            activity.mFirebaseAnalytics = mockAnalytics;

            Button newGameButton = activity.findViewById(R.id.classic_new_game_button);
            newGameButton.performClick();

            ArgumentCaptor<Bundle> bundleCaptor = ArgumentCaptor.forClass(Bundle.class);
            verify(mockAnalytics)
                .logEvent(eq(AnalyticsConstants.Event.NEW_GAME), bundleCaptor.capture());
            assertThat(bundleCaptor.getValue().getString(AnalyticsConstants.Param.GAME_TYPE))
                .isEqualTo(ClassicGame.GAME_TYPE_FOR_ANALYTICS);
          });
    }
  }

  @Test
  public void testNewArcadeGameAnalytics() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      scenario.onActivity(
          activity -> {
            FirebaseAnalytics mockAnalytics = mock(FirebaseAnalytics.class);
            activity.mFirebaseAnalytics = mockAnalytics;

            Button newGameButton = activity.findViewById(R.id.arcade_new_game_button);
            newGameButton.performClick();

            ArgumentCaptor<Bundle> bundleCaptor = ArgumentCaptor.forClass(Bundle.class);
            verify(mockAnalytics)
                .logEvent(eq(AnalyticsConstants.Event.NEW_GAME), bundleCaptor.capture());
            assertThat(bundleCaptor.getValue().getString(AnalyticsConstants.Param.GAME_TYPE))
                .isEqualTo(ArcadeGame.GAME_TYPE_FOR_ANALYTICS);
          });
    }
  }

  @Test
  public void testZenGameAnalytics() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      scenario.onActivity(
          activity -> {
            FirebaseAnalytics mockAnalytics = mock(FirebaseAnalytics.class);
            activity.mFirebaseAnalytics = mockAnalytics;

            Button zenButton = activity.findViewById(R.id.zen_button);
            zenButton.performClick();

            ArgumentCaptor<Bundle> bundleCaptor = ArgumentCaptor.forClass(Bundle.class);
            verify(mockAnalytics)
                .logEvent(eq(AnalyticsConstants.Event.NEW_GAME), bundleCaptor.capture());
            assertThat(bundleCaptor.getValue().getString(AnalyticsConstants.Param.GAME_TYPE))
                .isEqualTo(ZenGame.GAME_TYPE_FOR_ANALYTICS);
          });
    }
  }

  @Test
  public void testDailyGameAnalytics() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      scenario.onActivity(
          activity -> {
            FirebaseAnalytics mockAnalytics = mock(FirebaseAnalytics.class);
            activity.mFirebaseAnalytics = mockAnalytics;

            Button dailyButton = activity.findViewById(R.id.daily_play_button);
            dailyButton.performClick();

            ArgumentCaptor<Bundle> bundleCaptor = ArgumentCaptor.forClass(Bundle.class);
            verify(mockAnalytics)
                .logEvent(eq(AnalyticsConstants.Event.RESUME_GAME), bundleCaptor.capture());
            assertThat(bundleCaptor.getValue().getString(AnalyticsConstants.Param.GAME_TYPE))
                .isEqualTo(DailyGame.GAME_TYPE_FOR_ANALYTICS);
          });
    }
  }
}
