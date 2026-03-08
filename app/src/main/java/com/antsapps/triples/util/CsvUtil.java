package com.antsapps.triples.util;

import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.Game;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CsvUtil {
  public static String getClassicCsvContent(List<Game> games) {
    StringBuilder csv = new StringBuilder();
    csv.append("Date,Time Elapsed (ms),Cards Remaining,Hints Used\n");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    for (Game game : games) {
      csv.append(dateFormat.format(game.getDateStarted()));
      csv.append(",");
      csv.append(game.getTimeElapsed());
      csv.append(",");
      csv.append(game.getCardsRemaining());
      csv.append(",");
      csv.append(game.areHintsUsed());
      csv.append("\n");
    }
    return csv.toString();
  }

  public static String getArcadeCsvContent(List<Game> games) {
    StringBuilder csv = new StringBuilder();
    csv.append("Date,Time Elapsed (ms),Triples Found,Hints Used\n");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    for (Game game : games) {
      ArcadeGame arcadeGame = (ArcadeGame) game;
      csv.append(dateFormat.format(arcadeGame.getDateStarted()));
      csv.append(",");
      csv.append(arcadeGame.getTimeElapsed());
      csv.append(",");
      csv.append(arcadeGame.getNumTriplesFound());
      csv.append(",");
      csv.append(arcadeGame.areHintsUsed());
      csv.append("\n");
    }
    return csv.toString();
  }

  public static String getDailyCsvContent(List<DailyGame> games) {
    StringBuilder csv = new StringBuilder();
    csv.append("Date Started,Date Completed,Time Elapsed (ms),Triples Found,Hints Used\n");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    for (DailyGame game : games) {
      csv.append(dateFormat.format(game.getDateStarted()));
      csv.append(",");
      if (game.getDateCompleted() != null) {
        csv.append(dateFormat.format(game.getDateCompleted()));
      }
      csv.append(",");
      csv.append(game.getTimeElapsed());
      csv.append(",");
      csv.append(game.getNumTriplesFound());
      csv.append("/");
      csv.append(game.getTotalTriplesCount());
      csv.append(",");
      csv.append(game.areHintsUsed());
      csv.append("\n");
    }
    return csv.toString();
  }
}
