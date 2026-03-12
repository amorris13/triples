package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.BaseRobolectricTest;
import com.antsapps.triples.backend.Game.GameState;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DBAdapterTest extends BaseRobolectricTest {

  private DBAdapter dbAdapter;

  @Before
  @Override
  public void setupBase() {
    super.setupBase();
    dbAdapter = new DBAdapter(ApplicationProvider.getApplicationContext());
  }

  @After
  public void tearDown() {
    dbAdapter.close();
  }

  @Test
  public void testClassicGameRoundTrip() {
    ClassicGame game =
        new ClassicGame(
            -1,
            12345L,
            Collections.<Card>emptyList(),
            Lists.newArrayList(1000L, 2000L),
            new Deck(Collections.<Card>emptyList()),
            10000,
            new Date(),
            GameState.COMPLETED,
            true);

    long id = dbAdapter.addClassicGame(game);
    assertThat(id).isNotEqualTo(-1);
    game.setId(id);

    List<ClassicGame> classicGames = Lists.newArrayList();
    dbAdapter.initialize(classicGames, Lists.newArrayList(), Lists.newArrayList());

    assertThat(classicGames).hasSize(1);
    ClassicGame retrieved = classicGames.get(0);
    assertThat(retrieved.getId()).isEqualTo(id);
    assertThat(retrieved.getRandomSeed()).isEqualTo(12345L);
    assertThat(retrieved.getGameState()).isEqualTo(GameState.COMPLETED);
    assertThat(retrieved.getTimeElapsed()).isEqualTo(10000);
    assertThat(retrieved.areHintsUsed()).isTrue();
    assertThat(retrieved.getTripleFindTimes()).containsExactly(1000L, 2000L).inOrder();
    assertThat(retrieved.getDateStarted().getTime()).isEqualTo(game.getDateStarted().getTime());

    // Update
    ClassicGame updatedGame =
        new ClassicGame(
            id,
            12345L,
            Collections.<Card>emptyList(),
            Lists.newArrayList(1000L, 2000L, 3000L),
            new Deck(Collections.<Card>emptyList()),
            20000,
            game.getDateStarted(),
            GameState.COMPLETED,
            true);
    dbAdapter.updateClassicGame(updatedGame);

    classicGames.clear();
    dbAdapter.initialize(classicGames, Lists.newArrayList(), Lists.newArrayList());
    assertThat(classicGames.get(0).getTimeElapsed()).isEqualTo(20000);
    assertThat(classicGames.get(0).getTripleFindTimes())
        .containsExactly(1000L, 2000L, 3000L)
        .inOrder();

    // Delete
    dbAdapter.removeClassicGame(updatedGame);
    classicGames.clear();
    dbAdapter.initialize(classicGames, Lists.newArrayList(), Lists.newArrayList());
    assertThat(classicGames).isEmpty();
  }

  @Test
  public void testArcadeGameRoundTrip() {
    ArcadeGame game =
        new ArcadeGame(
            -1,
            67890L,
            Collections.<Card>emptyList(),
            Lists.newArrayList(500L),
            new Deck(Collections.<Card>emptyList()),
            15000,
            new Date(),
            GameState.COMPLETED,
            12,
            false);

    long id = dbAdapter.addArcadeGame(game);
    assertThat(id).isNotEqualTo(-1);
    game.setId(id);

    List<ArcadeGame> arcadeGames = Lists.newArrayList();
    dbAdapter.initialize(Lists.newArrayList(), arcadeGames, Lists.newArrayList());

    assertThat(arcadeGames).hasSize(1);
    ArcadeGame retrieved = arcadeGames.get(0);
    assertThat(retrieved.getId()).isEqualTo(id);
    assertThat(retrieved.getRandomSeed()).isEqualTo(67890L);
    assertThat(retrieved.getGameState()).isEqualTo(GameState.COMPLETED);
    assertThat(retrieved.getTimeElapsed()).isEqualTo(15000);
    assertThat(retrieved.getNumTriplesFound()).isEqualTo(12);
    assertThat(retrieved.getTripleFindTimes()).containsExactly(500L);

    // Update
    ArcadeGame updatedGame =
        new ArcadeGame(
            id,
            67890L,
            Collections.<Card>emptyList(),
            Lists.newArrayList(500L, 1500L),
            new Deck(Collections.<Card>emptyList()),
            15000,
            game.getDateStarted(),
            GameState.COMPLETED,
            15,
            false);
    dbAdapter.updateArcadeGame(updatedGame);

    arcadeGames.clear();
    dbAdapter.initialize(Lists.newArrayList(), arcadeGames, Lists.newArrayList());
    assertThat(arcadeGames.get(0).getNumTriplesFound()).isEqualTo(15);
    assertThat(arcadeGames.get(0).getTripleFindTimes()).containsExactly(500L, 1500L).inOrder();

    // Delete
    dbAdapter.removeArcadeGame(updatedGame);
    arcadeGames.clear();
    dbAdapter.initialize(Lists.newArrayList(), arcadeGames, Lists.newArrayList());
    assertThat(arcadeGames).isEmpty();
  }

  @Test
  public void testDailyGameRoundTrip() {
    DailyGame.Day day = DailyGame.Day.forToday();
    DailyGame game =
        new DailyGame(
            -1,
            day.getSeed(),
            Lists.newArrayList(new Card(0, 0, 0, 0), new Card(1, 1, 1, 1), new Card(2, 2, 2, 2)),
            Lists.newArrayList(800L),
            new Deck(Collections.<Card>emptyList()),
            5000,
            new Date(),
            day,
            GameState.COMPLETED,
            false,
            Collections.<Set<Card>>emptyList(),
            new Date());

    long id = dbAdapter.addDailyGame(game);
    assertThat(id).isNotEqualTo(-1);
    game.setId(id);

    List<DailyGame> dailyGames = Lists.newArrayList();
    dbAdapter.initialize(Lists.newArrayList(), Lists.newArrayList(), dailyGames);

    assertThat(dailyGames).hasSize(1);
    DailyGame retrieved = dailyGames.get(0);
    assertThat(retrieved.getId()).isEqualTo(id);
    assertThat(retrieved.getGameDay()).isEqualTo(day);
    assertThat(retrieved.getGameState()).isEqualTo(GameState.COMPLETED);
    assertThat(retrieved.getTimeElapsed()).isEqualTo(5000);
    assertThat(retrieved.getDateCompleted()).isNotNull();
    assertThat(retrieved.getTripleFindTimes()).containsExactly(800L);

    // Update
    DailyGame updatedGame =
        new DailyGame(
            id,
            day.getSeed(),
            Lists.newArrayList(new Card(0, 0, 0, 0), new Card(1, 1, 1, 1), new Card(2, 2, 2, 2)),
            Lists.newArrayList(800L, 1800L),
            new Deck(Collections.<Card>emptyList()),
            6000,
            game.getDateStarted(),
            day,
            GameState.COMPLETED,
            false,
            Collections.<Set<Card>>emptyList(),
            game.getDateCompleted());
    dbAdapter.updateDailyGame(updatedGame);

    dailyGames.clear();
    dbAdapter.initialize(Lists.newArrayList(), Lists.newArrayList(), dailyGames);
    assertThat(dailyGames.get(0).getTimeElapsed()).isEqualTo(6000);
    assertThat(dailyGames.get(0).getTripleFindTimes()).containsExactly(800L, 1800L).inOrder();

    // Delete
    dbAdapter.removeDailyGame(updatedGame);
    dailyGames.clear();
    dbAdapter.initialize(Lists.newArrayList(), Lists.newArrayList(), dailyGames);
    assertThat(dailyGames).isEmpty();
  }

  @Test
  public void testUpgradeFromVersion3() {
    SQLiteDatabase db = SQLiteDatabase.create(null);
    try {
      // Create version 3 schema
      db.execSQL(
          "CREATE TABLE games(game_id INTEGER PRIMARY KEY AUTOINCREMENT, game_state TEXT, game_random INTEGER, cards_in_play BLOB, cards_in_deck BLOB, time_elapsed INTEGER, date INTEGER)");

      // Insert data
      ContentValues values = new ContentValues();
      values.put("game_state", GameState.COMPLETED.name());
      values.put("game_random", 11111L);
      values.put("time_elapsed", 12345L);
      values.put("date", 67890L);
      db.insert("games", null, values);

      // Trigger upgrade
      dbAdapter.onUpgrade(db, 3, 7);

      // Verify tables exist
      assertTableExists(db, DBAdapter.TABLE_CLASSIC_GAMES);
      assertTableExists(db, DBAdapter.TABLE_ARCADE_GAMES);
      assertTableExists(db, DBAdapter.TABLE_DAILY_GAMES);

      // Verify columns exist for Classic games
      assertColumnExists(db, DBAdapter.TABLE_CLASSIC_GAMES, DBAdapter.COLUMN_TRIPLE_FIND_TIMES);
      assertColumnExists(db, DBAdapter.TABLE_CLASSIC_GAMES, DBAdapter.COLUMN_HINTS_USED);

      // Verify data preserved
      Cursor cursor = db.query(DBAdapter.TABLE_CLASSIC_GAMES, null, null, null, null, null, null);
      try {
        assertThat(cursor.moveToFirst()).isTrue();
        assertThat(cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.COLUMN_GAME_RANDOM)))
            .isEqualTo(11111L);
      } finally {
        cursor.close();
      }
    } finally {
      db.close();
    }
  }

  @Test
  public void testUpgradeFromVersion4() {
    SQLiteDatabase db = SQLiteDatabase.create(null);
    try {
      // Create version 4 schema
      db.execSQL(
          "CREATE TABLE games(game_id INTEGER PRIMARY KEY AUTOINCREMENT, game_state TEXT, game_random INTEGER, cards_in_play BLOB, cards_in_deck BLOB, time_elapsed INTEGER, date INTEGER)");
      db.execSQL(
          "CREATE TABLE arcade_games(game_id INTEGER PRIMARY KEY AUTOINCREMENT, game_state TEXT, game_random INTEGER, cards_in_play BLOB, cards_in_deck BLOB, time_elapsed INTEGER, date INTEGER, num_triples_found INTEGER)");

      // Trigger upgrade
      dbAdapter.onUpgrade(db, 4, 7);

      assertColumnExists(db, DBAdapter.TABLE_CLASSIC_GAMES, DBAdapter.COLUMN_TRIPLE_FIND_TIMES);
      assertColumnExists(db, DBAdapter.TABLE_ARCADE_GAMES, DBAdapter.COLUMN_TRIPLE_FIND_TIMES);
      assertColumnExists(db, DBAdapter.TABLE_CLASSIC_GAMES, DBAdapter.COLUMN_HINTS_USED);
      assertColumnExists(db, DBAdapter.TABLE_ARCADE_GAMES, DBAdapter.COLUMN_HINTS_USED);
      assertTableExists(db, DBAdapter.TABLE_DAILY_GAMES);
    } finally {
      db.close();
    }
  }

  @Test
  public void testUpgradeFromVersion5() {
    SQLiteDatabase db = SQLiteDatabase.create(null);
    try {
      // Create version 5 schema
      db.execSQL(
          "CREATE TABLE games(game_id INTEGER PRIMARY KEY AUTOINCREMENT, game_state TEXT, game_random INTEGER, cards_in_play BLOB, cards_in_deck BLOB, time_elapsed INTEGER, date INTEGER, triple_find_times BLOB)");
      db.execSQL(
          "CREATE TABLE arcade_games(game_id INTEGER PRIMARY KEY AUTOINCREMENT, game_state TEXT, game_random INTEGER, cards_in_play BLOB, cards_in_deck BLOB, time_elapsed INTEGER, date INTEGER, num_triples_found INTEGER, triple_find_times BLOB)");

      // Trigger upgrade
      dbAdapter.onUpgrade(db, 5, 7);

      assertColumnExists(db, DBAdapter.TABLE_CLASSIC_GAMES, DBAdapter.COLUMN_HINTS_USED);
      assertColumnExists(db, DBAdapter.TABLE_ARCADE_GAMES, DBAdapter.COLUMN_HINTS_USED);
      assertTableExists(db, DBAdapter.TABLE_DAILY_GAMES);
    } finally {
      db.close();
    }
  }

  @Test
  public void testUpgradeFromVersion6() {
    SQLiteDatabase db = SQLiteDatabase.create(null);
    try {
      // Create version 6 schema
      db.execSQL(
          "CREATE TABLE games(game_id INTEGER PRIMARY KEY AUTOINCREMENT, game_state TEXT, game_random INTEGER, cards_in_play BLOB, cards_in_deck BLOB, time_elapsed INTEGER, date INTEGER, triple_find_times BLOB, hints_used INTEGER)");
      db.execSQL(
          "CREATE TABLE arcade_games(game_id INTEGER PRIMARY KEY AUTOINCREMENT, game_state TEXT, game_random INTEGER, cards_in_play BLOB, cards_in_deck BLOB, time_elapsed INTEGER, date INTEGER, num_triples_found INTEGER, triple_find_times BLOB, hints_used INTEGER)");

      // Trigger upgrade
      dbAdapter.onUpgrade(db, 6, 7);

      // Verify Daily games table exists
      assertTableExists(db, DBAdapter.TABLE_DAILY_GAMES);
    } finally {
      db.close();
    }
  }

  private void assertTableExists(SQLiteDatabase db, String tableName) {
    Cursor cursor =
        db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'", null);
    try {
      assertThat(cursor.getCount()).isEqualTo(1);
    } finally {
      cursor.close();
    }
  }

  private void assertColumnExists(SQLiteDatabase db, String tableName, String columnName) {
    Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
    try {
      boolean exists = false;
      while (cursor.moveToNext()) {
        if (cursor.getString(cursor.getColumnIndexOrThrow("name")).equals(columnName)) {
          exists = true;
          break;
        }
      }
      assertThat(exists).isTrue();
    } finally {
      cursor.close();
    }
  }
}
