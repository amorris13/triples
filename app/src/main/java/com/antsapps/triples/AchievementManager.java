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
          .unlock(GamesServices.Achievement.CLASSIC_30S);
    }
    if (timeElapsed <= TimeUnit.SECONDS.toMillis(45)) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.CLASSIC_45S);
    }
    if (timeElapsed <= TimeUnit.MINUTES.toMillis(1)) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.CLASSIC_1M);
    }
    if (timeElapsed <= TimeUnit.MINUTES.toMillis(2)) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.CLASSIC_2M);
    }
    if (timeElapsed <= TimeUnit.MINUTES.toMillis(5)) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.CLASSIC_5M);
    }
    if (timeElapsed <= TimeUnit.MINUTES.toMillis(10)) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.CLASSIC_10M);
    }
    if (timeElapsed <= TimeUnit.MINUTES.toMillis(20)) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.CLASSIC_20M);
    }
  }

  private static void awardArcadeAchievements(Context context, int triplesFound) {
    if (triplesFound >= 25) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.ARCADE_25_TRIPLES);
    }
    if (triplesFound >= 20) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.ARCADE_20_TRIPLES);
    }
    if (triplesFound >= 15) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.ARCADE_15_TRIPLES);
    }
    if (triplesFound >= 10) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.ARCADE_10_TRIPLES);
    }
    if (triplesFound >= 5) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.ARCADE_5_TRIPLES);
    }
    if (triplesFound >= 2) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.ARCADE_2_TRIPLES);
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
          .unlock(GamesServices.Achievement.CLASSIC_5000);
    }
    if (count >= 1000) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.CLASSIC_1000);
    }
    if (count >= 500) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.CLASSIC_500);
    }
    if (count >= 100) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.CLASSIC_100);
    }
    if (count >= 50) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.CLASSIC_50);
    }
    if (count >= 10) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.CLASSIC_10);
    }
    if (count >= 1) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.CLASSIC_1);
    }
  }

  private static void awardArcadeCountAchievements(Context context, int count) {
    if (count >= 5000) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.ARCADE_5000);
    }
    if (count >= 1000) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.ARCADE_1000);
    }
    if (count >= 500) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.ARCADE_500);
    }
    if (count >= 100) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.ARCADE_100);
    }
    if (count >= 50) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.ARCADE_50);
    }
    if (count >= 10) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.ARCADE_10);
    }
    if (count >= 1) {
      PlayGames.getAchievementsClient((Activity) context)
          .unlock(GamesServices.Achievement.ARCADE_1);
    }
  }
}
