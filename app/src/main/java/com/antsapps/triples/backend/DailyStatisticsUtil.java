package com.antsapps.triples.backend;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DailyStatisticsUtil {

  public static class DailyStatistics {
    public final int longestStreak;
    public final int currentStreak;
    public final int totalGamesCompleted;

    public DailyStatistics(int longestStreak, int currentStreak, int totalGamesCompleted) {
      this.longestStreak = longestStreak;
      this.currentStreak = currentStreak;
      this.totalGamesCompleted = totalGamesCompleted;
    }
  }

  public static DailyStatistics computeDailyStatistics(Iterable<DailyGame> dailyGameList) {
    Set<DailyGame.Day> completedOnDayDays = new HashSet<>();
    int totalSolved = 0;
    for (DailyGame game : dailyGameList) {
      if (game.getDateCompleted() == null || game.areHintsUsed()) continue;
      totalSolved++;
      if (game.isCompletedOnTime()) {
        completedOnDayDays.add(game.getGameDay());
      }
    }

    int currentStreak = 0;
    Calendar cal = Application.getTimeProvider().getCalendar();
    if (!completedOnDayDays.contains(DailyGame.Day.forCalendar(cal))) {
      cal.add(Calendar.DAY_OF_YEAR, -1);
    }
    while (completedOnDayDays.contains(DailyGame.Day.forCalendar(cal))) {
      currentStreak++;
      cal.add(Calendar.DAY_OF_YEAR, -1);
    }

    int longestStreak = 0;
    int tempStreak = 0;
    List<DailyGame.Day> sortedDays = new ArrayList<>(completedOnDayDays);
    Collections.sort(sortedDays);
    Calendar lastCal = null;
    for (DailyGame.Day day : sortedDays) {
      Calendar currentCal = day.getCalendar();
      if (lastCal != null) {
        Calendar expectedCal = (Calendar) lastCal.clone();
        expectedCal.add(Calendar.DAY_OF_YEAR, 1);
        if (expectedCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR)
            && expectedCal.get(Calendar.DAY_OF_YEAR) == currentCal.get(Calendar.DAY_OF_YEAR)) {
          tempStreak++;
        } else {
          tempStreak = 1;
        }
      } else {
        tempStreak = 1;
      }
      lastCal = currentCal;
      longestStreak = Math.max(longestStreak, tempStreak);
    }
    return new DailyStatistics(longestStreak, currentStreak, totalSolved);
  }
}
