package com.antsapps.triples.backend;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import android.os.Bundle;
import android.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class Game implements Comparable<Game> {

  public interface OnUpdateGameStateListener {
    void onUpdateCardsInPlay(ImmutableList<Card> newCards,
        ImmutableList<Card> oldCards, int numRemaining);

    void onFinish();
  }

  public static final int MIN_CARDS_IN_PLAY = 12;

  public static final String ID_TAG = "game_id";

  private final Deck mDeck;

  private final List<Card> mCardsInPlay;

  private final Timer mTimer;

  private final long mRandomSeed;

  private long id;

  private final Date mDate;

  private final List<OnUpdateGameStateListener> mListeners = Lists
      .newArrayList();

  public static Game createFromSeed(long seed) {
    return new Game(-1, seed, Collections.<Card> emptyList(), new Deck(
        new Random(seed)), 0, new Date());
  }

  public Bundle saveState() {
    Bundle bundle = new Bundle();
    bundle.putLong("id", id);
    bundle.putLong("elapsed_time", mTimer.getElapsed());
    bundle.putByteArray("cards_in_play", getCardsInPlayAsByteArray());
    bundle.putByteArray("deck", getCardsInDeckAsByteArray());
    bundle.putLong("seed", mRandomSeed);
    bundle.putLong("date", mDate.getTime());
    return bundle;
  }

  public static Game createFromBundle(Bundle bundle) {
    long id = bundle.getLong("id");
    long elapsedTime = bundle.getLong("elapsed_time");
    long seed = bundle.getLong("seed");
    Date date = new Date(bundle.getLong("date"));
    List<Card> cardsInPlay = Utils.cardListFromByteArray(bundle
        .getByteArray("cards_in_play"));
    Deck deck = Deck.fromByteArray(bundle.getByteArray("deck"));
    return new Game(id, seed, cardsInPlay, deck, elapsedTime, date);
  }

  Game(long id,
      long seed,
      List<Card> cardsInPlay,
      Deck cardsInDeck,
      long timeElapsed,
      Date date) {
    this.id = id;
    mRandomSeed = seed;
    mCardsInPlay = Lists.newArrayList(cardsInPlay);
    mDeck = cardsInDeck;
    mTimer = new Timer(timeElapsed);
    mDate = date;
  }

  public void setOnTimerTickListener(OnTimerTickListener listener) {
    mTimer.setOnTimerTickListener(listener);
  }

  public void addOnUpdateGameStateListener(OnUpdateGameStateListener listener) {
    mListeners.add(listener);
  }

  public void begin() {
    // Add more cards so there is at least one valid triple.
    while (mCardsInPlay.size() < MIN_CARDS_IN_PLAY || !checkIfAnyValidTriples()) {
      for (int i = 0; i < 3; i++) {
        mCardsInPlay.add(mDeck.getNextCard());
      }
    }
    mTimer.start();
    dispatchGameStateUpdate(
        ImmutableList.copyOf(mCardsInPlay),
        ImmutableList.<Card> of(),
        mDeck.getCardsRemaining());
  }

  public void pause() {
    mTimer.pause();
  }

  public void resume() {
    mTimer.resume();
  }

  public void commitTriple(List<Card> cards) {
    commitTriple(Iterables.toArray(cards, Card.class));
  }

  public void commitTriple(Card... cards) {
    ImmutableList<Card> oldCards = ImmutableList.copyOf(mCardsInPlay);
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

    checkIfAnyValidTriples();
    dispatchGameStateUpdate(
        ImmutableList.copyOf(mCardsInPlay),
        oldCards,
        mDeck.getCardsRemaining());
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

  private void dispatchGameStateUpdate(ImmutableList<Card> newCards,
      ImmutableList<Card> oldCards, int numRemaining) {
    for (OnUpdateGameStateListener listener : mListeners) {
      listener.onUpdateCardsInPlay(newCards, oldCards, numRemaining);
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

  public long getRandomSeed() {
    return mRandomSeed;
  }

  public long getTimeElapsed() {
    return mTimer.getElapsed();
  }

  public Date getDateStarted() {
    return mDate;
  }
}
