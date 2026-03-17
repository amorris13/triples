package com.antsapps.triples.backend;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ZenGame extends Game {

  public static final String GAME_TYPE_FOR_ANALYTICS = "zen";

  private final boolean mIsBeginner;

  public static ZenGame createFromSeed(long seed, boolean isBeginner) {
    Random random = new Random(seed);
    Deck deck = isBeginner ? Deck.createBeginnerDeck(random) : new Deck(random);
    ZenGame game =
        new ZenGame(
            -1,
            seed,
            Collections.<Card>emptyList(),
            Collections.<Long>emptyList(),
            deck,
            0,
            new Date(),
            GameState.STARTING,
            false,
            isBeginner,
            Collections.<Set<Card>>emptyList());
    game.init();
    return game;
  }

  public ZenGame(
      long id,
      long seed,
      List<Card> cardsInPlay,
      List<Long> tripleFindTimes,
      Deck cardsInDeck,
      long timeElapsed,
      Date dateStarted,
      GameState gameState,
      boolean hintsUsed,
      boolean isBeginner,
      List<Set<Card>> foundTriples) {
    super(
        id,
        seed,
        cardsInPlay,
        tripleFindTimes,
        cardsInDeck,
        timeElapsed,
        dateStarted,
        gameState,
        hintsUsed,
        foundTriples);
    mIsBeginner = isBeginner;
  }

  @Override
  protected boolean isGameInValidState() {
    switch (mGameState) {
      case COMPLETED:
        return true; // Zen mode doesn't really have a COMPLETED state in normal play but for
        // consistency
      case PAUSED:
      case ACTIVE:
      case STARTING:
        return true;
      default:
        return false;
    }
  }

  @Override
  public void commitTriple(Card... cards) {
    super.commitTriple(cards);

    // Zen mode: unlimited play by recycling cards
    mDeck.readdCards(cards);
    mDeck.shuffle(new Random());

    // In Zen mode, we always ensure there are cards in play and valid triples.
    // The super.commitTriple already handles adding cards from deck if needed.
    // Since we re-add cards to the deck, the deck will never be empty.
  }

  public boolean isBeginner() {
    return mIsBeginner;
  }

  @Override
  public String getGameTypeForAnalytics() {
    return GAME_TYPE_FOR_ANALYTICS + (mIsBeginner ? "_beginner" : "");
  }
}
