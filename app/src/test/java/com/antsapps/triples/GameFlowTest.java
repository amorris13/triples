package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.cardsview.VerticalCardsView;
import java.util.List;
import org.junit.Test;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowToast;

public class GameFlowTest extends BaseRobolectricTest {

    @Test
    public void testClassicGameFlow() {
        Application app = Application.getInstance(ApplicationProvider.getApplicationContext());
        ClassicGame game = ClassicGame.createFromSeed(12345L);
        app.addClassicGame(game);

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ClassicGameActivity.class);
        intent.putExtra(Game.ID_TAG, game.getId());

        try (ActivityScenario<ClassicGameActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                VerticalCardsView cardsView = activity.findViewById(R.id.cards_view);
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

    @Test
    public void testClassicGameCompletionToast() {
        Application app = Application.getInstance(ApplicationProvider.getApplicationContext());
        // Clear existing games to ensure "Best Ever" toast
        app.clearClassicGames();

        ClassicGame game = ClassicGame.createFromSeed(12345L);
        app.addClassicGame(game);

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ClassicGameActivity.class);
        intent.putExtra(Game.ID_TAG, game.getId());

        try (ActivityScenario<ClassicGameActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                VerticalCardsView cardsView = activity.findViewById(R.id.cards_view);
                cardsView.measure(1080, 1920);
                cardsView.layout(0, 0, 1080, 1920);

                // Complete the game
                while (game.getGameState() != Game.GameState.COMPLETED) {
                    List<Card> cardsInPlay = game.getCardsInPlay();
                    List<Integer> tripleIndices = Game.getValidTriplePositions(cardsInPlay);
                    if (tripleIndices.isEmpty()) break;
                    for (int index : tripleIndices) {
                        clickCardAtPosition(cardsView, index);
                    }
                    ShadowLooper.idleMainLooper();
                }

                assertThat(game.getGameState()).isEqualTo(Game.GameState.COMPLETED);

                // Check for toast
                Toast latestToast = ShadowToast.getLatestToast();
                assertThat(latestToast).isNotNull();
                assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("Congratulations! That's your best score ever.");
            });
        }
    }

    private void clickCardAtPosition(VerticalCardsView cardsView, int index) {
        int widthOfCard = cardsView.getWidth() / VerticalCardsView.COLUMNS;
        // height of card calculation from VerticalCardsView
        int heightOfCard = (int) (widthOfCard * ((Math.sqrt(5) - 1) / 2));

        int x = (index % VerticalCardsView.COLUMNS) * widthOfCard + widthOfCard / 2;
        int y = (index / VerticalCardsView.COLUMNS) * heightOfCard + heightOfCard / 2;

        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0);
        cardsView.dispatchTouchEvent(event);
        event.recycle();

        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, 0);
        cardsView.dispatchTouchEvent(event);
        event.recycle();
    }
}
