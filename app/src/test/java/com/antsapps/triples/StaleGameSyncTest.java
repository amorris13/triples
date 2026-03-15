package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.Deck;
import com.antsapps.triples.backend.Game;
import com.google.common.collect.Iterables;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;

public class StaleGameSyncTest extends BaseRobolectricTest {

  private Application mApplication;

  @Before
  public void setUp() {
    mApplication = Application.getInstance(ApplicationProvider.getApplicationContext());
    mApplication.clearAllData();
  }

  @Test
  public void mergeClassicCurrent_withGameAlreadyCompletedLocally_returnsFalse() {
    long seed = 123456789L;
    // Create and complete a game locally
    ClassicGame localGame = ClassicGame.createFromSeed(seed);
    localGame.finish();
    mApplication.addClassicGame(localGame);
    mApplication.saveClassicGame(localGame);

    // Create a "cloud" game with the same seed (start date) but it's still active
    ClassicGame cloudGame = ClassicGame.createFromSeed(seed);
    cloudGame.resume();

    // Try to merge it as a current game
    boolean merged = mApplication.mergeClassicCurrent(cloudGame);

    assertThat(merged).isFalse();
    assertThat(Iterables.isEmpty(mApplication.getCurrentClassicGames())).isTrue();
    assertThat(Iterables.size(mApplication.getCompletedClassicGames())).isEqualTo(1);
  }

  @Test
  public void mergeClassicCurrent_withNewerStateOfSameGame_returnsTrue() {
    long seed = 123456789L;
    // Create a game locally
    ClassicGame localGame = ClassicGame.createFromSeed(seed);
    mApplication.addClassicGame(localGame);

    // Create a cloud game with same seed but more progress (more time elapsed)
    ClassicGame cloudGame =
        new ClassicGame(
            -1,
            seed,
            localGame.getCardsInPlay(),
            localGame.getTripleFindTimes(),
            new Deck(new Random(seed)),
            10000, // more time elapsed
            localGame.getDateStarted(),
            Game.GameState.ACTIVE,
            false);

    boolean merged = mApplication.mergeClassicCurrent(cloudGame);

    assertThat(merged).isTrue();
    ClassicGame current = Iterables.getOnlyElement(mApplication.getCurrentClassicGames());
    assertThat(current.getTimeElapsed()).isEqualTo(10000);
  }

  @Test
  public void mergeClassicCurrent_withDifferentGame_replacesLocalIfNoneCurrent() {
    long seed2 = 222L;

    ClassicGame cloudGame = ClassicGame.createFromSeed(seed2);

    boolean merged = mApplication.mergeClassicCurrent(cloudGame);

    assertThat(merged).isTrue();
    ClassicGame current = Iterables.getOnlyElement(mApplication.getCurrentClassicGames());
    assertThat(current.getDateStarted().getTime()).isEqualTo(seed2);
  }

  @Test
  public void mergeClassicCurrent_withDifferentGame_doesNotReplaceLocalIfOneCurrent() {
    long seed1 = 111L;
    long seed2 = 222L;

    ClassicGame localGame = ClassicGame.createFromSeed(seed1);
    mApplication.addClassicGame(localGame);

    ClassicGame cloudGame = ClassicGame.createFromSeed(seed2);

    boolean merged = mApplication.mergeClassicCurrent(cloudGame);

    assertThat(merged).isFalse();
    ClassicGame current = Iterables.getOnlyElement(mApplication.getCurrentClassicGames());
    assertThat(current.getDateStarted().getTime()).isEqualTo(seed1);
  }
}
