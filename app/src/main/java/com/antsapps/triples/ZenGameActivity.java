package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.ZenGame;

/** Zen/Beginner Game */
public class ZenGameActivity extends BaseGameActivity {

  private ZenGame mGame;
  private Application mApplication;

  @Override
  protected void init(Bundle savedInstanceState) {
    mApplication = Application.getInstance(this);

    if (getIntent().hasExtra(Game.ID_TAG)) {
      mGame = mApplication.getZenGame(getIntent().getLongExtra(Game.ID_TAG, 0));
    } else if (savedInstanceState != null) {
      mGame = mApplication.getZenGame(savedInstanceState.getLong(Game.ID_TAG));
    } else {
      throw new IllegalArgumentException("No savedInstanceState or intent containing key");
    }

    ViewStub stub = (ViewStub) findViewById(R.id.status_bar);
    stub.setVisibility(View.GONE);
  }

  @Override
  protected Game getGame() {
    return mGame;
  }

  @Override
  protected void saveGame() {
    mApplication.saveZenGame(mGame);
  }

  @Override
  protected void submitScore() {
    // Zen mode has no scores/leaderboards
  }

  @Override
  protected Intent createNewGame() {
    ZenGame game = ZenGame.createFromSeed(System.currentTimeMillis(), mGame.isBeginner());
    mApplication.addZenGame(game);
    Intent newGameIntent = new Intent(getBaseContext(), ZenGameActivity.class);
    newGameIntent.putExtra(Game.ID_TAG, game.getId());
    return newGameIntent;
  }
}
