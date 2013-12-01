package com.antsapps.triples;

import android.os.Bundle;

import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.Game;

/**
 * Classic Game
 */
public class ClassicGameActivity extends BaseGameActivity {

  private ClassicGame mGame;
  private Application mApplication;

  @Override
  protected Game getGame() {
    return mGame;
  }

  @Override
  protected void init(Bundle savedInstanceState) {
    setContentView(R.layout.classic_game);

    if (getIntent().hasExtra(Game.ID_TAG)) {
      // We are being created from the game list.
      mGame = mApplication.getClassicGame(getIntent().getLongExtra(Game.ID_TAG, 0));
    } else if (savedInstanceState != null) {
      // We are being restored
      mGame = mApplication.getClassicGame(savedInstanceState.getLong(Game.ID_TAG));
    } else {
      throw new IllegalArgumentException(
          "No savedInstanceState or intent containing key");
    }
  }

  @Override
  protected void saveGame() {
    mApplication.saveClassicGame(mGame);
  }

}
