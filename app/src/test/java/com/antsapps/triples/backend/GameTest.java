package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GameTest {

  @Test
  public void isValidTriple_allSame_isValid() {
    Card c1 = new Card(0, 0, 0, 0);
    Card c2 = new Card(1, 1, 1, 1);
    Card c3 = new Card(2, 2, 2, 2);
    assertThat(Game.isValidTriple(c1, c2, c3)).isTrue();
  }

  @Test
  public void isValidTriple_allDifferent_isValid() {
    // Each property is either all same or all different.
    // Number: 0, 0, 0 (all same)
    // Shape: 0, 1, 2 (all different)
    // Pattern: 1, 1, 1 (all same)
    // Color: 2, 1, 0 (all different)
    Card c1 = new Card(0, 0, 1, 2);
    Card c2 = new Card(0, 1, 1, 1);
    Card c3 = new Card(0, 2, 1, 0);
    assertThat(Game.isValidTriple(c1, c2, c3)).isTrue();
  }

  @Test
  public void isValidTriple_twoSameOneDifferent_isInvalid() {
    // Color: 0, 0, 1 (two same, one different) - INVALID
    Card c1 = new Card(0, 0, 0, 0);
    Card c2 = new Card(1, 1, 1, 0);
    Card c3 = new Card(2, 2, 2, 1);
    assertThat(Game.isValidTriple(c1, c2, c3)).isFalse();
  }

  @Test
  public void isValidTriple_duplicateCards_throwsException() {
    Card c1 = new Card(0, 0, 0, 0);
    Card c2 = c1;
    Card c3 = new Card(1, 1, 1, 1);
    assertThrows(IllegalArgumentException.class, () -> Game.isValidTriple(c1, c2, c3));
  }

  @Test
  public void getAValidTriple_findsTriple() {
    Card c1 = new Card(0, 0, 0, 0);
    Card c2 = new Card(1, 1, 1, 1);
    Card c3 = new Card(2, 2, 2, 2);
    Card c4 = new Card(0, 0, 0, 1);
    List<Card> cards = ImmutableList.of(c1, c2, c3, c4);

    Set<Card> triple = Game.getAValidTriple(cards, Sets.<Card>newHashSet());
    assertThat(triple).containsExactly(c1, c2, c3);
  }

  @Test
  public void getAValidTriple_noTriple_returnsNull() {
    Card c1 = new Card(0, 0, 0, 0);
    Card c2 = new Card(0, 0, 0, 1);
    Card c3 = new Card(0, 0, 0, 2);
    Card c4 = new Card(1, 1, 1, 1);
    // (c1, c2, c3) is a triple.
    // Let's pick 3 that are NOT a triple.
    List<Card> cards = ImmutableList.of(c1, c2, c4);

    Set<Card> triple = Game.getAValidTriple(cards, Sets.<Card>newHashSet());
    assertThat(triple).isNull();
  }

  @Test
  public void getValidTriplePositions_returnsCorrectIndices() {
    Card c1 = new Card(0, 0, 0, 0);
    Card c2 = new Card(0, 0, 0, 1);
    Card c3 = new Card(1, 1, 1, 1);
    Card c4 = new Card(2, 2, 2, 2);
    // (c1, c3, c4) is a triple. Indices: 0, 2, 3
    List<Card> cards = ImmutableList.of(c1, c2, c3, c4);

    List<Integer> positions = Game.getValidTriplePositions(cards);
    assertThat(positions).containsExactly(0, 2, 3);
  }
}
