import 'package:games_services/games_services.dart';
import 'package:google_sign_in/google_sign_in.dart';

class GamesServicesManager {
  static final GamesServicesManager _instance = GamesServicesManager._internal();
  factory GamesServicesManager() => _instance;
  GamesServicesManager._internal();

  final GoogleSignIn _googleSignIn = GoogleSignIn(
    scopes: ['email', 'https://www.googleapis.com/auth/games_lite'],
  );

  Future<void> signIn() async {
    try {
      await GamesServices.signIn();
      // Optional: also sign in with Google if needed for other services
      // await _googleSignIn.signIn();
    } catch (e) {
      print('Sign in failed: $e');
    }
  }

  Future<void> unlockAchievement(String achievementId) async {
    try {
      await GamesServices.unlock(achievement: Achievement(androidID: achievementId, iOSID: achievementId));
    } catch (e) {
      print('Unlock achievement failed: $e');
    }
  }

  Future<void> submitScore(String leaderboardId, int score) async {
    try {
      await GamesServices.submitScore(score: Score(androidID: leaderboardId, iOSID: leaderboardId, value: score));
    } catch (e) {
      print('Submit score failed: $e');
    }
  }
}
