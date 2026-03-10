package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;

import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DailyGameSeedTest {

  @Test
  public void getStartOfDaySeed_differentTimezones_sameSeed() {
    // 2024-05-20 in different timezones
    Calendar sydney = Calendar.getInstance(TimeZone.getTimeZone("Australia/Sydney"));
    sydney.set(2024, Calendar.MAY, 20, 10, 0, 0);

    Calendar la = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
    la.set(2024, Calendar.MAY, 20, 22, 0, 0);

    long seedSydney = DailyGame.getStartOfDaySeed(sydney);
    long seedLA = DailyGame.getStartOfDaySeed(la);

    assertThat(seedSydney).isEqualTo(seedLA);

    // Verify it's 2024-05-20 00:00:00 UTC
    Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    utc.setTimeInMillis(seedSydney);
    assertThat(utc.get(Calendar.YEAR)).isEqualTo(2024);
    assertThat(utc.get(Calendar.MONTH)).isEqualTo(Calendar.MAY);
    assertThat(utc.get(Calendar.DAY_OF_MONTH)).isEqualTo(20);
    assertThat(utc.get(Calendar.HOUR_OF_DAY)).isEqualTo(0);
    assertThat(utc.get(Calendar.MINUTE)).isEqualTo(0);
    assertThat(utc.get(Calendar.SECOND)).isEqualTo(0);
    assertThat(utc.get(Calendar.MILLISECOND)).isEqualTo(0);
  }

  @Test
  public void streakBuffer_within48Hours_isTrue() {
    long seed = 1000000L;
    long completedOnTime = seed + DailyGame.STREAK_BUFFER_MILLIS - 1; // 47h 59m 59s
    long completedLate = seed + DailyGame.STREAK_BUFFER_MILLIS + 1; // 48h 0m 1s

    assertThat(completedOnTime - seed < DailyGame.STREAK_BUFFER_MILLIS).isTrue();
    assertThat(completedLate - seed < DailyGame.STREAK_BUFFER_MILLIS).isFalse();
  }
}
