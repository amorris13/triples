package com.antsapps.triples.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Created by anthony on 2/12/13. */
public class ArcadeStatistics extends Statistics {
  private long mMostFound;
  private long mLeastFound;
  private long mAverageFound;
  private long mMostFoundDate;
  private long mLeastFoundDate;
  private long mP25;
  private long mP50;
  private long mP75;
  private long mP95;

  private final ArcadeGame.ArcadeStyle mStyle;

  ArcadeStatistics(
      Iterable<? extends ArcadeGame> iterable,
      Period period,
      boolean includeHinted,
      ArcadeGame.ArcadeStyle style) {
    super(iterable, period, includeHinted);
    mStyle = style;
    precalcStatistics();
  }

  @Override
  protected boolean shouldInclude(Game game) {
    return ((ArcadeGame) game).getStyle() == mStyle;
  }

  private void precalcStatistics() {
    if (mGamesInPeriod.isEmpty()) {
      return;
    }
    long mostFound = 0;
    long leastFound = Long.MAX_VALUE;
    long sumFound = 0;
    long mostDate = 0;
    long leastDate = 0;

    for (Game game : mGamesInPeriod) {
      ArcadeGame arcadeGame = (ArcadeGame) game;
      long found = arcadeGame.getNumTriplesFound();
      long date = arcadeGame.getDateStarted().getTime();

      sumFound += found;

      if (found < leastFound) {
        leastFound = found;
        leastDate = date;
      }
      if (found > mostFound) {
        mostFound = found;
        mostDate = date;
      }
    }

    int numGames = mGamesInPeriod.size();

    mMostFound = mostFound;
    mLeastFound = leastFound;
    mAverageFound = numGames != 0 ? sumFound / numGames : 0;
    mMostFoundDate = mostDate;
    mLeastFoundDate = leastDate;

    List<Long> values = new ArrayList<>();
    for (Game game : mGamesInPeriod) {
      values.add((long) ((ArcadeGame) game).getNumTriplesFound());
    }
    Collections.sort(values);
    mP25 = values.get((int) (values.size() * 0.25));
    mP50 = values.get((int) (values.size() * 0.50));
    mP75 = values.get((int) (values.size() * 0.75));
    mP95 = values.get((int) (values.size() * 0.95));
  }

  public long getMostFound() {
    return mMostFound;
  }

  public long getAverageFound() {
    return mAverageFound;
  }

  public long getLeastFound() {
    return mLeastFound;
  }

  public long getMostFoundDate() {
    return mMostFoundDate;
  }

  public long getLeastFoundDate() {
    return mLeastFoundDate;
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
