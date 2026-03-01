package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class UtilsTest {

  @Test
  public void cardToByte_and_cardFromByte_areReversible() {
    for (int n = 0; n < 3; n++) {
      for (int s = 0; s < 3; s++) {
        for (int p = 0; p < 3; p++) {
          for (int c = 0; c < 3; c++) {
            Card card = new Card(n, s, p, c);
            byte b = Utils.cardToByte(card);
            Card cardFromByte = Utils.cardFromByte(b);
            assertThat(cardFromByte).isEqualTo(card);
          }
        }
      }
    }
  }

  @Test
  public void cardListToByteArray_and_cardListFromByteArray_areReversible() {
    List<Card> cards = Lists.newArrayList(
        new Card(0, 0, 0, 0),
        new Card(1, 1, 1, 1),
        new Card(2, 2, 2, 2)
    );
    byte[] b = Utils.cardListToByteArray(cards);
    List<Card> cardsFromByte = Utils.cardListFromByteArray(b);
    assertThat(cardsFromByte).isEqualTo(cards);
  }

  @Test
  public void longListToByteArray_and_longListFromByteArray_areReversible() {
    List<Long> longs = Lists.newArrayList(1L, 2L, 3L, 4L, 5L);
    byte[] b = Utils.longListToByteArray(longs);
    List<Long> longsFromByte = Utils.longListFromByteArray(b);
    assertThat(longsFromByte).isEqualTo(longs);
  }

  @Test
  public void longListFromByteArray_null_returnsEmptyList() {
    assertThat(Utils.longListFromByteArray(null)).isEmpty();
  }

  @Test
  public void compareTo_handlesNullDates() {
    Date now = new Date();
    Date later = new Date(now.getTime() + 1000);

    // Both dates null, use IDs
    assertThat(Utils.compareTo(null, 1, null, 2)).isLessThan(0);
    assertThat(Utils.compareTo(null, 2, null, 1)).isGreaterThan(0);
    assertThat(Utils.compareTo(null, 1, null, 1)).isEqualTo(0);

    // One date null
    assertThat(Utils.compareTo(null, 1, now, 1)).isLessThan(0);
    assertThat(Utils.compareTo(now, 1, null, 1)).isGreaterThan(0);

    // Both dates non-null
    assertThat(Utils.compareTo(now, 1, later, 1)).isLessThan(0);
    assertThat(Utils.compareTo(later, 1, now, 1)).isGreaterThan(0);
    assertThat(Utils.compareTo(now, 1, now, 2)).isEqualTo(0); // compareTo for Date returns 0
  }
}
