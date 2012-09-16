package com.antsapps.triples.backend;

import java.util.Date;
import java.util.List;

public class ClassicGame extends Game {

  ClassicGame(long id,
      long seed,
      List<Card> cardsInPlay,
      Deck cardsInDeck,
      long timeElapsed,
      Date date,
      GameState gameState) {
    super(id, seed, cardsInPlay, cardsInDeck, timeElapsed, date, gameState);
    // TODO Auto-generated constructor stub
  }

  /**
   * A game is in a valid state if any of the following are true:
   * <ul>
   * <li>It is completed and there are no cards in the deck and no valid triples
   * on the board.
   * <li>It is not completed and there are at least {@link MIN_CARDS_IN_PLAY}
   * cards in play and at least one valid triple.
   * </ul>
   */
  private boolean isGameInValidState() {
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

}
