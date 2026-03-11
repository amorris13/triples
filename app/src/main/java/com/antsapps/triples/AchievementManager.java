package com.antsapps.triples;

import android.app.Activity;
import android.content.Context;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.DailyGame;
import com.google.android.gms.games.PlayGames;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AchievementManager {
  private static final String TAG = "AchievementManager";

  public static void awardClassicAchievements(Context context, long timeElapsed) {
    if (timeElapsed <= TimeUnit.SECONDS.toMillis(30)) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_classic__30s));
    }
    if (timeElapsed <= TimeUnit.SECONDS.toMillis(45)) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_classic__45s));
    }
    if (timeElapsed <= TimeUnit.MINUTES.toMillis(1)) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_classic__1m));
    }
    if (timeElapsed <= TimeUnit.MINUTES.toMillis(2)) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_classic__2m));
    }
    if (timeElapsed <= TimeUnit.MINUTES.toMillis(5)) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_classic__5m));
    }
    if (timeElapsed <= TimeUnit.MINUTES.toMillis(10)) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_classic__10m));
    }
    if (timeElapsed <= TimeUnit.MINUTES.toMillis(20)) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_classic__20m));
    }
  }

  public static void awardDailyAchievements(Context context, Application application) {
    Set<Long> completedOnDaySeeds = new HashSet<>();
    int totalSolved = 0;
    for (DailyGame game : application.getCompletedDailyGames()) {
      if (game.getDateCompleted() == null || game.areHintsUsed()) continue;
      totalSolved++;
      long startSeed = getStartOfDay(game.getDateStarted().getTime());
      if (getStartOfDay(game.getDateCompleted().getTime()) == startSeed) {
        completedOnDaySeeds.add(startSeed);
      }
    }

    awardDailyCountAchievements(context, totalSolved);

    int longestStreak = 0;
    int tempStreak = 0;
    List<Long> sortedSeeds = new ArrayList<>(completedOnDaySeeds);
    Collections.sort(sortedSeeds);
    Calendar lastCal = null;
    for (Long seed : sortedSeeds) {
      Calendar currentCal = Calendar.getInstance();
      currentCal.setTimeInMillis(seed);
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
    awardDailyStreakAchievements(context, longestStreak);
  }

  private static long getStartOfDay(long time) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(time);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTimeInMillis();
  }

  public static void awardArcadeAchievements(Context context, int triplesFound) {
    if (triplesFound >= 25) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_arcade_25_triples));
    }
    if (triplesFound >= 20) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_arcade_20_triples));
    }
    if (triplesFound >= 15) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_arcade_15_triples));
    }
    if (triplesFound >= 10) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_arcade_10_triples));
    }
    if (triplesFound >= 5) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_arcade_5_triples));
    }
    if (triplesFound >= 2) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_arcade_2_triples));
    }
  }

  public static void syncAchievements(Context context, Application application) {
    int completedClassicGames = 0;
    long fastestClassicTime = Long.MAX_VALUE;
    for (ClassicGame game : application.getCompletedClassicGames()) {
      if (!game.areHintsUsed()) {
        completedClassicGames++;
        fastestClassicTime = Math.min(fastestClassicTime, game.getTimeElapsed());
      }
    }
    if (completedClassicGames > 0) {
      awardClassicAchievements(context, fastestClassicTime);
      awardClassicCountAchievements(context, completedClassicGames);
    }

    int completedArcadeGames = 0;
    int maxArcadeTriples = 0;
    for (ArcadeGame game : application.getCompletedArcadeGames()) {
      if (!game.areHintsUsed()) {
        completedArcadeGames++;
        maxArcadeTriples = Math.max(maxArcadeTriples, game.getNumTriplesFound());
      }
    }
    if (completedArcadeGames > 0) {
      awardArcadeAchievements(context, maxArcadeTriples);
      awardArcadeCountAchievements(context, completedArcadeGames);
    }

    awardDailyAchievements(context, application);
  }

  public static void awardCountAchievements(Context context, Application application) {
    int completedClassicGames = 0;
    for (ClassicGame game : application.getCompletedClassicGames()) {
      if (!game.areHintsUsed()) {
        completedClassicGames++;
      }
    }
    awardClassicCountAchievements(context, completedClassicGames);

    int completedArcadeGames = 0;
    for (ArcadeGame game : application.getCompletedArcadeGames()) {
      if (!game.areHintsUsed()) {
        completedArcadeGames++;
      }
    }
    awardArcadeCountAchievements(context, completedArcadeGames);

    awardDailyAchievements(context, application);
  }

  private static void awardClassicCountAchievements(Context context, int count) {
    if (count >= 5000) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_classic_5000_games));
    }
    if (count >= 1000) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_classic_1000_games));
    }
    if (count >= 500) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_classic_500_games));
    }
    if (count >= 100) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_classic_100_games));
    }
    if (count >= 50) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_classic_50_games));
    }
    if (count >= 10) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_classic_10_games));
    }
    if (count >= 1) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_classic_1_game));
    }
  }

  private static void awardArcadeCountAchievements(Context context, int count) {
    if (count >= 5000) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_arcade_5000_games));
    }
    if (count >= 1000) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_arcade_1000_games));
    }
    if (count >= 500) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_arcade_500_games));
    }
    if (count >= 100) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_arcade_100_games));
    }
    if (count >= 50) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_arcade_50_games));
    }
    if (count >= 10) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_arcade_10_games));
    }
    if (count >= 1) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_arcade_1_game));
    }
  }

  private static void awardDailyCountAchievements(Context context, int count) {
    if (count >= 500) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_daily_500_puzzles));
    }
    if (count >= 250) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_daily_250_puzzles));
    }
    if (count >= 100) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_daily_100_puzzles));
    }
    if (count >= 50) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_daily_50_puzzles));
    }
    if (count >= 10) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_daily_10_puzzles));
    }
    if (count >= 1) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_daily_1_puzzle));
    }
  }

  private static void awardDailyStreakAchievements(Context context, int streak) {
    if (streak >= 365) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_daily_365_day_streak));
    }
    if (streak >= 180) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_daily_180_day_streak));
    }
    if (streak >= 90) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_daily_90_day_streak));
    }
    if (streak >= 30) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_daily_30_day_streak));
    }
    if (streak >= 14) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_daily_14_day_streak));
    }
    if (streak >= 7) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(context.getString(R.string.achievement_daily_7_day_streak));
    }
  }
}
