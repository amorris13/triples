package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;

import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.Deck;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.util.CsvUtil;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CsvExportTest {

    @Test
    public void testClassicCsvExportContent() {
        Date date = new Date(1700000000000L); // 2023-11-14 22:13:20
        ClassicGame game = new ClassicGame(1, 0, Lists.newArrayList(), Lists.newArrayList(), new Deck(Lists.newArrayList()), 60000, date, Game.GameState.COMPLETED, false);
        List<Game> games = Lists.newArrayList(game);

        String csv = CsvUtil.getClassicCsvContent(games);

        assertThat(csv).contains("Date,Time Elapsed (ms),Cards Remaining,Hints Used");
        assertThat(csv).contains("2023-11-14 22:13:20,60000,0,false");
    }

    @Test
    public void testArcadeCsvExportContent() {
        Date date = new Date(1700000000000L); // 2023-11-14 22:13:20
        ArcadeGame game = new ArcadeGame(1, 0, Lists.newArrayList(), Lists.newArrayList(), new Deck(new Random(0)), 60000, date, Game.GameState.COMPLETED, 15, true);
        List<Game> games = Lists.newArrayList(game);

        String csv = CsvUtil.getArcadeCsvContent(games);

        assertThat(csv).contains("Date,Time Elapsed (ms),Triples Found,Hints Used");
        assertThat(csv).contains("2023-11-14 22:13:20,60000,15,true");
    }
}
