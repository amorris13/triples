package com.antsapps.triples.backend;

import androidx.annotation.Nullable;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class Game implements Comparable<Game>, OnValidTripleSelectedListener {

  public interface OnUpdateGameStateListener {
    void onUpdateGameState(GameState state);

    void gameFinished();
  }

  public interface OnUpdateCardsInPlayListener {
    void onUpdateCardsInPlay(
        ImmutableList<Card> newCards,
        ImmutableList<Card> oldCards,
        int numRemaining,
        int numTriplesFound);

    void animateFoundTriple(Set<Card> triple);
  }

  public interface GameRenderer {
    void updateCardsInPlay(ImmutableList<Card> newCards);

    void addHint(Card card);

    void clearHintedCards();

    void clearSelectedCards();

    Set<Card> getSelectedCards();
  }

  /**
   * This reflects the game state as controlled by the user. This is orthogonal to that controlled
   * by Android's activity lifecycle.
   */
  public enum GameState {
    STARTING,
    ACTIVE,
    PAUSED,
    COMPLETED;
  }

  public static final int MIN_CARDS_IN_PLAY = 12;

  public static final String ID_TAG = "game_id";

  protected GameState mGameState;

  private boolean mActivitiyLifecycleActive;

  protected int mNumTriplesFound;

  protected final Deck mDeck;

  protected final List<Card> mCardsInPlay;

  protected final List<Long> mTripleFindTimes;

  protected final Set<Card> mHintedCards = Sets.newHashSet();

  protected boolean mHintsUsed;

  protected final Timer mTimer;

  private final long mRandomSeed;

  private long id;

  private final Date mDate;

  protected GameRenderer mGameRenderer;

  private final List<OnUpdateGameStateListener> mGameStateListeners = Lists.newArrayList();

  private final List<OnUpdateCardsInPlayListener> mCardsInPlayListeners = Lists.newArrayList();

  Game(
      long id,
      long seed,
      List<Card> cardsInPlay,
      List<Long> tripleFindTimes,
      Deck cardsInDeck,
      long timeElapsed,
      Date date,
      GameState gameState,
      boolean hintsUsed) {
    this.id = id;
    mRandomSeed = seed;
    mCardsInPlay = Lists.newArrayList(cardsInPlay);
    mTripleFindTimes = Lists.newArrayList(tripleFindTimes);
    mDeck = cardsInDeck;
    mTimer = new Timer(timeElapsed);
    mDate = date;
    mGameState = gameState;
    mHintsUsed = hintsUsed;
  }

  public void setGameRenderer(GameRenderer gameRenderer) {
    mGameRenderer = gameRenderer;
  }

  public void addOnTimerTickListener(OnTimerTickListener listener) {
    mTimer.addOnTimerTickListener(listener);
  }

  public void removeOnTimerTickListener(OnTimerTickListener listener) {
    mTimer.removeOnTimerTickListener(listener);
  }

  public void addOnUpdateGameStateListener(OnUpdateGameStateListener listener) {
    mGameStateListeners.add(listener);
  }

  public void removeOnUpdateGameStateListener(OnUpdateGameStateListener listener) {
    mGameStateListeners.remove(listener);
  }

  public void addOnUpdateCardsInPlayListener(OnUpdateCardsInPlayListener listener) {
    mCardsInPlayListeners.add(listener);
  }

  public void removeOnUpdateCardsInPlayListener(OnUpdateCardsInPlayListener listener) {
    mCardsInPlayListeners.remove(listener);
  }

  protected void init() {
    Preconditions.checkState(mCardsInPlay.isEmpty());
    // Add cards so there is at least one valid triple.
    while (mCardsInPlay.size() < MIN_CARDS_IN_PLAY || !checkIfAnyValidTriples()) {
      for (int i = 0; i < 3; i++) {
        mCardsInPlay.add(mDeck.getNextCard());
      }
    }
  }

  public void begin() {
    Preconditions.checkState(
        isGameInValidState(), "Game is not in a valid state. Game state = " + mGameState);
    dispatchCardsInPlayUpdate(ImmutableList.<Card>of());
    dispatchGameStateUpdate();
    updateTimer();
    if (mGameState == GameState.STARTING) {
      mGameState = GameState.ACTIVE;
    }
    dispatchGameStateUpdate();
  }

  protected abstract boolean isGameInValidState();

  public void resume() {
    if (mGameState == GameState.COMPLETED) {
      return;
    }
    mGameState = GameState.ACTIVE;
    updateTimer();
    dispatchGameStateUpdate();
  }

  public void shuffleCardsInPlay() {
    Collections.shuffle(mCardsInPlay);
    dispatchCardsInPlayUpdate(ImmutableList.copyOf(mCardsInPlay));
  }

  public void resumeFromLifecycle() {
    mActivitiyLifecycleActive = true;
    updateTimer();
  }

  public void pauseFromLifecycle() {
    mActivitiyLifecycleActive = false;
    updateTimer();
  }

  public void pause() {
    if (mGameState == GameState.COMPLETED) {
      return;
    }
    mGameState = GameState.PAUSED;
    updateTimer();
    dispatchGameStateUpdate();
  }

  private void updateTimer() {
    if ((mGameState == GameState.ACTIVE || mGameState == GameState.STARTING)
        && mActivitiyLifecycleActive) {
      mTimer.resume();
    } else {
      mTimer.pause();
    }
  }

  @Override
  public void onValidTripleSelected(Set<Card> cards) {
    commitTriple(Iterables.toArray(cards, Card.class));
  }

  public void commitTriple(Card... cards) {
    Preconditions.checkState(mGameState != GameState.COMPLETED, "Game is already completed.");
    ImmutableList<Card> oldCards = ImmutableList.copyOf(mCardsInPlay);
    if (!mCardsInPlay.containsAll(Lists.newArrayList(cards))) {
      throw new IllegalArgumentException(
          "Cards are not in the set. cards = " + cards + ", mCardsInPlay = " + mCardsInPlay);
    }
    if (!isValidFoundTriple(cards)) {
      return;
    }

    recordFoundTriple(cards);

    mHintedCards.clear();
    mGameRenderer.clearHintedCards();
    mGameRenderer.clearSelectedCards();

    updateDeckAfterValidTriple(cards);

    for (OnUpdateCardsInPlayListener listener : mCardsInPlayListeners) {
      listener.animateFoundTriple(Sets.newHashSet(cards));
    }

    dispatchCardsInPlayUpdate(oldCards);

    checkIfFinished();
  }

  protected boolean isValidFoundTriple(Card... cards) {
    return isValidTriple(cards);
  }

  protected void recordFoundTriple(Card... cards) {
    mNumTriplesFound++;
    mTripleFindTimes.add(mTimer.getElapsed());
  }

  protected void updateDeckAfterValidTriple(Card... cards) {
    for (int i = 0; i < 3; i++) {
      mCardsInPlay.set(mCardsInPlay.indexOf(cards[i]), null);
    }

    // Add more cards up to the minimum.
    while (numNotNull(mCardsInPlay) < MIN_CARDS_IN_PLAY && !mDeck.isEmpty()) {
      for (int i = 0; i < 3; i++) {
        mCardsInPlay.set(mCardsInPlay.indexOf(null), mDeck.getNextCard());
      }
    }

    // Remove any null cards by replacing them with the last cards.
    int numNotNull = numNotNull(mCardsInPlay);
    for (int i = 0; i < numNotNull; i++) {
      if (mCardsInPlay.get(i) == null) {
        removeTrailingNulls(mCardsInPlay);
        if (i == mCardsInPlay.size() - 1) break;
        mCardsInPlay.set(i, mCardsInPlay.remove(mCardsInPlay.size() - 1));
      }
    }
    removeTrailingNulls(mCardsInPlay);

    // Add more cards until there is a valid triple.
    while (!checkIfAnyValidTriples() && !mDeck.isEmpty()) {
      for (int i = 0; i < 3; i++) {
        mCardsInPlay.add(mDeck.getNextCard());
      }
    }
  }

  protected void checkIfFinished() {}

  protected void finish() {
    if (mGameState == GameState.COMPLETED) {
      return;
    }
    mGameState = GameState.COMPLETED;
    updateTimer();
    dispatchGameStateUpdate();
    for (OnUpdateGameStateListener listener : mGameStateListeners) {
      listener.gameFinished();
    }
  }

  private void dispatchGameStateUpdate() {
    for (OnUpdateGameStateListener listener : mGameStateListeners) {
      listener.onUpdateGameState(mGameState);
    }
  }

  public static boolean isValidTriple(Collection<Card> cards) {
    return isValidTriple(Iterables.toArray(cards, Card.class));
  }

  public static boolean isValidTriple(Card... cards) {
    if (cards.length != 3 || !isDistinct(cards)) {
      throw new IllegalArgumentException("Bad set of cards: " + Arrays.toString(cards));
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
    return (cards[0] != cards[1]) && (cards[0] != cards[2]) && (cards[1] != cards[2]);
  }

  protected boolean checkIfAnyValidTriples() {
    return getAValidTriple(mCardsInPlay, new HashSet<Card>()) != null;
  }

  public static List<Integer> getValidTriplePositions(List<Card> cardsInPlay) {
    Set<Card> aValidTriple = getAValidTriple(cardsInPlay, new HashSet<Card>());
    if (aValidTriple != null) {
      ImmutableList.Builder<Integer> positions = ImmutableList.builder();
      for (Card card : aValidTriple) {
        positions.add(cardsInPlay.indexOf(card));
      }
      return positions.build();
    } else {
      return ImmutableList.of();
    }
  }

  @Nullable
  public static Set<Card> getAValidTriple(List<Card> cardsInPlay, Set<Card> includingCards) {
    if (includingCards.size() > 3) {
      throw new IllegalArgumentException("including cards is too long");
    }

    if (includingCards.size() == 3) {
      if (isValidTriple(includingCards)) {
        return Sets.newHashSet(includingCards);
      } else {
        return null;
      }
    }

    for (Card card : cardsInPlay) {
      if (!includingCards.contains(card) && card != null) {
        includingCards.add(card);
        Set<Card> validTriple = getAValidTriple(cardsInPlay, includingCards);
        includingCards.remove(card);
        if (validTriple != null) {
          return validTriple;
        }
      }
    }
    return null;
  }

  public static List<Set<Card>> getAllValidTriples(List<Card> cards) {
    List<Set<Card>> validTriples = Lists.newArrayList();
    Set<Card> distinctCards = Sets.newHashSet(cards);
    distinctCards.remove(null);
    for (Set<Card> subset : Sets.combinations(distinctCards, 3)) {
      if (isValidTriple(subset)) {
        validTriples.add(subset);
      }
    }
    return validTriples;
  }

  private static int numNotNull(Iterable<Card> cards) {
    int countNotNull = 0;
    for (Card card : cards) {
      if (card != null) countNotNull++;
    }
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

  protected void dispatchCardsInPlayUpdate(ImmutableList<Card> oldCards) {
    ImmutableList<Card> newCards = ImmutableList.copyOf(mCardsInPlay);
    if (mGameRenderer != null) {
      mGameRenderer.updateCardsInPlay(newCards);
    }
    for (OnUpdateCardsInPlayListener listener : mCardsInPlayListeners) {
      listener.onUpdateCardsInPlay(newCards, oldCards, getCardsRemaining(), mNumTriplesFound);
    }
  }

  public int getCardsRemaining() {
    return mDeck.getCardsRemaining() + mCardsInPlay.size();
  }

  public List<Card> getCardsInPlay() {
    return ImmutableList.copyOf(mCardsInPlay);
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

  void setId(long id) {
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

  public List<Long> getTripleFindTimes() {
    return ImmutableList.copyOf(mTripleFindTimes);
  }

  public int getNumTriplesFound() {
    return mNumTriplesFound;
  }

  public boolean isNumTriplesFoundRelevant() {
    return false;
  }

  public Date getDateStarted() {
    return mDate;
  }

  public GameState getGameState() {
    return mGameState;
  }

  public boolean areHintsUsed() {
    return mHintsUsed;
  }

  public boolean getActivityLifecycleActive() {
    return mActivitiyLifecycleActive;
  }

  public abstract String getGameTypeForAnalytics();

  public boolean addHint() {
    mHintsUsed = true;
    if (mHintedCards.size() == 3) {
      return false;
    }

    Set<Card> selectedCards = mGameRenderer.getSelectedCards();

    // Calculate target triple
    Set<Card> targetTriple;
    if (mHintedCards.isEmpty()) {
      targetTriple = findValidTripleIncludingSelected(selectedCards);
    } else {
      targetTriple = getAValidTriple(mCardsInPlay, Sets.newHashSet(mHintedCards));
    }

    if (targetTriple == null) {
      return false;
    }

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

  protected void dispatchHint(Card card) {
    if (mHintedCards.add(card)) {
      mGameRenderer.addHint(card);
    }
  }

  private Set<Card> findValidTripleIncludingSelected(Set<Card> selectedCards) {
    for (int i = selectedCards.size(); i > 0; i--) {
      for (Set<Card> subset : Sets.combinations(selectedCards, i)) {
        Set<Card> triple = getAValidTriple(mCardsInPlay, Sets.newHashSet(subset));
        if (triple != null) {
          return triple;
        }
      }
    }
    return getAValidTriple(mCardsInPlay, Sets.<Card>newHashSet());
  }
}
