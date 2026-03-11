package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Lists;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DailyGameSeedTest {

  @Test
  public void getDailySeed_differentTimezones_sameSeed() {
    // 2024-05-20 in different timezones
    Calendar sydney = Calendar.getInstance(TimeZone.getTimeZone("Australia/Sydney"));
    sydney.set(2024, Calendar.MAY, 20, 10, 0, 0);

    Calendar la = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
    la.set(2024, Calendar.MAY, 20, 22, 0, 0);

    long seedSydney = DailyGame.getDailySeed(sydney);
    long seedLA = DailyGame.getDailySeed(la);

    assertThat(seedSydney).isEqualTo(seedLA);
    assertThat(seedSydney).isEqualTo(20240520L);

    // Verify it's 2024-05-20 00:00:00 UTC when converted back
    long timestamp = DailyGame.getTimestampFromSeed(seedSydney);
    Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    utc.setTimeInMillis(timestamp);
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
    long seed = 20240520L;
    long timestamp = DailyGame.getTimestampFromSeed(seed);
    List<Card> cards =
        Lists.newArrayList(new Card(0, 0, 0, 0), new Card(1, 1, 1, 1), new Card(2, 2, 2, 2));

    DailyGame gameOnTime =
        new DailyGame(
            1,
            seed,
            cards,
            Lists.newArrayList(),
            null,
            0,
            new Date(timestamp),
            Game.GameState.COMPLETED,
            false,
            Lists.newArrayList(),
            new Date(timestamp + DailyGame.STREAK_BUFFER_MILLIS - 1));
    DailyGame gameLate =
        new DailyGame(
            2,
            seed,
            cards,
            Lists.newArrayList(),
            null,
            0,
            new Date(timestamp),
            Game.GameState.COMPLETED,
            false,
            Lists.newArrayList(),
            new Date(timestamp + DailyGame.STREAK_BUFFER_MILLIS + 1));

    assertThat(gameOnTime.isCompletedOnTime()).isTrue();
    assertThat(gameLate.isCompletedOnTime()).isFalse();
  }

  @Test
  public void migrateOldSeed_isCorrect() {
    // 2024-05-20 10:00:00 UTC
    long oldSeed = 1716208800000L;
    assertThat(DailyGame.isOldSeed(oldSeed)).isTrue();
    assertThat(DailyGame.migrateOldSeed(oldSeed)).isEqualTo(20240520L);
    assertThat(DailyGame.isOldSeed(20240520L)).isFalse();
  }

  @Test
  public void getTimestampFromSeed_handlesOldAndNew() {
    long oldSeed = 1716208800000L;
    long newSeed = 20240520L;

    assertThat(DailyGame.getTimestampFromSeed(oldSeed)).isEqualTo(oldSeed);
    assertThat(DailyGame.getTimestampFromSeed(newSeed))
        .isEqualTo(1716163200000L); // 2024-05-20 00:00:00 UTC
  }

  @Test
  public void getDailySeed_isUtcConsistent() {
    // Midnight in Sydney is 2pm previous day in UTC (2024-05-19 14:00 UTC)
    Calendar sydneyMidnight = Calendar.getInstance(TimeZone.getTimeZone("Australia/Sydney"));
    sydneyMidnight.set(2024, Calendar.MAY, 20, 0, 0, 0);
    sydneyMidnight.set(Calendar.MILLISECOND, 0);

    // Midnight in LA is 7am same day in UTC (2024-05-20 07:00 UTC)
    Calendar laMidnight = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
    laMidnight.set(2024, Calendar.MAY, 20, 0, 0, 0);
    laMidnight.set(Calendar.MILLISECOND, 0);

    // Seed should depend on UTC day
    assertThat(DailyGame.getDailySeed(sydneyMidnight.getTimeInMillis())).isEqualTo(20240519L);
    assertThat(DailyGame.getDailySeed(laMidnight.getTimeInMillis())).isEqualTo(20240520L);

    // At any given absolute time, everyone gets the same seed
    long absoluteTime = 1716163200000L; // 2024-05-20 00:00:00 UTC
    assertThat(DailyGame.getDailySeed(absoluteTime)).isEqualTo(20240520L);
  }
}
