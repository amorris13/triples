import 'package:flutter/material.dart';
import '../models/card.dart' as model;

class CardPainter extends CustomPainter {
  final model.Card card;
  final bool isSelected;
  final bool isHinted;

  CardPainter({
    required this.card,
    this.isSelected = false,
    this.isHinted = false,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = Colors.white
      ..style = PaintingStyle.fill;

    final rrect = RRect.fromRectAndRadius(
      Rect.fromLTWH(0, 0, size.width, size.height),
      const Radius.circular(8),
    );

    // Draw card background
    canvas.drawRRect(rrect, paint);

    // Draw border
    paint.style = PaintingStyle.stroke;
    paint.strokeWidth = 2;
    if (isSelected) {
      paint.color = Colors.blue;
      paint.strokeWidth = 4;
    } else if (isHinted) {
      paint.color = Colors.orange;
      paint.strokeWidth = 4;
    } else {
      paint.color = Colors.grey.shade300;
    }
    canvas.drawRRect(rrect, paint);

    // Draw symbols
    _drawSymbols(canvas, size);
  }

  void _drawSymbols(Canvas canvas, Size size) {
    final symbolCount = card.number + 1;
    final symbolHeight = size.height * 0.2;
    final symbolWidth = size.width * 0.6;
    final spacing = size.height * 0.05;

    final totalHeight = (symbolCount * symbolHeight) + ((symbolCount - 1) * spacing);
    double startY = (size.height - totalHeight) / 2;

    for (int i = 0; i < symbolCount; i++) {
      final rect = Rect.fromCenter(
        center: Offset(size.width / 2, startY + symbolHeight / 2),
        width: symbolWidth,
        height: symbolHeight,
      );
      _drawSymbol(canvas, rect);
      startY += symbolHeight + spacing;
    }
  }

  void _drawSymbol(Canvas canvas, Rect rect) {
    final color = _getColor(card.color);
    final paint = Paint()
      ..color = color
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2;

    Path path = _getShapePath(rect, card.shape);

    // Fill pattern
    if (card.pattern == 1) {
      // Shaded (Stripes)
      canvas.save();
      canvas.clipPath(path);
      final stripePaint = Paint()
        ..color = color.withOpacity(0.3)
        ..style = PaintingStyle.stroke
        ..strokeWidth = 1;
      for (double x = rect.left; x < rect.right; x += 4) {
        canvas.drawLine(Offset(x, rect.top), Offset(x, rect.bottom), stripePaint);
      }
      canvas.restore();
    } else if (card.pattern == 2) {
      // Solid
      canvas.drawPath(path, Paint()..color = color..style = PaintingStyle.fill);
    }

    // Outline
    canvas.drawPath(path, paint);
  }

  Path _getShapePath(Rect rect, int shape) {
    final path = Path();
    switch (shape) {
      case 0: // Rectangle/Square
        path.addRect(rect);
        break;
      case 1: // Oval/Circle
        path.addOval(rect);
        break;
      case 2: // Diamond (Replicating original Triples shape)
        path.moveTo(rect.center.dx, rect.top);
        path.lineTo(rect.right, rect.center.dy);
        path.lineTo(rect.center.dx, rect.bottom);
        path.lineTo(rect.left, rect.center.dy);
        path.close();
        break;
    }
    return path;
  }

  Color _getColor(int color) {
    switch (color) {
      case 0: return Colors.red;
      case 1: return Colors.green;
      case 2: return Colors.purple;
      default: return Colors.black;
    }
  }

  @override
  bool shouldRepaint(covariant CardPainter oldDelegate) {
    return oldDelegate.card != card ||
           oldDelegate.isSelected != isSelected ||
           oldDelegate.isHinted != isHinted;
  }
}
