package com.antsapps.triples.backend;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

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

  public static byte[] serializeClassicCompleted(List<ClassicGame> games) {
    ClassicCompletedData.Builder builder = ClassicCompletedData.newBuilder();
    for (ClassicGame g : games) {
      builder.addClassicGames(ClassicGameSummary.newBuilder()
          .setDateStartedMillis(g.getDateStarted().getTime())
          .setTimeElapsedMillis(g.getTimeElapsed())
          .setHintsUsed(g.areHintsUsed())
          .build());
    }
    return builder.build().toByteArray();
  }

  public static List<ClassicGame> deserializeClassicCompleted(byte[] data) throws IOException {
    if (data == null || data.length == 0) return Collections.emptyList();
    ClassicCompletedData completedData = ClassicCompletedData.parseFrom(data);
    List<ClassicGame> games = new ArrayList<>(completedData.getClassicGamesCount());
    for (ClassicGameSummary summary : completedData.getClassicGamesList()) {
      games.add(new ClassicGame(-1, 0, Collections.<Card>emptyList(),
          Collections.<Long>emptyList(), new Deck(Collections.<Card>emptyList()),
          summary.getTimeElapsedMillis(), new Date(summary.getDateStartedMillis()),
          Game.GameState.COMPLETED, summary.getHintsUsed()));
    }
    return games;
  }

  public static byte[] serializeArcadeCompleted(List<ArcadeGame> games) {
    ArcadeCompletedData.Builder builder = ArcadeCompletedData.newBuilder();
    for (ArcadeGame g : games) {
      builder.addArcadeGames(ArcadeGameSummary.newBuilder()
          .setDateStartedMillis(g.getDateStarted().getTime())
          .setNumTriplesFound(g.getNumTriplesFound())
          .setHintsUsed(g.areHintsUsed())
          .build());
    }
    return builder.build().toByteArray();
  }

  public static List<ArcadeGame> deserializeArcadeCompleted(byte[] data) throws IOException {
    if (data == null || data.length == 0) return Collections.emptyList();
    ArcadeCompletedData completedData = ArcadeCompletedData.parseFrom(data);
    List<ArcadeGame> games = new ArrayList<>(completedData.getArcadeGamesCount());
    for (ArcadeGameSummary summary : completedData.getArcadeGamesList()) {
      games.add(new ArcadeGame(-1, 0, Collections.<Card>emptyList(),
          Collections.<Long>emptyList(), new Deck(Collections.<Card>emptyList()),
          ArcadeGame.TIME_LIMIT_MS + 100, new Date(summary.getDateStartedMillis()),
          Game.GameState.COMPLETED, summary.getNumTriplesFound(), summary.getHintsUsed()));
    }
    return games;
  }

  public static byte[] serializeDailyCompleted(List<DailyGame> games) {
    DailyCompletedData.Builder builder = DailyCompletedData.newBuilder();
    for (DailyGame g : games) {
      builder.addDailyGames(DailyGameSummary.newBuilder()
          .setDateStartedMillis(g.getDateStarted().getTime())
          .setTimeElapsedMillis(g.getTimeElapsed())
          .setHintsUsed(g.areHintsUsed())
          .setDateCompletedMillis(g.getDateCompleted() != null ? g.getDateCompleted().getTime() : 0)
          .build());
    }
    return builder.build().toByteArray();
  }

  public static List<DailyGame> deserializeDailyCompleted(byte[] data) throws IOException {
    if (data == null || data.length == 0) return Collections.emptyList();
    DailyCompletedData completedData = DailyCompletedData.parseFrom(data);
    List<DailyGame> games = new ArrayList<>(completedData.getDailyGamesCount());
    for (DailyGameSummary summary : completedData.getDailyGamesList()) {
      games.add(new DailyGame(-1, summary.getDateStartedMillis(),
          createFakeCardsInPlay(),
          Collections.<Long>emptyList(), new Deck(Collections.<Card>emptyList()),
          summary.getTimeElapsedMillis(), new Date(summary.getDateStartedMillis()),
          Game.GameState.COMPLETED, summary.getHintsUsed(), Collections.<Set<Card>>emptyList(),
          summary.getDateCompletedMillis() != 0 ? new Date(summary.getDateCompletedMillis()) : null));
    }
    return games;
  }

  public static byte[] serializeClassicGameState(ClassicGame game) {
    return ClassicGameState.newBuilder()
        .setId(game.getId())
        .setSeed(game.getRandomSeed())
        .setCardsInPlay(ByteString.copyFrom(game.getCardsInPlayAsByteArray()))
        .setTripleFindTimes(ByteString.copyFrom(Utils.longListToByteArray(game.getTripleFindTimes())))
        .setCardsInDeck(ByteString.copyFrom(game.getCardsInDeckAsByteArray()))
        .setTimeElapsedMillis(game.getTimeElapsed())
        .setDateStartedMillis(game.getDateStarted().getTime())
        .setGameState(toGameStateProto(game.getGameState()))
        .setHintsUsed(game.areHintsUsed())
        .build().toByteArray();
  }

  public static ClassicGame deserializeClassicGameState(byte[] data) throws IOException {
    ClassicGameState state = ClassicGameState.parseFrom(data);
    return new ClassicGame(state.getId(), state.getSeed(),
        Utils.cardListFromByteArray(state.getCardsInPlay().toByteArray()),
        Utils.longListFromByteArray(state.getTripleFindTimes().toByteArray()),
        Deck.fromByteArray(state.getCardsInDeck().toByteArray()),
        state.getTimeElapsedMillis(), new Date(state.getDateStartedMillis()),
        fromGameStateProto(state.getGameState()), state.getHintsUsed());
  }

  public static byte[] serializeArcadeGameState(ArcadeGame game) {
    return ArcadeGameState.newBuilder()
        .setId(game.getId())
        .setSeed(game.getRandomSeed())
        .setCardsInPlay(ByteString.copyFrom(game.getCardsInPlayAsByteArray()))
        .setTripleFindTimes(ByteString.copyFrom(Utils.longListToByteArray(game.getTripleFindTimes())))
        .setCardsInDeck(ByteString.copyFrom(game.getCardsInDeckAsByteArray()))
        .setTimeElapsedMillis(game.getTimeElapsed())
        .setDateStartedMillis(game.getDateStarted().getTime())
        .setGameState(toGameStateProto(game.getGameState()))
        .setNumTriplesFound(game.getNumTriplesFound())
        .setHintsUsed(game.areHintsUsed())
        .build().toByteArray();
  }

  public static ArcadeGame deserializeArcadeGameState(byte[] data) throws IOException {
    ArcadeGameState state = ArcadeGameState.parseFrom(data);
    return new ArcadeGame(state.getId(), state.getSeed(),
        Utils.cardListFromByteArray(state.getCardsInPlay().toByteArray()),
        Utils.longListFromByteArray(state.getTripleFindTimes().toByteArray()),
        Deck.fromByteArray(state.getCardsInDeck().toByteArray()),
        state.getTimeElapsedMillis(), new Date(state.getDateStartedMillis()),
        fromGameStateProto(state.getGameState()), state.getNumTriplesFound(), state.getHintsUsed());
  }

  public static byte[] serializeDailyGameState(DailyGame game) {
    DailyGameState.Builder builder = DailyGameState.newBuilder()
        .setId(game.getId())
        .setSeed(game.getRandomSeed())
        .setCardsInPlay(ByteString.copyFrom(game.getCardsInPlayAsByteArray()))
        .setTripleFindTimes(ByteString.copyFrom(Utils.longListToByteArray(game.getTripleFindTimes())))
        .setTimeElapsedMillis(game.getTimeElapsed())
        .setDateStartedMillis(game.getDateStarted().getTime())
        .setGameState(toGameStateProto(game.getGameState()))
        .setHintsUsed(game.areHintsUsed())
        .setFoundTriples(ByteString.copyFrom(Utils.triplesListToByteArray(game.getFoundTriples())));
    if (game.getDateCompleted() != null) {
      builder.setDateCompletedMillis(game.getDateCompleted().getTime());
    }
    return builder.build().toByteArray();
  }

  public static DailyGame deserializeDailyGameState(byte[] data) throws IOException {
    DailyGameState state = DailyGameState.parseFrom(data);
    return new DailyGame(state.getId(), state.getSeed(),
        Utils.cardListFromByteArray(state.getCardsInPlay().toByteArray()),
        Utils.longListFromByteArray(state.getTripleFindTimes().toByteArray()),
        new Deck(Collections.<Card>emptyList()),
        state.getTimeElapsedMillis(), new Date(state.getDateStartedMillis()),
        fromGameStateProto(state.getGameState()), state.getHintsUsed(),
        Utils.triplesListFromByteArray(state.getFoundTriples().toByteArray()),
        state.getDateCompletedMillis() != 0 ? new Date(state.getDateCompletedMillis()) : null);
  }

  private static GameStateProto toGameStateProto(Game.GameState state) {
    switch (state) {
      case STARTING: return GameStateProto.STARTING;
      case ACTIVE: return GameStateProto.ACTIVE;
      case PAUSED: return GameStateProto.PAUSED;
      case COMPLETED: return GameStateProto.COMPLETED;
      default: return GameStateProto.STARTING;
    }
  }

  private static Game.GameState fromGameStateProto(GameStateProto state) {
    switch (state) {
      case STARTING: return Game.GameState.STARTING;
      case ACTIVE: return Game.GameState.ACTIVE;
      case PAUSED: return Game.GameState.PAUSED;
      case COMPLETED: return Game.GameState.COMPLETED;
      default: return Game.GameState.STARTING;
    }
  }

  private static List<Card> createFakeCardsInPlay() {
    List<Card> cards = new ArrayList<>();
    for (int i = 0; i < 15; i++) {
        cards.add(new Card(i / 9, (i / 3) % 3, i % 3, 0));
    }
    return cards;
  }
}
