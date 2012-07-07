package com.antsapps.triples;

import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.antsapps.triples.backend.Game;

public class GameActivity extends SherlockActivity {
  private static final String KEY_GAME_STATE = "game_state";
  private Game mGame;
  private CardsView mCardsView;
  private boolean mPaused;
  private StatusBar mStatusBar;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    if (savedInstanceState == null) {
      // We were just launched -- set up a new game
      mGame = Game.createFromSeed(System.currentTimeMillis());
    } else {
      // We are being restored
      Bundle bundle = savedInstanceState.getBundle(KEY_GAME_STATE);
      if (bundle != null) {
        mGame = Game.createFromBundle(bundle);
      } else {
        mGame = Game.createFromSeed(System.currentTimeMillis());
      }
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
        // Intent intent = new Intent(this, RoundList.class);
        // intent.putExtra(Match.ID_TAG, mRound.getMatch().getId());
        // startActivity(intent);
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
    pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    resume();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putBundle(KEY_GAME_STATE, mGame.saveState());
  }

}