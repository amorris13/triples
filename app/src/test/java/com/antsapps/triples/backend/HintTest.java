package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class HintTest {

  private static class TestGame extends Game {
    TestGame(List<Card> cardsInPlay, Deck deck) {
      super(0, 0, cardsInPlay, Lists.<Long>newArrayList(), deck, 0, new Date(), GameState.ACTIVE, false);
    }

    @Override
    protected boolean isGameInValidState() {
      return true;
    }

    @Override
    public String getGameTypeForAnalytics() {
      return "test";
    }
  }

  private static class DummyRenderer implements Game.GameRenderer {
    Set<Card> selected = Sets.newHashSet();
    Set<Card> hinted = Sets.newHashSet();
    @Override public void updateCardsInPlay(ImmutableList<Card> newCards) {}
    @Override public void addHint(Card card) {
        hinted.add(card);
        // Mimic CardsView.addHint logic
        selected.retainAll(hinted);
    }
    @Override public void clearHintedCards() { hinted.clear(); }
    @Override public Set<Card> getSelectedCards() { return selected; }
  }

  @Test
  public void addHint_withSelectedCardInTriple_hintsThatTriple() {
    Card c1 = new Card(0, 0, 0, 0);
    Card c2 = new Card(1, 1, 1, 1);
    Card c3 = new Card(2, 2, 2, 2);
    Card c4 = new Card(0, 0, 0, 1);
    List<Card> cardsInPlay = Lists.newArrayList(c1, c2, c3, c4);
    for (int i = 0; i < 8; i++) {
        cardsInPlay.add(new Card(1, 2, 0, i % 3));
    }

    Deck deck = new Deck(Lists.<Card>newArrayList());
    TestGame game = new TestGame(cardsInPlay, deck);
    DummyRenderer renderer = new DummyRenderer();
    game.setGameRenderer(renderer);

    // User selected c1, which is part of triple {c1, c2, c3}
    renderer.selected.add(c1);

    game.addHint();

    // c1 should be hinted (to stay selected)
    assertThat(renderer.hinted).contains(c1);
  }

  @Test
  public void addHint_consecutiveHints_hintsAllCardsInTriple() {
    Card c1 = new Card(0, 0, 0, 0);
    Card c2 = new Card(1, 1, 1, 1);
    Card c3 = new Card(2, 2, 2, 2);
    List<Card> cardsInPlay = Lists.newArrayList(c1, c2, c3);
    for (int i = 0; i < 9; i++) {
        cardsInPlay.add(new Card(1, 2, 0, i % 3));
    }

    Deck deck = new Deck(Lists.<Card>newArrayList());
    TestGame game = new TestGame(cardsInPlay, deck);
    DummyRenderer renderer = new DummyRenderer();
    game.setGameRenderer(renderer);

    renderer.selected.add(c1);

    game.addHint(); // should hint c1
    game.addHint(); // should hint c2 or c3
    game.addHint(); // should hint the remaining one

    assertThat(renderer.hinted).containsExactly(c1, c2, c3);
  }

  @Test
  public void addHint_withMixedSelection_unselectsNonTripleCards() {
    Card c1 = new Card(0, 0, 0, 0);
    Card c2 = new Card(1, 1, 1, 1);
    Card c3 = new Card(2, 2, 2, 2);
    Card c4 = new Card(0, 0, 0, 1); // Not part of {c1, c2, c3}
    List<Card> cardsInPlay = Lists.newArrayList(c1, c2, c3, c4);
    for (int i = 0; i < 8; i++) {
        cardsInPlay.add(new Card(1, 2, 0, i % 3));
    }

    Deck deck = new Deck(Lists.<Card>newArrayList());
    TestGame game = new TestGame(cardsInPlay, deck);
    DummyRenderer renderer = new DummyRenderer();
    game.setGameRenderer(renderer);

    // User selected c1 (part of triple) and c4 (not part of that triple)
    renderer.selected.add(c1);
    renderer.selected.add(c4);

    game.addHint(); // should hint c1 and unselect c4

    assertThat(renderer.hinted).contains(c1);
    assertThat(renderer.selected).containsExactly(c1);
  }
}
