import 'dart:async';
import 'dart:typed_data';
import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';
import '../models/card.dart';
import '../engine/game.dart';
import '../engine/classic_game.dart';
import '../engine/arcade_game.dart';
import '../engine/zen_game.dart';
import '../engine/daily_game.dart';
import '../engine/deck.dart';
import 'utils.dart';

class DatabaseHelper {
  static final DatabaseHelper _instance = DatabaseHelper._internal();
  static Database? _database;

  factory DatabaseHelper() => _instance;

  DatabaseHelper._internal();

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  Future<Database> _initDatabase() async {
    final dbPath = await getDatabasesPath();
    final path = join(dbPath, 'triples.db');

    return await openDatabase(
      path,
      version: 1,
      onCreate: _onCreate,
    );
  }

  Future<void> _onCreate(Database db, int version) async {
    await db.execute('''
      CREATE TABLE classic_games (
        game_id INTEGER PRIMARY KEY AUTOINCREMENT,
        game_state TEXT,
        game_random INTEGER,
        cards_in_play BLOB,
        cards_in_deck BLOB,
        time_elapsed INTEGER,
        date_started INTEGER,
        triple_find_times BLOB,
        hints_used INTEGER,
        found_triples BLOB
      )
    ''');

    await db.execute('''
      CREATE TABLE arcade_games (
        game_id INTEGER PRIMARY KEY AUTOINCREMENT,
        game_state TEXT,
        game_random INTEGER,
        cards_in_play BLOB,
        cards_in_deck BLOB,
        time_elapsed INTEGER,
        date_started INTEGER,
        triple_find_times BLOB,
        hints_used INTEGER,
        found_triples BLOB
      )
    ''');

    await db.execute('''
      CREATE TABLE zen_games (
        game_id INTEGER PRIMARY KEY AUTOINCREMENT,
        game_state TEXT,
        game_random INTEGER,
        cards_in_play BLOB,
        cards_in_deck BLOB,
        time_elapsed INTEGER,
        date_started INTEGER,
        triple_find_times BLOB,
        hints_used INTEGER,
        found_triples BLOB,
        is_beginner INTEGER
      )
    ''');

    await db.execute('''
      CREATE TABLE daily_games (
        game_id INTEGER PRIMARY KEY AUTOINCREMENT,
        game_state TEXT,
        game_random INTEGER,
        cards_in_play BLOB,
        time_elapsed INTEGER,
        date_started INTEGER,
        daily_game_date TEXT,
        found_triples BLOB,
        triple_find_times BLOB,
        hints_used INTEGER,
        date_completed INTEGER
      )
    ''');
  }

  Future<int> insertClassicGame(ClassicGame game) async {
    final db = await database;
    return await db.insert('classic_games', {
      'game_state': game.gameState.name,
      'game_random': game.randomSeed,
      'cards_in_play': Utils.cardListToByteArray(game.cardsInPlay),
      'cards_in_deck': Utils.cardListToByteArray(game.deck.cards),
      'time_elapsed': game.timeElapsed,
      'date_started': game.dateStarted.millisecondsSinceEpoch,
      'triple_find_times': Utils.intListToByteArray(game.tripleFindTimes),
      'hints_used': game.hintsUsed ? 1 : 0,
      'found_triples': Utils.triplesListToByteArray(game.foundTriples),
    });
  }

  Future<List<ClassicGame>> getClassicGames() async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query('classic_games');

    return List.generate(maps.length, (i) {
      return ClassicGame(
        id: maps[i]['game_id'],
        gameState: GameState.values.firstWhere((e) => e.name == maps[i]['game_state']),
        randomSeed: maps[i]['game_random'],
        cardsInPlay: Utils.cardListFromByteArray(maps[i]['cards_in_play'] as Uint8List),
        deck: Deck.fromCards(Utils.cardListFromByteArray(maps[i]['cards_in_deck'] as Uint8List).whereType<Card>().toList()),
        timeElapsed: maps[i]['time_elapsed'],
        dateStarted: DateTime.fromMillisecondsSinceEpoch(maps[i]['date_started']),
        tripleFindTimes: Utils.intListFromByteArray(maps[i]['triple_find_times'] as Uint8List),
        hintsUsed: maps[i]['hints_used'] == 1,
        foundTriples: Utils.triplesListFromByteArray(maps[i]['found_triples'] as Uint8List),
      );
    });
  }
}
