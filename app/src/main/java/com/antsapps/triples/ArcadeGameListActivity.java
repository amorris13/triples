package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;

import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.stats.ArcadeStatisticsFragment;
import com.antsapps.triples.stats.BaseStatisticsFragment;

public class ArcadeGameListActivity extends BaseGameListActivity {

  private Application mApplication;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mApplication = Application.getInstance(getApplication());
  }

  @Override
  protected Class<? extends BaseStatisticsFragment> getStatisticsFragmentClass() {
    return ArcadeStatisticsFragment.class;
  }

  @Override
  protected Class<? extends BaseCurrentGameListFragment> getCurrentGamesFragment() {
    return ArcadeCurrentGameListFragment.class;
  }

  @Override
  protected Intent createNewGame() {
    ArcadeGame game = ArcadeGame.createFromSeed(System.currentTimeMillis());
    mApplication.addArcadeGame(game);
    Intent newGameIntent = new Intent(getBaseContext(), ArcadeGameActivity.class);
    newGameIntent.putExtra(Game.ID_TAG, game.getId());
    return newGameIntent;
  }
}