package com.antsapps.triples;

import android.app.Activity;
import android.util.Log;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.CloudSaveSerializer;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.Game;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.GamesClientStatusCodes;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataBuffer;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CloudSaveManager {
  private static final String TAG = "CloudSaveManager";

  private static final String CLASSIC_COMPLETED = "ClassicCompleted";
  private static final String ARCADE_COMPLETED = "ArcadeCompleted";
  private static final String DAILY_COMPLETED = "DailyCompleted";
  private static final String CLASSIC_CURRENT = "ClassicCurrent";
  private static final String ARCADE_CURRENT = "ArcadeCurrent";
  private static final String DAILY_CURRENT_PREFIX = "DailyInProgress_";

  public static void saveAll(final Activity activity, final Application application) {
    Log.d(TAG, "saveAll");
    PlayGames.getGamesSignInClient(activity)
        .isAuthenticated()
        .addOnCompleteListener(
            task -> {
              boolean isAuthenticated = (task.isSuccessful() && task.getResult().isAuthenticated());
              if (isAuthenticated) {
                doSaveAll(activity, application);
              } else {
                Log.d(TAG, "saveAll: not authenticated, skipping");
              }
            });
  }

  private static void doSaveAll(final Activity activity, final Application application) {
    // Completed games
    saveToCloud(
        activity,
        CLASSIC_COMPLETED,
        CloudSaveSerializer.serializeClassicCompleted(
            Lists.newArrayList(application.getCompletedClassicGames())));
    saveToCloud(
        activity,
        ARCADE_COMPLETED,
        CloudSaveSerializer.serializeArcadeCompleted(
            Lists.newArrayList(application.getCompletedArcadeGames())));
    saveToCloud(
        activity,
        DAILY_COMPLETED,
        CloudSaveSerializer.serializeDailyCompleted(
            Lists.newArrayList(application.getCompletedDailyGames())));

    // Current games
    ClassicGame classicCurrent = Iterables.getFirst(application.getCurrentClassicGames(), null);
    if (classicCurrent != null) {
      saveToCloud(
          activity, CLASSIC_CURRENT, CloudSaveSerializer.serializeClassicGameState(classicCurrent));
    }

    ArcadeGame arcadeCurrent = Iterables.getFirst(application.getCurrentArcadeGames(), null);
    if (arcadeCurrent != null) {
      saveToCloud(
          activity, ARCADE_CURRENT, CloudSaveSerializer.serializeArcadeGameState(arcadeCurrent));
    }

    for (DailyGame dailyCurrent : application.getCurrentDailyGames()) {
      saveToCloud(
          activity,
          DAILY_CURRENT_PREFIX + dailyCurrent.getGameDay().toString(),
          CloudSaveSerializer.serializeDailyGameState(dailyCurrent));
    }
  }

  private static void saveToCloud(
      final Activity activity, final String snapshotName, final byte[] data) {
    final SnapshotsClient snapshotsClient = PlayGames.getSnapshotsClient(activity);
    snapshotsClient
        .open(snapshotName, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                Snapshot snapshot = task.getResult().getData();
                snapshot.getSnapshotContents().writeBytes(data);
                SnapshotMetadataChange metadataChange =
                    new SnapshotMetadataChange.Builder()
                        .setDescription("Triples Game Data: " + snapshotName)
                        .build();
                snapshotsClient.commitAndClose(snapshot, metadataChange);
                Log.d(TAG, "Cloud save successful: " + snapshotName);
              } else {
                handleSnapshotError("save " + snapshotName, task.getException());
              }
            });
  }

  public static Task<Void> syncAll(final Activity activity, final Application application) {
    Log.d(TAG, "syncAll");
    TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
    PlayGames.getGamesSignInClient(activity)
        .isAuthenticated()
        .addOnCompleteListener(
            task -> {
              boolean isAuthenticated = (task.isSuccessful() && task.getResult().isAuthenticated());
              if (isAuthenticated) {
                doSyncAll(activity, application)
                    .addOnCompleteListener(
                        t -> {
                          if (t.isSuccessful()) {
                            tcs.setResult(null);
                          } else {
                            tcs.setException(t.getException());
                          }
                        });
              } else {
                Log.d(TAG, "syncAll: not authenticated, skipping");
                tcs.setResult(null);
              }
            });
    return tcs.getTask();
  }

  private interface Merger {
    boolean merge(byte[] data) throws IOException;

    byte[] serializeMerged();
  }

  private static Task<Void> doSyncAll(final Activity activity, final Application application) {
    final SnapshotsClient snapshotsClient = PlayGames.getSnapshotsClient(activity);
    List<Task<?>> tasks = new ArrayList<>();

    // Sync individual files
    tasks.add(
        syncFile(
            snapshotsClient,
            CLASSIC_COMPLETED,
            new Merger() {
              @Override
              public boolean merge(byte[] data) throws IOException {
                return application.mergeClassicCompleted(
                    CloudSaveSerializer.deserializeClassicCompleted(data));
              }

              @Override
              public byte[] serializeMerged() {
                return CloudSaveSerializer.serializeClassicCompleted(
                    Lists.newArrayList(application.getCompletedClassicGames()));
              }
            }));

    tasks.add(
        syncFile(
            snapshotsClient,
            ARCADE_COMPLETED,
            new Merger() {
              @Override
              public boolean merge(byte[] data) throws IOException {
                return application.mergeArcadeCompleted(
                    CloudSaveSerializer.deserializeArcadeCompleted(data));
              }

              @Override
              public byte[] serializeMerged() {
                return CloudSaveSerializer.serializeArcadeCompleted(
                    Lists.newArrayList(application.getCompletedArcadeGames()));
              }
            }));

    tasks.add(
        syncFile(
            snapshotsClient,
            DAILY_COMPLETED,
            new Merger() {
              @Override
              public boolean merge(byte[] data) throws IOException {
                return application.mergeDailyCompleted(
                    CloudSaveSerializer.deserializeDailyCompleted(data));
              }

              @Override
              public byte[] serializeMerged() {
                return CloudSaveSerializer.serializeDailyCompleted(
                    Lists.newArrayList(application.getCompletedDailyGames()));
              }
            }));

    tasks.add(
        syncFile(
            snapshotsClient,
            CLASSIC_CURRENT,
            new Merger() {
              @Override
              public boolean merge(byte[] data) throws IOException {
                return application.mergeClassicCurrent(
                    CloudSaveSerializer.deserializeClassicGameState(data));
              }

              @Override
              public byte[] serializeMerged() {
                ClassicGame g = Iterables.getFirst(application.getCurrentClassicGames(), null);
                return g != null ? CloudSaveSerializer.serializeClassicGameState(g) : null;
              }
            }));

    tasks.add(
        syncFile(
            snapshotsClient,
            ARCADE_CURRENT,
            new Merger() {
              @Override
              public boolean merge(byte[] data) throws IOException {
                return application.mergeArcadeCurrent(
                    CloudSaveSerializer.deserializeArcadeGameState(data));
              }

              @Override
              public byte[] serializeMerged() {
                ArcadeGame g = Iterables.getFirst(application.getCurrentArcadeGames(), null);
                return g != null ? CloudSaveSerializer.serializeArcadeGameState(g) : null;
              }
            }));

    // Sync all DailyCurrent files
    TaskCompletionSource<Void> dailyTcs = new TaskCompletionSource<>();
    tasks.add(dailyTcs.getTask());

    snapshotsClient
        .load(true)
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                SnapshotMetadataBuffer buffer = task.getResult().get();
                List<Task<?>> dailyTasks = new ArrayList<>();
                try {
                  for (SnapshotMetadata metadata : buffer) {
                    String name = metadata.getUniqueName();
                    if (name.startsWith(DAILY_CURRENT_PREFIX)) {
                      dailyTasks.add(
                          syncFile(
                              snapshotsClient,
                              name,
                              new Merger() {
                                @Override
                                public boolean merge(byte[] data) throws IOException {
                                  boolean changed =
                                      application.mergeDailyCurrent(
                                          CloudSaveSerializer.deserializeDailyGameState(data));
                                  // Cleanup if completed
                                  DailyGame.Day gameDay =
                                      DailyGame.Day.fromString(
                                          name.substring(DAILY_CURRENT_PREFIX.length()));
                                  DailyGame g = application.getDailyGameByGameDay(gameDay);
                                  if (g != null && g.getGameState() == Game.GameState.COMPLETED) {
                                    snapshotsClient.delete(metadata);
                                  }
                                  return changed;
                                }

                                @Override
                                public byte[] serializeMerged() {
                                  DailyGame.Day gameDay =
                                      DailyGame.Day.fromString(
                                          name.substring(DAILY_CURRENT_PREFIX.length()));
                                  DailyGame g = application.getDailyGameByGameDay(gameDay);
                                  return (g != null && g.getGameState() != Game.GameState.COMPLETED)
                                      ? CloudSaveSerializer.serializeDailyGameState(g)
                                      : null;
                                }
                              }));
                    }
                  }
                  Tasks.whenAll(dailyTasks).addOnCompleteListener(t -> dailyTcs.setResult(null));
                } finally {
                  buffer.release();
                }
              } else {
                dailyTcs.setResult(null);
              }
            });

    return Tasks.whenAll(tasks);
  }

  private static Task<Void> syncFile(
      final SnapshotsClient snapshotsClient, final String snapshotName, final Merger merger) {
    TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
    snapshotsClient
        .open(snapshotName, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                Snapshot snapshot = task.getResult().getData();
                try {
                  byte[] data = snapshot.getSnapshotContents().readFully();
                  boolean changed = merger.merge(data);
                  if (changed) {
                    byte[] mergedData = merger.serializeMerged();
                    if (mergedData != null) {
                      snapshot.getSnapshotContents().writeBytes(mergedData);
                      SnapshotMetadataChange metadataChange =
                          new SnapshotMetadataChange.Builder()
                              .setDescription("Triples Game Data: " + snapshotName)
                              .build();
                      snapshotsClient.commitAndClose(snapshot, metadataChange);
                    } else {
                      snapshotsClient.discardAndClose(snapshot);
                    }
                  } else {
                    snapshotsClient.discardAndClose(snapshot);
                  }
                  Log.d(TAG, "Cloud sync successful: " + snapshotName + ". Changed: " + changed);
                  tcs.setResult(null);
                } catch (IOException e) {
                  Log.e(TAG, "Error reading/deserializing cloud data for " + snapshotName, e);
                  snapshotsClient.discardAndClose(snapshot);
                  tcs.setResult(null);
                }
              } else {
                handleSnapshotError("sync " + snapshotName, task.getException());
                tcs.setResult(null);
              }
            });
    return tcs.getTask();
  }

  private static void handleSnapshotError(String operation, Exception exception) {
    if (exception instanceof ApiException) {
      ApiException apiException = (ApiException) exception;
      if (apiException.getStatusCode() == GamesClientStatusCodes.SIGN_IN_REQUIRED) {
        Log.w(TAG, "Cloud " + operation + " skipped: User is not fully signed in for Saved Games.");
        return;
      }
    }
    Log.e(TAG, "Error opening snapshot for " + operation, exception);
  }

  public static void deleteFromCloud(final Activity activity) {
    Log.d(TAG, "deleteFromCloud");
    PlayGames.getGamesSignInClient(activity)
        .isAuthenticated()
        .addOnCompleteListener(
            task -> {
              boolean isAuthenticated = (task.isSuccessful() && task.getResult().isAuthenticated());
              if (isAuthenticated) {
                SnapshotsClient snapshotsClient = PlayGames.getSnapshotsClient(activity);
                snapshotsClient
                    .load(true)
                    .addOnCompleteListener(
                        loadTask -> {
                          if (loadTask.isSuccessful()) {
                            SnapshotMetadataBuffer buffer = loadTask.getResult().get();
                            try {
                              for (SnapshotMetadata metadata : buffer) {
                                snapshotsClient.delete(metadata);
                              }
                              Log.d(TAG, "All cloud data deleted successfully");
                            } finally {
                              buffer.release();
                            }
                          } else {
                            Log.e(
                                TAG,
                                "Error loading snapshots for deletion",
                                loadTask.getException());
                          }
                        });
              } else {
                Log.d(TAG, "deleteFromCloud: not authenticated, skipping");
              }
            });
  }
}
