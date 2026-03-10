package com.antsapps.triples.backend;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class DailyGame extends Game {

  public static final String GAME_TYPE_FOR_ANALYTICS = "daily";

  public static final long STREAK_BUFFER_MILLIS = 48 * 60 * 60 * 1000L; // 48 hours

  private final List<Set<Card>> mAllTriples;
  private final List<Set<Card>> mFoundTriples;
  private Date mDateCompleted;

  public static long getStartOfDaySeed(long dateMillis) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(dateMillis);
    return getStartOfDaySeed(cal);
  }

  public static long getStartOfDaySeed(Calendar cal) {
    Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    utcCal.set(
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH),
        0,
        0,
        0);
    utcCal.set(Calendar.MILLISECOND, 0);
    return utcCal.getTimeInMillis();
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

    DailyGame game =
        new DailyGame(
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
    super(
        id,
        seed,
        cardsInPlay,
        tripleFindTimes,
        cardsInDeck,
        timeElapsed,
        date,
        gameState,
        hintsUsed);
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

  @Override
  protected boolean isValidFoundTriple(Card... cards) {
    return super.isValidFoundTriple(cards) && !mFoundTriples.contains(Sets.newHashSet(cards));
  }

  @Override
  protected void recordFoundTriple(Card... cards) {
    super.recordFoundTriple();
    mFoundTriples.add(Sets.newHashSet(cards));
    mNumTriplesFound = mFoundTriples.size();
  }

  @Override
  protected void updateDeckAfterValidTriple(Card... cards) {
    // Nothing to do here in puzzle mode.
  }

  @Override
  protected void checkIfFinished() {
    if (mFoundTriples.size() == mAllTriples.size()) {
      mDateCompleted = new Date();
      finish();
    }
  }

  public int getTotalTriplesCount() {
    return mAllTriples.size();
  }

  public List<Set<Card>> getFoundTriples() {
    return Collections.unmodifiableList(mFoundTriples);
  }

  public Date getDateCompleted() {
    return mDateCompleted;
  }

  @Override
  public boolean addHint() {
    if (mHintedCards.size() == 3) {
      return false;
    }

    Set<Card> selectedCards = mGameRenderer.getSelectedCards();

    // Calculate target triple
    Set<Card> targetTriple = null;
    if (!mHintedCards.isEmpty()) {
      for (Set<Card> triple : mAllTriples) {
        if (!mFoundTriples.contains(triple) && triple.containsAll(mHintedCards)) {
          targetTriple = triple;
          break;
        }
      }
    }

    if (targetTriple == null) {
      mHintedCards.clear();
      mGameRenderer.clearHintedCards();
      targetTriple = findUnfoundTripleIncludingSelected(selectedCards);
    }

    if (targetTriple == null) {
      return false;
    }

    mHintsUsed = true;
    boolean hintedNewCard = false;

    // 1. Hint all selected cards in the triple that aren't hinted yet.
    // This ensures they stay selected in the UI.
    for (Card c : targetTriple) {
      if (selectedCards.contains(c) && !mHintedCards.contains(c)) {
        dispatchHint(c);
      }
    }

    // 2. Hint at least one card that was not selected (the "actual" hint).
    for (Card c : targetTriple) {
      if (!mHintedCards.contains(c) && !selectedCards.contains(c)) {
        dispatchHint(c);
        hintedNewCard = true;
        break;
      }
    }

    // 3. Fallback: if we haven't hinted a new card (e.g. all remaining cards in the triple
    // are selected), hint one of them.
    if (!hintedNewCard && mHintedCards.size() < 3) {
      for (Card c : targetTriple) {
        if (!mHintedCards.contains(c)) {
          dispatchHint(c);
          break;
        }
      }
    }

    return true;
  }

  private Set<Card> findUnfoundTripleIncludingSelected(Set<Card> selectedCards) {
    for (int i = selectedCards.size(); i > 0; i--) {
      for (Set<Card> subset : Sets.combinations(selectedCards, i)) {
        for (Set<Card> triple : mAllTriples) {
          if (!mFoundTriples.contains(triple) && triple.containsAll(subset)) {
            return triple;
          }
        }
      }
    }
    for (Set<Card> triple : mAllTriples) {
      if (!mFoundTriples.contains(triple)) {
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
