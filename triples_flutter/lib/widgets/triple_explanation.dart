import 'package:flutter/material.dart';
import '../models/card.dart';
import '../models/triple_analysis.dart';
import 'card_painter.dart';

class TripleExplanation extends StatelessWidget {
  final Set<Card> cards;

  const TripleExplanation({super.key, required this.cards});

  @override
  Widget build(BuildContext context) {
    if (cards.length != 3) return const SizedBox.shrink();

    final cardList = cards.toList();
    final analysis = TripleAnalysis(
      foundTriple: cards,
      time: 0,
      duration: 0,
      allAvailable: [],
      cardsInPlay: [],
    );

    return Column(
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: cardList.map((card) =>
            SizedBox(
              width: 60,
              height: 80,
              child: CustomPaint(
                painter: CardPainter(card: card),
              ),
            )
          ).toList(),
        ),
        const SizedBox(height: 16),
        Text('Summary: ${analysis.getSummaryLabel()}'),
        const Divider(),
        _buildPropertyRow('Number', PropertyType.number, cardList),
        _buildPropertyRow('Shape', PropertyType.shape, cardList),
        _buildPropertyRow('Pattern', PropertyType.pattern, cardList),
        _buildPropertyRow('Color', PropertyType.color, cardList),
      ],
    );
  }

  Widget _buildPropertyRow(String label, PropertyType type, List<Card> cards) {
    final v1 = cards[0].getValue(type);
    final v2 = cards[1].getValue(type);
    final v3 = cards[2].getValue(type);
    final isSame = TripleAnalysis.isPropertySame(v1, v2, v3);

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label),
          Text(isSame ? 'Same ($v1)' : 'Different ($v1, $v2, $v3)'),
        ],
      ),
    );
  }
}
