package com.antsapps.triples.backend;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Set;

public class TripleAnalysis {
  public final Set<Card> foundTriple;
  public final long findTime;
  public final long duration;
  public final List<Set<Card>> allAvailableTriples;
  public final List<Card> boardState;

  public TripleAnalysis(
      Set<Card> foundTriple,
      long findTime,
      long duration,
      List<Set<Card>> allAvailableTriples,
      List<Card> boardState) {
    this.foundTriple = foundTriple;
    this.findTime = findTime;
    this.duration = duration;
    this.allAvailableTriples = allAvailableTriples;
    this.boardState = ImmutableList.copyOf(boardState);
  }

  public int getNumDifferentProperties() {
    return getNumDifferentProperties(foundTriple);
  }

  public static int getNumDifferentProperties(Set<Card> triple) {
    int diffCount = 0;
    Card[] cards = triple.toArray(new Card[0]);
    if (!isPropertySame(cards[0].mNumber, cards[1].mNumber, cards[2].mNumber)) diffCount++;
    if (!isPropertySame(cards[0].mShape, cards[1].mShape, cards[2].mShape)) diffCount++;
    if (!isPropertySame(cards[0].mPattern, cards[1].mPattern, cards[2].mPattern)) diffCount++;
    if (!isPropertySame(cards[0].mColor, cards[1].mColor, cards[2].mColor)) diffCount++;
    return diffCount;
  }

  private static boolean isPropertySame(int v1, int v2, int v3) {
    return v1 == v2 && v2 == v3;
  }
}
