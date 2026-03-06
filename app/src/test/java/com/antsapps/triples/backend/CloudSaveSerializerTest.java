package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Test;

public class CloudSaveSerializerTest {

  @Test
  public void testSerializationRoundTrip() throws IOException {
    List<ClassicGame> classicGames = new ArrayList<>();
    classicGames.add(new ClassicGame(1, 123, Collections.<Card>emptyList(),
        Collections.<Long>emptyList(), new Deck(Collections.<Card>emptyList()),
        60000, new Date(1000000), Game.GameState.COMPLETED, false));
    classicGames.add(new ClassicGame(2, 456, Collections.<Card>emptyList(),
        Collections.<Long>emptyList(), new Deck(Collections.<Card>emptyList()),
        120000, new Date(2000000), Game.GameState.COMPLETED, true));

    List<ArcadeGame> arcadeGames = new ArrayList<>();
    arcadeGames.add(new ArcadeGame(3, 789, Collections.<Card>emptyList(),
        Collections.<Long>emptyList(), new Deck(Collections.<Card>emptyList()),
        ArcadeGame.TIME_LIMIT_MS + 100, new Date(3000000), Game.GameState.COMPLETED, 15, false));

    byte[] serialized = CloudSaveSerializer.serialize(classicGames, arcadeGames);
    CloudSaveSerializer.CloudData deserialized = CloudSaveSerializer.deserialize(serialized);

    assertThat(deserialized.classicGames).hasSize(2);
    assertThat(deserialized.classicGames.get(0).getDateStarted().getTime()).isEqualTo(1000000);
    assertThat(deserialized.classicGames.get(0).getTimeElapsed()).isEqualTo(60000);
    assertThat(deserialized.classicGames.get(0).areHintsUsed()).isFalse();
    assertThat(deserialized.classicGames.get(1).getDateStarted().getTime()).isEqualTo(2000000);
    assertThat(deserialized.classicGames.get(1).getTimeElapsed()).isEqualTo(120000);
    assertThat(deserialized.classicGames.get(1).areHintsUsed()).isTrue();

    assertThat(deserialized.arcadeGames).hasSize(1);
    assertThat(deserialized.arcadeGames.get(0).getDateStarted().getTime()).isEqualTo(3000000);
    assertThat(deserialized.arcadeGames.get(0).getNumTriplesFound()).isEqualTo(15);
    assertThat(deserialized.arcadeGames.get(0).areHintsUsed()).isFalse();
  }

  @Test
  public void testEmptyData() throws IOException {
    byte[] serialized = CloudSaveSerializer.serialize(new ArrayList<ClassicGame>(), new ArrayList<ArcadeGame>());
    CloudSaveSerializer.CloudData deserialized = CloudSaveSerializer.deserialize(serialized);
    assertThat(deserialized.classicGames).isEmpty();
    assertThat(deserialized.arcadeGames).isEmpty();
  }
}
