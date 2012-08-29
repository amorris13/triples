package com.antsapps.triples.backend;

import java.util.Collections;
import java.util.List;

public class Statistics {

  private final List<Game> mGamesInPeriod;
  private long mFastTime;
  private long mSlowTime;
  private long mAverageTime;
  private long mStartDate;
  private long mFinishDate;
  private long mFastDate;
  private long mSlowDate;

  Statistics(Iterable<Game> iterable, Period period) {
    mGamesInPeriod = period.filter(iterable);
    Collections.sort(mGamesInPeriod, new Game.DateGameComparator());
    precalcStatistics();
  }

  public int getNumGames() {
    return mGamesInPeriod.size();
  }

  private void precalcStatistics() {
    long fastTime = Long.MAX_VALUE;
    long slowTime = 0;
    long sumTime = 0;
    long startDate = Long.MAX_VALUE;
    long finishDate = 0;
    long fastDate = 0;
    long slowDate = 0;

    for (Game game : mGamesInPeriod) {
      long time = game.getTimeElapsed();
      long date = game.getDateStarted().getTime();

      sumTime += time;

      if (time < fastTime) {
        fastTime = time;
        fastDate = date;
      }
      if (time > slowTime) {
        slowTime = time;
        slowDate = date;
      }

      if (date < startDate) {
        startDate = date;
      }
      if (date > finishDate) {
        finishDate = date;
      }
    }

    mFastTime = fastTime;
    mSlowTime = slowTime;
    mAverageTime = sumTime / getNumGames();
    mStartDate = startDate;
    mFinishDate = finishDate;
    mFastDate = fastDate;
    mSlowDate = slowDate;
  }

  public long getFastestTime() {
    return mFastTime;
  }

  public long getAverageTime() {
    return mAverageTime;
  }

  public long getSlowestTime() {
    return mSlowTime;
  }

  public long getStartDate() {
    return mStartDate;
  }

  public long getFinishDate() {
    return mFinishDate;
  }

  public long getFastestDate() {
    return mFastDate;
  }

  public long getSlowestDate() {
    return mSlowDate;
  }

  public long[][] getData() {
    final int X = 0;
    final int Y = 1;
    long[][] data = new long[getNumGames()][2];

    int i = 0;
    for (Game game : mGamesInPeriod) {
      data[i][X] = game.getDateStarted().getTime();
      data[i][Y] = game.getTimeElapsed();
      i++;
    }
    return data;
  }
}
