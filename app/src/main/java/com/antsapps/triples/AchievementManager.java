package com.antsapps.triples;

import android.app.Activity;
import android.content.Context;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.Game;
import com.google.android.gms.games.PlayGames;
import java.util.concurrent.TimeUnit;

public class AchievementManager {
  private static final String TAG = "AchievementManager";

  public static void awardAchievementsForGame(Context context, Game game) {
    if (game.areHintsUsed()) {
      return;
    }

    if (game instanceof ClassicGame) {
      awardClassicAchievements(context, ((ClassicGame) game).getTimeElapsed());
    } else if (game instanceof ArcadeGame) {
      awardArcadeAchievements(context, ((ArcadeGame) game).getNumTriplesFound());
    }
  }

  private static void awardClassicAchievements(Context context, long timeElapsed) {
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

  private static void awardArcadeAchievements(Context context, int triplesFound) {
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
}
