package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DailyGameTest {

  private static class TestRenderer implements Game.GameRenderer {
    @Override
    public void updateCardsInPlay(ImmutableList<Card> newCards) {}

    @Override
    public void addHint(Card card) {}

    @Override
    public void clearHintedCards() {}

    @Override
    public Set<Card> getSelectedCards() {
      return Collections.emptySet();
    }

    @Override
    public void clearSelectedCards() {}
  }


  @Test
  public void createFromSeed_isDeterministic() {
    long seed = 12345L;
    DailyGame game1 = DailyGame.createFromSeed(seed);
    DailyGame game2 = DailyGame.createFromSeed(seed);

    assertThat(game1.getCardsInPlay()).isEqualTo(game2.getCardsInPlay());
    assertThat(game1.getTotalTriplesCount()).isEqualTo(game2.getTotalTriplesCount());
  }

  @Test
  public void createFromSeed_hasAtLeast4Triples() {
    DailyGame game = DailyGame.createFromSeed(System.currentTimeMillis());
    assertThat(game.getTotalTriplesCount()).isAtLeast(4);
    assertThat(game.getCardsInPlay()).hasSize(15);
  }

  @Test
  public void commitTriple_updatesFoundCount() {
    DailyGame game = DailyGame.createFromSeed(12345L);
    game.setGameRenderer(new TestRenderer());
    game.begin();

    List<Card> cards = game.getCardsInPlay();
    List<Set<Card>> allTriples = Game.getAllValidTriples(cards);

    Set<Card> triple = allTriples.get(0);
    game.commitTriple(triple.toArray(new Card[0]));

    assertThat(game.getNumTriplesFound()).isEqualTo(1);
    assertThat(game.getFoundTriples()).contains(triple);
  }

  @Test
  public void commitTriple_sameTripleTwice_doesNotIncrement() {
    DailyGame game = DailyGame.createFromSeed(12345L);
    game.setGameRenderer(new TestRenderer());
    game.begin();

    List<Card> cards = game.getCardsInPlay();
    List<Set<Card>> allTriples = Game.getAllValidTriples(cards);

    Set<Card> triple = allTriples.get(0);
    game.commitTriple(triple.toArray(new Card[0]));
    game.commitTriple(triple.toArray(new Card[0]));

    assertThat(game.getNumTriplesFound()).isEqualTo(1);
  }

  @Test
  public void commitAllTriples_finishesGame() {
    DailyGame game = DailyGame.createFromSeed(12345L);
    game.setGameRenderer(new TestRenderer());
    game.begin();

    List<Card> cards = game.getCardsInPlay();
    List<Set<Card>> allTriples = Game.getAllValidTriples(cards);

    for (Set<Card> triple : allTriples) {
      game.commitTriple(triple.toArray(new Card[0]));
    }

    assertThat(game.getGameState()).isEqualTo(Game.GameState.COMPLETED);
  }
}
