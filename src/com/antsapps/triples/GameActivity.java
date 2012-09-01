package com.antsapps.triples;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Game.GameState;
import com.antsapps.triples.backend.Game.OnUpdateGameStateListener;

public class GameActivity extends SherlockActivity implements
    OnUpdateGameStateListener {
  private Game mGame;
  private ViewSwitcher mViewSwitcher;
  private GameCardsView mCardsView;
  private GameState mGameState;
  private StatusBar mStatusBar;
  private Application mApplication;
  private PowerManager.WakeLock mWakeLock;

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
    mGame.addOnUpdateCardsInPlayListener(mStatusBar);

    mCardsView = (GameCardsView) findViewById(R.id.cards_view);
    mCardsView.setGame(mGame);
    mGame.addOnUpdateCardsInPlayListener(mCardsView);
    mGame.addOnUpdateGameStateListener(mCardsView);

    mViewSwitcher = (ViewSwitcher) findViewById(R.id.view_switcher);

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
      case R.id.help:
        Intent helpIntent = new Intent(getBaseContext(), HelpActivity.class);
        startActivity(helpIntent);
        return true;
      case R.id.settings:
        Intent settingsIntent = new Intent(getBaseContext(),
            SettingsActivity.class);
        startActivity(settingsIntent);
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
  protected void onResume() {
    super.onResume();
    mGame.resumeFromLifecycle();

    SharedPreferences sharedPref = PreferenceManager
        .getDefaultSharedPreferences(this);
    if (sharedPref.getBoolean(getString(R.string.pref_screen_lock), true)) {
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    if (sharedPref.getBoolean(getString(R.string.pref_orientation_lock), false)) {
      String orientation = sharedPref.getString(
          getString(R.string.pref_orientation),
          "portrait");
      if (orientation.equals("portrait")) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      } else if (orientation.equals("landscape")) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      }
    } else {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    mApplication.saveGame(mGame);
    mGame.pauseFromLifecycle();
    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putLong(Game.ID_TAG, mGame.getId());
  }

  @Override
  protected void onDestroy() {
    mGame.removeOnUpdateGameStateListener(mCardsView);
    mGame.removeOnUpdateCardsInPlayListener(mCardsView);

    mGame.removeOnUpdateCardsInPlayListener(mStatusBar);
    mGame.setOnTimerTickListener(null);

    mGame.removeOnUpdateGameStateListener(this);

    super.onDestroy();
  }

  @Override
  public void onUpdateGameState(GameState state) {
    mGameState = state;

    int childToDisplay = 0;
    switch (mGameState) {
      case PAUSED:
        childToDisplay = 1;
        break;
      case COMPLETED:
      case ACTIVE:
      case STARTING:
        childToDisplay = 0;
        break;
    }
    if (mViewSwitcher.getDisplayedChild() != childToDisplay) {
      mViewSwitcher.setDisplayedChild(childToDisplay);
    }

    if (mGameState == GameState.COMPLETED) {
      mCardsView.setAlpha(0.5f);
      Toast.makeText(this, R.string.game_over, Toast.LENGTH_LONG).show();
    }

    invalidateOptionsMenu();
  }
}
