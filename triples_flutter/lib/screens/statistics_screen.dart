import 'package:flutter/material.dart';
import '../engine/statistics.dart';
import '../utils/database_helper.dart';

class StatisticsScreen extends StatefulWidget {
  const StatisticsScreen({super.key});

  @override
  State<StatisticsScreen> createState() => _StatisticsScreenState();
}

class _StatisticsScreenState extends State<StatisticsScreen> {
  late Future<Statistics> _statsFuture;

  @override
  void initState() {
    super.initState();
    _statsFuture = _loadStatistics();
  }

  Future<Statistics> _loadStatistics() async {
    final classicGames = await DatabaseHelper().getClassicGames();
    return Statistics(classicGames);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Statistics')),
      body: FutureBuilder<Statistics>(
        future: _statsFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snapshot.hasError) {
            return Center(child: Text('Error: ${snapshot.error}'));
          }
          final stats = snapshot.data!;
          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              _buildStatRow('Games Played', '${stats.numGames}'),
              _buildStatRow('Games Analyzed', '${stats.numGamesWithAnalysis}'),
              const Divider(),
              const Text('Analysis Summary', style: TextStyle(fontWeight: FontWeight.bold)),
              ...stats.getAnalysis().map((a) => ListTile(
                title: Text('Found Triple: ${a.foundTriple.length} cards'),
                subtitle: Text('Duration: ${a.duration / 1000}s, Pattern: ${a.getSummaryLabel()}'),
              )),
            ],
          );
        },
      ),
    );
  }

  Widget _buildStatRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label),
          Text(value, style: const TextStyle(fontWeight: FontWeight.bold)),
        ],
      ),
    );
  }
}
