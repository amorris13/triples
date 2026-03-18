package com.antsapps.triples.backend;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.antsapps.triples.CloudSaveManager;
import com.antsapps.triples.backend.Game.GameState;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Application extends OnStateChangedReporter {
  private static final String TAG = "Application";

  private static Application INSTANCE;

  // Should remain sorted
  private final List<ClassicGame> mClassicGames = Lists.newArrayList();
  private final List<ArcadeGame> mArcadeGames = Lists.newArrayList();
  private final List<DailyGame> mDailyGames = Lists.newArrayList();

  private final MutableLiveData<List<ClassicGame>> mClassicGamesLiveData =
      new MutableLiveData<>(mClassicGames);
  private final MutableLiveData<List<ArcadeGame>> mArcadeGamesLiveData =
      new MutableLiveData<>(mArcadeGames);
  private final MutableLiveData<List<DailyGame>> mDailyGamesLiveData =
      new MutableLiveData<>(mDailyGames);

  private ZenGame mZenGame;
  private ZenGame mBeginnerGame;

  private static TimeProvider sTimeProvider = new SystemTimeProvider();

  public static void setTimeProvider(TimeProvider timeProvider) {
    sTimeProvider = timeProvider;
  }

  public static TimeProvider getTimeProvider() {
    return sTimeProvider;
  }

  public static Long sSeed = null;

  public final DBAdapter database;

  private Application(Context context) {
    super();
    database = new DBAdapter(context);
    database.initialize(mClassicGames, mArcadeGames, mDailyGames);
    if (isDebug()) {
      prefillDatabaseIfEmpty();
    }
  }

  private boolean isDebug() {
    try {
      Class<?> buildConfigClass = Class.forName("com.antsapps.triples.BuildConfig");
      return (Boolean) buildConfigClass.getField("DEBUG").get(null);
    } catch (Exception e) {
      Log.e(TAG, "Could not find BuildConfig", e);
      return false;
    }
  }

  private void prefillDatabaseIfEmpty() {
    if (mClassicGames.isEmpty()) {
      Random random = sSeed != null ? new Random(sSeed) : new Random();
      for (int i = 0; i < 10; i++) {
        long seed = random.nextLong();
        ClassicGame game =
            new ClassicGame(
                -1,
                seed,
                Lists.<Card>newArrayList(),
                Lists.<Long>newArrayList(),
                new Deck(Lists.<Card>newArrayList()),
                (long) (120000 + random.nextInt(300000)), // 2 to 7 minutes
                new Date(
                    sTimeProvider.currentTimeMillis()
                        - (long) i * 24 * 60 * 60 * 1000), // one per day
                GameState.COMPLETED,
                false);
        addClassicGame(game);
      }
    }
    if (mArcadeGames.isEmpty()) {
      Random random = sSeed != null ? new Random(sSeed) : new Random();
      for (int i = 0; i < 10; i++) {
        long seed = random.nextLong();
        ArcadeGame game =
            new ArcadeGame(
                -1,
                seed,
                Lists.<Card>newArrayList(),
                Lists.<Long>newArrayList(),
                new Deck(new Random(seed)),
                ArcadeGame.TIME_LIMIT_MS + 100,
                new Date(sTimeProvider.currentTimeMillis() - (long) i * 24 * 60 * 60 * 1000),
                GameState.COMPLETED,
                10 + random.nextInt(20), // 10 to 30 triples
                false);
        addArcadeGame(game);
      }
    }
  }

  public static Application getInstance(Context context) {
    if (INSTANCE == null) {
      INSTANCE = new Application(context);
    }
    return INSTANCE;
  }

  public void addClassicGame(ClassicGame game) {
    game.setId(database.addClassicGame(game));
    mClassicGames.add(game);
    Log.i(TAG, "addGame. now mClassicGames = " + mClassicGames);
    notifyStateChanged();
  }

  public void saveClassicGame(ClassicGame game) {
    database.updateClassicGame(game);
    notifyStateChanged();
  }

  public void uploadToCloud(Activity activity) {
    CloudSaveManager.saveAll(activity, this);
  }

  public void deleteClassicGame(ClassicGame game) {
    mClassicGames.remove(game);
    database.removeClassicGame(game);
    notifyStateChanged();
  }

  public ClassicGame getClassicGame(long id) {
    for (ClassicGame game : mClassicGames) {
      if (game.getId() == id) {
        return game;
      }
    }
    return null;
  }

  public Iterable<ClassicGame> getCurrentClassicGames() {
    return Iterables.filter(
        mClassicGames,
        new Predicate<Game>() {
          @Override
          public boolean apply(Game game) {
            return game.getGameState() == GameState.ACTIVE
                || game.getGameState() == GameState.PAUSED
                || game.getGameState() == GameState.STARTING;
          }
        });
  }

  public Iterable<ClassicGame> getCompletedClassicGames() {
    return Iterables.filter(
        mClassicGames,
        new Predicate<Game>() {
          @Override
          public boolean apply(Game game) {
            return game.getGameState() == GameState.COMPLETED;
          }
        });
  }

  public ClassicStatistics getClassicStatistics(Period period, boolean includeHinted) {
    return new ClassicStatistics(getCompletedClassicGames(), period, includeHinted);
  }

  public void addArcadeGame(ArcadeGame game) {
    game.setId(database.addArcadeGame(game));
    mArcadeGames.add(game);
    Log.i(TAG, "addGame. now mArcadeGames = " + mArcadeGames);
    notifyStateChanged();
  }

  public void saveArcadeGame(ArcadeGame game) {
    database.updateArcadeGame(game);
    notifyStateChanged();
  }

  public void deleteArcadeGame(ArcadeGame game) {
    mArcadeGames.remove(game);
    database.removeArcadeGame(game);
    notifyStateChanged();
  }

  public ArcadeGame getArcadeGame(long id) {
    for (ArcadeGame game : mArcadeGames) {
      if (game.getId() == id) {
        return game;
      }
    }
    return null;
  }

  public Iterable<ArcadeGame> getCurrentArcadeGames() {
    return Iterables.filter(
        mArcadeGames,
        new Predicate<Game>() {
          @Override
          public boolean apply(Game game) {
            return game.getGameState() == GameState.ACTIVE
                || game.getGameState() == GameState.PAUSED
                || game.getGameState() == GameState.STARTING;
          }
        });
  }

  public Iterable<ArcadeGame> getCompletedArcadeGames() {
    return Iterables.filter(
        mArcadeGames,
        new Predicate<Game>() {
          @Override
          public boolean apply(Game game) {
            return game.getGameState() == GameState.COMPLETED;
          }
        });
  }

  public ArcadeStatistics getArcadeStatistics(Period period, boolean includeHinted) {
    return new ArcadeStatistics(getCompletedArcadeGames(), period, includeHinted);
  }

  public ZenGame getZenGame(boolean isBeginner) {
    long seed = sSeed != null ? sSeed : sTimeProvider.currentTimeMillis();
    if (isBeginner) {
      if (mBeginnerGame == null) {
        mBeginnerGame = ZenGame.createFromSeed(seed, true);
      }
      return mBeginnerGame;
    } else {
      if (mZenGame == null) {
        mZenGame = ZenGame.createFromSeed(seed, false);
      }
      return mZenGame;
    }
  }

  public void resetZenGame(boolean isBeginner) {
    if (isBeginner) {
      mBeginnerGame = null;
    } else {
      mZenGame = null;
    }
    notifyStateChanged();
  }

  public void addDailyGame(DailyGame game) {
    game.setId(database.addDailyGame(game));
    mDailyGames.add(game);
    notifyStateChanged();
  }

  public void saveDailyGame(DailyGame game) {
    database.updateDailyGame(game);
    notifyStateChanged();
  }

  public DailyGame getDailyGame(long id) {
    for (DailyGame game : mDailyGames) {
      if (game.getId() == id) {
        return game;
      }
    }
    return null;
  }

  public DailyGame getDailyGameForDate(DailyGame.Day day) {
    for (DailyGame game : mDailyGames) {
      if (game.getGameDay().equals(day)) {
        return game;
      }
    }

    DailyGame game = DailyGame.createFromDay(day);
    addDailyGame(game);
    return game;
  }

  public Iterable<DailyGame> getCompletedDailyGames() {
    return Iterables.filter(
        mDailyGames,
        new Predicate<Game>() {
          @Override
          public boolean apply(Game game) {
            return game.getGameState() == GameState.COMPLETED;
          }
        });
  }

  public Iterable<DailyGame> getCurrentDailyGames() {
    return Iterables.filter(
        mDailyGames,
        new Predicate<Game>() {
          @Override
          public boolean apply(Game game) {
            return game.getGameState() == GameState.ACTIVE
                || game.getGameState() == GameState.PAUSED
                || game.getGameState() == GameState.STARTING;
          }
        });
  }

  public DailyGame getDailyGameByGameDay(DailyGame.Day gameDay) {
    return Iterables.find(mDailyGames, game -> game.getGameDay().equals(gameDay));
  }

  /**
   * Merges the completed Classic games from the cloud into the local list.
   *
   * @param cloudGames The list of completed Classic games from the cloud.
   * @return true if the local list was modified or if the cloud data is stale and should be
   *     overwritten/deleted.
   */
  public boolean mergeClassicCompleted(List<ClassicGame> cloudGames) {
    boolean changed = false;
    Set<Long> localDates = new HashSet<>();
    for (ClassicGame g : getCompletedClassicGames()) {
      localDates.add(g.getDateStarted().getTime());
    }
    for (ClassicGame cloudGame : cloudGames) {
      if (!localDates.contains(cloudGame.getDateStarted().getTime())) {
        addClassicGame(cloudGame);
        changed = true;
      }
    }
    return changed;
  }

  /**
   * Merges the completed Arcade games from the cloud into the local list.
   *
   * @param cloudGames The list of completed Arcade games from the cloud.
   * @return true if the local list was modified or if the cloud data is stale and should be
   *     overwritten/deleted.
   */
  public boolean mergeArcadeCompleted(List<ArcadeGame> cloudGames) {
    boolean changed = false;
    Set<Long> localDates = new HashSet<>();
    for (ArcadeGame g : getCompletedArcadeGames()) {
      localDates.add(g.getDateStarted().getTime());
    }
    for (ArcadeGame cloudGame : cloudGames) {
      if (!localDates.contains(cloudGame.getDateStarted().getTime())) {
        addArcadeGame(cloudGame);
        changed = true;
      }
    }
    return changed;
  }

  /**
   * Merges the completed Daily games from the cloud into the local list.
   *
   * @param cloudGames The list of completed Daily games from the cloud.
   * @return true if the local list was modified or if the cloud data is stale and should be
   *     overwritten/deleted.
   */
  public boolean mergeDailyCompleted(List<DailyGame> cloudGames) {
    boolean changed = false;
    Set<Long> localSeeds = new HashSet<>();
    for (DailyGame g : getCompletedDailyGames()) {
      localSeeds.add(g.getRandomSeed());
    }
    for (DailyGame cloudGame : cloudGames) {
      if (!localSeeds.contains(cloudGame.getRandomSeed())) {
        DailyGame local = getDailyGameByGameDay(cloudGame.getGameDay());
        if (local != null) {
          // Update local game with cloud data
          // For simplicity, just delete and re-add or update
          mDailyGames.remove(local);
          database.removeDailyGame(local);
        }
        addDailyGame(cloudGame);
        changed = true;
      }
    }
    return changed;
  }

  /**
   * Merges the current Classic game from the cloud into the local state.
   *
   * @param cloudGame The current Classic game from the cloud.
   * @return true if the local state was updated or if the cloud data is stale and should be
   *     overwritten/deleted.
   */
  public boolean mergeClassicCurrent(ClassicGame cloudGame) {
    for (ClassicGame localCompleted : getCompletedClassicGames()) {
      if (localCompleted.getDateStarted().equals(cloudGame.getDateStarted())) {
        // Already completed locally, so cloud state is stale (it's still in the "Current" slot
        // in the cloud).
        return true;
      }
      if (localCompleted.getDateStarted().after(cloudGame.getDateStarted())) {
        return true;
      }
    }

    ClassicGame localCurrent = Iterables.getFirst(getCurrentClassicGames(), null);
    if (localCurrent == null) {
      addClassicGame(cloudGame);
      return true;
    }

    if (localCurrent.getDateStarted().equals(cloudGame.getDateStarted())) {
      if (cloudGame.getTimeElapsed() > localCurrent.getTimeElapsed()
          || cloudGame.getCardsRemaining() < localCurrent.getCardsRemaining()) {
        deleteClassicGame(localCurrent);
        addClassicGame(cloudGame);
        return true;
      }
      return false;
    }

    if (localCurrent.getDateStarted().after(cloudGame.getDateStarted())) {
      return true;
    }

    return false;
  }

  /**
   * Merges the current Arcade game from the cloud into the local state.
   *
   * @param cloudGame The current Arcade game from the cloud.
   * @return true if the local state was updated or if the cloud data is stale and should be
   *     overwritten/deleted.
   */
  public boolean mergeArcadeCurrent(ArcadeGame cloudGame) {
    for (ArcadeGame localCompleted : getCompletedArcadeGames()) {
      if (localCompleted.getDateStarted().equals(cloudGame.getDateStarted())) {
        // Already completed locally, so cloud state is stale (it's still in the "Current" slot
        // in the cloud).
        return true;
      }
      if (localCompleted.getDateStarted().after(cloudGame.getDateStarted())) {
        return true;
      }
    }

    ArcadeGame localCurrent = Iterables.getFirst(getCurrentArcadeGames(), null);
    if (localCurrent == null) {
      addArcadeGame(cloudGame);
      return true;
    }

    if (localCurrent.getDateStarted().equals(cloudGame.getDateStarted())) {
      if (cloudGame.getNumTriplesFound() > localCurrent.getNumTriplesFound()) {
        deleteArcadeGame(localCurrent);
        addArcadeGame(cloudGame);
        return true;
      }
      return false;
    }

    if (localCurrent.getDateStarted().after(cloudGame.getDateStarted())) {
      return true;
    }

    return false;
  }

  /**
   * Merges the current Daily game from the cloud into the local state.
   *
   * @param cloudGame The current Daily game from the cloud.
   * @return true if the local state was updated or if the cloud data is stale and should be
   *     overwritten/deleted.
   */
  public boolean mergeDailyCurrent(DailyGame cloudGame) {
    DailyGame local = getDailyGameByGameDay(cloudGame.getGameDay());
    if (local != null && local.getGameState() == GameState.COMPLETED) {
      return true;
    }
    if (local == null || cloudGame.getNumTriplesFound() > local.getNumTriplesFound()) {
      if (local != null) {
        mDailyGames.remove(local);
        database.removeDailyGame(local);
      }
      addDailyGame(cloudGame);
      return true;
    }
    return false;
  }

  public List<DailyGame> getDailyGames() {
    return Lists.newArrayList(mDailyGames);
  }

  public void clearAllData() {
    mClassicGames.clear();
    mArcadeGames.clear();
    mDailyGames.clear();
    mZenGame = null;
    mBeginnerGame = null;
    database.clearAllData();
    notifyStateChanged();
  }

  /** This should be called whenever the state of the object is changed. */
  @Override
  protected void notifyStateChanged() {
    mClassicGamesLiveData.setValue(mClassicGames);
    mArcadeGamesLiveData.setValue(mArcadeGames);
    mDailyGamesLiveData.setValue(mDailyGames);
    super.notifyStateChanged();
  }

  public LiveData<List<ClassicGame>> getClassicGamesLiveData() {
    return mClassicGamesLiveData;
  }

  public LiveData<List<ArcadeGame>> getArcadeGamesLiveData() {
    return mArcadeGamesLiveData;
  }

  public LiveData<List<DailyGame>> getDailyGamesLiveData() {
    return mDailyGamesLiveData;
  }
}
