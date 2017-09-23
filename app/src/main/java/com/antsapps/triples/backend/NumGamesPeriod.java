package com.antsapps.triples.backend;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

public final class NumGamesPeriod<T extends Game> implements Period<T> {

  private final int mNumber;

  public NumGamesPeriod(int number) {
    mNumber = number;
  }

  @Override
  public List<T> filter(Iterable<T> games) {
    List<T> copy = Lists.newArrayList(games);
    if (copy.isEmpty()) {
      return Collections.<T>emptyList();
    } else {
      Collections.sort(copy, GameProperty.DATE.createReversableComparator());
      return copy.subList(Math.max(0, copy.size() - mNumber), copy.size());
    }
  }
}
