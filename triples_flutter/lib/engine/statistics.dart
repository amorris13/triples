import '../engine/game.dart';
import '../models/triple_analysis.dart';
import 'game_reconstruction.dart';

class Statistics {
  final List<Game> games;

  Statistics(this.games);

  int get numGames => games.length;

  int get numGamesWithAnalysis {
    return games.where((game) => game.foundTriples.isNotEmpty).length;
  }

  List<TripleAnalysis> getAnalysis() {
    final List<TripleAnalysis> analysis = [];
    for (var game in games) {
      analysis.addAll(game.reconstruct());
    }
    return analysis;
  }
}
