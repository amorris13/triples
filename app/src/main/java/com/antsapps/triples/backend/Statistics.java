package com.antsapps.triples.backend;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;

public class Statistics {

  protected final List<Game> mGamesInPeriod;

  Statistics(Iterable<? extends Game> iterable, Period period, boolean includeHinted) {
    mGamesInPeriod =
        period.filter(Iterables.filter(iterable, g -> includeHinted ? true : !g.areHintsUsed()));
  }

  public int getNumGames() {
    return mGamesInPeriod.size();
  }

  public List<Game> getData() {
    return Lists.newArrayList(mGamesInPeriod);
  }
}
