package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StatisticsTest {

  private static Game createGame(long id, Date date) {
    return new ClassicGame(id, 0, ImmutableList.<Card>of(), ImmutableList.<Long>of(), null, 0, date, Game.GameState.ACTIVE);
  }

  @Test
  public void datePeriod_filtersCorrectly() {
    Date now = new Date();
    Date hourAgo = new Date(now.getTime() - 3600 * 1000);
    Date twoHoursAgo = new Date(now.getTime() - 2 * 3600 * 1000);

    Game g1 = createGame(1, twoHoursAgo);
    Game g2 = createGame(2, hourAgo);
    Game g3 = createGame(3, now);

    List<Game> games = ImmutableList.of(g1, g2, g3);

    DatePeriod<Game> period = DatePeriod.fromSince(new Date(now.getTime() - 5400 * 1000)); // 1.5 hours ago
    List<Game> filtered = period.filter(games);

    assertThat(filtered).containsExactly(g2, g3).inOrder();
  }

  @Test
  public void numGamesPeriod_filtersCorrectly() {
    Date now = new Date();
    Game g1 = createGame(1, new Date(now.getTime() - 3000));
    Game g2 = createGame(2, new Date(now.getTime() - 2000));
    Game g3 = createGame(3, new Date(now.getTime() - 1000));

    List<Game> games = ImmutableList.of(g1, g2, g3);

    NumGamesPeriod<Game> period = new NumGamesPeriod<>(2);
    List<Game> filtered = period.filter(games);

    assertThat(filtered).hasSize(2);
    assertThat(filtered).containsExactly(g2, g3).inOrder();
  }

  @Test
  public void statistics_wrapsFilteredGames() {
    Date now = new Date();
    Game g1 = createGame(1, now);
    List<Game> games = ImmutableList.of(g1);

    Statistics stats = new Statistics(games, Period.ALL_TIME);
    assertThat(stats.getNumGames()).isEqualTo(1);
    assertThat(stats.getData()).containsExactly(g1);
  }
}
