package com.antsapps.triples.backend;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

class Deck {

  final List<Card> mCards = Lists.newArrayList();

  public Deck(Random random) {
    initialize(random);
  }

  public Deck(List<Card> cards) {
    Collections.copy(mCards, cards);
  }

  private void initialize(Random random) {
    for (int number = 0; number < Card.MAX_VARIABLES; number++) {
      for (int shape = 0; shape < Card.MAX_VARIABLES; shape++) {
        for (int pattern = 0; pattern < Card.MAX_VARIABLES; pattern++) {
          for (int color = 0; color < Card.MAX_VARIABLES; color++) {
            mCards.add(new Card(number, shape, pattern, color));
          }
        }
      }
    }
    Collections.shuffle(mCards, random);
  }

  public Card getNextCard() {
    if (mCards.isEmpty()){
      return null;
    } else {
      return mCards.remove(0);
    }
  }

  public boolean isEmpty() {
    return mCards.isEmpty();
  }

  public int getCardsRemaining() {
    return mCards.size();
  }

  public byte[] toByteArray() {
    byte[] b = new byte[mCards.size()];
    for(int i = 0; i < mCards.size(); i++) {
      b[i] = Utils.cardToByte(mCards.get(i));
    }
    return b;
  }

  public static Deck fromByteArray(byte[] b) {
    List<Card> cards = Lists.newArrayList();
    for(int i = 0; i < b.length; i++) {
      cards.add(Utils.cardFromByte(b[i]));
    }
    return null;
  }
}
