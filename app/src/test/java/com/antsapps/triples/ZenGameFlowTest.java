package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.ZenGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.cardsview.VerticalCardsView;
import java.util.List;
import org.junit.Test;
import org.robolectric.shadows.ShadowLooper;

public class ZenGameFlowTest extends BaseRobolectricTest {

    @Test
    public void testZenGameFlow() {
        Application app = Application.getInstance(ApplicationProvider.getApplicationContext());
        ZenGame game = ZenGame.createFromSeed(12345L, false);
        app.addZenGame(game);

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ZenGameActivity.class);
        intent.putExtra(Game.ID_TAG, game.getId());

        try (ActivityScenario<ZenGameActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                VerticalCardsView cardsView = activity.findViewById(R.id.cards_view);
                assertThat(cardsView).isNotNull();

                TextView modeLabel = activity.findViewById(R.id.zen_mode_label);
                assertThat(modeLabel.getText().toString()).isEqualTo("Zen");

                // Force layout so cards have bounds
                cardsView.measure(1080, 1920);
                cardsView.layout(0, 0, 1080, 1920);

                List<Card> cardsInPlayBefore = game.getCardsInPlay();
                List<Integer> tripleIndices = Game.getValidTriplePositions(cardsInPlayBefore);
                assertThat(tripleIndices).hasSize(3);

                // Click the three cards of the triple
                for (int index : tripleIndices) {
                    clickCardAtPosition(cardsView, index);
                }

                // Wait for the game logic to process the triple
                ShadowLooper.idleMainLooper();

                // In Zen mode, cards are recycled, so cardsInPlay should be updated but still have at least MIN_CARDS_IN_PLAY
                List<Card> cardsInPlayAfter = game.getCardsInPlay();
                assertThat(cardsInPlayAfter.size()).isAtLeast(Game.MIN_CARDS_IN_PLAY);

                // The cards that were in the triple should no longer be in the same positions (likely replaced)
                // or at least the state has updated.
            });
        }
    }

    @Test
    public void testBeginnerGameFlow() {
        Application app = Application.getInstance(ApplicationProvider.getApplicationContext());
        ZenGame game = ZenGame.createFromSeed(12345L, true);
        app.addZenGame(game);

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ZenGameActivity.class);
        intent.putExtra(Game.ID_TAG, game.getId());

        try (ActivityScenario<ZenGameActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                TextView modeLabel = activity.findViewById(R.id.zen_mode_label);
                assertThat(modeLabel.getText().toString()).isEqualTo("Beginner");

                List<Card> cardsInPlay = game.getCardsInPlay();
                for (Card card : cardsInPlay) {
                    assertThat(card.mPattern).isEqualTo(1);
                }
            });
        }
    }

    private void clickCardAtPosition(VerticalCardsView cardsView, int index) {
        int widthOfCard = cardsView.getWidth() / VerticalCardsView.COLUMNS;
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
