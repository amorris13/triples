package com.antsapps.triples.backend;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
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

  private ZenGame mZenGame;
  private ZenGame mBeginnerGame;

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
      Random random = new Random();
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
                    System.currentTimeMillis() - (long) i * 24 * 60 * 60 * 1000), // one per day
                GameState.COMPLETED,
                false);
        addClassicGame(game);
      }
    }
    if (mArcadeGames.isEmpty()) {
      Random random = new Random();
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
                new Date(System.currentTimeMillis() - (long) i * 24 * 60 * 60 * 1000),
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
                || game.getGameState() == GameState.PAUSED;
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

  public ClassicStatistics getClassicStatistics(Period period) {
    return new ClassicStatistics(getCompletedClassicGames(), period);
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
                || game.getGameState() == GameState.PAUSED;
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

  public ArcadeStatistics getArcadeStatistics(Period period) {
    return new ArcadeStatistics(getCompletedArcadeGames(), period);
  }

  public ZenGame getZenGame(boolean isBeginner) {
    if (isBeginner) {
      if (mBeginnerGame == null) {
        mBeginnerGame = ZenGame.createFromSeed(System.currentTimeMillis(), true);
      }
      return mBeginnerGame;
    } else {
      if (mZenGame == null) {
        mZenGame = ZenGame.createFromSeed(System.currentTimeMillis(), false);
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

  public DailyGame getDailyGameForDate(long dateMillis) {
    long daySeed = DailyGame.getStartOfDaySeed(dateMillis);

    for (DailyGame game : mDailyGames) {
      if (game.getRandomSeed() == daySeed) {
        return game;
      }
    }

    DailyGame game = DailyGame.createFromSeed(daySeed);
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

  public DailyGame getDailyGameBySeed(long seed) {
    for (DailyGame game : mDailyGames) {
      if (game.getRandomSeed() == seed) {
        return game;
      }
    }
    return null;
  }

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

  public boolean mergeDailyCompleted(List<DailyGame> cloudGames) {
    boolean changed = false;
    Set<Long> localSeeds = new HashSet<>();
    for (DailyGame g : getCompletedDailyGames()) {
      localSeeds.add(g.getRandomSeed());
    }
    for (DailyGame cloudGame : cloudGames) {
      if (!localSeeds.contains(cloudGame.getRandomSeed())) {
        DailyGame local = getDailyGameBySeed(cloudGame.getRandomSeed());
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

  public boolean mergeClassicCurrent(ClassicGame cloudGame) {
    ClassicGame localCurrent = Iterables.getFirst(getCurrentClassicGames(), null);
    if (localCurrent == null || cloudGame.getTimeElapsed() > localCurrent.getTimeElapsed() || cloudGame.getCardsRemaining() < localCurrent.getCardsRemaining()) {
        if (localCurrent != null) {
            deleteClassicGame(localCurrent);
        }
        addClassicGame(cloudGame);
        return true;
    }
    return false;
  }

  public boolean mergeArcadeCurrent(ArcadeGame cloudGame) {
    ArcadeGame localCurrent = Iterables.getFirst(getCurrentArcadeGames(), null);
    if (localCurrent == null || cloudGame.getNumTriplesFound() > localCurrent.getNumTriplesFound()) {
        if (localCurrent != null) {
            deleteArcadeGame(localCurrent);
        }
        addArcadeGame(cloudGame);
        return true;
    }
    return false;
  }

  public boolean mergeDailyCurrent(DailyGame cloudGame) {
    DailyGame local = getDailyGameBySeed(cloudGame.getRandomSeed());
    if (local == null || (local.getGameState() != GameState.COMPLETED && cloudGame.getNumTriplesFound() > local.getNumTriplesFound())) {
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
}
