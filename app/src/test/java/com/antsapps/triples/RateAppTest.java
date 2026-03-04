package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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

public class RateAppTest extends BaseRobolectricTest {

    @Test
    public void testRateAppButtonIsVisibleOnCompletion() {
        Application app = Application.getInstance(ApplicationProvider.getApplicationContext());
        ClassicGame game = ClassicGame.createFromSeed(12345L);
        app.addClassicGame(game);

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ClassicGameActivity.class);
        intent.putExtra(Game.ID_TAG, game.getId());

        try (ActivityScenario<ClassicGameActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                VerticalCardsView cardsView = activity.findViewById(R.id.cards_view);

                // Force layout so cards have bounds
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

                // Check for Rate App button
                View rateAppButton = activity.findViewById(R.id.view_switcher).findViewById(R.id.rate_app);
                assertThat(rateAppButton).isNotNull();
                assertThat(rateAppButton.getVisibility()).isEqualTo(View.VISIBLE);
                assertThat(((Button)rateAppButton).getText().toString().equalsIgnoreCase("Rate App")).isTrue();
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
