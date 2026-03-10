package com.antsapps.triples.backend;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import java.util.Comparator;

public enum GameProperty {
  DATE((lhs, rhs) -> lhs.getDateStarted().compareTo(rhs.getDateStarted()), true),

  TIME_ELAPSED((lhs, rhs) -> Longs.compare(lhs.getTimeElapsed(), rhs.getTimeElapsed()), true),

  CARDS_REMAINING(
      (lhs, rhs) -> Ints.compare(lhs.getCardsRemaining(), rhs.getCardsRemaining()), true),

  NUM_TRIPLES_FOUND(
      (lhs, rhs) -> {
        if (lhs.isNumTriplesFoundRelevant() && rhs.isNumTriplesFoundRelevant()) {
          return Ints.compare(lhs.getNumTriplesFound(), rhs.getNumTriplesFound());
        } else {
          return 0;
        }
      },
      false);

  private final Comparator<Game> mComparator;
  private final boolean mDefaultAscending;

  private GameProperty(Comparator<Game> comparator, boolean defaultAscending) {
    mComparator = comparator;
    mDefaultAscending = defaultAscending;
  }

  public ReversableComparator<Game> createReversableComparator() {
    ReversableComparator<Game> gameReversableComparator =
        new ReversableComparator<Game>(mComparator);
    if (!mDefaultAscending) {
      gameReversableComparator.reverse();
    }
    return gameReversableComparator;
  }
}
