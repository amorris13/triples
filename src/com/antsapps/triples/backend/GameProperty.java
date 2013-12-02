package com.antsapps.triples.backend;

import java.util.Comparator;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public enum GameProperty {

  DATE(new Comparator<Game>() {
    @Override
    public int compare(Game lhs, Game rhs) {
      return lhs.getDateStarted().compareTo(rhs.getDateStarted());
    }
  }, true),

  TIME_ELAPSED(new Comparator<Game>() {
    @Override
    public int compare(Game lhs, Game rhs) {
      return Longs.compare(lhs.getTimeElapsed(), rhs.getTimeElapsed());
    }
  }, true),

  CARDS_REMAINING(new Comparator<Game>() {
    @Override
    public int compare(Game lhs, Game rhs) {
      return Ints.compare(lhs.getCardsRemaining(), rhs.getCardsRemaining());
    }
  }, true),

  NUM_TRIPLES_FOUND(new Comparator<Game>() {
    @Override
    public int compare(Game lhs, Game rhs) {
      if (lhs instanceof ArcadeGame && rhs instanceof ArcadeGame) {
        return Ints.compare(((ArcadeGame) lhs).getNumTriplesFound(),
            ((ArcadeGame) rhs).getNumTriplesFound());
      } else {
        return 0;
      }
    }
  }, false);

  private final Comparator<Game> mComparator;
  private final boolean mDefaultAscending;

  private GameProperty(Comparator<Game> comparator, boolean defaultAscending) {
    mComparator = comparator;
    mDefaultAscending = defaultAscending;
  }

  public ReversableComparator<Game> createReversableComparator() {
    ReversableComparator<Game> gameReversableComparator = new ReversableComparator<Game>
        (mComparator);
    if (!mDefaultAscending) {
      gameReversableComparator.reverse();
    }
    return gameReversableComparator;
  }
}
