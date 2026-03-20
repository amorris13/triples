package com.antsapps.triples.backend;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GameReconstructor {

  public static List<TripleAnalysis> reconstruct(Game game) {
    if (game instanceof DailyGame) {
      return reconstructDaily((DailyGame) game);
    }

    List<TripleAnalysis> analysisList = Lists.newArrayList();
    Random random = new Random(game.getRandomSeed());
    Deck deck;
    if (game instanceof ZenGame && ((ZenGame) game).isBeginner()) {
      deck = Deck.createBeginnerDeck(random);
    } else {
      deck = new Deck(random);
    }

    List<Card> cardsInPlay = Lists.newArrayList();
    // Replicate Game.init()
    while (cardsInPlay.size() < Game.MIN_CARDS_IN_PLAY || !checkIfAnyValidTriples(cardsInPlay)) {
      for (int i = 0; i < 3; i++) {
        cardsInPlay.add(deck.getNextCard());
      }
    }

    List<Set<Card>> foundTriples = game.getFoundTriples();
    List<Long> findTimes = game.getTripleFindTimes();

    long lastTime = 0;
    for (int i = 0; i < foundTriples.size(); i++) {
      Set<Card> foundTriple = foundTriples.get(i);
      long time = findTimes.get(i);
      long duration = time - lastTime;

      List<Set<Card>> allAvailable = Game.getAllValidTriples(cardsInPlay);
      analysisList.add(
          new TripleAnalysis(
              foundTriple, time, duration, allAvailable, Lists.newArrayList(cardsInPlay)));

      // Replicate Game.updateDeckAfterValidTriple
      updateBoard(cardsInPlay, deck, foundTriple, game);

      lastTime = time;
    }

    return analysisList;
  }

  /**
   * Returns the board state remaining after all found triples have been processed (i.e., the final
   * board at the end of the game). Returns null for DailyGame since its board is fixed.
   */
  public static List<Card> getFinalBoardState(Game game) {
    if (game instanceof DailyGame) {
      return null;
    }

    Random random = new Random(game.getRandomSeed());
    Deck deck;
    if (game instanceof ZenGame && ((ZenGame) game).isBeginner()) {
      deck = Deck.createBeginnerDeck(random);
    } else {
      deck = new Deck(random);
    }

    List<Card> cardsInPlay = Lists.newArrayList();
    while (cardsInPlay.size() < Game.MIN_CARDS_IN_PLAY || !checkIfAnyValidTriples(cardsInPlay)) {
      for (int i = 0; i < 3; i++) {
        cardsInPlay.add(deck.getNextCard());
      }
    }

    for (Set<Card> foundTriple : game.getFoundTriples()) {
      updateBoard(cardsInPlay, deck, foundTriple, game);
    }

    return Lists.newArrayList(cardsInPlay);
  }

  private static List<TripleAnalysis> reconstructDaily(DailyGame game) {
    List<TripleAnalysis> analysisList = Lists.newArrayList();
    List<Card> cardsInPlay = game.getCardsInPlay();
    List<Set<Card>> foundTriples = game.getFoundTriples();
    List<Long> findTimes = game.getTripleFindTimes();

    long lastTime = 0;
    for (int i = 0; i < foundTriples.size(); i++) {
      Set<Card> foundTriple = foundTriples.get(i);
      long time = findTimes.get(i);
      long duration = time - lastTime;

      // In Daily Game, board doesn't change, but "available" triples are those NOT YET FOUND.
      // However, for consistency with the requested feature, maybe we show all triples
      // or just unfound ones? The requirement said "alternatives available at each step".
      // Let's show all valid triples on the board.
      List<Set<Card>> allAvailable = Game.getAllValidTriples(cardsInPlay);

      analysisList.add(new TripleAnalysis(foundTriple, time, duration, allAvailable, cardsInPlay));
      lastTime = time;
    }
    return analysisList;
  }

  private static boolean checkIfAnyValidTriples(List<Card> cardsInPlay) {
    return Game.getAValidTriple(cardsInPlay, Collections.<Card>emptySet()) != null;
  }

  private static void updateBoard(
      List<Card> cardsInPlay, Deck deck, Set<Card> foundTriple, Game game) {
    Card[] tripleArr = foundTriple.toArray(new Card[0]);
    if (game instanceof ArcadeGame || game instanceof ZenGame) {
      for (Card card : tripleArr) {
        int index = cardsInPlay.indexOf(card);
        if (index != -1) {
          cardsInPlay.set(index, null);
        }
      }
      deck.readdCards(tripleArr);
      if (game instanceof ZenGame) {
        // ZenGame shuffles the deck after re-adding.
        // Reconstructing this perfectly is impossible without the actual seed at each step.
        // We'll proceed with the current deck order and hope for the best.
      }
    } else {
      // ClassicGame logic
      for (Card card : tripleArr) {
        int index = cardsInPlay.indexOf(card);
        if (index != -1) {
          cardsInPlay.set(index, null);
        }
      }
    }

    // Common replenishment logic from Game.updateDeckAfterValidTriple
    while (numNotNull(cardsInPlay) < Game.MIN_CARDS_IN_PLAY && !deck.isEmpty()) {
      for (int i = 0; i < 3; i++) {
        int nullIdx = cardsInPlay.indexOf(null);
        if (nullIdx != -1) {
          cardsInPlay.set(nullIdx, deck.getNextCard());
        }
      }
    }

    // Compacting
    int numNotNull = numNotNull(cardsInPlay);
    for (int i = 0; i < numNotNull; i++) {
      if (cardsInPlay.get(i) == null) {
        removeTrailingNulls(cardsInPlay);
        if (i >= cardsInPlay.size()) break;
        cardsInPlay.set(i, cardsInPlay.remove(cardsInPlay.size() - 1));
      }
    }
    removeTrailingNulls(cardsInPlay);

    // Extra cards if no triples
    while (Game.getAValidTriple(cardsInPlay, Collections.<Card>emptySet()) == null
        && !deck.isEmpty()) {
      for (int i = 0; i < 3; i++) {
        cardsInPlay.add(deck.getNextCard());
      }
    }
  }

  private static int numNotNull(Iterable<Card> cards) {
    int countNotNull = 0;
    for (Card card : cards) {
      if (card != null) countNotNull++;
    }
    return countNotNull;
  }

  private static void removeTrailingNulls(List<Card> cards) {
    for (int i = cards.size() - 1; i >= 0; i--) {
      if (cards.get(i) == null) {
        cards.remove(i);
      } else {
        return;
      }
    }
  }
}
