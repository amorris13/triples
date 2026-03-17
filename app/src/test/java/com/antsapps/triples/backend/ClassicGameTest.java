package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ClassicGameTest {

  @Test
  public void testPlayFullGame_unshuffledDeck() {
    // Create an unshuffled deck where every 3 cards form a triple.
    List<Card> cards = Lists.newArrayList();
    for (int number = 0; number < Card.MAX_VARIABLES; number++) {
      for (int shape = 0; shape < Card.MAX_VARIABLES; shape++) {
        for (int pattern = 0; pattern < Card.MAX_VARIABLES; pattern++) {
          for (int color = 0; color < Card.MAX_VARIABLES; color++) {
            cards.add(new Card(number, shape, pattern, color));
          }
        }
      }
    }
    Deck deck = new Deck(cards);
    ClassicGame game =
        new ClassicGame(
            1,
            0,
            Collections.emptyList(),
            Collections.emptyList(),
            deck,
            0,
            new Date(),
            Game.GameState.STARTING,
            false,
            Collections.emptyList());
    game.init();
    game.setGameRenderer(new FakeGameRenderer());
    game.begin();

    assertThat(game.getGameState()).isEqualTo(Game.GameState.ACTIVE);
    assertThat(game.getCardsRemaining()).isEqualTo(81);
    assertThat(game.getCardsInPlay()).hasSize(12);

    // Play until only 12 cards are left in total
    while (game.getCardsRemaining() > 12) {
      playATriple(game);
    }

    assertThat(game.getCardsRemaining()).isEqualTo(12);
    assertThat(game.getCardsInPlay()).hasSize(12);
    assertThat(game.getGameState()).isEqualTo(Game.GameState.ACTIVE);

    // 12 cards left
    playATriple(game);
    assertThat(game.getCardsRemaining()).isEqualTo(9);
    assertThat(game.getCardsInPlay()).hasSize(9);
    assertThat(game.getGameState()).isEqualTo(Game.GameState.ACTIVE);

    // 9 cards left
    playATriple(game);
    assertThat(game.getCardsRemaining()).isEqualTo(6);
    assertThat(game.getCardsInPlay()).hasSize(6);
    assertThat(game.getGameState()).isEqualTo(Game.GameState.ACTIVE);

    // 6 cards left
    playATriple(game);
    assertThat(game.getCardsRemaining()).isEqualTo(3);
    assertThat(game.getCardsInPlay()).hasSize(3);
    assertThat(game.getGameState()).isEqualTo(Game.GameState.ACTIVE);

    // 3 cards left
    playATriple(game);
    assertThat(game.getCardsRemaining()).isEqualTo(0);
    assertThat(game.getCardsInPlay()).hasSize(0);
    assertThat(game.getGameState()).isEqualTo(Game.GameState.COMPLETED);
  }

  private void playATriple(ClassicGame game) {
    List<Card> cardsInPlay = game.getCardsInPlay();
    List<Integer> tripleIndices = Game.getValidTriplePositions(cardsInPlay);
    assertThat(tripleIndices).hasSize(3);
    Card[] triple = new Card[3];
    for (int i = 0; i < 3; i++) {
      triple[i] = cardsInPlay.get(tripleIndices.get(i));
    }
    game.commitTriple(triple);
  }

  @Test
  public void testDeckWithThirteenCards() {
    List<Card> cards = Lists.newArrayList();
    for (int i = 0; i < 13; i++) {
      cards.add(new Card(i / 9, (i / 3) % 3, i % 3, 0));
    }
    // This is 13 cards.
    // Triple 1: (0,0,0,0), (0,0,1,0), (0,0,2,0) -> indices 0, 1, 2
    // Extra card: (1,1,1,0) - wait, let's make sure it's distinct.
    // i=0: (0,0,0,0)
    // i=1: (0,0,1,0)
    // i=2: (0,0,2,0)
    // Triple 1 is cards at 0, 1, 2.

    Deck deck = new Deck(cards);
    ClassicGame game =
        new ClassicGame(
            1,
            0,
            Collections.emptyList(),
            Collections.emptyList(),
            deck,
            0,
            new Date(),
            Game.GameState.STARTING,
            false,
            Collections.emptyList());
    game.init();
    game.setGameRenderer(new FakeGameRenderer());
    game.begin();

    assertThat(game.getCardsRemaining()).isEqualTo(13);
    assertThat(game.getCardsInPlay()).hasSize(12);

    // Play triple 1.
    game.commitTriple(cards.get(0), cards.get(1), cards.get(2));

    // It should have tried to draw 3 cards, but only 1 was left.
    // Then it should have removed the nulls.
    assertThat(game.getCardsInPlay()).hasSize(10);
    assertThat(game.getCardsRemaining()).isEqualTo(10);
    // None of the cards in play should be null.
    for (Card card : game.getCardsInPlay()) {
      assertThat(card).isNotNull();
    }
  }
}
