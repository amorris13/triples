package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.Deck;
import com.antsapps.triples.backend.Game;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;

public class CloudSyncBugTest extends BaseRobolectricTest {

  private Application mApplication;

  @Before
  public void setUp() {
    mApplication = Application.getInstance(ApplicationProvider.getApplicationContext());
    mApplication.clearAllData();
  }

  @Test
  public void mergeClassicCurrent_withStaleCloudGame_returnsTrueAndDoesNotAdd() {
    long timeStale = 1000L;
    long timeNewer = 2000L;

    // Start a game
    ClassicGame newerGame =
        new ClassicGame(
            -1,
            0,
            Collections.<Card>emptyList(),
            Collections.<Long>emptyList(),
            new Deck(new Random(0)),
            0,
            new Date(timeNewer),
            Game.GameState.ACTIVE,
            false);
    mApplication.addClassicGame(newerGame);

    // Simulate cloud sync with the stale game
    ClassicGame staleCloudGame =
        new ClassicGame(
            -1,
            0,
            Collections.<Card>emptyList(),
            Collections.<Long>emptyList(),
            new Deck(new Random(0)),
            0,
            new Date(timeStale),
            Game.GameState.ACTIVE,
            false);

    boolean changed = mApplication.mergeClassicCurrent(staleCloudGame);

    // It should return true to signal that the cloud state needs updating/deletion
    assertThat(changed).isTrue();
    // But it should NOT have added the stale game
    assertThat(Iterables.size(mApplication.getCurrentClassicGames())).isEqualTo(1);
    assertThat(
            Iterables.getOnlyElement(mApplication.getCurrentClassicGames())
                .getDateStarted()
                .getTime())
        .isEqualTo(timeNewer);
  }

  @Test
  public void mergeArcadeCurrent_withStaleCloudGame_returnsTrueAndDoesNotAdd() {
    long timeStale = 1000L;
    long timeNewer = 2000L;

    // Start a game
    ArcadeGame newerGame =
        new ArcadeGame(
            -1,
            0,
            Collections.<Card>emptyList(),
            Collections.<Long>emptyList(),
            new Deck(new Random(0)),
            0,
            new Date(timeNewer),
            Game.GameState.ACTIVE,
            0,
            false);
    mApplication.addArcadeGame(newerGame);

    // Simulate cloud sync with the stale game
    ArcadeGame staleCloudGame =
        new ArcadeGame(
            -1,
            0,
            Collections.<Card>emptyList(),
            Collections.<Long>emptyList(),
            new Deck(new Random(0)),
            0,
            new Date(timeStale),
            Game.GameState.ACTIVE,
            0,
            false);

    boolean changed = mApplication.mergeArcadeCurrent(staleCloudGame);

    assertThat(changed).isTrue();
    assertThat(Iterables.size(mApplication.getCurrentArcadeGames())).isEqualTo(1);
    assertThat(
            Iterables.getOnlyElement(mApplication.getCurrentArcadeGames())
                .getDateStarted()
                .getTime())
        .isEqualTo(timeNewer);
  }

  @Test
  public void mergeClassicCurrent_withStaleCloudGameAndNoCurrentLocally_returnsTrueAndDoesNotAdd() {
    long timeStale = 1000L;
    long timeNewer = 2000L;

    // A game was started at 2000L and COMPLETED.
    ClassicGame completedGame =
        new ClassicGame(
            -1,
            0,
            Collections.<Card>emptyList(),
            Collections.<Long>emptyList(),
            new Deck(Collections.<Card>emptyList()),
            10000,
            new Date(timeNewer),
            Game.GameState.COMPLETED,
            false);
    mApplication.addClassicGame(completedGame);

    // Simulate cloud sync with the stale game (started at 1000L)
    ClassicGame staleCloudGame =
        new ClassicGame(
            -1,
            0,
            Collections.<Card>emptyList(),
            Collections.<Long>emptyList(),
            new Deck(new Random(0)),
            0,
            new Date(timeStale),
            Game.GameState.ACTIVE,
            false);

    boolean changed = mApplication.mergeClassicCurrent(staleCloudGame);

    assertThat(changed).isTrue();
    assertThat(Iterables.isEmpty(mApplication.getCurrentClassicGames())).isTrue();
  }

  @Test
  public void mergeDailyCurrent_alreadyCompletedLocally_returnsTrueAndDoesNotAdd() {
    DailyGame.Day today = DailyGame.Day.forToday();
    // Actually, I'll just create it as completed
    DailyGame completedGame =
        new DailyGame(
            -1,
            today.getSeed(),
            Lists.newArrayList(new Card(0, 0, 0, 0), new Card(1, 1, 1, 1), new Card(2, 2, 2, 2)),
            Collections.<Long>emptyList(),
            new Deck(Collections.<Card>emptyList()),
            5000,
            new Date(),
            today,
            Game.GameState.COMPLETED,
            false,
            Collections.emptyList(),
            new Date());
    mApplication.addDailyGame(completedGame);

    // Simulate cloud sync with an in-progress version of today's game
    DailyGame cloudGame =
        new DailyGame(
            -1,
            today.getSeed(),
            Lists.newArrayList(new Card(0, 0, 0, 0), new Card(1, 1, 1, 1), new Card(2, 2, 2, 2)),
            Collections.<Long>emptyList(),
            new Deck(Collections.<Card>emptyList()),
            0,
            new Date(),
            today,
            Game.GameState.ACTIVE,
            false,
            Collections.emptyList(),
            null);

    boolean changed = mApplication.mergeDailyCurrent(cloudGame);

    assertThat(changed).isTrue();
    DailyGame local = mApplication.getDailyGameByGameDay(today);
    assertThat(local.getGameState()).isEqualTo(Game.GameState.COMPLETED);
    // Should still only have one daily game for today
    assertThat(mApplication.getDailyGames()).hasSize(1);
  }
}
