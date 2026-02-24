package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CardTest {

  @Test
  public void constructor_validArguments_setsFields() {
    Card card = new Card(1, 2, 0, 1);
    assertThat(card.mNumber).isEqualTo(1);
    assertThat(card.mShape).isEqualTo(2);
    assertThat(card.mPattern).isEqualTo(0);
    assertThat(card.mColor).isEqualTo(1);
  }

  @Test
  public void constructor_invalidNumber_throwsException() {
    try {
      new Card(3, 0, 0, 0);
      fail("Should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      assertThat(expected).hasMessageThat().contains("number = 3");
    }
  }

  @Test
  public void constructor_negativeNumber_throwsException() {
    try {
      new Card(-1, 0, 0, 0);
      fail("Should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      assertThat(expected).hasMessageThat().contains("number = -1");
    }
  }

  @Test
  public void equals_sameFields_isTrue() {
    Card card1 = new Card(1, 1, 1, 1);
    Card card2 = new Card(1, 1, 1, 1);
    assertThat(card1).isEqualTo(card2);
  }

  @Test
  public void equals_differentFields_isFalse() {
    Card card1 = new Card(1, 1, 1, 1);
    Card card2 = new Card(1, 1, 1, 2);
    assertThat(card1).isNotEqualTo(card2);
  }

  @Test
  public void hashCode_sameFields_isSame() {
    Card card1 = new Card(1, 1, 1, 1);
    Card card2 = new Card(1, 1, 1, 1);
    assertThat(card1.hashCode()).isEqualTo(card2.hashCode());
  }

  @Test
  public void hashCode_differentFields_isDifferent() {
    Card card1 = new Card(1, 1, 1, 1);
    Card card2 = new Card(1, 1, 1, 2);
    assertThat(card1.hashCode()).isNotEqualTo(card2.hashCode());
  }

  @Test
  public void toString_containsFields() {
    Card card = new Card(1, 2, 0, 1);
    String toString = card.toString();
    assertThat(toString).contains("mNumber=1");
    assertThat(toString).contains("mShape=2");
    assertThat(toString).contains("mPattern=0");
    assertThat(toString).contains("mColor=1");
  }
}
