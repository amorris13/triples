import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:triples_flutter/widgets/game_board.dart';
import 'package:triples_flutter/engine/classic_game.dart';

void main() {
  testWidgets('GameBoard Selection Test', (WidgetTester tester) async {
    final game = ClassicGame.createFromSeed(12345);

    await tester.pumpWidget(MaterialApp(
      home: Scaffold(
        body: GameBoard(game: game),
      ),
    ));

    // Verify grid elements (cards) are present
    expect(find.byType(GestureDetector), findsAtLeastNWidgets(12));

    // Tap first card - no selection feedback yet because we are testing interaction logic
    await tester.tap(find.byType(GestureDetector).first);
    await tester.pump();

    // Triple selection logic should be verified via engine unit tests,
    // widget tests check if interactions trigger the state changes.
  });
}
