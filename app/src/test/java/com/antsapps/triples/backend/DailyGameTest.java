package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DailyGameTest {

  @Test
  public void createFromDay_isDeterministic() {
    DailyGame.Day day = new DailyGame.Day(2026, 03, 11);
    DailyGame game1 = DailyGame.createFromDay(day);
    DailyGame game2 = DailyGame.createFromDay(day);

    assertThat(game1.getCardsInPlay()).isEqualTo(game2.getCardsInPlay());
    assertThat(game1.getTotalTriplesCount()).isEqualTo(game2.getTotalTriplesCount());
  }

  @Test
  public void createFromSeed_hasAtLeast4Triples() {
    DailyGame game = DailyGame.createFromDay(DailyGame.Day.forToday());
    assertThat(game.getTotalTriplesCount()).isAtLeast(4);
    assertThat(game.getCardsInPlay()).hasSize(15);
  }

  @Test
  public void commitTriple_updatesFoundCount() {
    DailyGame game = DailyGame.createFromDay(new DailyGame.Day(2026, 03, 11));
    game.setGameRenderer(new FakeGameRenderer());
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
    DailyGame game = DailyGame.createFromDay(new DailyGame.Day(2026, 03, 11));
    game.setGameRenderer(new FakeGameRenderer());
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
    DailyGame game = DailyGame.createFromDay(new DailyGame.Day(2026, 03, 11));
    game.setGameRenderer(new FakeGameRenderer());
    game.begin();

    List<Card> cards = game.getCardsInPlay();
    List<Set<Card>> allTriples = Game.getAllValidTriples(cards);

    for (Set<Card> triple : allTriples) {
      game.commitTriple(triple.toArray(new Card[0]));
    }

    assertThat(game.getGameState()).isEqualTo(Game.GameState.COMPLETED);
  }
}
