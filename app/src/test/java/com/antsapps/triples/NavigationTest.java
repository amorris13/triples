package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.test.core.app.ActivityScenario;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.ZenGame;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.firebase.analytics.FirebaseAnalytics;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
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
            FirebaseAnalytics mockAnalytics = mock(FirebaseAnalytics.class);
            activity.mFirebaseAnalytics = mockAnalytics;

            Button newGameButton = activity.findViewById(R.id.classic_new_game_button);
            assertThat(newGameButton).isNotNull();

            newGameButton.performClick();

            ShadowActivity shadowActivity = shadowOf(activity);
            Intent nextIntent = shadowActivity.getNextStartedActivity();
            assertThat(nextIntent.getComponent().getClassName())
                .isEqualTo(ClassicGameActivity.class.getName());

            ArgumentCaptor<Bundle> bundleCaptor = ArgumentCaptor.forClass(Bundle.class);
            verify(mockAnalytics)
                .logEvent(eq(AnalyticsConstants.Event.NEW_GAME), bundleCaptor.capture());
            assertThat(bundleCaptor.getValue().getString(AnalyticsConstants.Param.GAME_TYPE))
                .isEqualTo(ClassicGame.GAME_TYPE_FOR_ANALYTICS);
          });
    }
  }

  @Test
  public void testNavigateToZenGame() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      scenario.onActivity(
          activity -> {
            FirebaseAnalytics mockAnalytics = mock(FirebaseAnalytics.class);
            activity.mFirebaseAnalytics = mockAnalytics;

            Button zenButton = activity.findViewById(R.id.zen_button);
            assertThat(zenButton).isNotNull();

            zenButton.performClick();

            ShadowActivity shadowActivity = shadowOf(activity);
            Intent nextIntent = shadowActivity.getNextStartedActivity();
            assertThat(nextIntent.getComponent().getClassName())
                .isEqualTo(ZenGameActivity.class.getName());
            assertThat(nextIntent.getBooleanExtra(ZenGameActivity.IS_BEGINNER, true)).isFalse();

            ArgumentCaptor<Bundle> bundleCaptor = ArgumentCaptor.forClass(Bundle.class);
            verify(mockAnalytics)
                .logEvent(eq(AnalyticsConstants.Event.NEW_GAME), bundleCaptor.capture());
            assertThat(bundleCaptor.getValue().getString(AnalyticsConstants.Param.GAME_TYPE))
                .isEqualTo(ZenGame.GAME_TYPE_FOR_ANALYTICS);
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
  public void testNavigateToNewArcadeGame() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      scenario.onActivity(
          activity -> {
            FirebaseAnalytics mockAnalytics = mock(FirebaseAnalytics.class);
            activity.mFirebaseAnalytics = mockAnalytics;

            Button newGameButton = activity.findViewById(R.id.arcade_new_game_button);
            assertThat(newGameButton).isNotNull();

            newGameButton.performClick();

            ShadowActivity shadowActivity = shadowOf(activity);
            Intent nextIntent = shadowActivity.getNextStartedActivity();
            assertThat(nextIntent.getComponent().getClassName())
                .isEqualTo(ArcadeGameActivity.class.getName());

            ArgumentCaptor<Bundle> bundleCaptor = ArgumentCaptor.forClass(Bundle.class);
            verify(mockAnalytics)
                .logEvent(eq(AnalyticsConstants.Event.NEW_GAME), bundleCaptor.capture());
            assertThat(bundleCaptor.getValue().getString(AnalyticsConstants.Param.GAME_TYPE))
                .isEqualTo(ArcadeGame.GAME_TYPE_FOR_ANALYTICS);
          });
    }
  }

  @Test
  public void testNavigateToResumeClassicGame() {
    Application app =
        Application.getInstance(androidx.test.core.app.ApplicationProvider.getApplicationContext());
    for (ClassicGame g : app.getCurrentClassicGames()) {
      app.deleteClassicGame(g);
    }
    ClassicGame game = ClassicGame.createFromSeed(123456789L);
    game.begin();
    game.commitTriple(
        game.getCardsInPlay().get(0),
        game.getCardsInPlay().get(1),
        game.getCardsInPlay().get(2)); // Make it resumable
    app.addClassicGame(game);
    app.saveClassicGame(game);

    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      scenario.onActivity(
          activity -> {
            FirebaseAnalytics mockAnalytics = mock(FirebaseAnalytics.class);
            activity.mFirebaseAnalytics = mockAnalytics;

            activity.runOnUiThread(() -> activity.onResume());
            org.robolectric.shadows.ShadowLooper.idleMainLooper();
            Button resumeButton = activity.findViewById(R.id.classic_resume_button);
            assertThat(resumeButton).isNotNull();
            // assertThat(resumeButton.getVisibility()).isEqualTo(android.view.View.VISIBLE);

            resumeButton.performClick();

            ShadowActivity shadowActivity = shadowOf(activity);
            Intent nextIntent = shadowActivity.getNextStartedActivity();
            assertThat(nextIntent.getComponent().getClassName())
                .isEqualTo(ClassicGameActivity.class.getName());
            assertThat(nextIntent.getLongExtra(Game.ID_TAG, -1)).isEqualTo(game.getId());

            ArgumentCaptor<Bundle> bundleCaptor = ArgumentCaptor.forClass(Bundle.class);
            verify(mockAnalytics)
                .logEvent(eq(AnalyticsConstants.Event.RESUME_GAME), bundleCaptor.capture());
            assertThat(bundleCaptor.getValue().getString(AnalyticsConstants.Param.GAME_TYPE))
                .isEqualTo(ClassicGame.GAME_TYPE_FOR_ANALYTICS);
          });
    }
  }

  @Test
  public void testNavigateToResumeArcadeGame() {
    Application app =
        Application.getInstance(androidx.test.core.app.ApplicationProvider.getApplicationContext());
    for (ArcadeGame g : app.getCurrentArcadeGames()) {
      app.deleteArcadeGame(g);
    }
    ArcadeGame game = ArcadeGame.createFromSeed(987654321L);
    game.begin();
    game.commitTriple(
        game.getCardsInPlay().get(0),
        game.getCardsInPlay().get(1),
        game.getCardsInPlay().get(2)); // Make it resumable
    app.addArcadeGame(game);
    app.saveArcadeGame(game);

    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      scenario.onActivity(
          activity -> {
            FirebaseAnalytics mockAnalytics = mock(FirebaseAnalytics.class);
            activity.mFirebaseAnalytics = mockAnalytics;

            activity.runOnUiThread(() -> activity.onResume());
            org.robolectric.shadows.ShadowLooper.idleMainLooper();
            Button resumeButton = activity.findViewById(R.id.arcade_resume_button);
            assertThat(resumeButton).isNotNull();
            // assertThat(resumeButton.getVisibility()).isEqualTo(android.view.View.VISIBLE);

            resumeButton.performClick();

            ShadowActivity shadowActivity = shadowOf(activity);
            Intent nextIntent = shadowActivity.getNextStartedActivity();
            assertThat(nextIntent.getComponent().getClassName())
                .isEqualTo(ArcadeGameActivity.class.getName());
            assertThat(nextIntent.getLongExtra(Game.ID_TAG, -1)).isEqualTo(game.getId());

            ArgumentCaptor<Bundle> bundleCaptor = ArgumentCaptor.forClass(Bundle.class);
            verify(mockAnalytics)
                .logEvent(eq(AnalyticsConstants.Event.RESUME_GAME), bundleCaptor.capture());
            assertThat(bundleCaptor.getValue().getString(AnalyticsConstants.Param.GAME_TYPE))
                .isEqualTo(ArcadeGame.GAME_TYPE_FOR_ANALYTICS);
          });
    }
  }

  @Test
  public void testMainActivityReactiveUpdates() {
    Application app =
        Application.getInstance(androidx.test.core.app.ApplicationProvider.getApplicationContext());

    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      scenario.onActivity(
          activity -> {
            Button resumeButton = activity.findViewById(R.id.classic_resume_button);
            assertThat(resumeButton.getVisibility()).isEqualTo(android.view.View.GONE);

            // Add a game in progress reactively
            ClassicGame game = ClassicGame.createFromSeed(12345L);
            game.setGameRenderer(
                new Game.GameRenderer() {
                  @Override
                  public void updateCardsInPlay(ImmutableList<Card> newCards) {}

                  @Override
                  public void addHint(Card card) {}

                  @Override
                  public void clearHintedCards() {}

                  @Override
                  public void clearSelectedCards() {}

                  @Override
                  public java.util.Set<Card> getSelectedCards() {
                    return Sets.newHashSet();
                  }
                });
            game.begin();
            java.util.Set<Card> triple = game.getAllValidTriples(game.getCardsInPlay()).get(0);
            game.commitTriple(triple.toArray(new Card[0]));
            app.addClassicGame(game);

            org.robolectric.shadows.ShadowLooper.idleMainLooper();

            // Button should now be visible without activity restart/onResume
            assertThat(resumeButton.getVisibility()).isEqualTo(android.view.View.VISIBLE);
            assertThat(resumeButton.getText().toString())
                .contains(String.valueOf(game.getCardsRemaining()));

            // Complete the game reactively
            while (game.getGameState() != Game.GameState.COMPLETED) {
              java.util.List<java.util.Set<Card>> triples =
                  game.getAllValidTriples(game.getCardsInPlay());
              if (triples.isEmpty()) {
                break;
              }
              game.commitTriple(triples.get(0).toArray(new Card[0]));
              app.saveClassicGame(game);
            }
            game.finish();
            app.saveClassicGame(game);

            org.robolectric.shadows.ShadowLooper.idleMainLooper();

            // Button should disappear
            assertThat(resumeButton.getVisibility()).isEqualTo(android.view.View.GONE);
          });
    }
  }

  @Test
  public void testDailyPuzzleReactiveUpdates() {
    Application app =
        Application.getInstance(androidx.test.core.app.ApplicationProvider.getApplicationContext());

    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      scenario.onActivity(
          activity -> {
            Button dailyPlayButton = activity.findViewById(R.id.daily_play_button);
            Button dailyStatsButton = activity.findViewById(R.id.daily_statistics_button);
            android.view.View dailyCompletedText = activity.findViewById(R.id.daily_completed_text);

            assertThat(dailyPlayButton.getVisibility()).isEqualTo(android.view.View.VISIBLE);
            assertThat(dailyPlayButton.getText().toString())
                .isEqualTo(activity.getString(R.string.play));

            // Start a daily game and find a triple
            DailyGame game = app.getDailyGameForDate(DailyGame.Day.forToday());
            game.setGameRenderer(
                new Game.GameRenderer() {
                  @Override
                  public void updateCardsInPlay(ImmutableList<Card> newCards) {}

                  @Override
                  public void addHint(Card card) {}

                  @Override
                  public void clearHintedCards() {}

                  @Override
                  public void clearSelectedCards() {}

                  @Override
                  public java.util.Set<Card> getSelectedCards() {
                    return Sets.newHashSet();
                  }
                });
            java.util.Set<Card> triple = game.getAllValidTriples(game.getCardsInPlay()).get(0);
            game.commitTriple(triple.toArray(new Card[0]));
            app.saveDailyGame(game);

            org.robolectric.shadows.ShadowLooper.idleMainLooper();

            // Check progress text
            assertThat(dailyPlayButton.getText().toString())
                .isEqualTo(
                    activity.getString(
                        R.string.daily_play_progress_format, 1, game.getTotalTriplesCount()));

            // Complete the game
            while (game.getGameState() != Game.GameState.COMPLETED) {
              java.util.List<java.util.Set<Card>> triples =
                  game.getAllValidTriples(game.getCardsInPlay());
              java.util.Set<Card> unfoundTriple = null;
              for (java.util.Set<Card> t : triples) {
                if (!game.getFoundTriples().contains(t)) {
                  unfoundTriple = t;
                  break;
                }
              }
              if (unfoundTriple == null) break;
              game.commitTriple(unfoundTriple.toArray(new Card[0]));
              app.saveDailyGame(game);
            }
            org.robolectric.shadows.ShadowLooper.idleMainLooper();

            // Check completed state
            assertThat(dailyPlayButton.getVisibility()).isEqualTo(android.view.View.GONE);
            assertThat(dailyCompletedText.getVisibility()).isEqualTo(android.view.View.VISIBLE);

            // Check streak in statistics button
            // Since we just completed today's game, current streak should be 1
            assertThat(dailyStatsButton.getText().toString())
                .isEqualTo(activity.getString(R.string.statistics_streak_format, 1));
          });
    }
  }

  @Test
  public void testNavigateToDailyGame() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      scenario.onActivity(
          activity -> {
            FirebaseAnalytics mockAnalytics = mock(FirebaseAnalytics.class);
            activity.mFirebaseAnalytics = mockAnalytics;

            Button dailyButton = activity.findViewById(R.id.daily_play_button);
            assertThat(dailyButton).isNotNull();

            dailyButton.performClick();

            ShadowActivity shadowActivity = shadowOf(activity);
            Intent nextIntent = shadowActivity.getNextStartedActivity();
            assertThat(nextIntent.getComponent().getClassName())
                .isEqualTo(DailyGameActivity.class.getName());
            assertThat(nextIntent.hasExtra(Game.ID_TAG)).isTrue();

            ArgumentCaptor<Bundle> bundleCaptor = ArgumentCaptor.forClass(Bundle.class);
            verify(mockAnalytics)
                .logEvent(eq(AnalyticsConstants.Event.NEW_GAME), bundleCaptor.capture());
            assertThat(bundleCaptor.getValue().getString(AnalyticsConstants.Param.GAME_TYPE))
                .isEqualTo(DailyGame.GAME_TYPE_FOR_ANALYTICS);
          });
    }
  }
}
