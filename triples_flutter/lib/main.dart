import 'package:flutter/material.dart';
import 'screens/home_screen.dart';

void main() {
  runApp(const TriplesApp());
}

class TriplesApp extends StatelessWidget {
  const TriplesApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Triples',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const HomeScreen(),
    );
  }
}
