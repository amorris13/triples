package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class CloudSaveSerializerTest {

  @Test
  public void testClassicCompletedRoundTrip() throws IOException {
    List<ClassicGame> classicGames = new ArrayList<>();
    classicGames.add(new ClassicGame(1, 123, Collections.<Card>emptyList(),
        Collections.<Long>emptyList(), new Deck(Collections.<Card>emptyList()),
        60000, new Date(1000000), Game.GameState.COMPLETED, false));
    classicGames.add(new ClassicGame(2, 456, Collections.<Card>emptyList(),
        Collections.<Long>emptyList(), new Deck(Collections.<Card>emptyList()),
        120000, new Date(2000000), Game.GameState.COMPLETED, true));

    byte[] serialized = CloudSaveSerializer.serializeClassicCompleted(classicGames);
    List<ClassicGame> deserialized = CloudSaveSerializer.deserializeClassicCompleted(serialized);

    assertThat(deserialized).hasSize(2);
    assertThat(deserialized.get(0).getTimeElapsed()).isEqualTo(60000);
    assertThat(deserialized.get(1).getTimeElapsed()).isEqualTo(120000);
    assertThat(deserialized.get(1).areHintsUsed()).isTrue();
  }

  @Test
  public void testArcadeCompletedRoundTrip() throws IOException {
    List<ArcadeGame> arcadeGames = new ArrayList<>();
    arcadeGames.add(
        new ArcadeGame(
            3,
            789,
            Collections.<Card>emptyList(),
            Collections.<Long>emptyList(),
            new Deck(Collections.<Card>emptyList()),
            ArcadeGame.TIME_LIMIT_MS + 100,
            new Date(3000000),
            Game.GameState.COMPLETED,
            15,
            false));

    byte[] serialized = CloudSaveSerializer.serializeArcadeCompleted(arcadeGames);
    List<ArcadeGame> deserialized = CloudSaveSerializer.deserializeArcadeCompleted(serialized);

    assertThat(deserialized).hasSize(1);
    assertThat(deserialized.get(0).getNumTriplesFound()).isEqualTo(15);
  }

  @Test
  public void testDailyCompletedRoundTrip() throws IOException {
    List<DailyGame> dailyGames = new ArrayList<>();
    List<Card> cards = Arrays.asList(
        new Card(0,0,0,0), new Card(1,1,1,1), new Card(2,2,2,2),
        new Card(0,1,1,2), new Card(1,2,2,0), new Card(2,0,0,1),
        new Card(0,2,2,1), new Card(1,0,0,2), new Card(2,1,1,0),
        new Card(0,0,1,1), new Card(1,1,2,2), new Card(2,2,0,0),
        new Card(0,1,2,0), new Card(1,2,0,1), new Card(2,0,1,2)
    );
    dailyGames.add(new DailyGame(1, 1000000,
        cards,
        Collections.<Long>emptyList(), new Deck(Collections.<Card>emptyList()),
        60000, new Date(1000000), Game.GameState.COMPLETED, false, Collections.<Set<Card>>emptyList(),
        new Date(2000000)));

    byte[] serialized = CloudSaveSerializer.serializeDailyCompleted(dailyGames);
    List<DailyGame> deserialized = CloudSaveSerializer.deserializeDailyCompleted(serialized);

    assertThat(deserialized).hasSize(1);
    assertThat(deserialized.get(0).getDateCompleted().getTime()).isEqualTo(2000000);
  }

  @Test
  public void testClassicGameStateRoundTrip() throws IOException {
    ClassicGame game = new ClassicGame(1, 123,
        Arrays.asList(new Card(0,0,0,0), new Card(1,1,1,1)),
        Arrays.asList(1000L, 2000L),
        new Deck(Arrays.asList(new Card(2,2,2,2))),
        5000L, new Date(1000000L), Game.GameState.ACTIVE, true);

    byte[] serialized = CloudSaveSerializer.serializeClassicGameState(game);
    ClassicGame deserialized = CloudSaveSerializer.deserializeClassicGameState(serialized);

    assertThat(deserialized.getRandomSeed()).isEqualTo(123);
    assertThat(deserialized.getCardsInPlay()).hasSize(2);
    assertThat(deserialized.getTripleFindTimes()).containsExactly(1000L, 2000L).inOrder();
    assertThat(deserialized.getCardsRemaining()).isEqualTo(3);
    assertThat(deserialized.getTimeElapsed()).isEqualTo(5000);
    assertThat(deserialized.getGameState()).isEqualTo(Game.GameState.ACTIVE);
    assertThat(deserialized.areHintsUsed()).isTrue();
  }

  @Test
  public void testArcadeGameStateRoundTrip() throws IOException {
    ArcadeGame game = new ArcadeGame(1, 123,
        Arrays.asList(new Card(0,0,0,0)),
        Arrays.asList(1000L),
        new Deck(Collections.<Card>emptyList()),
        5000L, new Date(1000000L), Game.GameState.PAUSED, 10, false);

    byte[] serialized = CloudSaveSerializer.serializeArcadeGameState(game);
    ArcadeGame deserialized = CloudSaveSerializer.deserializeArcadeGameState(serialized);

    assertThat(deserialized.getGameState()).isEqualTo(Game.GameState.PAUSED);
    assertThat(deserialized.getNumTriplesFound()).isEqualTo(10);
  }

  @Test
  public void testDailyGameStateRoundTrip() throws IOException {
    Set<Card> triple = com.google.common.collect.Sets.newHashSet(new Card(0,0,0,0), new Card(1,1,1,1), new Card(2,2,2,2));
    List<Card> cards = Arrays.asList(
        new Card(0,0,0,0), new Card(1,1,1,1), new Card(2,2,2,2),
        new Card(0,1,1,2), new Card(1,2,2,0), new Card(2,0,0,1),
        new Card(0,2,2,1), new Card(1,0,0,2), new Card(2,1,1,0),
        new Card(0,0,1,1), new Card(1,1,2,2), new Card(2,2,0,0),
        new Card(0,1,2,0), new Card(1,2,0,1), new Card(2,0,1,2)
    );
    DailyGame game = new DailyGame(1, 123,
        cards,
        Arrays.asList(1000L),
        new Deck(Collections.<Card>emptyList()),
        5000L, new Date(123), Game.GameState.ACTIVE, false,
        Collections.singletonList(triple),
        null);

    byte[] serialized = CloudSaveSerializer.serializeDailyGameState(game);
    DailyGame deserialized = CloudSaveSerializer.deserializeDailyGameState(serialized);

    assertThat(deserialized.getRandomSeed()).isEqualTo(123);
    assertThat(deserialized.getFoundTriples()).hasSize(1);
    assertThat(deserialized.getDateCompleted()).isNull();
  }
}
