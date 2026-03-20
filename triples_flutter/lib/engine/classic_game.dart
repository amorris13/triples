import 'dart:math';
import '../models/card.dart';
import 'deck.dart';
import 'game.dart';

class ClassicGame extends Game {
  static const String gameTypeForAnalytics = 'classic';

  ClassicGame({
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

  factory ClassicGame.createFromSeed(int seed) {
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

    return ClassicGame(
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
  void checkIfFinished() {
    if (Game.getAValidTriple(cardsInPlay, {}) == null && deck.isEmpty) {
      finish();
    }
  }

  @override
  String getGameTypeForAnalytics() {
    return gameTypeForAnalytics;
  }
}
