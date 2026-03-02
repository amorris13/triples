package com.antsapps.triples;

import android.util.Log;

import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.Game;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import java.util.concurrent.TimeUnit;

public class AchievementManager {
  private static final String TAG = "AchievementManager";

  public static void awardAchievementsForGame(GoogleApiClient googleApiClient, Game game) {
    if (googleApiClient == null || !googleApiClient.isConnected() || game.areHintsUsed()) {
      return;
    }

    if (game instanceof ClassicGame) {
      awardClassicAchievements(googleApiClient, ((ClassicGame) game).getTimeElapsed());
    } else if (game instanceof ArcadeGame) {
      awardArcadeAchievements(googleApiClient, ((ArcadeGame) game).getNumTriplesFound());
    }
  }

  private static void awardClassicAchievements(GoogleApiClient googleApiClient, long timeElapsed) {
    if (timeElapsed <= TimeUnit.SECONDS.toMillis(30)) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.CLASSIC_30S);
    }
    if (timeElapsed <= TimeUnit.SECONDS.toMillis(45)) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.CLASSIC_45S);
    }
    if (timeElapsed <= TimeUnit.MINUTES.toMillis(1)) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.CLASSIC_1M);
    }
    if (timeElapsed <= TimeUnit.MINUTES.toMillis(2)) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.CLASSIC_2M);
    }
    if (timeElapsed <= TimeUnit.MINUTES.toMillis(5)) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.CLASSIC_5M);
    }
    if (timeElapsed <= TimeUnit.MINUTES.toMillis(10)) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.CLASSIC_10M);
    }
    if (timeElapsed <= TimeUnit.MINUTES.toMillis(20)) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.CLASSIC_20M);
    }
  }

  private static void awardArcadeAchievements(GoogleApiClient googleApiClient, int triplesFound) {
    if (triplesFound >= 25) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.ARCADE_25_TRIPLES);
    }
    if (triplesFound >= 20) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.ARCADE_20_TRIPLES);
    }
    if (triplesFound >= 15) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.ARCADE_15_TRIPLES);
    }
    if (triplesFound >= 10) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.ARCADE_10_TRIPLES);
    }
    if (triplesFound >= 5) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.ARCADE_5_TRIPLES);
    }
    if (triplesFound >= 2) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.ARCADE_2_TRIPLES);
    }
  }

  public static void syncAchievements(GoogleApiClient googleApiClient, Application application) {
    if (googleApiClient == null || !googleApiClient.isConnected()) {
      return;
    }

    int completedClassicGames = 0;
    long fastestClassicTime = Long.MAX_VALUE;
    for (ClassicGame game : application.getCompletedClassicGames()) {
      if (!game.areHintsUsed()) {
        completedClassicGames++;
        fastestClassicTime = Math.min(fastestClassicTime, game.getTimeElapsed());
      }
    }
    if (completedClassicGames > 0) {
      awardClassicAchievements(googleApiClient, fastestClassicTime);
      awardClassicCountAchievements(googleApiClient, completedClassicGames);
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
      awardArcadeAchievements(googleApiClient, maxArcadeTriples);
      awardArcadeCountAchievements(googleApiClient, completedArcadeGames);
    }
  }

  public static void awardCountAchievements(GoogleApiClient googleApiClient, Application application) {
    if (googleApiClient == null || !googleApiClient.isConnected()) {
      return;
    }

    int completedClassicGames = 0;
    for (ClassicGame game : application.getCompletedClassicGames()) {
      if (!game.areHintsUsed()) {
        completedClassicGames++;
      }
    }
    awardClassicCountAchievements(googleApiClient, completedClassicGames);

    int completedArcadeGames = 0;
    for (ArcadeGame game : application.getCompletedArcadeGames()) {
      if (!game.areHintsUsed()) {
        completedArcadeGames++;
      }
    }
    awardArcadeCountAchievements(googleApiClient, completedArcadeGames);
  }

  private static void awardClassicCountAchievements(GoogleApiClient googleApiClient, int count) {
    if (count >= 5000) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.CLASSIC_5000);
    }
    if (count >= 1000) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.CLASSIC_1000);
    }
    if (count >= 500) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.CLASSIC_500);
    }
    if (count >= 100) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.CLASSIC_100);
    }
    if (count >= 50) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.CLASSIC_50);
    }
    if (count >= 10) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.CLASSIC_10);
    }
    if (count >= 1) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.CLASSIC_1);
    }
  }

  private static void awardArcadeCountAchievements(GoogleApiClient googleApiClient, int count) {
    if (count >= 5000) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.ARCADE_5000);
    }
    if (count >= 1000) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.ARCADE_1000);
    }
    if (count >= 500) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.ARCADE_500);
    }
    if (count >= 100) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.ARCADE_100);
    }
    if (count >= 50) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.ARCADE_50);
    }
    if (count >= 10) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.ARCADE_10);
    }
    if (count >= 1) {
      Games.Achievements.unlock(googleApiClient, GamesServices.Achievement.ARCADE_1);
    }
  }
}
