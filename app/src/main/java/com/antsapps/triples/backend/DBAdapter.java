package com.antsapps.triples.backend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.antsapps.triples.backend.Game.GameState;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DBAdapter extends SQLiteOpenHelper {
  public static final String TABLE_CLASSIC_GAMES = "games";
  public static final String TABLE_ARCADE_GAMES = "arcade_games";
  public static final String COLUMN_GAME_ID = "game_id";
  public static final String COLUMN_GAME_STATE = "game_state";
  public static final String COLUMN_GAME_RANDOM = "game_random";
  public static final String COLUMN_CARDS_IN_PLAY = "cards_in_play";
  public static final String COLUMN_CARDS_IN_DECK = "cards_in_deck";
  public static final String COLUMN_TIME_ELAPSED = "time_elapsed";
  public static final String COLUMN_DATE = "date";
  public static final String COLUMN_NUM_TRIPLES_FOUND = "num_triples_found"; // ARCADE only
  public static final String COLUMN_TRIPLE_FIND_TIMES = "triple_find_times";
  public static final String COLUMN_HINTS_USED = "hints_used";
  /** The name of the database file on the file system */
  private static final String DATABASE_NAME = "Triples.db";
  /** The version of the database that this class understands. */
  private static final int DATABASE_VERSION = 6;

  private static final String CREATE_CLASSIC_GAMES =
      "CREATE TABLE "
          + TABLE_CLASSIC_GAMES
          + "("
          + COLUMN_GAME_ID
          + " INTEGER PRIMARY KEY AUTOINCREMENT, " //
          + COLUMN_GAME_STATE
          + " TEXT, " //
          + COLUMN_GAME_RANDOM
          + " INTEGER, " //
          + COLUMN_CARDS_IN_PLAY
          + " BLOB, " //
          + COLUMN_CARDS_IN_DECK
          + " BLOB, " //
          + COLUMN_TIME_ELAPSED
          + " INTEGER, " //
          + COLUMN_DATE
          + " INTEGER, " //
          + COLUMN_TRIPLE_FIND_TIMES
          + " BLOB, " //
          + COLUMN_HINTS_USED
          + " INTEGER)";
  private static final String CREATE_ARCADE_GAMES =
      "CREATE TABLE "
          + TABLE_ARCADE_GAMES
          + "("
          + COLUMN_GAME_ID
          + " INTEGER PRIMARY KEY AUTOINCREMENT, " //
          + COLUMN_GAME_STATE
          + " TEXT, " //
          + COLUMN_GAME_RANDOM
          + " INTEGER, " //
          + COLUMN_CARDS_IN_PLAY
          + " BLOB, " //
          + COLUMN_CARDS_IN_DECK
          + " BLOB, " //
          + COLUMN_TIME_ELAPSED
          + " INTEGER, " //
          + COLUMN_DATE
          + " INTEGER, " //
          + COLUMN_NUM_TRIPLES_FOUND
          + " INTEGER, " //
          + COLUMN_TRIPLE_FIND_TIMES
          + " BLOB, " //
          + COLUMN_HINTS_USED
          + " INTEGER)";
  private static final String TAG = "DBAdapter";

  /** Constructor */
  public DBAdapter(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  /**
   * Execute all of the SQL statements in the String[] array
   *
   * @param db The database on which to execute the statements
   * @param sql An array of SQL statements to execute
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
    String[] sql = new String[] {CREATE_CLASSIC_GAMES, CREATE_ARCADE_GAMES};
    db.beginTransaction();
    try {
      // Create tables & test data
      execMultipleSQL(db, sql);
      db.setTransactionSuccessful();
    } catch (SQLException e) {
      Log.e("DBAdapter", e.toString());
    } finally {
      db.endTransaction();
    }
  }

  /** Called when the database must be upgraded */
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.w(DATABASE_NAME, "Upgrading database from version " + oldVersion + " to " + newVersion);

    if (oldVersion < 4) {
      String[] sql = new String[] {CREATE_ARCADE_GAMES};
      db.beginTransaction();
      try {
        // Create tables & test data
        execMultipleSQL(db, sql);
        db.setTransactionSuccessful();
      } catch (SQLException e) {
        Log.e("DBAdapter-Upgrade", e.toString());
      } finally {
        db.endTransaction();
      }
    }
    if (oldVersion < 5) {
      db.beginTransaction();
      try {
        db.execSQL("ALTER TABLE " + TABLE_CLASSIC_GAMES + " ADD COLUMN " + COLUMN_TRIPLE_FIND_TIMES + " BLOB");
        db.execSQL("ALTER TABLE " + TABLE_ARCADE_GAMES + " ADD COLUMN " + COLUMN_TRIPLE_FIND_TIMES + " BLOB");
        db.setTransactionSuccessful();
      } catch (SQLException e) {
        Log.e("DBAdapter-Upgrade", e.toString());
      } finally {
        db.endTransaction();
      }
    }
    if (oldVersion < 6) {
      db.beginTransaction();
      try {
        db.execSQL("ALTER TABLE " + TABLE_CLASSIC_GAMES + " ADD COLUMN " + COLUMN_HINTS_USED + " INTEGER DEFAULT 0");
        db.execSQL("ALTER TABLE " + TABLE_ARCADE_GAMES + " ADD COLUMN " + COLUMN_HINTS_USED + " INTEGER DEFAULT 0");
        db.setTransactionSuccessful();
      } catch (SQLException e) {
        Log.e("DBAdapter-Upgrade", e.toString());
      } finally {
        db.endTransaction();
      }
    }
  }

  public void initialize(List<ClassicGame> classicGames, List<ArcadeGame> arcadeGames) {
    Log.i("DBAdapter", "initialize");
    initClassicGames(classicGames);
    initArcadeGames(arcadeGames);
  }

  // Classic Game stuff

  private void initClassicGames(List<ClassicGame> classicGames) {
    // Clear everything
    classicGames.clear();

    // Do classic games first.
    Cursor classicGamesCursor =
        getWritableDatabase()
            .query(
                TABLE_CLASSIC_GAMES,
                new String[] {
                  COLUMN_GAME_ID,
                  COLUMN_GAME_STATE,
                  COLUMN_GAME_RANDOM,
                  COLUMN_CARDS_IN_PLAY,
                  COLUMN_CARDS_IN_DECK,
                  COLUMN_TIME_ELAPSED,
                  COLUMN_DATE,
                  COLUMN_TRIPLE_FIND_TIMES,
                  COLUMN_HINTS_USED
                },
                null,
                null,
                null,
                null,
                null);
    classicGamesCursor.moveToFirst();
    while (!classicGamesCursor.isAfterLast()) {
      ClassicGame game =
          new ClassicGame(
              classicGamesCursor.getLong(0),
              classicGamesCursor.getLong(2),
              Utils.cardListFromByteArray(classicGamesCursor.getBlob(3)),
              Utils.longListFromByteArray(classicGamesCursor.getBlob(7)),
              Deck.fromByteArray(classicGamesCursor.getBlob(4)),
              classicGamesCursor.getLong(5),
              new Date(classicGamesCursor.getLong(6)),
              GameState.valueOf(classicGamesCursor.getString(1)),
              classicGamesCursor.getInt(8) != 0);
      classicGames.add(game);
      classicGamesCursor.moveToNext();
    }
    classicGamesCursor.close();

    Collections.sort(classicGames);
  }

  public long addClassicGame(ClassicGame game) {
    Log.i(TAG, "adding game with seed = " + game.getRandomSeed());
    return getWritableDatabase().insert(TABLE_CLASSIC_GAMES, null, createClassicGameValues(game));
  }

  public void updateClassicGame(ClassicGame game) {
    getWritableDatabase()
        .update(
            TABLE_CLASSIC_GAMES,
            createClassicGameValues(game),
            COLUMN_GAME_ID + " = " + game.getId(),
            null);
  }

  public void removeClassicGame(ClassicGame game) {
    getWritableDatabase().delete(TABLE_CLASSIC_GAMES, COLUMN_GAME_ID + " = " + game.getId(), null);
  }

  private ContentValues createClassicGameValues(ClassicGame game) {
    ContentValues values = new ContentValues();
    values.put(COLUMN_GAME_STATE, game.getGameState().name());
    values.put(COLUMN_GAME_RANDOM, game.getRandomSeed());
    values.put(COLUMN_CARDS_IN_PLAY, game.getCardsInPlayAsByteArray());
    values.put(COLUMN_CARDS_IN_DECK, game.getCardsInDeckAsByteArray());
    values.put(COLUMN_TIME_ELAPSED, game.getTimeElapsed());
    values.put(COLUMN_DATE, game.getDateStarted().getTime());
    values.put(COLUMN_TRIPLE_FIND_TIMES, Utils.longListToByteArray(game.getTripleFindTimes()));
    values.put(COLUMN_HINTS_USED, game.areHintsUsed() ? 1 : 0);
    return values;
  }

  // Arcade Game methods

  private void initArcadeGames(List<ArcadeGame> arcadeGames) {
    // Clear everything
    arcadeGames.clear();

    // Do classic games first.
    Cursor arcadeGamesCursor =
        getWritableDatabase()
            .query(
                TABLE_ARCADE_GAMES,
                new String[] {
                  COLUMN_GAME_ID,
                  COLUMN_GAME_STATE,
                  COLUMN_GAME_RANDOM,
                  COLUMN_CARDS_IN_PLAY,
                  COLUMN_CARDS_IN_DECK,
                  COLUMN_TIME_ELAPSED,
                  COLUMN_DATE,
                  COLUMN_NUM_TRIPLES_FOUND,
                  COLUMN_TRIPLE_FIND_TIMES,
                  COLUMN_HINTS_USED
                },
                null,
                null,
                null,
                null,
                null);
    arcadeGamesCursor.moveToFirst();
    while (!arcadeGamesCursor.isAfterLast()) {
      ArcadeGame game =
          new ArcadeGame(
              arcadeGamesCursor.getLong(0),
              arcadeGamesCursor.getLong(2),
              Utils.cardListFromByteArray(arcadeGamesCursor.getBlob(3)),
              Utils.longListFromByteArray(arcadeGamesCursor.getBlob(8)),
              Deck.fromByteArray(arcadeGamesCursor.getBlob(4)),
              arcadeGamesCursor.getLong(5),
              new Date(arcadeGamesCursor.getLong(6)),
              GameState.valueOf(arcadeGamesCursor.getString(1)),
              arcadeGamesCursor.getInt(7),
              arcadeGamesCursor.getInt(9) != 0);
      arcadeGames.add(game);
      arcadeGamesCursor.moveToNext();
    }
    arcadeGamesCursor.close();

    Collections.sort(arcadeGames);
  }

  public long addArcadeGame(ArcadeGame game) {
    return getWritableDatabase().insert(TABLE_ARCADE_GAMES, null, createArcadeGameValues(game));
  }

  public void updateArcadeGame(ArcadeGame game) {
    getWritableDatabase()
        .update(
            TABLE_ARCADE_GAMES,
            createArcadeGameValues(game),
            COLUMN_GAME_ID + " = " + game.getId(),
            null);
  }

  public void removeArcadeGame(ArcadeGame game) {
    getWritableDatabase().delete(TABLE_ARCADE_GAMES, COLUMN_GAME_ID + " = " + game.getId(), null);
  }

  private ContentValues createArcadeGameValues(ArcadeGame game) {
    ContentValues values = new ContentValues();
    values.put(COLUMN_GAME_STATE, game.getGameState().name());
    values.put(COLUMN_GAME_RANDOM, game.getRandomSeed());
    values.put(COLUMN_CARDS_IN_PLAY, game.getCardsInPlayAsByteArray());
    values.put(COLUMN_CARDS_IN_DECK, game.getCardsInDeckAsByteArray());
    values.put(COLUMN_TIME_ELAPSED, game.getTimeElapsed());
    values.put(COLUMN_DATE, game.getDateStarted().getTime());
    values.put(COLUMN_NUM_TRIPLES_FOUND, game.getNumTriplesFound());
    values.put(COLUMN_TRIPLE_FIND_TIMES, Utils.longListToByteArray(game.getTripleFindTimes()));
    values.put(COLUMN_HINTS_USED, game.areHintsUsed() ? 1 : 0);
    return values;
  }
}
