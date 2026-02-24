package com.antsapps.triples.backend;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class ClassicGame extends Game {

  public static final String GAME_TYPE_FOR_ANALYTICS = "classic";
  public static final String TUTORIAL_TYPE_FOR_ANALYTICS = "tutorial";

  public static ClassicGame createFromSeed(long seed) {
    ClassicGame game =
        new ClassicGame(
            -1,
            seed,
            Collections.<Card>emptyList(),
            new Deck(new Random(seed)),
            0,
            new Date(),
            GameState.STARTING);
    game.init();
    return game;
  }

  public static ClassicGame createTutorial(long seed) {
    ClassicGame game =
        new ClassicGame(
            -1,
            seed,
            Collections.<Card>emptyList(),
            Deck.createTutorialDeck(new Random(seed)),
            0,
            new Date(),
            GameState.STARTING);
    game.setIsTutorial(true);
    game.init();
    return game;
  }

  ClassicGame(
      long id,
      long seed,
      List<Card> cardsInPlay,
      Deck cardsInDeck,
      long timeElapsed,
      Date date,
      GameState gameState) {
    super(id, seed, cardsInPlay, cardsInDeck, timeElapsed, date, gameState);
  }

  /**
   * A game is in a valid state if any of the following are true:
   *
   * <ul>
   *   <li>It is completed and there are no cards in the deck and no valid triples on the board.
   *   <li>It is not completed and there are at least {@link MIN_CARDS_IN_PLAY} cards in play and at
   *       least one valid triple.
   * </ul>
   */
  @Override
  protected boolean isGameInValidState() {
    switch (mGameState) {
      case COMPLETED:
        return !checkIfAnyValidTriples() && mDeck.isEmpty();
      case PAUSED:
      case ACTIVE:
      case STARTING:
        return checkIfAnyValidTriples()
            && (mCardsInPlay.size() >= MIN_CARDS_IN_PLAY || mDeck.isEmpty());
      default:
        return false;
    }
  }

  @Override
  public void commitTriple(Card... cards) {
    super.commitTriple(cards);

    if (!checkIfAnyValidTriples()) {
      finish();
    }
  }

  @Override
  public String getGameTypeForAnalytics() {
    return isTutorial() ? TUTORIAL_TYPE_FOR_ANALYTICS : GAME_TYPE_FOR_ANALYTICS;
  }
}
