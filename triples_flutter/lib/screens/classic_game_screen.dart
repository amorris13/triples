import 'package:flutter/material.dart';
import 'dart:math';
import '../engine/classic_game.dart';
import '../engine/game.dart';
import '../widgets/game_board.dart';
import '../widgets/triple_explanation.dart';
import '../models/card.dart';

class ClassicGameScreen extends StatefulWidget {
  const ClassicGameScreen({super.key});

  @override
  State<ClassicGameScreen> createState() => _ClassicGameScreenState();
}

class _ClassicGameScreenState extends State<ClassicGameScreen> {
  late ClassicGame _game;
  bool _showExplanation = false;
  Set<Card>? _lastTriple;

  @override
  void initState() {
    super.initState();
    _game = ClassicGame.createFromSeed(Random().nextInt(1000000));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Classic Mode'),
        actions: [
          IconButton(
            icon: const Icon(Icons.help_outline),
            onPressed: () => _game.addHint({}), // TODO: pass selected cards
          ),
          IconButton(
            icon: const Icon(Icons.info_outline),
            onPressed: () {
              setState(() {
                _showExplanation = !_showExplanation;
              });
            },
          ),
        ],
      ),
      body: Column(
        children: [
          // HUD (Timer, Remaining)
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('Time: ${_formatDuration(_game.timeElapsed)}'),
                Text('Deck: ${_game.deck.cardsRemaining}'),
              ],
            ),
          ),

          // Game Board
          Expanded(
            child: GameBoard(
              game: _game,
              onTripleSelected: (triple) {
                setState(() {
                  _game.commitTriple(triple);
                  _lastTriple = triple;
                });
                if (_game.gameState == GameState.completed) {
                  _showEndGameDialog();
                }
              },
            ),
          ),

          // Explanation Overlay
          if (_showExplanation && _lastTriple != null)
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: TripleExplanation(cards: _lastTriple!),
            ),
        ],
      ),
    );
  }

  String _formatDuration(int ms) {
    final seconds = (ms / 1000).truncate();
    final minutes = (seconds / 60).truncate();
    final remainingSeconds = seconds % 60;
    return '$minutes:${remainingSeconds.toString().padLeft(2, '0')}';
  }

  void _showEndGameDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Congratulations!'),
        content: Text('You completed the game in ${_formatDuration(_game.timeElapsed)}.'),
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
