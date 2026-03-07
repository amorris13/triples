package com.antsapps.triples;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.CloudSaveSerializer;
import com.antsapps.triples.backend.Game;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.GamesClientStatusCodes;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CloudSaveManager {
  private static final String TAG = "CloudSaveManager";
  private static final String SNAPSHOT_NAME = "TriplesSaveData";

  public static void saveToCloud(final Activity activity, final Application application) {
    Log.d(TAG, "saveToCloud");
    PlayGames.getGamesSignInClient(activity)
        .isAuthenticated()
        .addOnCompleteListener(
            task -> {
              boolean isAuthenticated = (task.isSuccessful() && task.getResult().isAuthenticated());
              if (isAuthenticated) {
                doSaveToCloud(activity, application);
              } else {
                Log.d(TAG, "saveToCloud: not authenticated, skipping");
              }
            });
  }

  private static void doSaveToCloud(final Activity activity, final Application application) {
    final SnapshotsClient snapshotsClient = PlayGames.getSnapshotsClient(activity);
    snapshotsClient.open(SNAPSHOT_NAME, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
        .addOnCompleteListener(new OnCompleteListener<SnapshotsClient.DataOrConflict<Snapshot>>() {
          @Override
          public void onComplete(@NonNull Task<SnapshotsClient.DataOrConflict<Snapshot>> task) {
            if (task.isSuccessful()) {
              Snapshot snapshot = task.getResult().getData();
              try {
                byte[] data = CloudSaveSerializer.serialize(
                    Lists.newArrayList(application.getCompletedClassicGames()),
                    Lists.newArrayList(application.getCompletedArcadeGames()));
                snapshot.getSnapshotContents().writeBytes(data);
                SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                    .setDescription("Triples Game Data")
                    .build();
                snapshotsClient.commitAndClose(snapshot, metadataChange);
                Log.d(TAG, "Cloud save successful");
              } catch (IOException e) {
                Log.e(TAG, "Error serializing cloud data", e);
                snapshotsClient.discardAndClose(snapshot);
              }
            } else {
              handleSnapshotError("save", task.getException());
            }
          }
        });
  }

  public static void syncWithCloud(final Activity activity, final Application application) {
    Log.d(TAG, "syncWithCloud");
    PlayGames.getGamesSignInClient(activity)
        .isAuthenticated()
        .addOnCompleteListener(
            task -> {
              boolean isAuthenticated = (task.isSuccessful() && task.getResult().isAuthenticated());
              if (isAuthenticated) {
                doSyncWithCloud(activity, application);
              } else {
                Log.d(TAG, "syncWithCloud: not authenticated, skipping");
              }
            });
  }

  private static void doSyncWithCloud(final Activity activity, final Application application) {
    final SnapshotsClient snapshotsClient = PlayGames.getSnapshotsClient(activity);
    snapshotsClient.open(SNAPSHOT_NAME, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
        .addOnCompleteListener(new OnCompleteListener<SnapshotsClient.DataOrConflict<Snapshot>>() {
          @Override
          public void onComplete(@NonNull Task<SnapshotsClient.DataOrConflict<Snapshot>> task) {
            if (task.isSuccessful()) {
              Snapshot snapshot = task.getResult().getData();
              try {
                byte[] data = snapshot.getSnapshotContents().readFully();
                CloudSaveSerializer.CloudData cloudData = CloudSaveSerializer.deserialize(data);
                boolean changed = merge(application, cloudData);
                if (changed) {
                  // Write merged data back to the current snapshot
                  byte[] mergedData = CloudSaveSerializer.serialize(
                      Lists.newArrayList(application.getCompletedClassicGames()),
                      Lists.newArrayList(application.getCompletedArcadeGames()));
                  snapshot.getSnapshotContents().writeBytes(mergedData);
                  SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                      .setDescription("Triples Game Data")
                      .build();
                  snapshotsClient.commitAndClose(snapshot, metadataChange);
                } else {
                  snapshotsClient.discardAndClose(snapshot);
                }
                Log.d(TAG, "Cloud sync successful. Changed: " + changed);
              } catch (IOException e) {
                Log.e(TAG, "Error reading/deserializing cloud data", e);
                snapshotsClient.discardAndClose(snapshot);
              }
            } else {
              handleSnapshotError("sync", task.getException());
            }
          }
        });
  }

  private static void handleSnapshotError(String operation, Exception exception) {
    if (exception instanceof ApiException) {
      ApiException apiException = (ApiException) exception;
      if (apiException.getStatusCode() == GamesClientStatusCodes.SIGN_IN_REQUIRED) {
        Log.w(TAG, "Cloud " + operation + " skipped: User is not fully signed in for Saved Games. " +
            "Ensure 'Saved Games' is enabled in the Google Play Console.");
        return;
      }
    }
    Log.e(TAG, "Error opening snapshot for " + operation, exception);
  }

  @VisibleForTesting
  static boolean merge(Application application, CloudSaveSerializer.CloudData cloudData) {
    boolean changed = false;

    // Classic Games
    Set<Long> localClassicDates = new HashSet<>();
    for (ClassicGame g : application.getCompletedClassicGames()) {
      localClassicDates.add(g.getDateStarted().getTime());
    }
    for (ClassicGame cloudGame : cloudData.classicGames) {
      if (!localClassicDates.contains(cloudGame.getDateStarted().getTime())) {
        application.addClassicGame(cloudGame);
        changed = true;
      }
    }

    // Arcade Games
    Set<Long> localArcadeDates = new HashSet<>();
    for (ArcadeGame g : application.getCompletedArcadeGames()) {
      localArcadeDates.add(g.getDateStarted().getTime());
    }
    for (ArcadeGame cloudGame : cloudData.arcadeGames) {
      if (!localArcadeDates.contains(cloudGame.getDateStarted().getTime())) {
        application.addArcadeGame(cloudGame);
        changed = true;
      }
    }

    return changed;
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
                snapshotsClient.open(SNAPSHOT_NAME, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
                    .addOnCompleteListener(openTask -> {
                      if (openTask.isSuccessful()) {
                        Snapshot snapshot = openTask.getResult().getData();
                        snapshotsClient.delete(snapshot.getMetadata())
                            .addOnCompleteListener(deleteTask -> {
                              if (deleteTask.isSuccessful()) {
                                Log.d(TAG, "Cloud data deleted successfully");
                              } else {
                                Log.e(TAG, "Error deleting cloud data", deleteTask.getException());
                              }
                            });
                      } else {
                        handleSnapshotError("delete", openTask.getException());
                      }
                    });
              } else {
                Log.d(TAG, "deleteFromCloud: not authenticated, skipping");
              }
            });
  }
}
