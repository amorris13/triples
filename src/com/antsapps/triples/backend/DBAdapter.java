package com.antsapps.triples.backend;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter extends SQLiteOpenHelper {
  /** The name of the database file on the file system */
  private static final String DATABASE_NAME = "Triples.db";
  /** The version of the database that this class understands. */
  private static final int DATABASE_VERSION = 2;

  public static final String TABLE_GAMES = "games";

  public static final String COLUMN_GAME_ID = "game_id";
  public static final String COLUMN_GAME_STATE = "game_state";
  public static final String COLUMN_GAME_RANDOM = "game_random";
  public static final String COLUMN_CARDS_IN_PLAY = "cards_in_play";
  public static final String COLUMN_CARDS_IN_DECK = "cards_in_deck";
  public static final String COLUMN_TIME_ELAPSED = "time_elapsed";
  public static final String COLUMN_DATE = "date";

  private static final String CREATE_GAMES = "CREATE TABLE " + TABLE_GAMES
      + "(" + COLUMN_GAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " //
      + COLUMN_GAME_STATE + " INTEGER, " //
      + COLUMN_GAME_RANDOM + " INTEGER, " //
      + COLUMN_CARDS_IN_PLAY + " BLOB, " //
      + COLUMN_CARDS_IN_DECK + " BLOB, " //
      + COLUMN_TIME_ELAPSED + " INTEGER, " //
      + COLUMN_DATE + " INTEGER)";
  private static final String DROP_GAMES = "DROP TABLE IF EXISTS "
      + TABLE_GAMES;
  private static final String TAG = "DBAdapter";

  /** Constructor */
  public DBAdapter(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  /**
   * Execute all of the SQL statements in the String[] array
   *
   * @param db
   *          The database on which to execute the statements
   * @param sql
   *          An array of SQL statements to execute
   */
  private void execMultipleSQL(SQLiteDatabase db, String[] sql) {
    for (String s : sql) {
      if (s.trim().length() > 0) {
        db.execSQL(s);
      }
    }
  }

  /** Called when it is time to create the database */
  @Override
  public void onCreate(SQLiteDatabase db) {
    Log.i("DBAdaptor", "onCreate");
    String[] sql = new String[] { CREATE_GAMES };
    db.beginTransaction();
    try {
      // Create tables & test data
      execMultipleSQL(db, sql);
      db.setTransactionSuccessful();
    } catch (SQLException e) {
      Log.e("Error creating tables and debug data", e.toString());
    } finally {
      db.endTransaction();
    }
  }

  /** Called when the database must be upgraded */
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.w(DATABASE_NAME, "Upgrading database from version " + oldVersion
        + " to " + newVersion + ", which will destroy all old data");

    String[] sql = new String[] { DROP_GAMES };
    db.beginTransaction();
    try {
      // Create tables & test data
      execMultipleSQL(db, sql);
      db.setTransactionSuccessful();
    } catch (SQLException e) {
      Log.e("Error creating tables and debug data", e.toString());
    } finally {
      db.endTransaction();
    }

    // This is cheating. In the real world, you'll need to add columns, not
    // rebuild from scratch
    onCreate(db);
  }

  public void initialize(List<Game> games) {
    Log.i("DBAdapter", "initialize");
    // Clear everything
    games.clear();

    // Do players first.
    Cursor gamesCursor = getWritableDatabase().query(
        TABLE_GAMES,
        new String[] { COLUMN_GAME_ID, COLUMN_GAME_STATE, COLUMN_GAME_RANDOM,
            COLUMN_CARDS_IN_PLAY, COLUMN_CARDS_IN_DECK, COLUMN_TIME_ELAPSED,
            COLUMN_DATE },
        null,
        null,
        null,
        null,
        null);
    gamesCursor.moveToFirst();
    while (!gamesCursor.isAfterLast()) {
      Game game = cursorToGame(gamesCursor);
      Log.i(TAG, "retrieved game with seed = " + game.getRandomSeed());
      games.add(game);
      gamesCursor.moveToNext();
    }
    gamesCursor.close();

    Collections.sort(games);
  }

  private static Game cursorToGame(Cursor cursor) {
    long id = cursor.getLong(0);
    int state = cursor.getInt(1);
    long seed = cursor.getLong(2);
    List<Card> cardsInPlay = Utils.cardListFromByteArray(cursor.getBlob(3));
    Deck deck = Deck.fromByteArray(cursor.getBlob(4));
    long timeElapsed = cursor.getLong(5);
    Date date = new Date(cursor.getLong(6));
    Game game = new Game(id, seed, cardsInPlay, deck,
        timeElapsed, date);
    return game;
  }

  public long addGame(Game game) {
    Log.i(TAG, "adding game with seed = " + game.getRandomSeed());
    return getWritableDatabase().insert(
        TABLE_GAMES,
        null,
        createGameValues(game));
  }

  public void updateGame(Game game) {
    getWritableDatabase().update(
        TABLE_GAMES,
        createGameValues(game),
        COLUMN_GAME_ID + " = " + game.getId(),
        null);
  }

  public void removeGame(Game game) {
    getWritableDatabase().delete(
        TABLE_GAMES,
        COLUMN_GAME_ID + " = " + game.getId(),
        null);
  }


  private ContentValues createGameValues(Game game) {
    ContentValues values = new ContentValues();
    values.put(COLUMN_GAME_STATE, 0);
    values.put(COLUMN_GAME_RANDOM, game.getRandomSeed());
    values.put(COLUMN_CARDS_IN_PLAY, game.getCardsInPlayAsByteArray());
    values.put(COLUMN_CARDS_IN_DECK, game.getCardsInDeckAsByteArray());
    values.put(COLUMN_TIME_ELAPSED, game.getTimeElapsed());
    values.put(COLUMN_DATE, 0);
    return values;
  }
}
