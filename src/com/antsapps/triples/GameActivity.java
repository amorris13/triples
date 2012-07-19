package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Game.GameState;
import com.antsapps.triples.backend.Game.OnUpdateGameStateListener;
import com.google.common.collect.ImmutableList;

public class GameActivity extends SherlockActivity implements
    OnUpdateGameStateListener {
  private Game mGame;
  private ViewSwitcher mViewSwitcher;
  private CardsView mCardsView;
  private GameState mGameState;
  private StatusBar mStatusBar;
  private Application mApplication;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mApplication = Application.getInstance(getApplication());

    if (getIntent().hasExtra(Game.ID_TAG)) {
      // We are being created from the game list.
      mGame = mApplication.getGame(getIntent().getLongExtra(Game.ID_TAG, 0));
    } else if (savedInstanceState != null) {
      // We are being restored
      mGame = mApplication.getGame(savedInstanceState.getLong(Game.ID_TAG));
    } else {
      throw new IllegalArgumentException(
          "No savedInstanceState or intent containing key");
    }

    mGame.addOnUpdateGameStateListener(this);
    mGameState = mGame.getGameState();

    mStatusBar = (StatusBar) findViewById(R.id.status_bar);
    mGame.setOnTimerTickListener(mStatusBar);
    mGame.addOnUpdateGameStateListener(mStatusBar);

    mCardsView = (CardsView) findViewById(R.id.cards_view);
    mCardsView.setGame(mGame);
    mGame.addOnUpdateGameStateListener(mCardsView);

    mViewSwitcher = (ViewSwitcher) findViewById(R.id.view_switcher);
    mViewSwitcher.setDisplayedChild(mGameState == GameState.PAUSED ? 1 : 0);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    mGame.begin();
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    menu.findItem(R.id.pause).setVisible(mGameState == GameState.ACTIVE);
    menu.findItem(R.id.play).setVisible(mGameState == GameState.PAUSED);
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
        mGame.pause();
        return true;
      case R.id.play:
        mGame.resume();
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

  @Override
  protected void onPause() {
    super.onPause();
    mApplication.saveGame(mGame);
    if (mGameState != GameState.COMPLETED) {
      mGame.pause();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (mGameState != GameState.COMPLETED) {
      mGame.resume();
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putLong(Game.ID_TAG, mGame.getId());
  }

  @Override
  public void onUpdateCardsInPlay(ImmutableList<Card> newCards,
      ImmutableList<Card> oldCards, int numRemaining) {
    // Do Nothing
  }

  @Override
  public void onUpdateGameState(GameState state) {
    mGameState = state;
    switch(mGameState) {
      case COMPLETED:
        Toast.makeText(this, R.string.game_over, Toast.LENGTH_LONG).show();
        break;
      case PAUSED:
        mViewSwitcher.setDisplayedChild(1);
        break;
      case ACTIVE:
      case STARTING:
        mViewSwitcher.setDisplayedChild(0);
        break;
    }
    invalidateOptionsMenu();
  }
}