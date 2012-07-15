package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Game;

public class GameActivity extends SherlockActivity {
  private static final String KEY_GAME_STATE = "game_state";
  private Game mGame;
  private CardsView mCardsView;
  private boolean mPaused;
  private StatusBar mStatusBar;
  private Application mApplication;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mApplication = Application.getInstance(getApplication());

    if (getIntent().hasExtra(Game.ID_TAG)){
      // We are being created from the game list.
      mGame = mApplication.getGame(getIntent().getLongExtra(Game.ID_TAG, 0));
    } else if (savedInstanceState != null) {
      // We are being restored
      mGame = mApplication.getGame(savedInstanceState.getLong(Game.ID_TAG));
    } else {
      throw new IllegalArgumentException(
          "No savedInstanceState or intent containing key");
    }

    mStatusBar = (StatusBar) findViewById(R.id.status_bar);
    mGame.setOnTimerTickListener(mStatusBar);
    mGame.addOnUpdateGameStateListener(mStatusBar);

    mCardsView = (CardsView) findViewById(R.id.cards_view);
    mCardsView.setGame(mGame);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    mGame.begin();
    mPaused = false;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    menu.findItem(R.id.pause).setVisible(!mPaused);
    menu.findItem(R.id.play).setVisible(mPaused);
    return true;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getSupportMenuInflater();
    inflater.inflate(R.menu.game, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.pause:
        pause();
        return true;
      case R.id.play:
        resume();
        return true;
      case android.R.id.home:
        // app icon in action bar clicked; go up one level
        Intent intent = new Intent(this, GameListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void pause() {
    mPaused = true;
    mGame.pause();
    mCardsView.pause();
    invalidateOptionsMenu();
  }

  private void resume() {
    mPaused = false;
    mGame.resume();
    mCardsView.resume();
    invalidateOptionsMenu();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mApplication.saveGame(mGame);
    pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    resume();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putLong(Game.ID_TAG, mGame.getId());
  }
}