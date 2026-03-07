package com.antsapps.triples.backend;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

import java.util.Calendar;

public class DailyGame extends Game {

  public static final String GAME_TYPE_FOR_ANALYTICS = "daily";

  private final List<Set<Card>> mAllTriples;
  private final List<Set<Card>> mFoundTriples;
  private Date mDateCompleted;

  public static long getStartOfDaySeed(long dateMillis) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(dateMillis);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTimeInMillis();
  }

  public static DailyGame createFromSeed(long seed) {
    Random random = new Random(seed);
    List<Card> cardsInPlay = Lists.newArrayList();
    List<Set<Card>> allTriples;

    while (true) {
      cardsInPlay.clear();
      Deck deck = new Deck(random);
      for (int i = 0; i < 15; i++) {
        cardsInPlay.add(deck.getNextCard());
      }
      allTriples = Game.getAllValidTriples(cardsInPlay);
      if (allTriples.size() >= 4) {
        break;
      }
    }

    DailyGame game = new DailyGame(
        -1,
        seed,
        cardsInPlay,
        Collections.<Long>emptyList(),
        new Deck(Collections.<Card>emptyList()),
        0,
        new Date(seed),
        GameState.STARTING,
        false,
        Collections.<Set<Card>>emptyList(),
        null);
    return game;
  }

  public DailyGame(
      long id,
      long seed,
      List<Card> cardsInPlay,
      List<Long> tripleFindTimes,
      Deck cardsInDeck,
      long timeElapsed,
      Date date,
      GameState gameState,
      boolean hintsUsed,
      List<Set<Card>> foundTriples,
      Date dateCompleted) {
    super(id, seed, cardsInPlay, tripleFindTimes, cardsInDeck, timeElapsed, date, gameState, hintsUsed);
    mAllTriples = Game.getAllValidTriples(mCardsInPlay);
    mFoundTriples = Lists.newArrayList(foundTriples);
    mNumTriplesFound = mFoundTriples.size();
    mDateCompleted = dateCompleted;
  }

  @Override
  protected void init() {
    // Already initialized in createFromSeed or constructor
  }

  @Override
  protected boolean isGameInValidState() {
    return true;
  }

  public interface OnTripleFoundListener {
    void onTripleFound(Set<Card> triple);
  }

  private OnTripleFoundListener mOnTripleFoundListener;

  public void setOnTripleFoundListener(OnTripleFoundListener listener) {
    mOnTripleFoundListener = listener;
  }

  @Override
  public void commitTriple(Card... cards) {
    Set<Card> triple = Sets.newHashSet(cards);
    if (!mFoundTriples.contains(triple) && mAllTriples.contains(triple)) {
      mFoundTriples.add(triple);
      mNumTriplesFound = mFoundTriples.size();
      mTripleFindTimes.add(mTimer.getElapsed());

      mHintedCards.clear();
      if (mGameRenderer != null) {
        mGameRenderer.clearHintedCards();
        mGameRenderer.clearSelectedCards();
      }

      if (mOnTripleFoundListener != null) {
        mOnTripleFoundListener.onTripleFound(triple);
      }

      if (mFoundTriples.size() == mAllTriples.size()) {
        mDateCompleted = new Date();
        finish();
      }
    }
  }

  public int getTotalTriplesCount() {
    return mAllTriples.size();
  }

  public List<Set<Card>> getAllTriples() {
    return Collections.unmodifiableList(mAllTriples);
  }

  public List<Set<Card>> getFoundTriples() {
    return Collections.unmodifiableList(mFoundTriples);
  }

  public Date getDateCompleted() {
    return mDateCompleted;
  }

  @Override
  public boolean addHint() {
    mHintsUsed = true;
    if (mHintedCards.size() == 3) {
      return false;
    }

    Set<Card> selectedCards = mGameRenderer.getSelectedCards();

    // Find an unfound triple that includes as many selected cards as possible
    Set<Card> targetTriple = null;
    for (int i = selectedCards.size(); i >= 0; i--) {
      for (Set<Card> subset : Sets.combinations(selectedCards, i)) {
        targetTriple = getAnUnfoundTripleIncluding(subset);
        if (targetTriple != null) break;
      }
      if (targetTriple != null) break;
    }

    if (targetTriple == null) {
      return false;
    }

    // Now hint one card from targetTriple that isn't already hinted
    // Prefer hinting selected cards that aren't hinted yet (to keep them selected)
    for (Card c : targetTriple) {
      if (selectedCards.contains(c) && !mHintedCards.contains(c)) {
        dispatchHint(c);
        return true;
      }
    }
    // Then hint a non-selected card
    for (Card c : targetTriple) {
      if (!mHintedCards.contains(c)) {
        dispatchHint(c);
        return true;
      }
    }

    return false;
  }

  private Set<Card> getAnUnfoundTripleIncluding(Set<Card> subset) {
    for (Set<Card> triple : mAllTriples) {
      if (!mFoundTriples.contains(triple) && triple.containsAll(subset)) {
        return triple;
      }
    }
    return null;
  }

  @Override
  public String getGameTypeForAnalytics() {
    return GAME_TYPE_FOR_ANALYTICS;
  }
}
