package com.antsapps.triples.backend;

import android.util.Log;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Serializes and deserializes game data for cloud storage using Protocol Buffers.
 */
public class CloudSaveSerializer {
  private static final String TAG = "CloudSaveSerializer";

  public static byte[] serialize(List<ClassicGame> classicGames, List<ArcadeGame> arcadeGames)
      throws IOException {
    CloudSaveData.Builder dataBuilder = CloudSaveData.newBuilder();

    for (ClassicGame g : classicGames) {
      if (g.getGameState() == Game.GameState.COMPLETED) {
        dataBuilder.addClassicGames(ClassicGameSummary.newBuilder()
            .setDateStartedMillis(g.getDateStarted().getTime())
            .setTimeElapsedMillis(g.getTimeElapsed())
            .setHintsUsed(g.areHintsUsed())
            .build());
      }
    }

    for (ArcadeGame g : arcadeGames) {
      if (g.getGameState() == Game.GameState.COMPLETED) {
        dataBuilder.addArcadeGames(ArcadeGameSummary.newBuilder()
            .setDateStartedMillis(g.getDateStarted().getTime())
            .setNumTriplesFound(g.getNumTriplesFound())
            .setHintsUsed(g.areHintsUsed())
            .build());
      }
    }

    return dataBuilder.build().toByteArray();
  }

  public static class CloudData {
    public final List<ClassicGame> classicGames;
    public final List<ArcadeGame> arcadeGames;

    public CloudData(List<ClassicGame> classicGames, List<ArcadeGame> arcadeGames) {
      this.classicGames = classicGames;
      this.arcadeGames = arcadeGames;
    }
  }

  public static CloudData deserialize(byte[] data) throws IOException {
    if (data == null || data.length == 0) {
      return new CloudData(new ArrayList<ClassicGame>(), new ArrayList<ArcadeGame>());
    }

    CloudSaveData cloudSaveData;
    try {
      cloudSaveData = CloudSaveData.parseFrom(data);
    } catch (InvalidProtocolBufferException e) {
      throw new IOException("Failed to parse protobuf data", e);
    }

    List<ClassicGame> classicGames = new ArrayList<>(cloudSaveData.getClassicGamesCount());
    for (ClassicGameSummary summary : cloudSaveData.getClassicGamesList()) {
      classicGames.add(new ClassicGame(
          -1, 0, Collections.<Card>emptyList(), Collections.<Long>emptyList(),
          new Deck(Collections.<Card>emptyList()), summary.getTimeElapsedMillis(),
          new Date(summary.getDateStartedMillis()),
          Game.GameState.COMPLETED, summary.getHintsUsed()));
    }

    List<ArcadeGame> arcadeGames = new ArrayList<>(cloudSaveData.getArcadeGamesCount());
    for (ArcadeGameSummary summary : cloudSaveData.getArcadeGamesList()) {
      arcadeGames.add(new ArcadeGame(
          -1, 0, Collections.<Card>emptyList(), Collections.<Long>emptyList(),
          new Deck(Collections.<Card>emptyList()), ArcadeGame.TIME_LIMIT_MS + 100,
          new Date(summary.getDateStartedMillis()), Game.GameState.COMPLETED,
          summary.getNumTriplesFound(), summary.getHintsUsed()));
    }

    return new CloudData(classicGames, arcadeGames);
  }
}
