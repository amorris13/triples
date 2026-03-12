package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.content.Intent;
import android.os.Bundle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.cardsview.VerticalCardsView;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.robolectric.shadows.ShadowLooper;

public class TripleAnalyticsTest extends BaseRobolectricTest {

  private FirebaseAnalytics mMockFirebaseAnalytics;

  @Before
  public void setUp() {
    mMockFirebaseAnalytics = mock(FirebaseAnalytics.class);
  }

  @Test
  public void testTripleFoundAnalytics_NoHint() {
    Application app = Application.getInstance(ApplicationProvider.getApplicationContext());
    ClassicGame game = ClassicGame.createFromSeed(12345L);
    app.addClassicGame(game);

    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), ClassicGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());

    try (ActivityScenario<ClassicGameActivity> scenario = ActivityScenario.launch(intent)) {
      scenario.onActivity(
          activity -> {
            // Inject mock analytics
            activity.mFirebaseAnalytics = mMockFirebaseAnalytics;

            VerticalCardsView cardsView = activity.findViewById(R.id.cards_view);
            // Force layout so cards have bounds
            cardsView.measure(1080, 1920);
            cardsView.layout(0, 0, 1080, 1920);

            List<Card> cardsInPlay = game.getCardsInPlay();
            List<Integer> tripleIndices = Game.getValidTriplePositions(cardsInPlay);

            // Click the three cards of the triple
            for (int index : tripleIndices) {
              TestUtils.clickCardAtPosition(cardsView, index);
            }

            // Wait for the game logic to process the triple
            ShadowLooper.idleMainLooper();

            // Verify analytics call
            ArgumentCaptor<Bundle> bundleCaptor = ArgumentCaptor.forClass(Bundle.class);
            verify(mMockFirebaseAnalytics, atLeastOnce())
                .logEvent(eq(AnalyticsConstants.Event.FIND_TRIPLE), bundleCaptor.capture());

            Bundle capturedBundle = bundleCaptor.getValue();
            assertThat(capturedBundle.getString(AnalyticsConstants.Param.GAME_TYPE))
                .isEqualTo(ClassicGame.GAME_TYPE_FOR_ANALYTICS);
            assertThat(capturedBundle.getBoolean(AnalyticsConstants.Param.HINT_USED)).isFalse();
          });
    }
  }

  @Test
  public void testTripleFoundAnalytics_WithHint() {
    Application app = Application.getInstance(ApplicationProvider.getApplicationContext());
    ClassicGame game = ClassicGame.createFromSeed(12345L);
    app.addClassicGame(game);

    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), ClassicGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());

    try (ActivityScenario<ClassicGameActivity> scenario = ActivityScenario.launch(intent)) {
      scenario.onActivity(
          activity -> {
            // Inject mock analytics
            activity.mFirebaseAnalytics = mMockFirebaseAnalytics;

            VerticalCardsView cardsView = activity.findViewById(R.id.cards_view);
            // Force layout so cards have bounds
            cardsView.measure(1080, 1920);
            cardsView.layout(0, 0, 1080, 1920);

            // Request a hint
            game.addHint();

            List<Card> cardsInPlay = game.getCardsInPlay();
            List<Integer> tripleIndices = Game.getValidTriplePositions(cardsInPlay);

            // Click the three cards of the triple
            for (int index : tripleIndices) {
              TestUtils.clickCardAtPosition(cardsView, index);
            }

            // Wait for the game logic to process the triple
            ShadowLooper.idleMainLooper();

            // Verify analytics call
            ArgumentCaptor<Bundle> bundleCaptor = ArgumentCaptor.forClass(Bundle.class);
            verify(mMockFirebaseAnalytics, atLeastOnce())
                .logEvent(eq(AnalyticsConstants.Event.FIND_TRIPLE), bundleCaptor.capture());

            Bundle capturedBundle = bundleCaptor.getValue();
            assertThat(capturedBundle.getString(AnalyticsConstants.Param.GAME_TYPE))
                .isEqualTo(ClassicGame.GAME_TYPE_FOR_ANALYTICS);
            assertThat(capturedBundle.getBoolean(AnalyticsConstants.Param.HINT_USED)).isTrue();
          });
    }
  }
}
