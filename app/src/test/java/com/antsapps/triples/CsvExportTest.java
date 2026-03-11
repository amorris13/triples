package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;

import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.Deck;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.util.CsvUtil;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CsvExportTest {

  @Before
  // Set timezone to UTC
  public void setTimeZoneToUtc() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Test
  public void testClassicCsvExportContent() {
    Date date = new Date(1700000000000L); // 2023-11-14 22:13:20 UTC
    ClassicGame game =
        new ClassicGame(
            1,
            0,
            Lists.newArrayList(),
            Lists.newArrayList(),
            new Deck(Lists.newArrayList()),
            60000,
            date,
            Game.GameState.COMPLETED,
            false);
    List<Game> games = Lists.newArrayList(game);

    String csv = CsvUtil.getClassicCsvContent(games);

    assertThat(csv).contains("Date,Time Elapsed (ms),Cards Remaining,Hints Used");
    assertThat(csv).contains("2023-11-14 22:13:20,60000,0,false");
  }

  @Test
  public void testArcadeCsvExportContent() {
    Date date = new Date(1700000000000L); // 2023-11-14 22:13:20
    ArcadeGame game =
        new ArcadeGame(
            1,
            0,
            Lists.newArrayList(),
            Lists.newArrayList(),
            new Deck(new Random(0)),
            60000,
            date,
            Game.GameState.COMPLETED,
            15,
            true);
    List<Game> games = Lists.newArrayList(game);

    String csv = CsvUtil.getArcadeCsvContent(games);

    assertThat(csv).contains("Date,Time Elapsed (ms),Triples Found,Hints Used");
    assertThat(csv).contains("2023-11-14 22:13:20,60000,15,true");
  }

  @Test
  public void testDailyCsvExportContent() {
    DailyGame.Day day = new DailyGame.Day(2023, 11, 14);
    long seed = day.getSeed();
    Date date = day.getCalendar().getTime();
    Date dateCompleted = new Date(date.getTime() + 120000); // 2 minutes later

    DailyGame game =
        new DailyGame(
            1,
            seed,
            Lists.<Card>newArrayList(
                new Card(0, 0, 0, 0),
                new Card(0, 0, 0, 1),
                new Card(0, 0, 0, 2),
                new Card(0, 0, 1, 0),
                new Card(0, 0, 1, 1),
                new Card(0, 0, 1, 2),
                new Card(0, 0, 2, 0),
                new Card(0, 0, 2, 1),
                new Card(0, 0, 2, 2),
                new Card(0, 1, 0, 0),
                new Card(0, 1, 0, 1),
                new Card(0, 1, 0, 2),
                new Card(0, 1, 1, 0),
                new Card(0, 1, 1, 1),
                new Card(0, 1, 1, 2)),
            Lists.<Long>newArrayList(),
            new Deck(Lists.<Card>newArrayList()),
            120000,
            date,
            day,
            Game.GameState.COMPLETED,
            false,
            Lists.<Set<Card>>newArrayList(),
            dateCompleted);
    List<DailyGame> games = Lists.newArrayList(game);

    String csv = CsvUtil.getDailyCsvContent(games);

    assertThat(csv)
        .contains("Puzzle Date,Date Completed,Time Elapsed (ms),Triples Found,Hints Used");
    assertThat(csv).contains("2023-11-14,2023-11-14 00:02:00,120000,0/14,false");
  }
}
