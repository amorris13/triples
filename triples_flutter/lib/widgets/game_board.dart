import 'package:flutter/material.dart';
import '../engine/game.dart';
import '../models/card.dart';
import 'card_painter.dart';

class GameBoard extends StatefulWidget {
  final Game game;
  final Function(Set<Card>)? onTripleSelected;

  const GameBoard({
    super.key,
    required this.game,
    this.onTripleSelected,
  });

  @override
  State<GameBoard> createState() => _GameBoardState();
}

class _GameBoardState extends State<GameBoard> {
  final Set<Card> _selectedCards = {};

  @override
  Widget build(BuildContext context) {
    return GridView.builder(
      padding: const EdgeInsets.all(8),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3,
        mainAxisSpacing: 8,
        crossAxisSpacing: 8,
        childAspectRatio: 0.7,
      ),
      itemCount: widget.game.cardsInPlay.length,
      itemBuilder: (context, index) {
        final card = widget.game.cardsInPlay[index];
        if (card == null) return const SizedBox.shrink();

        final isSelected = _selectedCards.contains(card);
        final isHinted = widget.game.hintedCards.contains(card);

        return GestureDetector(
          onTap: () => _onCardTapped(card),
          child: CustomPaint(
            painter: CardPainter(
              card: card,
              isSelected: isSelected,
              isHinted: isHinted,
            ),
          ),
        );
      },
    );
  }

  void _onCardTapped(Card card) {
    setState(() {
      if (_selectedCards.contains(card)) {
        _selectedCards.remove(card);
      } else if (_selectedCards.length < 3) {
        _selectedCards.add(card);
      }

      if (_selectedCards.length == 3) {
        if (Game.isValidTriple(_selectedCards)) {
          widget.onTripleSelected?.call(Set.from(_selectedCards));
          _selectedCards.clear();
        } else {
          // TODO: Shake/feedback for invalid selection
          _selectedCards.clear();
        }
      }
    });
  }
}
