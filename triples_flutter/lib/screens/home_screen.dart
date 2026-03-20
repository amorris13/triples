import 'package:flutter/material.dart';
import 'classic_game_screen.dart';
import 'arcade_game_screen.dart';
import 'zen_game_screen.dart';
import 'daily_game_screen.dart';
import 'statistics_screen.dart';
import 'settings_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Triples'),
        actions: [
          IconButton(
            icon: const Icon(Icons.settings),
            onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (c) => const SettingsScreen())),
          ),
        ],
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(
              onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (c) => const ClassicGameScreen())),
              child: const Text('Classic Mode'),
            ),
            ElevatedButton(
              onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (c) => const ArcadeGameScreen())),
              child: const Text('Arcade Mode'),
            ),
            ElevatedButton(
              onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (c) => const ZenGameScreen())),
              child: const Text('Zen Mode'),
            ),
            ElevatedButton(
              onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (c) => const DailyGameScreen())),
              child: const Text('Daily Mode'),
            ),
            const SizedBox(height: 32),
            ElevatedButton(
              onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (c) => const StatisticsScreen())),
              child: const Text('Statistics'),
            ),
            ElevatedButton(
              onPressed: () => {}, // TODO: Help
              child: const Text('How to Play'),
            ),
          ],
        ),
      ),
    );
  }
}
