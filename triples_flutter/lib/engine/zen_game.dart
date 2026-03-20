import 'dart:math';
import '../models/card.dart';
import 'deck.dart';
import 'game.dart';

class ZenGame extends Game {
  static const String gameTypeForAnalytics = 'zen';
  final bool isBeginner;

  ZenGame({
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
    required this.isBeginner,
  });

  factory ZenGame.createFromSeed(int seed, bool isBeginner) {
    final random = Random(seed);
    Deck deck;
    if (isBeginner) {
      final List<Card> cards = [];
      for (int n = 0; n < Card.maxVariables; n++) {
        for (int s = 0; s < Card.maxVariables; s++) {
          for (int c = 0; c < Card.maxVariables; c++) {
            cards.add(Card(number: n, shape: s, pattern: 0, color: c));
          }
        }
      }
      cards.shuffle(random);
      deck = Deck.fromCards(cards);
    } else {
      deck = Deck(random);
    }

    final List<Card?> cardsInPlay = [];
    while (cardsInPlay.length < Game.minCardsInPlay ||
           Game.getAValidTriple(cardsInPlay, {}) == null) {
      for (int i = 0; i < 3; i++) {
        if (!deck.isEmpty) {
          cardsInPlay.add(deck.getNextCard());
        }
      }
    }

    return ZenGame(
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
      isBeginner: isBeginner,
    );
  }

  @override
  void updateBoard(List<Card?> cardsInPlay, Deck deck, Set<Card> foundTriple, Random random) {
    super.updateBoard(cardsInPlay, deck, foundTriple, random);
    deck.readdCards(foundTriple);
    deck.shuffle(random);
  }

  @override
  String getGameTypeForAnalytics() {
    return gameTypeForAnalytics + (isBeginner ? '_beginner' : '');
  }
}
