import 'dart:typed_data';
import '../models/card.dart';

class Utils {
  static int cardToByte(Card card) {
    return (card.number << 6) | (card.shape << 4) | (card.pattern << 2) | card.color;
  }

  static Card cardFromByte(int b) {
    int number = (b >> 6) & 3;
    int shape = (b >> 4) & 3;
    int pattern = (b >> 2) & 3;
    int color = b & 3;
    return Card(number: number, shape: shape, pattern: pattern, color: color);
  }

  static Uint8List cardListToByteArray(List<Card?> cards) {
    final bytes = Uint8List(cards.length);
    for (int i = 0; i < cards.length; i++) {
      if (cards[i] != null) {
        bytes[i] = cardToByte(cards[i]!);
      } else {
        bytes[i] = 0xFF; // Represent null as 0xFF
      }
    }
    return bytes;
  }

  static List<Card?> cardListFromByteArray(Uint8List b) {
    final List<Card?> cards = [];
    for (int i = 0; i < b.length; i++) {
      if (b[i] == 0xFF) {
        cards.add(null);
      } else {
        cards.add(cardFromByte(b[i]));
      }
    }
    return cards;
  }

  static Uint8List intListToByteArray(List<int> ints) {
    final b = ByteData(ints.length * 8);
    for (int i = 0; i < ints.length; i++) {
      b.setInt64(i * 8, ints[i]);
    }
    return b.buffer.asUint8List();
  }

  static List<int> intListFromByteArray(Uint8List b) {
    final bd = ByteData.sublistView(b);
    final List<int> ints = [];
    for (int i = 0; i < b.length ~/ 8; i++) {
      ints.add(bd.getInt64(i * 8));
    }
    return ints;
  }

  static Uint8List triplesListToByteArray(List<Set<Card>> triples) {
    final bytes = Uint8List(triples.length * 3);
    for (int i = 0; i < triples.length; i++) {
      final triple = triples[i].toList();
      for (int j = 0; j < 3; j++) {
        bytes[i * 3 + j] = cardToByte(triple[j]);
      }
    }
    return bytes;
  }

  static List<Set<Card>> triplesListFromByteArray(Uint8List b) {
    final List<Set<Card>> triples = [];
    for (int i = 0; i < b.length ~/ 3; i++) {
      final Set<Card> triple = {};
      for (int j = 0; j < 3; j++) {
        triple.add(cardFromByte(b[i * 3 + j]));
      }
      triples.add(triple);
    }
    return triples;
  }
}
