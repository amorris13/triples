package com.antsapps.triples.backend;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;

public class Utils {

  public static long compareTo(Date date1, long id1, Date date2, long id2) {
    if (date1 == null) {
      if (date2 == null) {
        return (int) (id1 - id2);
      } else {
        return -1;
      }
    } else {
      if (date2 == null) {
        return 1;
      } else {
        return date1.compareTo(date2);
      }
    }
  }

  public static byte cardToByte(Card card) {
    return (byte) (card.mNumber << 6 + card.mShape << 4 + card.mPattern << 2 + card.mColor);
  }

  public static Card cardFromByte(byte b) {
    int number = b >> 6 & 4;
    int shape = b >> 4 & 4;
    int pattern = b >> 2 & 4;
    int color = b & 4;
    return new Card(number, shape, pattern, color);
  }

  public static byte[] cardListToByteArray(List<Card> cards) {
    byte[] b = new byte[cards.size()];
    for (int i = 0; i < cards.size(); i++) {
      b[i] = cardToByte(cards.get(i));
    }
    return b;
  }

  public static List<Card> cardListFromByteArray(byte[] b) {
    List<Card> cards = Lists.newArrayList();
    for (int i = 0; i < b.length; i++) {
      cards.add(cardFromByte(b[i]));
    }
    return cards;
  }

  private Utils() {
  }
}
