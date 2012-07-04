package com.antsapps.triples.backend;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import android.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class Game implements Comparable<Game> {

  public interface OnUpdateGameStateListener {
    void onUpdateGameState(Game game);
  }

  public static final int MIN_CARDS_IN_PLAY = 12;
  public static final int MAX_CARDS_IN_PLAY = 21;

  public static final String ID_TAG = "game_id";

  private final Deck mDeck;

  private final List<Card> mCardsInPlay = Lists.newArrayList();

  private final Timer mTimer;

  private final long mRandomSeed;

  private long id;

  private Date mDate;

  private final List<OnUpdateGameStateListener> mListeners = Lists.newArrayList();

  public Game(long seed) {
    mRandomSeed = seed;
    mDeck = new Deck(new Random(seed));
    mTimer = new Timer();
  }

  public Game(long id,
      long seed,
      List<Card> cardsInPlay,
      List<Card> cardsInDeck,
      long timeElapsed) {
    this.id = id;
    mRandomSeed = seed;
    Collections.copy(mCardsInPlay, cardsInPlay);
    mDeck = new Deck(cardsInDeck);
    mTimer = new Timer(timeElapsed);
  }

  public void setOnTimerTickListener(OnTimerTickListener listener) {
    mTimer.setOnTimerTickListener(listener);
  }

  public void addOnUpdateGameStateListener(OnUpdateGameStateListener listener) {
    mListeners.add(listener);
  }

  public void begin() {
    for (int i = 0; i < MIN_CARDS_IN_PLAY; i++) {
      mCardsInPlay.add(mDeck.getNextCard());
    }

    // Add more cards so there is at least one valid triple.
    while (!checkIfAnyValidTriples()) {
      for (int i = 0; i < 3; i++) {
        mCardsInPlay.add(mDeck.getNextCard());
      }
    }
    mTimer.start();
    dispatchGameStateUpdate();
  }

  public void pause() {
    mTimer.pause();
  }

  public void resume() {
    mTimer.resume();
  }

  public ImmutableList<Card> getCurrentlyInPlay() {
    synchronized (mCardsInPlay) {
      return ImmutableList.copyOf(mCardsInPlay);
    }
  }

  public void commitTriple(List<Card> cards) {
    commitTriple(Iterables.toArray(cards, Card.class));
  }

  public void commitTriple(Card... cards) {
    synchronized (mCardsInPlay) {
      if (!mCardsInPlay.containsAll(Lists.newArrayList(cards))) {
        throw new IllegalArgumentException("Cards are not in the set. cards = "
            + cards + ", mCardsInPlay = " + mCardsInPlay);
      }
      if (!isValidTriple(cards)) {
        throw new IllegalArgumentException("Cards are not a valid triple");
      }

      for (int i = 0; i < 3; i++) {
        mCardsInPlay.set(mCardsInPlay.indexOf(cards[i]), null);
      }

      // Add more cards up to the minimum.
      while (numNotNull(mCardsInPlay) < MIN_CARDS_IN_PLAY) {
        for (int i = 0; i < 3; i++) {
          mCardsInPlay.set(mCardsInPlay.indexOf(null), mDeck.getNextCard());
        }
      }

      // Remove any null cards by replacing them with the last cards.
      int numNotNull = numNotNull(mCardsInPlay);
      for (int i = 0; i < numNotNull; i++) {
        if (mCardsInPlay.get(i) == null) {
          removeTrailingNulls(mCardsInPlay);
          if (i == mCardsInPlay.size() - 1)
            break;
          mCardsInPlay.set(i, mCardsInPlay.remove(mCardsInPlay.size() - 1));
        }
      }
      removeTrailingNulls(mCardsInPlay);

      // Add more cards until there is a valid triple.
      while (!checkIfAnyValidTriples()) {
        for (int i = 0; i < 3; i++) {
          mCardsInPlay.add(mDeck.getNextCard());
        }
      }
    }

    checkIfAnyValidTriples();
    dispatchGameStateUpdate();
  }

  public static boolean isValidTriple(List<Card> cards) {
    return isValidTriple(Iterables.toArray(cards, Card.class));
  }

  public static boolean isValidTriple(Card... cards) {
    if (cards.length != 3 || !isDistinct(cards)) {
      throw new IllegalArgumentException();
    }
    if ((cards[0].mNumber + cards[1].mNumber + cards[2].mNumber) % 3 == 0) {
      if ((cards[0].mShape + cards[1].mShape + cards[2].mShape) % 3 == 0) {
        if ((cards[0].mPattern + cards[1].mPattern + cards[2].mPattern) % 3 == 0) {
          if ((cards[0].mColor + cards[1].mColor + cards[2].mColor) % 3 == 0) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private static boolean isDistinct(Card... cards) {
    return (cards[0] != cards[1]) && (cards[0] != cards[2])
        && (cards[1] != cards[2]);
  }

  private boolean checkIfAnyValidTriples() {
    for (int i = 0; i < mCardsInPlay.size(); i++) {
      Card c0 = mCardsInPlay.get(i);
      if (c0 == null)
        continue;
      for (int j = i + 1; j < mCardsInPlay.size(); j++) {
        Card c1 = mCardsInPlay.get(j);
        if (c1 == null)
          continue;
        for (int k = j + 1; k < mCardsInPlay.size(); k++) {
          Card c2 = mCardsInPlay.get(k);
          if (c2 == null)
            continue;
          if (isValidTriple(c0, c1, c2)) {
            Log.i(
                "Game",
                String.format("Valid triple for positions %d %d %d", i, j, k));
            return true;
          }
        }
      }
    }
    return false;
  }

  private static int numNotNull(Iterable<Card> cards) {
    int countNotNull = 0;
    for (Card card : cards) {
      if (card != null)
        countNotNull++;
    }
    Log.i("Game", "countNotNull = " + countNotNull);
    return countNotNull;
  }

  private static void removeTrailingNulls(List<Card> cards) {
    Iterator<Card> reverseIt = Lists.reverse(cards).iterator();
    while (reverseIt.hasNext()) {
      if (reverseIt.next() == null) {
        reverseIt.remove();
      } else {
        return;
      }
    }
  }

  private void dispatchGameStateUpdate() {
    for(OnUpdateGameStateListener listener : mListeners) {
      listener.onUpdateGameState(this);
    }
  }

  public int getCardsRemaining() {
    return mDeck.getCardsRemaining();
  }

  byte[] getCardsInPlayAsByteArray() {
    return Utils.cardListToByteArray(mCardsInPlay);
  }

  byte[] getCardsInDeckAsByteArray() {
    return mDeck.toByteArray();
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  @Override
  public int compareTo(Game another) {
    return (int) Utils.compareTo(mDate, id, another.mDate, another.id);
  }

  long getRandomSeed() {
    return mRandomSeed;
  }

  long getTimeElapsed() {
    // TODO Auto-generated method stub
    return mTimer.getElapsed();
  }
}
