package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ArcadeStatisticsTest {

  private static ArcadeGame createGame(int numTriplesFound, ArcadeGame.ArcadeStyle style) {
    return new ArcadeGame(
        0,
        0,
        ImmutableList.<Card>of(),
        ImmutableList.<Long>of(),
        new Deck(new Random(0)),
        ArcadeGame.TIME_LIMIT_MS + 100,
        new Date(),
        Game.GameState.COMPLETED,
        numTriplesFound,
        false,
        ImmutableList.<Set<Card>>of(),
        style);
  }

  @Test
  public void filtering_byStyleWorks() {
    List<ArcadeGame> games = new ArrayList<>();
    games.add(createGame(10, ArcadeGame.ArcadeStyle.FIXED));
    games.add(createGame(20, ArcadeGame.ArcadeStyle.BONUS));
    games.add(createGame(15, ArcadeGame.ArcadeStyle.FIXED));

    ArcadeStatistics fixedStats =
        new ArcadeStatistics(games, Period.ALL_TIME, true, ArcadeGame.ArcadeStyle.FIXED);
    System.out.println("Fixed stats num games: " + fixedStats.getNumGames());
    assertThat(fixedStats.getNumGames()).isEqualTo(2);

    ArcadeStatistics bonusStats =
        new ArcadeStatistics(games, Period.ALL_TIME, true, ArcadeGame.ArcadeStyle.BONUS);
    System.out.println("Bonus stats num games: " + bonusStats.getNumGames());
    assertThat(bonusStats.getNumGames()).isEqualTo(1);
  }
}
