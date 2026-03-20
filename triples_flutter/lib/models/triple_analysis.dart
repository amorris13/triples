import 'package:collection/collection.dart';
import '../models/card.dart';

class TripleAnalysis {
  final Set<Card> foundTriple;
  final int time; // cumulative time in milliseconds
  final int duration; // duration to find this triple
  final List<Set<Card>> allAvailable;
  final List<Card?> cardsInPlay;

  TripleAnalysis({
    required this.foundTriple,
    required this.time,
    required this.duration,
    required this.allAvailable,
    required this.cardsInPlay,
  });

  static bool isPropertySame(int v1, int v2, int v3) {
    return v1 == v2 && v2 == v3;
  }

  int getNumSameProperties() {
    int same = 0;
    for (var type in PropertyType.values) {
      if (isPropertySame(
          foundTriple.elementAt(0).getValue(type),
          foundTriple.elementAt(1).getValue(type),
          foundTriple.elementAt(2).getValue(type))) {
        same++;
      }
    }
    return same;
  }

  int getNumDifferentProperties() {
    return PropertyType.values.length - getNumSameProperties();
  }

  // Returns a string like "3s 1d" (3 same, 1 different)
  String getSummaryLabel() {
    int same = getNumSameProperties();
    int diff = PropertyType.values.length - same;
    return '${same}s ${diff}d';
  }
}
