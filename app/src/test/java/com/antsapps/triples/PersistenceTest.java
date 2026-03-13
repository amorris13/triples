package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.Game;
import com.google.common.collect.Sets;

import java.util.List;
import org.junit.Test;

public class PersistenceTest extends BaseRobolectricTest {

  @Test
  public void testGamePersistence() {
    Context context = ApplicationProvider.getApplicationContext();
    Application app = Application.getInstance(context);

    // 1. Create and add a game
    ClassicGame game = ClassicGame.createFromSeed(54321L);
    game.setGameRenderer(mock(Game.GameRenderer.class)); // prevent NPE if commitTriple calls it
    app.addClassicGame(game);
    long gameId = game.getId();
    assertThat(gameId).isNotEqualTo(-1);

    // 2. Modify the game (simulate finding a triple)
    List<Integer> tripleIndices = Game.getValidTriplePositions(game.getCardsInPlay());
    game.onValidTripleSelected(Sets.newHashSet(
        game.getCardsInPlay().get(tripleIndices.get(0)),
        game.getCardsInPlay().get(tripleIndices.get(1)),
        game.getCardsInPlay().get(tripleIndices.get(2))));
    app.saveClassicGame(game);

    // 3. Re-initialize Application and DBAdapter (simulated by clearing instance or just reading
    // from DB)
    // In Robolectric, the DB is in-memory by default for the session or tied to the context.
    // We can verify by getting the game again from the app.
    ClassicGame retrievedGame = app.getClassicGame(gameId);
    assertThat(retrievedGame).isNotNull();
    assertThat(retrievedGame.getRandomSeed()).isEqualTo(54321L);
    assertThat(retrievedGame.getTripleFindTimes()).hasSize(1);
  }
}
