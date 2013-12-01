package com.antsapps.triples.backend;

import java.util.List;

import android.content.Context;
import android.util.Log;

import com.antsapps.triples.backend.Game.GameState;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class Application extends OnStateChangedReporter {
  private static final String TAG = "Application";

  private static Application INSTANCE;

  /** Should remain sorted */
  private final List<ClassicGame> mClassicGames = Lists.newArrayList();;
  private final List<ArcadeGame> mArcadeGames = Lists.newArrayList();;

  public final DBAdapter database;

  private Application(Context context) {
    super();
    database = new DBAdapter(context);
    database.initialize(mClassicGames, mArcadeGames);
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
    return Iterables.filter(mClassicGames, new Predicate<Game>() {
      @Override
      public boolean apply(Game game) {
        return game.getGameState() != GameState.COMPLETED;
      }
    });
  }

  public Iterable<ClassicGame> getCompletedClassicGames() {
    return Iterables.filter(mClassicGames, new Predicate<Game>() {
      @Override
      public boolean apply(Game game) {
        return game.getGameState() == GameState.COMPLETED;
      }
    });
  }

  public Statistics getClassicStatistics(Period period) {
    return new Statistics(getCompletedClassicGames(), period);
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
    return Iterables.filter(mArcadeGames, new Predicate<Game>() {
      @Override
      public boolean apply(Game game) {
        return game.getGameState() != GameState.COMPLETED;
      }
    });
  }

  public Iterable<ArcadeGame> getCompletedArcadeGames() {
    return Iterables.filter(mArcadeGames, new Predicate<Game>() {
      @Override
      public boolean apply(Game game) {
        return game.getGameState() == GameState.COMPLETED;
      }
    });
  }

  public Statistics getArcadeStatistics(Period period) {
    return new Statistics(getCompletedArcadeGames(), period);
  }
}
