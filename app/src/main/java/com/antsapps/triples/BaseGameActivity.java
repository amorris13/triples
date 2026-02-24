package com.antsapps.triples;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.ActionBar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Game.GameState;
import com.antsapps.triples.backend.Game.OnUpdateGameStateListener;
import com.antsapps.triples.cardsview.CardsView;
import com.antsapps.triples.stats.TimelineView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class BaseGameActivity extends BaseTriplesActivity
    implements OnUpdateGameStateListener {

  public static final int VIEW_CARDS = 0;
  public static final int VIEW_PAUSED = 1;
  public static final int VIEW_COMPLETED = 2;

  private FirebaseAnalytics mFirebaseAnalytics;

  private ViewAnimator mViewAnimator;
  private CardsView mCardsView;
  private GameState mGameState;

  private boolean shouldSubmitScoreOnSignIn = false;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.game);

    init(savedInstanceState);

    getGame().addOnUpdateGameStateListener(this);
    GameState originalGameState = getGame().getGameState();

    mCardsView = (CardsView) findViewById(R.id.cards_view);
    mCardsView.setOnValidTripleSelectedListener(getGame());
    mCardsView.setEnabled(originalGameState != GameState.COMPLETED);
    getGame().setGameRenderer(mCardsView);

    mViewAnimator = findViewById(R.id.view_switcher);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    getGame().begin();

    if (originalGameState == GameState.STARTING) {
      mCardsView.shouldSlideIn();
    }

    mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
  }

  protected abstract Game getGame();

  /**
   * This must initialize the game (so that getGame() doesn't return null) and set the content view
   * with a layout that contains a StatusBar (R.id.status_bar) and a CardsView (R.id.cards_view).
   *
   * @param savedInstanceState
   */
  protected abstract void init(Bundle savedInstanceState);

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    menu.findItem(R.id.pause).setVisible(mGameState == GameState.ACTIVE);
    menu.findItem(R.id.play).setVisible(mGameState == GameState.PAUSED);
    return true;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.game, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    int itemId = item.getItemId();
    if (itemId == R.id.hint) {
      getGame().addHint();
      return true;
    } else if (itemId == R.id.pause) {
      getGame().pause();
      return true;
    } else if (itemId == R.id.play) {
      getGame().resume();
      return true;
    } else if (itemId == R.id.help) {
      Intent helpIntent = new Intent(getBaseContext(), HelpActivity.class);
      startActivity(helpIntent);
      return true;
    } else if (itemId == R.id.settings) {
      Intent settingsIntent = new Intent(getBaseContext(), SettingsActivity.class);
      startActivity(settingsIntent);
      return true;
    } else if (itemId == android.R.id.home) {// app icon in action bar clicked; go up one level
      Intent intent = new Intent(this, getParentClass());
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  protected abstract Class<? extends BaseTriplesActivity> getParentClass();

  @Override
  protected void onResume() {
    super.onResume();
    getGame().resumeFromLifecycle();

    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    if (sharedPref.getBoolean(getString(R.string.pref_screen_lock), true)) {
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    if (sharedPref.getBoolean(getString(R.string.pref_orientation_lock), false)) {
      String orientation = sharedPref.getString(getString(R.string.pref_orientation), "portrait");
      if (orientation.equals("portrait")) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      } else if (orientation.equals("landscape")) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      }
    } else {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }
    updateViewSwitcher();
  }

  @Override
  protected void onPause() {
    super.onPause();
    saveGame();
    getGame().pauseFromLifecycle();
    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    updateViewSwitcher();
  }

  protected abstract void saveGame();

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putLong(Game.ID_TAG, getGame().getId());
  }

  @Override
  protected void onDestroy() {
    getGame().setGameRenderer(null);
    getGame().removeOnUpdateGameStateListener(this);

    super.onDestroy();
  }

  @Override
  public void onUpdateGameState(GameState state) {
    mGameState = state;

    updateViewSwitcher();

    if (mGameState == GameState.COMPLETED) {
      mCardsView.setAlpha(0.5f);
    }

    invalidateOptionsMenu();
  }

  @Override
  public void gameFinished() {
    Log.i("BaseGameActivity", "game finished");
    updateViewSwitcher();
    logGameFinished();
    if (isSignedIn()) {
      submitScore();
    } else {
      shouldSubmitScoreOnSignIn = true;
    }
  }

  private void logGameFinished() {
    Bundle bundle = new Bundle();
    bundle.putString(AnalyticsConstants.Param.GAME_TYPE, getGame().getGameTypeForAnalytics());
    mFirebaseAnalytics.logEvent(AnalyticsConstants.Event.FINISH_GAME, bundle);
  }

  protected abstract void submitScore();

  private void updateViewSwitcher() {
    int childToDisplay = VIEW_CARDS;
    if (mGameState == GameState.PAUSED || !getGame().getActivityLifecycleActive()) {
      childToDisplay = VIEW_PAUSED;
    } else if (mGameState == GameState.COMPLETED) {
      childToDisplay = VIEW_COMPLETED;
      updateStatistics();
    } else {
      childToDisplay = VIEW_CARDS;
    }
    if (mViewAnimator.getDisplayedChild() != childToDisplay) {
      mViewAnimator.setDisplayedChild(childToDisplay);
    }
  }

  private void updateStatistics() {
    List<Long> findTimes = getGame().getTripleFindTimes();
    if (!findTimes.isEmpty()) {
      long fastest = Long.MAX_VALUE;
      long slowest = 0;
      long lastTime = 0;
      for (long time : findTimes) {
        long duration = time - lastTime;
        fastest = Math.min(fastest, duration);
        slowest = Math.max(slowest, duration);
        lastTime = time;
      }

      ((TextView) findViewById(R.id.fastest_triple))
          .setText(DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(fastest)));
      ((TextView) findViewById(R.id.slowest_triple))
          .setText(DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(slowest)));

      ((TimelineView) findViewById(R.id.timeline))
          .setTripleFindTimes(findTimes, getGame().getTimeElapsed());
    }
  }

  @Override
  public void onSignInFailed() {}

  @Override
  public void onSignInSucceeded() {
    if (mGameState == GameState.COMPLETED && shouldSubmitScoreOnSignIn) {
      submitScore();
    }
    shouldSubmitScoreOnSignIn = false;
  }

  @Override
  public void onSignOut() {}

  public void newGame(View view) {
    Intent newGameIntent = createNewGame();
    logNewGame();
    startActivity(newGameIntent);
  }

  protected abstract Intent createNewGame();

  private void logNewGame() {
    Bundle bundle = new Bundle();
    bundle.putString(AnalyticsConstants.Param.GAME_TYPE, getGame().getGameTypeForAnalytics());
    mFirebaseAnalytics.logEvent(AnalyticsConstants.Event.NEW_GAME, bundle);
  }
}
