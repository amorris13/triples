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

    // List sorted descending: 100000, 99000, ..., 1000
    // size = 100
    // p95 = index (100 * 0.95) = index 95 = 5000
    // p75 = index (100 * 0.75) = index 75 = 25000
    // p50 = index (100 * 0.50) = index 50 = 50000
    // p25 = index (100 * 0.25) = index 25 = 75000

    assertThat(stats.getP95()).isEqualTo(5000L);
    assertThat(stats.getP75()).isEqualTo(25000L);
    assertThat(stats.getP50()).isEqualTo(50000L);
    assertThat(stats.getP25()).isEqualTo(75000L);
  }
}
