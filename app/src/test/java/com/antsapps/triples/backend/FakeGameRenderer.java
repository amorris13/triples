package com.antsapps.triples.backend;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.util.Set;

/** Canonical fake implementation of {@link Game.GameRenderer} for testing. */
public class FakeGameRenderer implements Game.GameRenderer {
  public ImmutableList<Card> mCardsInPlay = ImmutableList.of();
  public final Set<Card> mSelectedCards = Sets.newHashSet();
  public final Set<Card> mHintedCards = Sets.newHashSet();

  @Override
  public void updateCardsInPlay(ImmutableList<Card> newCards) {
    mCardsInPlay = newCards;
  }

  @Override
  public void addHint(Card card) {
    mHintedCards.add(card);
    // Mimic CardsView.addHint logic: unselect any card that is not hinted.
    mSelectedCards.retainAll(mHintedCards);
  }

  @Override
  public void clearHintedCards() {
    mHintedCards.clear();
  }

  @Override
  public void clearSelectedCards() {
    mSelectedCards.clear();
  }

  @Override
  public Set<Card> getSelectedCards() {
    return mSelectedCards;
  }
}
