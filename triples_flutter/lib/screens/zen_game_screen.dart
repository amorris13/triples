import 'package:flutter/material.dart';
import 'dart:math';
import '../engine/zen_game.dart';
import '../engine/game.dart';
import '../widgets/game_board.dart';

class ZenGameScreen extends StatefulWidget {
  final bool isBeginner;
  const ZenGameScreen({super.key, this.isBeginner = false});

  @override
  State<ZenGameScreen> createState() => _ZenGameScreenState();
}

class _ZenGameScreenState extends State<ZenGameScreen> {
  late ZenGame _game;

  @override
  void initState() {
    super.initState();
    _game = ZenGame.createFromSeed(Random().nextInt(1000000), widget.isBeginner);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Zen Mode${widget.isBeginner ? " (Beginner)" : ""}')),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('Found: ${_game.numTriplesFound}'),
                ElevatedButton(
                  onPressed: () => setState(() => _game.shuffleCardsInPlay()),
                  child: const Text('Shuffle'),
                ),
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
              },
            ),
          ),
        ],
      ),
    );
  }
}
