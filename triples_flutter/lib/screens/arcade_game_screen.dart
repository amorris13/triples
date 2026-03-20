import 'package:flutter/material.dart';
import 'dart:math';
import '../engine/arcade_game.dart';
import '../engine/game.dart';
import '../widgets/game_board.dart';

class ArcadeGameScreen extends StatefulWidget {
  const ArcadeGameScreen({super.key});

  @override
  State<ArcadeGameScreen> createState() => _ArcadeGameScreenState();
}

class _ArcadeGameScreenState extends State<ArcadeGameScreen> {
  late ArcadeGame _game;

  @override
  void initState() {
    super.initState();
    _game = ArcadeGame.createFromSeed(Random().nextInt(1000000));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Arcade Mode')),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('Time Left: ${_formatDuration(ArcadeGame.timeLimitMs - _game.timeElapsed)}'),
                Text('Triples Found: ${_game.numTriplesFound}'),
              ],
            ),
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

  String _formatDuration(int ms) {
    if (ms < 0) ms = 0;
    final seconds = (ms / 1000).truncate();
    return '$seconds s';
  }

  void _showEndGameDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Game Over!'),
        content: Text('You found ${_game.numTriplesFound} triples.'),
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
