// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:triples_flutter/main.dart';

void main() {
  testWidgets('Triples Home Screen Smoke Test', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const TriplesApp());

    // Verify that our app starts with 'Triples'.
    expect(find.text('Triples'), findsOneWidget);
    expect(find.text('Classic Mode'), findsOneWidget);
    expect(find.text('Arcade Mode'), findsOneWidget);
    expect(find.text('Zen Mode'), findsOneWidget);
    expect(find.text('Daily Mode'), findsOneWidget);

    // Navigate to Classic Mode
    await tester.tap(find.text('Classic Mode'));
    await tester.pumpAndSettle();

    // Verify we are in Classic Mode
    expect(find.text('Classic Mode'), findsOneWidget);
  });
}
