package com.antsapps.triples.backend;

import java.util.List;



public class Statistics {

  private final List<Game> mGamesInPeriod;

  Statistics(Iterable<Game> iterable, Period period) {
    mGamesInPeriod = period.filter(iterable);
  }

  public int getNumGames() {
    return mGamesInPeriod.size();
  }

  public long getFastestTime() {
    long minTime = Integer.MAX_VALUE;
    for (Game game : mGamesInPeriod) {
      minTime = Math.min(game.getTimeElapsed(), minTime);
    }
    return minTime;
  }

  public long getAverageTime() {
    long sum = 0;
    for (Game game : mGamesInPeriod) {
      sum += game.getTimeElapsed();
    }
    return sum / mGamesInPeriod.size();
  }
}
