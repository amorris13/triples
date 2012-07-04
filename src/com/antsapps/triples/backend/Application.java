package com.antsapps.triples.backend;

import java.util.List;

import android.content.Context;

import com.google.common.collect.Lists;

public class Application {
  private static Application INSTANCE;

  /** Should remain sorted */
  private final List<Game> mGames;

  public final DBAdapter database;

  private Application(Context context) {
    super();
    mGames = Lists.newArrayList();
    database = new DBAdapter(context);
    init();
  }

  public static Application setInstance(Context context) {
    if (INSTANCE == null) {
      INSTANCE = new Application(context);
    }
    return INSTANCE;
  }

  public static Application getInstance() {
    return INSTANCE;
  }

  private void init() {
    database.initialize(mGames);
  }

  public void addGame(Game game) {
    game.setId(database.addGame(game));
    mGames.add(game);
  }

  public void deleteGame(Game game) {
    mGames.remove(game);
    database.removeGame(game);
  }

  public List<Game> getGames() {
    return mGames;
  }

  public Game getGame(long id) {
    for(Game game : mGames){
      if(game.getId() == id){
        return game;
      }
    }
    return null;
  }
}
