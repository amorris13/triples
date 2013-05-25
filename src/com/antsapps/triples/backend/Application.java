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
  private final List<ClassicGame> mClassicGames;

  public final DBAdapter database;

  private Application(Context context) {
    super();
    mClassicGames = Lists.newArrayList();
    database = new DBAdapter(context);
    init();
  }

  public static Application getInstance(Context context) {
    if (INSTANCE == null) {
      INSTANCE = new Application(context);
    }
    return INSTANCE;
  }

  private void init() {
    database.initialize(mClassicGames);
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

  public List<Game> getAllGames() {
    return mClassicGames;
  }

  public Game getGame(long id) {
    for (Game game : mClassicGames) {
      if (game.getId() == id) {
        return game;
      }
    }
    return null;
  }

  public Iterable<Game> getCurrentGames() {
    return Iterables.filter(mClassicGames, new Predicate<Game>() {
      @Override
      public boolean apply(Game game) {
        return game.getGameState() == GameState.PAUSED
            || game.getGameState() == GameState.ACTIVE;
      }
    });
  }

  public Iterable<Game> getCompletedGames() {
    return Iterables.filter(mClassicGames, new Predicate<Game>() {
      @Override
      public boolean apply(Game game) {
        return game.getGameState() == GameState.COMPLETED;
      }
    });
  }

  public Statistics getStatistics(Period period) {
    return new Statistics(getCompletedGames(), period);
  }
}
