package com.antsapps.triples.backend;

/** Created by anthony on 2/12/13. */
public class ArcadeStatistics extends Statistics {
  private long mMostFound;
  private long mLeastFound;
  private long mAverageFound;
  private long mMostFoundDate;
  private long mLeastFoundDate;

  ArcadeStatistics(Iterable<? extends ArcadeGame> iterable, Period period, boolean includeHinted) {
    super(iterable, period, includeHinted);
    precalcStatistics();
  }

  private void precalcStatistics() {
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
}
