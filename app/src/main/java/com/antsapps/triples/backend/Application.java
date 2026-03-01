package com.antsapps.triples.backend;

import android.content.Context;
import android.util.Log;

import com.antsapps.triples.backend.Game.GameState;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;
import java.util.Random;

public class Application extends OnStateChangedReporter {
  private static final String TAG = "Application";

  private static Application INSTANCE;

  // Should remain sorted
  private final List<ClassicGame> mClassicGames = Lists.newArrayList();
  private final List<ArcadeGame> mArcadeGames = Lists.newArrayList();
  private ClassicGame mTutorialGame;

  public final DBAdapter database;

  private Application(Context context) {
    super();
    database = new DBAdapter(context);
    database.initialize(mClassicGames, mArcadeGames);
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
                new Date(System.currentTimeMillis() - (long) i * 24 * 60 * 60 * 1000), // one per day
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
    if (game.isTutorial()) {
      mTutorialGame = game;
      mTutorialGame.setId(Long.MAX_VALUE);
    } else {
      game.setId(database.addClassicGame(game));
      mClassicGames.add(game);
    }
    Log.i(TAG, "addGame. now mClassicGames = " + mClassicGames);
    notifyStateChanged();
  }

  public void saveClassicGame(ClassicGame game) {
    if (game.isTutorial()) {
      return;
    }
    database.updateClassicGame(game);
    notifyStateChanged();
  }

  public void deleteClassicGame(ClassicGame game) {
    mClassicGames.remove(game);
    database.removeClassicGame(game);
    notifyStateChanged();
  }

  public ClassicGame getClassicGame(long id) {
    if (id == Long.MAX_VALUE && mTutorialGame != null) {
      return mTutorialGame;
    }
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
}
