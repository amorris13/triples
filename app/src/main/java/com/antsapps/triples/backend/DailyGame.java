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

public class DailyGame extends Game {

  public static final String GAME_TYPE_FOR_ANALYTICS = "daily";

  private final List<Set<Card>> mAllTriples;
  private final List<Set<Card>> mFoundTriples;

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
        new Date(),
        GameState.STARTING,
        false,
        Collections.<Set<Card>>emptyList());
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
      List<Set<Card>> foundTriples) {
    super(id, seed, cardsInPlay, tripleFindTimes, cardsInDeck, timeElapsed, date, gameState, hintsUsed);
    mAllTriples = Game.getAllValidTriples(mCardsInPlay);
    mFoundTriples = Lists.newArrayList(foundTriples);
    mNumTriplesFound = mFoundTriples.size();
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
  public void commitTriple(Card... cards) {
    Set<Card> triple = Sets.newHashSet(cards);
    if (!mFoundTriples.contains(triple) && mAllTriples.contains(triple)) {
      mFoundTriples.add(triple);
      mNumTriplesFound = mFoundTriples.size();
      mTripleFindTimes.add(mTimer.getElapsed());

      if (mFoundTriples.size() == mAllTriples.size()) {
        finish();
      } else {
        dispatchCardsInPlayUpdate(ImmutableList.copyOf(mCardsInPlay));
      }
    }
  }

  public int getTotalTriplesCount() {
    return mAllTriples.size();
  }

  public List<Set<Card>> getFoundTriples() {
    return Collections.unmodifiableList(mFoundTriples);
  }

  @Override
  public String getGameTypeForAnalytics() {
    return GAME_TYPE_FOR_ANALYTICS;
  }
}
