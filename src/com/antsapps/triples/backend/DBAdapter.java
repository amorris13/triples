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

import com.antsapps.triples.backend.Game.GameState;

public class DBAdapter extends SQLiteOpenHelper {
  /** The name of the database file on the file system */
  private static final String DATABASE_NAME = "Triples.db";
  /** The version of the database that this class understands. */
  private static final int DATABASE_VERSION = 3;

  public static final String TABLE_CLASSIC_GAMES = "games";

  public static final String COLUMN_GAME_ID = "game_id";
  public static final String COLUMN_GAME_STATE = "game_state";
  public static final String COLUMN_GAME_RANDOM = "game_random";
  public static final String COLUMN_CARDS_IN_PLAY = "cards_in_play";
  public static final String COLUMN_CARDS_IN_DECK = "cards_in_deck";
  public static final String COLUMN_TIME_ELAPSED = "time_elapsed";
  public static final String COLUMN_DATE = "date";

  private static final String CREATE_CLASSIC_GAMES = "CREATE TABLE " + TABLE_CLASSIC_GAMES
      + "(" + COLUMN_GAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " //
      + COLUMN_GAME_STATE + " TEXT, " //
      + COLUMN_GAME_RANDOM + " INTEGER, " //
      + COLUMN_CARDS_IN_PLAY + " BLOB, " //
      + COLUMN_CARDS_IN_DECK + " BLOB, " //
      + COLUMN_TIME_ELAPSED + " INTEGER, " //
      + COLUMN_DATE + " INTEGER)";
  private static final String DROP_GAMES = "DROP TABLE IF EXISTS "
      + TABLE_CLASSIC_GAMES;
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
    String[] sql = new String[] { CREATE_CLASSIC_GAMES };
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

  public void initialize(List<ClassicGame> classicGames) {
    Log.i("DBAdapter", "initialize");
    // Clear everything
    classicGames.clear();

    // Do classic games first.
    Cursor classicGamesCursor = getWritableDatabase().query(
        TABLE_CLASSIC_GAMES,
        new String[] { COLUMN_GAME_ID, COLUMN_GAME_STATE, COLUMN_GAME_RANDOM,
            COLUMN_CARDS_IN_PLAY, COLUMN_CARDS_IN_DECK, COLUMN_TIME_ELAPSED,
            COLUMN_DATE },
        null,
        null,
        null,
        null,
        null);
    classicGamesCursor.moveToFirst();
    while (!classicGamesCursor.isAfterLast()) {
      ClassicGame game = cursorToClassicGame(classicGamesCursor);
      classicGames.add(game);
      classicGamesCursor.moveToNext();
    }
    classicGamesCursor.close();

    Collections.sort(classicGames);
  }

  private static ClassicGame cursorToClassicGame(Cursor cursor) {
    long id = cursor.getLong(0);
    GameState state = GameState.valueOf(cursor.getString(1));
    long seed = cursor.getLong(2);
    List<Card> cardsInPlay = Utils.cardListFromByteArray(cursor.getBlob(3));
    Deck deck = Deck.fromByteArray(cursor.getBlob(4));
    long timeElapsed = cursor.getLong(5);
    Date date = new Date(cursor.getLong(6));
    ClassicGame game = new ClassicGame(id, seed, cardsInPlay, deck,
        timeElapsed, date, state);
    return game;
  }

  public long addClassicGame(ClassicGame game) {
    Log.i(TAG, "adding game with seed = " + game.getRandomSeed());
    return getWritableDatabase().insert(
        TABLE_CLASSIC_GAMES,
        null,
        createClassicGameValues(game));
  }

  public void updateClassicGame(ClassicGame game) {
    getWritableDatabase().update(
        TABLE_CLASSIC_GAMES,
        createClassicGameValues(game),
        COLUMN_GAME_ID + " = " + game.getId(),
        null);
  }

  public void removeClassicGame(ClassicGame game) {
    getWritableDatabase().delete(
        TABLE_CLASSIC_GAMES,
        COLUMN_GAME_ID + " = " + game.getId(),
        null);
  }


  private ContentValues createClassicGameValues(ClassicGame game) {
    ContentValues values = new ContentValues();
    values.put(COLUMN_GAME_STATE, game.getGameState().name());
    values.put(COLUMN_GAME_RANDOM, game.getRandomSeed());
    values.put(COLUMN_CARDS_IN_PLAY, game.getCardsInPlayAsByteArray());
    values.put(COLUMN_CARDS_IN_DECK, game.getCardsInDeckAsByteArray());
    values.put(COLUMN_TIME_ELAPSED, game.getTimeElapsed());
    values.put(COLUMN_DATE, game.getDateStarted().getTime());
    return values;
  }
}
