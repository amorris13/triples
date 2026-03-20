import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  late SharedPreferences _prefs;
  bool _hideHints = false;

  @override
  void initState() {
    super.initState();
    _loadSettings();
  }

  Future<void> _loadSettings() async {
    _prefs = await SharedPreferences.getInstance();
    setState(() {
      _hideHints = _prefs.getBool('pref_hide_hint') ?? false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: ListView(
        children: [
          SwitchListTile(
            title: const Text('Hide Hints'),
            subtitle: const Text('Move hint icon to overflow menu'),
            value: _hideHints,
            onChanged: (value) async {
              setState(() {
                _hideHints = value;
              });
              await _prefs.setBool('pref_hide_hint', value);
            },
          ),
          // TODO: Add more settings (Card customization, etc)
        ],
      ),
    );
  }
}
