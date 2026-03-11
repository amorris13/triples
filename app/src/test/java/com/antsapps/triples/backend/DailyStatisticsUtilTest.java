package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;

import com.antsapps.triples.BaseRobolectricTest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class DailyStatisticsUtilTest extends BaseRobolectricTest {

  private DailyGame createGame(
      DailyGame.Day day, boolean completed, boolean hintsUsed, boolean completedOnTime) {
    Date dateCompleted = null;
    if (completed) {
      if (completedOnTime) {
        dateCompleted = day.getCalendar().getTime();
      } else {
        // More than 48 hours after the start of the day
        dateCompleted =
            new Date(day.getCalendar().getTimeInMillis() + DailyGame.STREAK_BUFFER_MILLIS + 1000);
      }
    }

    // DailyGame constructor calls Game.getAllValidTriples(mCardsInPlay)
    // which calls Sets.combinations(mCardsInPlay, 3).
    // So we must provide at least 3 cards to avoid IllegalArgumentException.
    List<Card> cards = new ArrayList<>();
    cards.add(new Card(0, 0, 0, 0));
    cards.add(new Card(1, 1, 1, 1));
    cards.add(new Card(2, 2, 2, 2));

    return new DailyGame(
        -1,
        day.getSeed(),
        cards,
        Collections.<Long>emptyList(),
        new Deck(Collections.<Card>emptyList()),
        0,
        new Date(),
        day,
        completed ? Game.GameState.COMPLETED : Game.GameState.ACTIVE,
        hintsUsed,
        Collections.<Set<Card>>emptyList(),
        dateCompleted) {
      @Override
      protected void init() {
        // override to avoid potential issues if it's called
      }
    };
  }

  @Test
  public void testEmptyList() {
    DailyStatisticsUtil.DailyStatistics stats =
        DailyStatisticsUtil.computeDailyStatistics(new ArrayList<>());
    assertThat(stats.totalGamesCompleted).isEqualTo(0);
    assertThat(stats.currentStreak).isEqualTo(0);
    assertThat(stats.longestStreak).isEqualTo(0);
  }

  @Test
  public void testTotalSolved() {
    List<DailyGame> games = new ArrayList<>();
    DailyGame.Day day1 = new DailyGame.Day(2024, 1, 1);
    DailyGame.Day day2 = new DailyGame.Day(2024, 1, 2);
    DailyGame.Day day3 = new DailyGame.Day(2024, 1, 3);
    games.add(createGame(day1, true, false, true)); // Solved, no hints
    games.add(createGame(day2, true, true, true)); // Solved, but hints used
    games.add(createGame(day3, false, false, false)); // Not solved

    DailyStatisticsUtil.DailyStatistics stats = DailyStatisticsUtil.computeDailyStatistics(games);
    assertThat(stats.totalGamesCompleted).isEqualTo(1);
  }

  @Test
  public void testCurrentStreak_CompletedToday() {
    Calendar cal = Calendar.getInstance();
    DailyGame.Day today = DailyGame.Day.forCalendar(cal);
    cal.add(Calendar.DAY_OF_YEAR, -1);
    DailyGame.Day yesterday = DailyGame.Day.forCalendar(cal);

    List<DailyGame> games = new ArrayList<>();
    games.add(createGame(today, true, false, true));
    games.add(createGame(yesterday, true, false, true));

    DailyStatisticsUtil.DailyStatistics stats = DailyStatisticsUtil.computeDailyStatistics(games);
    assertThat(stats.currentStreak).isEqualTo(2);
  }

  @Test
  public void testCurrentStreak_CompletedYesterday() {
    Calendar cal = Calendar.getInstance();
    // skip today
    cal.add(Calendar.DAY_OF_YEAR, -1);
    DailyGame.Day yesterday = DailyGame.Day.forCalendar(cal);
    cal.add(Calendar.DAY_OF_YEAR, -1);
    DailyGame.Day dayBefore = DailyGame.Day.forCalendar(cal);

    List<DailyGame> games = new ArrayList<>();
    games.add(createGame(yesterday, true, false, true));
    games.add(createGame(dayBefore, true, false, true));

    DailyStatisticsUtil.DailyStatistics stats = DailyStatisticsUtil.computeDailyStatistics(games);
    assertThat(stats.currentStreak).isEqualTo(2);
  }

  @Test
  public void testCurrentStreak_Broken() {
    Calendar cal = Calendar.getInstance();
    // skip today
    // skip yesterday
    cal.add(Calendar.DAY_OF_YEAR, -2);
    DailyGame.Day dayBeforeYesterday = DailyGame.Day.forCalendar(cal);

    List<DailyGame> games = new ArrayList<>();
    games.add(createGame(dayBeforeYesterday, true, false, true));

    DailyStatisticsUtil.DailyStatistics stats = DailyStatisticsUtil.computeDailyStatistics(games);
    assertThat(stats.currentStreak).isEqualTo(0);
  }

  @Test
  public void testLongestStreak() {
    List<DailyGame> games = new ArrayList<>();
    games.add(createGame(new DailyGame.Day(2024, 1, 1), true, false, true));
    games.add(createGame(new DailyGame.Day(2024, 1, 2), true, false, true));
    games.add(createGame(new DailyGame.Day(2024, 1, 3), true, false, true));

    // Gap
    games.add(createGame(new DailyGame.Day(2024, 1, 5), true, false, true));
    games.add(createGame(new DailyGame.Day(2024, 1, 6), true, false, true));

    DailyStatisticsUtil.DailyStatistics stats = DailyStatisticsUtil.computeDailyStatistics(games);
    assertThat(stats.longestStreak).isEqualTo(3);
  }

  @Test
  public void testLongestStreak_YearBoundary() {
    List<DailyGame> games = new ArrayList<>();
    games.add(createGame(new DailyGame.Day(2023, 12, 31), true, false, true));
    games.add(createGame(new DailyGame.Day(2024, 1, 1), true, false, true));

    DailyStatisticsUtil.DailyStatistics stats = DailyStatisticsUtil.computeDailyStatistics(games);
    assertThat(stats.longestStreak).isEqualTo(2);
  }
}
