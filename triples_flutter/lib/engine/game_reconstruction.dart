import 'dart:math';
import 'package:collection/collection.dart';
import '../models/card.dart';
import '../models/triple_analysis.dart';
import 'deck.dart';
import 'game.dart';

extension GameReconstruction on Game {
  List<TripleAnalysis> reconstruct() {
    final List<TripleAnalysis> analysisList = [];
    final Random random = Random(randomSeed);
    final Deck deck = Deck(random);

    // Initial board setup matches creation logic
    final List<Card?> cardsInPlay = [];
    while (cardsInPlay.whereNotNull().length < Game.minCardsInPlay ||
           Game.getAValidTriple(cardsInPlay, {}) == null) {
      for (int i = 0; i < 3; i++) {
        if (!deck.isEmpty) {
          cardsInPlay.add(deck.getNextCard());
        }
      }
    }

    int lastTime = 0;
    for (int i = 0; i < foundTriples.length; i++) {
      final foundTriple = foundTriples[i];
      final time = tripleFindTimes[i];
      final duration = time - lastTime;

      final allAvailable = Game.getAllValidTriples(cardsInPlay);
      analysisList.add(TripleAnalysis(
        foundTriple: foundTriple,
        time: time,
        duration: duration,
        allAvailable: allAvailable,
        cardsInPlay: List.from(cardsInPlay),
      ));

      updateBoard(cardsInPlay, deck, foundTriple, random);
      lastTime = time;
    }

    return analysisList;
  }
}
