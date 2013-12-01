package com.antsapps.triples;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Period;
import com.antsapps.triples.backend.Statistics;
import com.antsapps.triples.stats.StatisticsFragment;

public class ClassicGameListActivity extends BaseGameListActivity {

  private Application mApplication;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mApplication = Application.getInstance(getApplication());
  }

  @Override
  protected Class<StatisticsFragment> getStatisticsFragmentClass() {
    return StatisticsFragment.class;
  }

  @Override
  protected Class<CurrentGameListFragment> getCurrentGamesFragment() {
    return CurrentGameListFragment.class;
  }

  @Override
  protected Intent createNewGame() {
    ClassicGame game = ClassicGame.createFromSeed(System.currentTimeMillis());
    mApplication.addClassicGame(game);
    Intent newGameIntent = new Intent(getBaseContext(), ClassicGameActivity.class);
    newGameIntent.putExtra(Game.ID_TAG, game.getId());
    return newGameIntent;
  }

  @Override
  protected void uploadExistingTopScoresIfNecessary() {
    SharedPreferences settings = getSharedPreferences(SIGNIN_PREFS, 0);
    if (settings.getBoolean(UPLOADED_EXISTING_TOP_SCORE, false)) {
      return;
    }

    Statistics stats = mApplication.getClassicStatistics(Period.ALL_TIME);
    mHelper.getGamesClient().submitScore(GamesServices.Leaderboard.CLASSIC, stats.getFastestTime());

    // We need an Editor object to make preference changes.
    // All objects are from android.context.Context
    SharedPreferences.Editor editor = settings.edit();
    editor.putBoolean(UPLOADED_EXISTING_TOP_SCORE, true);
    // Commit the edits!
    editor.commit();
  }
}