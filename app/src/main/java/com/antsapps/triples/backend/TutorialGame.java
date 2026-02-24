package com.antsapps.triples.backend;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class TutorialGame extends Game {

  public static final String GAME_TYPE_FOR_ANALYTICS = "tutorial";

  public static TutorialGame createFromSeed(long seed) {
    TutorialGame game =
        new TutorialGame(
            -1,
            seed,
            Collections.<Card>emptyList(),
            Collections.<Long>emptyList(),
            new Deck(new Random(seed)),
            0,
            new Date(),
            GameState.STARTING);
    game.init();
    return game;
  }

  TutorialGame(
      long id,
      long seed,
      List<Card> cardsInPlay,
      List<Long> tripleFindTimes,
      Deck cardsInDeck,
      long timeElapsed,
      Date date,
      GameState gameState) {
    super(id, seed, cardsInPlay, tripleFindTimes, cardsInDeck, timeElapsed, date, gameState);
  }

  @Override
  protected boolean isGameInValidState() {
    return true;
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
    return GAME_TYPE_FOR_ANALYTICS;
  }
}
