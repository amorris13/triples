import 'package:flutter_test/flutter_test.dart';
import 'package:triples_flutter/engine/game.dart';
import 'package:triples_flutter/models/card.dart';

void main() {
  test('isValidTriple returns true for a valid triple', () {
    final c1 = Card(number: 0, shape: 0, pattern: 0, color: 0);
    final c2 = Card(number: 1, shape: 1, pattern: 1, color: 1);
    final c3 = Card(number: 2, shape: 2, pattern: 2, color: 2);
    expect(Game.isValidTriple({c1, c2, c3}), isTrue);
  });

  test('isValidTriple returns false for an invalid triple', () {
    final c1 = Card(number: 0, shape: 0, pattern: 0, color: 0);
    final c2 = Card(number: 1, shape: 1, pattern: 1, color: 1);
    final c3 = Card(number: 2, shape: 2, pattern: 2, color: 1); // Invalid color
    expect(Game.isValidTriple({c1, c2, c3}), isFalse);
  });

  test('isValidTriple returns true for another valid triple (same properties)', () {
    final c1 = Card(number: 1, shape: 0, pattern: 2, color: 0);
    final c2 = Card(number: 1, shape: 1, pattern: 2, color: 1);
    final c3 = Card(number: 1, shape: 2, pattern: 2, color: 2);
    // Number: 1, 1, 1 (all same)
    // Shape: 0, 1, 2 (all different)
    // Pattern: 2, 2, 2 (all same)
    // Color: 0, 1, 2 (all different)
    expect(Game.isValidTriple({c1, c2, c3}), isTrue);
  });
}
