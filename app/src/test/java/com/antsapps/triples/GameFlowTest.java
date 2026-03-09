package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;

import android.content.Intent;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.cardsview.CardsView;
import java.util.List;
import org.junit.Test;
import org.robolectric.shadows.ShadowLooper;

public class GameFlowTest extends BaseRobolectricTest {

  @Test
  public void testClassicGameFlow() {
    Application app = Application.getInstance(ApplicationProvider.getApplicationContext());
    ClassicGame game = ClassicGame.createFromSeed(12345L);
    app.addClassicGame(game);

    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), ClassicGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());

    try (ActivityScenario<ClassicGameActivity> scenario = ActivityScenario.launch(intent)) {
      scenario.onActivity(
          activity -> {
            CardsView cardsView = activity.findViewById(R.id.cards_view);
            assertThat(cardsView).isNotNull();

            // Force layout so cards have bounds
            cardsView.measure(1080, 1920);
            cardsView.layout(0, 0, 1080, 1920);

            List<Card> cardsInPlay = game.getCardsInPlay();
            List<Integer> tripleIndices = Game.getValidTriplePositions(cardsInPlay);
            assertThat(tripleIndices).hasSize(3);

            int remainingBefore = game.getCardsRemaining();

            // Click the three cards of the triple
            for (int index : tripleIndices) {
              clickCardAtPosition(cardsView, index);
            }

            // Wait for the game logic to process the triple
            ShadowLooper.idleMainLooper();

            // Verify remaining count decreased
            TextView remainingText = activity.findViewById(R.id.cards_remaining_text);
            int remainingAfter = Integer.parseInt(remainingText.getText().toString());
            assertThat(remainingAfter).isEqualTo(remainingBefore - 3);
          });
    }
  }

  private void clickCardAtPosition(CardsView cardsView, int index) {
    TestUtils.clickCardAtPosition(cardsView, index);
  }
}
