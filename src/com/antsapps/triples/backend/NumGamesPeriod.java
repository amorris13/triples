package com.antsapps.triples.backend;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

public final class NumGamesPeriod implements Period {

  private final int mNumber;

  public NumGamesPeriod(int number) {
    mNumber = number;
  }

  @Override
  public List<Game> filter(Iterable<Game> games) {
    List<Game> copy = Lists.newArrayList(games);
    if (copy.isEmpty()) {
      return Collections.<Game> emptyList();
    } else {
      Collections.sort(copy, new Game.DateGameComparator());
      return copy.subList(Math.max(0, copy.size() - mNumber), copy.size() - 1);
    }
  }
}