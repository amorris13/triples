package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ViewAnimator;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.Deck;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.cardsview.CardsView;
import com.google.common.collect.Lists;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.util.List;
import org.junit.Test;
import org.robolectric.shadows.ShadowLooper;

public class GameEndFlowTest extends BaseRobolectricTest {

  @Test
  public void testClassicGameEndFlow_unshuffledDeck() {
    Application.sSeed = 12345L;
    FirebaseAnalytics mockFirebaseAnalytics = mock(FirebaseAnalytics.class);
    Application app = Application.getInstance(ApplicationProvider.getApplicationContext());
    app.clearAllData();

    // Create a small deck to speed up the test.
    List<Card> cards = Lists.newArrayList();
    for (int i = 0; i < 15; i++) {
      cards.add(new Card(i / 9, (i / 3) % 3, i % 3, 0));
    }
    Deck deck = new Deck(cards);
    ClassicGame game = ClassicGame.createFromSeed(0);
    // Overwrite the deck in the created game.
    try {
      java.lang.reflect.Field deckField = Game.class.getDeclaredField("mDeck");
      deckField.setAccessible(true);
      deckField.set(game, deck);

      java.lang.reflect.Field cardsInPlayField = Game.class.getDeclaredField("mCardsInPlay");
      cardsInPlayField.setAccessible(true);
      List<Card> cardsInPlay = (List<Card>) cardsInPlayField.get(game);
      cardsInPlay.clear();
      for (int i = 0; i < 12; i++) {
        cardsInPlay.add(deck.getNextCard());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    app.addClassicGame(game);

    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), ClassicGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());

    try (ActivityScenario<ClassicGameActivity> scenario = ActivityScenario.launch(intent)) {
      scenario.onActivity(
          activity -> {
            activity.mFirebaseAnalytics = mockFirebaseAnalytics;
            CardsView cardsView = activity.findViewById(R.id.cards_view);

            // Force layout
            cardsView.measure(1080, 1920);
            cardsView.layout(0, 0, 1080, 1920);

            // Play all triples
            while (game.getGameState() != Game.GameState.COMPLETED) {
              List<Card> cardsInPlay = game.getCardsInPlay();
              List<Integer> tripleIndices = Game.getValidTriplePositions(cardsInPlay);
              assertThat(tripleIndices).hasSize(3);

              for (int index : tripleIndices) {
                TestUtils.clickCardAtPosition(cardsView, index);
              }
              ShadowLooper.idleMainLooper();
            }

            assertThat(game.getGameState()).isEqualTo(Game.GameState.COMPLETED);

            // Verify UI transition
            ViewAnimator viewSwitcher = activity.findViewById(R.id.view_switcher);
            assertThat(viewSwitcher.getDisplayedChild()).isEqualTo(BaseGameActivity.VIEW_COMPLETED);

            View statsButton = activity.findViewById(R.id.statistics_button);
            assertThat(statsButton.getVisibility()).isEqualTo(View.VISIBLE);

            // Verify finish game analytics
            org.mockito.ArgumentCaptor<Bundle> bundleCaptor =
                org.mockito.ArgumentCaptor.forClass(Bundle.class);
            verify(mockFirebaseAnalytics, atLeastOnce())
                .logEvent(eq(AnalyticsConstants.Event.FINISH_GAME), bundleCaptor.capture());

            Bundle capturedBundle = bundleCaptor.getValue();
            assertThat(capturedBundle.getString(AnalyticsConstants.Param.GAME_TYPE))
                .isEqualTo(ClassicGame.GAME_TYPE_FOR_ANALYTICS);
            assertThat(capturedBundle.containsKey(AnalyticsConstants.Param.DURATION)).isTrue();
            assertThat(capturedBundle.containsKey(AnalyticsConstants.Param.TRIPLES_FOUND)).isTrue();
          });
    }
  }
}
