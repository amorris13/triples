package com.antsapps.triples.backend;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class DailyGame extends Game {

  public static class Day implements Comparable<Day> {
    private final int mYear;

    private final int mMonth;
    private final int mDay;

    public Day(int year, int month, int day) {
      Preconditions.checkArgument(year > 0 && year <= 9999);
      Preconditions.checkArgument(month >= 1 && month <= 12);
      Preconditions.checkArgument(day >= 1 && day <= 31);

      mYear = year;
      mMonth = month;
      mDay = day;
    }

    public int getYear() {
      return mYear;
    }

    public int getMonth() {
      return mMonth;
    }

    public int getDay() {
      return mDay;
    }

    public long getSeed() {
      return (long) mYear * 10000 + mMonth * 100 + mDay;
    }

    public static Day fromString(String string) {
      int dateInt = Integer.parseInt(string);
      return new Day(dateInt / 10000, (dateInt / 100) % 100, dateInt % 100);
    }

    public static Day forToday() {
      Calendar cal = Application.getTimeProvider().getCalendar();
      return forCalendar(cal);
    }

    public static Day forCalendar(Calendar cal) {
      return new Day(
          cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    }

    public Calendar getCalendar() {
      Calendar cal = Application.getTimeProvider().getCalendar();
      cal.set(mYear, mMonth - 1, mDay, 0, 0, 0);
      cal.set(Calendar.MILLISECOND, 0);
      return cal;
    }

    @Override
    public String toString() {
      return String.format("%04d%02d%02d", mYear, mMonth, mDay);
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Day day)) {
        return false;
      }
      return mYear == day.mYear && mMonth == day.mMonth && mDay == day.mDay;
    }

    @Override
    public int hashCode() {
      return Objects.hash(mYear, mMonth, mDay);
    }

    @Override
    public int compareTo(Day o) {
      if (mYear != o.mYear) {
        return mYear - o.mYear;
      }
      if (mMonth != o.mMonth) {
        return mMonth - o.mMonth;
      }
      return mDay - o.mDay;
    }
  }

  public static final String GAME_TYPE_FOR_ANALYTICS = "daily";

  public static final long STREAK_BUFFER_MILLIS = 48 * 60 * 60 * 1000L; // 48 hours

  private final List<Set<Card>> mAllTriples;
  private final List<Set<Card>> mFoundTriples;

  private final Day mGameDay;
  private Date mDateCompleted;

  public boolean isCompletedOnTime() {
    if (mDateCompleted == null) {
      return false;
    }
    return mDateCompleted.getTime() - getGameDay().getCalendar().getTimeInMillis()
        < STREAK_BUFFER_MILLIS;
  }

  public static DailyGame createFromDay(Day day) {
    long seed = day.getSeed();
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
            Application.getTimeProvider().now(),
            day,
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
      Date dateStarted,
      Day gameDay,
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
        dateStarted,
        gameState,
        hintsUsed);
    mAllTriples = Game.getAllValidTriples(mCardsInPlay);
    mFoundTriples = Lists.newArrayList(foundTriples);
    mNumTriplesFound = mFoundTriples.size();
    mGameDay = gameDay;
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
      mDateCompleted = Application.getTimeProvider().now();
      finish();
    }
  }

  public int getTotalTriplesCount() {
    return mAllTriples.size();
  }

  public List<Set<Card>> getFoundTriples() {
    return Collections.unmodifiableList(mFoundTriples);
  }

  public DailyGame.Day getGameDay() {
    return mGameDay;
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
