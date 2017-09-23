package com.antsapps.triples.backend;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class ArcadeGame extends Game implements OnTimerTickListener {

  public static final long TIME_LIMIT_MS = 1 * 60 * 1000;

  private int mNumTriplesFound;

  public static ArcadeGame createFromSeed(long seed) {
    ArcadeGame game =
        new ArcadeGame(
            -1,
            seed,
            Collections.<Card>emptyList(),
            new Deck(new Random(seed)),
            0,
            new Date(),
            GameState.STARTING,
            0);
    game.init();
    return game;
  }

  ArcadeGame(
      long id,
      long seed,
      List<Card> cardsInPlay,
      Deck cardsInDeck,
      long timeElapsed,
      Date date,
      GameState gameState,
      int numTriplesFound) {
    super(id, seed, cardsInPlay, cardsInDeck, timeElapsed, date, gameState);
    mNumTriplesFound = numTriplesFound;
    mTimer.addOnTimerTickListener(this);
  }

  /**
   * A game is in a valid state if any of the following are true:
   *
   * <ul>
   *   <li>It is completed and the time has been exceeded.
   *   <li>It is not completed and the time has not been exceeded.
   * </ul>
   */
  @Override
  protected boolean isGameInValidState() {
    switch (mGameState) {
      case COMPLETED:
        return mTimer.getElapsed() > TIME_LIMIT_MS;
      case PAUSED:
      case ACTIVE:
      case STARTING:
        return mTimer.getElapsed() <= TIME_LIMIT_MS;
      default:
        return false;
    }
  }

  @Override
  public void commitTriple(Card... cards) {
    super.commitTriple(cards);

    mNumTriplesFound++;
    mDeck.readdCards(cards);
  }

  @Override
  public void onTimerTick(long elapsedTime) {
    if (elapsedTime > TIME_LIMIT_MS) {
      finish();
    }
  }

  public int getNumTriplesFound() {
    return mNumTriplesFound;
  }
}
