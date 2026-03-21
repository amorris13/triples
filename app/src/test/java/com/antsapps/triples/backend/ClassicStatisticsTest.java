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
public class ClassicStatisticsTest {

  private static ClassicGame createGame(long timeElapsed) {
    return new ClassicGame(
        0,
        0,
        ImmutableList.<Card>of(),
        ImmutableList.<Long>of(),
        new Deck(new Random(0)),
        timeElapsed,
        new Date(),
        Game.GameState.COMPLETED,
        false,
        ImmutableList.<Set<Card>>of());
  }

  @Test
  public void percentiles_areCorrectlyCalculated() {
    List<ClassicGame> games = new ArrayList<>();
    // 100 games with times 1, 2, ..., 100 seconds
    for (int i = 1; i <= 100; i++) {
      games.add(createGame(i * 1000L));
    }

    ClassicStatistics stats = new ClassicStatistics(games, Period.ALL_TIME, true);

    // After fix:
    // sorted ascending: 1000, 2000, ..., 100000
    // size = 100
    // p95 = index (100 * 0.05) = index 5 = 6000
    // p75 = index (100 * 0.25) = index 25 = 26000
    // p50 = index (100 * 0.50) = index 50 = 51000
    // p25 = index (100 * 0.75) = index 75 = 76000

    assertThat(stats.getP95()).isEqualTo(6000L);
    assertThat(stats.getP75()).isEqualTo(26000L);
    assertThat(stats.getP50()).isEqualTo(51000L);
    assertThat(stats.getP25()).isEqualTo(76000L);
  }
}
