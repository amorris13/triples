package com.antsapps.triples.backend;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;

public class Statistics {

  protected final List<Game> mGamesInPeriod;
  private List<TripleAnalysis> mAnalysis;

  Statistics(Iterable<? extends Game> iterable, Period period, boolean includeHinted) {
    mGamesInPeriod =
        period.filter(Iterables.filter(iterable, g -> includeHinted || !g.areHintsUsed()));
  }

  public int getNumGames() {
    return mGamesInPeriod.size();
  }

  public int getNumGamesWithAnalysis() {
    int count = 0;
    for (Game game : mGamesInPeriod) {
      if (!game.getFoundTriples().isEmpty()) {
        count++;
      }
    }
    return count;
  }

  public List<Game> getData() {
    return Lists.newArrayList(mGamesInPeriod);
  }

  public List<TripleAnalysis> getAnalysis() {
    if (mAnalysis == null) {
      mAnalysis = new ArrayList<>();
      for (Game game : mGamesInPeriod) {
        mAnalysis.addAll(game.reconstruct());
      }
    }
    return mAnalysis;
  }
}
