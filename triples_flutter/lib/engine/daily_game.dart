import 'dart:math';
import 'package:collection/collection.dart';
import '../models/card.dart';
import 'deck.dart';
import 'game.dart';

class Day implements Comparable<Day> {
  final int year;
  final int month;
  final int day;

  Day(this.year, this.month, this.day) {
    assert(year > 0 && year <= 9999);
    assert(month >= 1 && month <= 12);
    assert(day >= 1 && day <= 31);
  }

  int get seed => year * 10000 + month * 100 + day;

  static Day fromString(String string) {
    int dateInt = int.parse(string);
    return Day(dateInt ~/ 10000, (dateInt ~/ 100) % 100, dateInt % 100);
  }

  static Day forToday() {
    final now = DateTime.now();
    return Day(now.year, now.month, now.day);
  }

  @override
  String toString() {
    return '${year.toString().padLeft(4, '0')}${month.toString().padLeft(2, '0')}${day.toString().padLeft(2, '0')}';
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is Day &&
          runtimeType == other.runtimeType &&
          year == other.year &&
          month == other.month &&
          day == other.day;

  @override
  int get hashCode => year.hashCode ^ month.hashCode ^ day.hashCode;

  @override
  int compareTo(Day other) {
    if (year != other.year) return year - other.year;
    if (month != other.month) return month - other.month;
    return day - other.day;
  }
}

class DailyGame extends Game {
  static const String gameTypeForAnalytics = 'daily';
  final List<Set<Card>> allTriples;
  final Day gameDay;
  DateTime? dateCompleted;

  DailyGame({
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
    required this.allTriples,
    required this.gameDay,
    this.dateCompleted,
  });

  factory DailyGame.createFromDay(Day day) {
    int seed = day.seed;
    Random random = Random(seed);
    List<Card?> cardsInPlay = [];
    List<Set<Card>> allTriples;

    while (true) {
      cardsInPlay.clear();
      final deck = Deck(random);
      for (int i = 0; i < 15; i++) {
        cardsInPlay.add(deck.getNextCard());
      }
      allTriples = Game.getAllValidTriples(cardsInPlay);
      if (allTriples.length >= 4) {
        break;
      }
    }

    return DailyGame(
      id: -1,
      randomSeed: seed,
      cardsInPlay: cardsInPlay,
      tripleFindTimes: [],
      foundTriples: [],
      deck: Deck.fromCards([]),
      timeElapsed: 0,
      dateStarted: DateTime.now(),
      gameState: GameState.starting,
      hintsUsed: false,
      allTriples: allTriples,
      gameDay: day,
    );
  }

  @override
  void commitTriple(Set<Card> triple) {
    if (gameState == GameState.completed) return;
    if (!cardsInPlay.containsAll(triple)) return;
    if (!Game.isValidTriple(triple)) return;
    if (foundTriples.any((ft) => const SetEquality().equals(ft, triple))) return;

    _recordFoundTriple(triple);
    checkIfFinished();
  }

  @override
  void updateBoard(List<Card?> cardsInPlay, Deck deck, Set<Card> foundTriple, Random random) {
    // Puzzle mode board doesn't change
  }

  @override
  void checkIfFinished() {
    if (foundTriples.length == allTriples.length) {
      dateCompleted = DateTime.now();
      finish();
    }
  }

  @override
  String getGameTypeForAnalytics() {
    return gameTypeForAnalytics;
  }
}
