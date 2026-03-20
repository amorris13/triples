import 'dart:math';
import 'package:collection/collection.dart';
import '../models/card.dart';
import 'deck.dart';
import 'game.dart';

class ArcadeGame extends Game {
  static const int timeLimitMs = 60 * 1000;
  static const String gameTypeForAnalytics = 'arcade';

  ArcadeGame({
    required super.id,
    required super.randomSeed,
    required super.cardsInPlay,
    required super.tripleFindTimes,
    required super.foundTriples,
    required super.deck,
    required super.timeElapsed,
    required super.dateStarted,
    required super.gameState,
    required super.hintsUsed,
  });

  factory ArcadeGame.createFromSeed(int seed) {
    final random = Random(seed);
    final deck = Deck(random);
    final List<Card?> cardsInPlay = [];

    // Initial board setup
    while (cardsInPlay.length < Game.minCardsInPlay ||
           Game.getAValidTriple(cardsInPlay, {}) == null) {
      for (int i = 0; i < 3; i++) {
        if (!deck.isEmpty) {
          cardsInPlay.add(deck.getNextCard());
        }
      }
    }

    return ArcadeGame(
      id: -1,
      randomSeed: seed,
      cardsInPlay: cardsInPlay,
      tripleFindTimes: [],
      foundTriples: [],
      deck: deck,
      timeElapsed: 0,
      dateStarted: DateTime.now(),
      gameState: GameState.starting,
      hintsUsed: false,
    );
  }

  @override
  void updateBoard(List<Card?> cardsInPlay, Deck deck, Set<Card> foundTriple, Random random) {
    // Standard update
    super.updateBoard(cardsInPlay, deck, foundTriple, random);

    // In arcade, cards go back to the deck
    deck.readdCards(foundTriple);
  }

  void tick(int elapsedMs) {
    if (elapsedMs > timeLimitMs && gameState != GameState.completed) {
      finish();
    }
  }

  @override
  String getGameTypeForAnalytics() {
    return gameTypeForAnalytics;
  }
}
