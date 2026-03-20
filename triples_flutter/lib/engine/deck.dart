import 'dart:math';
import '../models/card.dart';

class Deck {
  final List<Card> _cards = [];

  Deck(Random random) {
    for (int n = 0; n < Card.maxVariables; n++) {
      for (int s = 0; s < Card.maxVariables; s++) {
        for (int p = 0; p < Card.maxVariables; p++) {
          for (int c = 0; c < Card.maxVariables; c++) {
            _cards.add(Card(number: n, shape: s, pattern: p, color: c));
          }
        }
      }
    }
    _cards.shuffle(random);
  }

  Deck.fromCards(List<Card> cards) {
    _cards.addAll(cards);
  }

  bool get isEmpty => _cards.isEmpty;

  int get cardsRemaining => _cards.length;

  Card getNextCard() {
    if (_cards.isEmpty) return Card(number: 0, shape: 0, pattern: 0, color: 0); // Should not happen if checked
    return _cards.removeAt(0);
  }

  void readdCards(Iterable<Card> cards) {
    _cards.addAll(cards);
  }

  void shuffle(Random random) {
    _cards.shuffle(random);
  }

  List<Card> get cards => List.unmodifiable(_cards);
}
