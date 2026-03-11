package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.antsapps.triples.BaseRobolectricTest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.junit.Test;

public class DailyStatisticsUtilTest extends BaseRobolectricTest {

  private DailyGame mockGame(
      DailyGame.Day day, boolean completed, boolean hintsUsed, boolean completedOnTime) {
    DailyGame game = mock(DailyGame.class);
    when(game.getGameDay()).thenReturn(day);
    when(game.getDateCompleted()).thenReturn(completed ? new Date() : null);
    when(game.areHintsUsed()).thenReturn(hintsUsed);
    when(game.isCompletedOnTime()).thenReturn(completedOnTime);
    return game;
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
    games.add(mockGame(day1, true, false, true)); // Solved, no hints
    games.add(mockGame(day2, true, true, true)); // Solved, but hints used
    games.add(mockGame(day3, false, false, false)); // Not solved

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
    games.add(mockGame(today, true, false, true));
    games.add(mockGame(yesterday, true, false, true));

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
    games.add(mockGame(yesterday, true, false, true));
    games.add(mockGame(dayBefore, true, false, true));

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
    games.add(mockGame(dayBeforeYesterday, true, false, true));

    DailyStatisticsUtil.DailyStatistics stats = DailyStatisticsUtil.computeDailyStatistics(games);
    assertThat(stats.currentStreak).isEqualTo(0);
  }

  @Test
  public void testLongestStreak() {
    List<DailyGame> games = new ArrayList<>();
    games.add(mockGame(new DailyGame.Day(2024, 1, 1), true, false, true));
    games.add(mockGame(new DailyGame.Day(2024, 1, 2), true, false, true));
    games.add(mockGame(new DailyGame.Day(2024, 1, 3), true, false, true));

    // Gap
    games.add(mockGame(new DailyGame.Day(2024, 1, 5), true, false, true));
    games.add(mockGame(new DailyGame.Day(2024, 1, 6), true, false, true));

    DailyStatisticsUtil.DailyStatistics stats = DailyStatisticsUtil.computeDailyStatistics(games);
    assertThat(stats.longestStreak).isEqualTo(3);
  }

  @Test
  public void testLongestStreak_YearBoundary() {
    List<DailyGame> games = new ArrayList<>();
    games.add(mockGame(new DailyGame.Day(2023, 12, 31), true, false, true));
    games.add(mockGame(new DailyGame.Day(2024, 1, 1), true, false, true));

    DailyStatisticsUtil.DailyStatistics stats = DailyStatisticsUtil.computeDailyStatistics(games);
    assertThat(stats.longestStreak).isEqualTo(2);
  }
}
