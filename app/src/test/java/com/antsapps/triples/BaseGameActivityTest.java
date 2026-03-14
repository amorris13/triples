package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class BaseGameActivityTest extends BaseRobolectricTest {

  @Test
  public void testFormatElapsedTime() {
    // New behavior with hundredths of a second
    assertThat(BaseGameActivity.formatElapsedTime(500)).isEqualTo("0:00.50");
    assertThat(BaseGameActivity.formatElapsedTime(1500)).isEqualTo("0:01.50");
    assertThat(BaseGameActivity.formatElapsedTime(61000)).isEqualTo("1:01.00");
    assertThat(BaseGameActivity.formatElapsedTime(61123)).isEqualTo("1:01.12");
    assertThat(BaseGameActivity.formatElapsedTime(3601000)).isEqualTo("1:00:01.00");
    assertThat(BaseGameActivity.formatElapsedTime(3661123)).isEqualTo("1:01:01.12");
  }
}
