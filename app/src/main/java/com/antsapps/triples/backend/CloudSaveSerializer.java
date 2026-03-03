package com.antsapps.triples.backend;

import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Serializes and deserializes game data for cloud storage in a compact binary format.
 */
public class CloudSaveSerializer {
  private static final String TAG = "CloudSaveSerializer";
  private static final int MAGIC_NUMBER = 0x54524950; // 'TRIP'
  private static final int CURRENT_VERSION = 1;

  public static byte[] serialize(List<ClassicGame> classicGames, List<ArcadeGame> arcadeGames)
      throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);

    dos.writeInt(MAGIC_NUMBER);
    dos.writeInt(CURRENT_VERSION);

    // Filter to only completed games for statistics
    List<ClassicGame> completedClassic = new ArrayList<>();
    for (ClassicGame g : classicGames) {
      if (g.getGameState() == Game.GameState.COMPLETED) {
        completedClassic.add(g);
      }
    }
    dos.writeInt(completedClassic.size());
    for (ClassicGame g : completedClassic) {
      dos.writeLong(g.getDateStarted().getTime());
      dos.writeLong(g.getTimeElapsed());
      dos.writeBoolean(g.areHintsUsed());
    }

    List<ArcadeGame> completedArcade = new ArrayList<>();
    for (ArcadeGame g : arcadeGames) {
      if (g.getGameState() == Game.GameState.COMPLETED) {
        completedArcade.add(g);
      }
    }
    dos.writeInt(completedArcade.size());
    for (ArcadeGame g : completedArcade) {
      dos.writeLong(g.getDateStarted().getTime());
      dos.writeInt(g.getNumTriplesFound());
      dos.writeBoolean(g.areHintsUsed());
    }

    dos.flush();
    return baos.toByteArray();
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

    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));

    int magic = dis.readInt();
    if (magic != MAGIC_NUMBER) {
      throw new IOException("Invalid magic number in cloud data");
    }

    int version = dis.readInt();
    if (version > CURRENT_VERSION) {
      Log.w(TAG, "Cloud data version " + version + " is newer than supported " + CURRENT_VERSION);
    }

    int numClassic = dis.readInt();
    List<ClassicGame> classicGames = new ArrayList<>(numClassic);
    for (int i = 0; i < numClassic; i++) {
      long dateMillis = dis.readLong();
      long timeElapsed = dis.readLong();
      boolean hintsUsed = dis.readBoolean();
      // We create a skeleton game object for statistics.
      // Seed and other data are not restored for cloud-synced games.
      classicGames.add(new ClassicGame(
          -1, 0, Collections.<Card>emptyList(), Collections.<Long>emptyList(),
          new Deck(Collections.<Card>emptyList()), timeElapsed, new Date(dateMillis),
          Game.GameState.COMPLETED, hintsUsed));
    }

    int numArcade = dis.readInt();
    List<ArcadeGame> arcadeGames = new ArrayList<>(numArcade);
    for (int i = 0; i < numArcade; i++) {
      long dateMillis = dis.readLong();
      int numTriplesFound = dis.readInt();
      boolean hintsUsed = dis.readBoolean();
      arcadeGames.add(new ArcadeGame(
          -1, 0, Collections.<Card>emptyList(), Collections.<Long>emptyList(),
          new Deck(Collections.<Card>emptyList()), ArcadeGame.TIME_LIMIT_MS + 100,
          new Date(dateMillis), Game.GameState.COMPLETED, numTriplesFound, hintsUsed));
    }

    return new CloudData(classicGames, arcadeGames);
  }
}
