package com.antsapps.triples.backend;

/** Created by anthony on 2/12/13. */
public class ClassicStatistics extends Statistics {
  private long mFastTime;
  private long mSlowTime;
  private long mAverageTime;
  private long mStartDate;
  private long mFinishDate;
  private long mFastDate;
  private long mSlowDate;

  ClassicStatistics(Iterable<? extends Game> iterable, Period period) {
    super(iterable, period);
    precalcStatistics();
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
      if (game.areHintsUsed()) {
        continue;
      }
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

    int numGamesNotHinted = 0;
    for (Game game : mGamesInPeriod) {
      if (!game.areHintsUsed()) {
        numGamesNotHinted++;
      }
    }

    mFastTime = fastTime;
    mSlowTime = slowTime;
    mAverageTime = numGamesNotHinted != 0 ? sumTime / numGamesNotHinted : 0;
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
}
