import 'package:flutter/material.dart';
import '../engine/daily_game.dart';
import '../engine/game.dart';
import '../widgets/game_board.dart';

class DailyGameScreen extends StatefulWidget {
  const DailyGameScreen({super.key});

  @override
  State<DailyGameScreen> createState() => _DailyGameScreenState();
}

class _DailyGameScreenState extends State<DailyGameScreen> {
  late DailyGame _game;

  @override
  void initState() {
    super.initState();
    _game = DailyGame.createFromDay(Day.forToday());
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Daily Mode - ${_game.gameDay}')),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Text('Found: ${_game.foundTriples.length} / ${_game.allTriples.length}'),
          ),
          Expanded(
            child: GameBoard(
              game: _game,
              onTripleSelected: (triple) {
                setState(() {
                  _game.commitTriple(triple);
                });
                if (_game.gameState == GameState.completed) {
                  _showEndGameDialog();
                }
              },
            ),
          ),
        ],
      ),
    );
  }

  void _showEndGameDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Puzzle Complete!'),
        content: const Text('You found all the triples in today\'s puzzle.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('OK'),
          ),
        ],
      ),
    );
  }
}
