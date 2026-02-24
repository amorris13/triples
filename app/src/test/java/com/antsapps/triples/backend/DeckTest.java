package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DeckTest {

  @Test
  public void newDeck_has81Cards() {
    Deck deck = new Deck(new Random());
    assertThat(deck.getCardsRemaining()).isEqualTo(81);
  }

  @Test
  public void getNextCard_returnsAll81DistinctCards() {
    Deck deck = new Deck(new Random());
    int count = 0;
    Set<Card> cards = new HashSet<>();
    while (!deck.isEmpty()) {
      Card card = deck.getNextCard();
      assertThat(card).isNotNull();
      cards.add(card);
      count++;
    }
    assertThat(count).isEqualTo(81);
    assertThat(cards).hasSize(81);
    assertThat(deck.getNextCard()).isNull();
  }

  @Test
  public void toByteArray_and_fromByteArray_areReversible() {
    Deck deck = new Deck(new Random());
    byte[] b = deck.toByteArray();
    Deck deck2 = Deck.fromByteArray(b);

    assertThat(deck2.getCardsRemaining()).isEqualTo(deck.getCardsRemaining());
    while (!deck.isEmpty()) {
      assertThat(deck2.getNextCard()).isEqualTo(deck.getNextCard());
    }
  }

  @Test
  public void readdCards_addsCardsToDeck() {
    Deck deck = new Deck(new ArrayList<Card>());
    assertThat(deck.isEmpty()).isTrue();

    Card card1 = new Card(0, 0, 0, 0);
    Card card2 = new Card(1, 1, 1, 1);
    deck.readdCards(card1, card2);

    assertThat(deck.getCardsRemaining()).isEqualTo(2);
    assertThat(deck.getNextCard()).isEqualTo(card1);
    assertThat(deck.getNextCard()).isEqualTo(card2);
  }
}
