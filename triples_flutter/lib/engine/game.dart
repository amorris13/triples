import 'dart:math';
import 'package:collection/collection.dart';
import '../models/card.dart';
import '../models/triple_analysis.dart';
import 'deck.dart';

enum GameState {
  starting,
  active,
  paused,
  completed,
}

abstract class Game {
  static const int minCardsInPlay = 12;

  final int id;
  final int randomSeed;
  final Random random;
  final List<Card?> cardsInPlay;
  final List<int> tripleFindTimes;
  final List<Set<Card>> foundTriples;
  final Deck deck;
  final DateTime dateStarted;

  GameState _gameState;
  int _numTriplesFound = 0;
  int _timeElapsed = 0; // in milliseconds
  bool _hintsUsed = false;
  final Set<Card> _hintedCards = {};

  Game({
    required this.id,
    required this.randomSeed,
    required List<Card?> cardsInPlay,
    required List<int> tripleFindTimes,
    required List<Set<Card>> foundTriples,
    required this.deck,
    required int timeElapsed,
    required this.dateStarted,
    required GameState gameState,
    required bool hintsUsed,
  })  : random = Random(randomSeed),
        cardsInPlay = List.from(cardsInPlay),
        tripleFindTimes = List.from(tripleFindTimes),
        foundTriples = List.from(foundTriples),
        _timeElapsed = timeElapsed,
        _gameState = gameState,
        _hintsUsed = hintsUsed,
        _numTriplesFound = foundTriples.length;

  GameState get gameState => _gameState;
  int get numTriplesFound => _numTriplesFound;
  int get timeElapsed => _timeElapsed;
  bool get hintsUsed => _hintsUsed;
  Set<Card> get hintedCards => Set.unmodifiable(_hintedCards);

  void resume() {
    if (_gameState == GameState.completed) return;
    _gameState = GameState.active;
  }

  void pause() {
    if (_gameState == GameState.completed) return;
    _gameState = GameState.paused;
  }

  void shuffleCardsInPlay() {
    cardsInPlay.shuffle(random);
  }

  static bool isValidTriple(Iterable<Card> cards) {
    if (cards.length != 3) return false;
    final list = cards.toList();
    if (list[0] == list[1] || list[0] == list[2] || list[1] == list[2]) return false;

    for (var type in PropertyType.values) {
      int sum = list[0].getValue(type) + list[1].getValue(type) + list[2].getValue(type);
      if (sum % 3 != 0) return false;
    }
    return true;
  }

  void commitTriple(Set<Card> triple) {
    if (_gameState == GameState.completed) return;
    if (!cardsInPlay.toSet().containsAll(triple)) return;
    if (!isValidTriple(triple)) return;

    _recordFoundTriple(triple);

    _hintedCards.clear();

    updateBoard(cardsInPlay, deck, triple, random);

    checkIfFinished();
  }

  void _recordFoundTriple(Set<Card> triple) {
    _numTriplesFound++;
    tripleFindTimes.add(_timeElapsed);
    foundTriples.add(triple);
  }

  void updateBoard(List<Card?> cardsInPlay, Deck deck, Set<Card> foundTriple, Random random) {
    for (var card in foundTriple) {
      int index = cardsInPlay.indexOf(card);
      if (index != -1) {
        cardsInPlay[index] = null;
      }
    }

    // Common replenishment logic
    while (cardsInPlay.whereNotNull().length < minCardsInPlay && !deck.isEmpty) {
      for (int i = 0; i < 3; i++) {
        int nullIdx = cardsInPlay.indexOf(null);
        if (nullIdx != -1) {
          cardsInPlay[nullIdx] = deck.getNextCard();
        } else {
          // If no null slots, just add
          cardsInPlay.add(deck.getNextCard());
        }
      }
    }

    // Compacting (removing nulls and keeping length)
    cardsInPlay.removeWhere((card) => card == null);

    // Extra cards if no triples
    while (getAValidTriple(cardsInPlay, {}) == null && !deck.isEmpty) {
      for (int i = 0; i < 3; i++) {
        cardsInPlay.add(deck.getNextCard());
      }
    }
  }

  void checkIfFinished() {}

  void finish() {
    _gameState = GameState.completed;
  }

  static Set<Card>? getAValidTriple(List<Card?> cardsInPlay, Set<Card> includingCards) {
    final notNullCards = cardsInPlay.whereNotNull().toList();
    if (includingCards.length > 3) return null;
    if (includingCards.length == 3) {
      return isValidTriple(includingCards) ? Set.from(includingCards) : null;
    }

    for (var card in notNullCards) {
      if (!includingCards.contains(card)) {
        final nextIncluding = Set<Card>.from(includingCards)..add(card);
        final triple = getAValidTriple(notNullCards, nextIncluding);
        if (triple != null) return triple;
      }
    }
    return null;
  }

  static List<Set<Card>> getAllValidTriples(List<Card?> cards) {
    final distinctCards = cards.whereNotNull().toSet().toList();
    final List<Set<Card>> validTriples = [];

    for (int i = 0; i < distinctCards.length; i++) {
      for (int j = i + 1; j < distinctCards.length; j++) {
        for (int k = j + 1; k < distinctCards.length; k++) {
          final triple = {distinctCards[i], distinctCards[j], distinctCards[k]};
          if (isValidTriple(triple)) {
            validTriples.add(triple);
          }
        }
      }
    }
    return validTriples;
  }

  bool addHint(Set<Card> selectedCards) {
    _hintsUsed = true;
    if (_hintedCards.length == 3) return false;

    Set<Card>? targetTriple = _findValidTripleIncludingSelected(selectedCards);
    if (targetTriple == null) return false;

    // Hint at least one card not already hinted
    for (var card in targetTriple) {
      if (!_hintedCards.contains(card)) {
        _hintedCards.add(card);
        return true;
      }
    }
    return false;
  }

  Set<Card>? _findValidTripleIncludingSelected(Set<Card> selectedCards) {
    // Try to find a triple that includes as many of the selected cards as possible
    for (int i = selectedCards.length; i > 0; i--) {
      // Simplification: just find any triple including at least one selected if possible
      // In actual implementation we might want more complex logic like the original Java
      final triple = getAValidTriple(cardsInPlay, selectedCards.take(i).toSet());
      if (triple != null) return triple;
    }
    return getAValidTriple(cardsInPlay, {});
  }

  String getGameTypeForAnalytics();
}
