package com.antsapps.triples.backend;

import com.google.common.collect.Lists;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;

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
    return (byte) ((card.mNumber << 6) + (card.mShape << 4) + (card.mPattern << 2) + card.mColor);
  }

  public static Card cardFromByte(byte b) {
    int number = (b >>> 6) & 3;
    int shape = (b >>> 4) & 3;
    int pattern = (b >>> 2) & 3;
    int color = b & 3;
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

  public static byte[] longListToByteArray(List<Long> longs) {
    ByteBuffer bb = ByteBuffer.allocate(longs.size() * 8);
    for (long l : longs) {
      bb.putLong(l);
    }
    return bb.array();
  }

  public static List<Long> longListFromByteArray(byte[] b) {
    if (b == null) {
      return Lists.newArrayList();
    }
    ByteBuffer bb = ByteBuffer.wrap(b);
    List<Long> longs = Lists.newArrayList();
    while (bb.hasRemaining()) {
      longs.add(bb.getLong());
    }
    return longs;
  }

  private Utils() {}
}
