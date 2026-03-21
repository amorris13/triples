package com.antsapps.triples.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Created by anthony on 2/12/13. */
public class ClassicStatistics extends Statistics {
  private long mFastTime;
  private long mSlowTime;
  private long mAverageTime;
  private long mStartDate;
  private long mFinishDate;
  private long mFastDate;
  private long mSlowDate;
  private long mP25;
  private long mP50;
  private long mP75;
  private long mP95;

  ClassicStatistics(Iterable<? extends Game> iterable, Period period, boolean includeHinted) {
    super(iterable, period, includeHinted);
    precalcStatistics();
  }

  private void precalcStatistics() {
    if (mGamesInPeriod.isEmpty()) {
      return;
    }
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

    int numGames = mGamesInPeriod.size();

    mFastTime = fastTime;
    mSlowTime = slowTime;
    mAverageTime = numGames != 0 ? sumTime / numGames : 0;
    mStartDate = startDate;
    mFinishDate = finishDate;
    mFastDate = fastDate;
    mSlowDate = slowDate;

    List<Long> times = new ArrayList<>();
    for (Game game : mGamesInPeriod) {
      times.add(game.getTimeElapsed());
    }
    Collections.sort(times);
    mP25 = times.get((int) (times.size() * 0.75));
    mP50 = times.get((int) (times.size() * 0.50));
    mP75 = times.get((int) (times.size() * 0.25));
    mP95 = times.get((int) (times.size() * 0.05));
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

  public long getP25() {
    return mP25;
  }

  public long getP50() {
    return mP50;
  }

  public long getP75() {
    return mP75;
  }

  public long getP95() {
    return mP95;
  }
}
