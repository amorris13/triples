package com.antsapps.triples.backend;

import java.util.Date;
import java.util.List;

import android.util.Log;

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
    Log.i("Utils", "cardToByte.  Number = " + card.mNumber + "card.mNumber << 6 = " + (card.mNumber << 6));
    Log.i("Utils", "cardToByte.  Shape = " + card.mShape + "card.mShape << 4 = " + (card.mShape << 4));
    Log.i("Utils", "cardToByte.  Pattern = " + card.mPattern + "card.mPattern << 2 = " + (card.mPattern << 2));
    Log.i("Utils", "cardToByte.  Color = " + card.mColor + "card.mColor << 0 = " + (card.mColor << 0));
    return (byte) ((card.mNumber << 6) + (card.mShape << 4) + (card.mPattern << 2) + card.mColor);
  }

  public static Card cardFromByte(byte b) {
    Log.i("Utils", "cardFromByte. b = " + b);
    int number = (b >>> 6) & 3;
    int shape = (b >>> 4) & 3;
    int pattern = (b >>> 2) & 3;
    int color = b & 3;
    Log.i("Utils", "cardFromByte. number = " + number + ", shape = " + shape
        + ", pattern = " + pattern + ", color = " + color);
    return new Card(number, shape, pattern, color);
  }

  public static byte[] cardListToByteArray(List<Card> cards) {
    byte[] b = new byte[cards.size()];
    for (int i = 0; i < cards.size(); i++) {
      b[i] = cardToByte(cards.get(i));
      Log.i("Utils", "card = " + cards.get(i) + ", byte = " + b[i]);
    }
    return b;
  }

  public static List<Card> cardListFromByteArray(byte[] b) {
    List<Card> cards = Lists.newArrayList();
    for (int i = 0; i < b.length; i++) {
      cards.add(cardFromByte(b[i]));
      Log.i("Utils", "card = " + cards.get(i) + ", byte = " + b[i]);
    }
    return cards;
  }

  private Utils() {
  }
}
