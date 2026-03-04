package com.antsapps.triples.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Test;

public class ZenGameTest {

  private static class TestRenderer implements Game.GameRenderer {
    @Override
    public void updateCardsInPlay(ImmutableList<Card> newCards) {}
    @Override
    public void addHint(Card card) {}
    @Override
    public void clearHintedCards() {}
    @Override
    public java.util.Set<Card> getSelectedCards() { return Collections.emptySet(); }
  }

  @Test
  public void testZenGameRecycling() {
    ZenGame game = ZenGame.createFromSeed(12345L, false);
    game.setGameRenderer(new TestRenderer());
    game.begin();

    int initialDeckSize = game.mDeck.getCardsRemaining();
    List<Card> cardsInPlay = Lists.newArrayList(game.getCardsInPlay());

    // Find a valid triple
    List<Integer> positions = Game.getValidTriplePositions(cardsInPlay);
    assertFalse(positions.isEmpty());

    Card c1 = cardsInPlay.get(positions.get(0));
    Card c2 = cardsInPlay.get(positions.get(1));
    Card c3 = cardsInPlay.get(positions.get(2));

    // Commit triple
    game.commitTriple(c1, c2, c3);

    // In Zen mode, cards are re-added to deck.
    // Deck size should remain the same after committing a triple (3 cards removed from play, 3 added from deck, 3 re-added to deck)
    assertEquals(initialDeckSize, game.mDeck.getCardsRemaining());
  }

  @Test
  public void testBeginnerDeck() {
    ZenGame game = ZenGame.createFromSeed(12345L, true);
    assertTrue(game.isBeginner());

    List<Card> cardsInPlay = game.getCardsInPlay();
    for (Card card : cardsInPlay) {
      assertEquals(0, card.mPattern);
    }

    // Check deck as well
    while (!game.mDeck.isEmpty()) {
      Card card = game.mDeck.getNextCard();
      assertEquals(0, card.mPattern);
    }
  }
}
