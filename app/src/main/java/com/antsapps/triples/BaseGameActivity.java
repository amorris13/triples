vpackage com.antsapps.triples;

import android.content.DialogInterface;
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
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ViewAnimator;

import androidx.core.view.MenuItemCompat;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.ArcadeStatistics;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.ClassicStatistics;
import com.antsapps.triples.backend.DatePeriod;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Game.GameState;
import com.antsapps.triples.backend.Game.OnUpdateGameStateListener;
import com.antsapps.triples.backend.Period;
import com.antsapps.triples.cardsview.CardsView;
import com.antsapps.triples.stats.TimelineView;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class BaseGameActivity extends BaseTriplesActivity
    implements OnUpdateGameStateListener {

  public static final int VIEW_CARDS = 0;
  public static final int VIEW_PAUSED = 1;
  public static final int VIEW_COMPLETED = 2;

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

    mCardsView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            // Ensure width and height are greater than 0 before refreshing drawables
            if (mCardsView.getWidth() > 0 && mCardsView.getHeight() > 0) {
                mCardsView.refreshDrawables();
                mCardsView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        }
    });

    mViewAnimator = findViewById(R.id.view_switcher);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    getGame().begin();

    findViewById(R.id.bottom_separator).setBackgroundColor(getAccentColor());
    ((TextView) findViewById(R.id.paused)).setTextColor(getAccentColor());
    findViewById(R.id.rate_app).setBackgroundTintList(android.content.res.ColorStateList.valueOf(getAccentColor()));
    findViewById(R.id.statistics_button).setBackgroundTintList(android.content.res.ColorStateList.valueOf(getAccentColor()));
    findViewById(R.id.new_game_button).setBackgroundTintList(android.content.res.ColorStateList.valueOf(getAccentColor()));

    if (originalGameState == GameState.STARTING) {
      mCardsView.shouldSlideIn();
    }

    mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
  }

  protected abstract Game getGame();

  protected abstract int getAccentColor();

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

    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    boolean hideHint = sharedPref.getBoolean(getString(R.string.pref_hide_hint), false);
    menu.findItem(R.id.hint).setVisible(!hideHint);

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
      handleHintSelection();
      return true;
    } else if (itemId == R.id.pause) {
      getGame().pause();
      logGameEvent(AnalyticsConstants.Event.PAUSE_GAME);
      return true;
    } else if (itemId == R.id.play) {
      getGame().resume();
      logGameEvent(AnalyticsConstants.Event.RESUME_GAME);
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
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void handleHintSelection() {
    final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    if (sharedPref.getBoolean(getString(R.string.pref_dont_ask_for_hint), false)) {
      getGame().addHint();
      logGameEvent(AnalyticsConstants.Event.USE_HINT);
      updateHintUsedIndicator();
    } else {
      View checkBoxView = View.inflate(this, R.layout.remember_checkbox, null);
      final CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
      checkBox.setText(R.string.dont_ask_again);

      MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
      builder.setTitle(R.string.hint_confirmation_title);
      builder.setMessage(R.string.hint_confirmation_message);
      builder.setView(checkBoxView);
      builder.setPositiveButton(
          R.string.yes,
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              if (checkBox.isChecked()) {
                sharedPref
                    .edit()
                    .putBoolean(getString(R.string.pref_dont_ask_for_hint), true)
                    .commit();
              }
              getGame().addHint();
              logGameEvent(AnalyticsConstants.Event.USE_HINT);
              updateHintUsedIndicator();
            }
          });
      builder.setNegativeButton(
          R.string.no,
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              // do nothing
            }
          });
      builder.show();
    }
  }

  private void updateHintUsedIndicator() {
    View hintUsedIndicator = findViewById(R.id.hint_used_text);
    if (hintUsedIndicator != null) {
      hintUsedIndicator.setVisibility(getGame().areHintsUsed() ? View.VISIBLE : View.GONE);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    getGame().resumeFromLifecycle();

    updateHintUsedIndicator();

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
    if (mCardsView.getWidth() > 0 && mCardsView.getHeight() > 0) {
      mCardsView.refreshDrawables();
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
    logGameEvent(AnalyticsConstants.Event.FINISH_GAME);
    if (isSignedIn()) {
      submitScore();
      AchievementManager.awardAchievementsForGame(this, getGame());
      AchievementManager.awardCountAchievements(this, Application.getInstance(this));
    } else {
      shouldSubmitScoreOnSignIn = true;
    }
  }

  protected abstract void submitScore();

  private void updateViewSwitcher() {
    int childToDisplay = VIEW_CARDS;
    if (mGameState == GameState.COMPLETED) {
      childToDisplay = VIEW_COMPLETED;
      updateStatistics();
    } else if (mGameState == GameState.PAUSED || !getGame().getActivityLifecycleActive()) {
      childToDisplay = VIEW_PAUSED;
    } else {
      childToDisplay = VIEW_CARDS;
    }
    if (mViewAnimator.getDisplayedChild() != childToDisplay) {
      mViewAnimator.setDisplayedChild(childToDisplay);
    }
  }

  private static String formatElapsedTime(long elapsedMillis) {
    long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis);
    if (seconds < 3600) {
      return String.format("%d:%02d", seconds / 60, seconds % 60);
    } else {
      return DateUtils.formatElapsedTime(seconds);
    }
  }

  private void updateStatistics() {
    List<Long> findTimes = getGame().getTripleFindTimes();
    if (!findTimes.isEmpty()) {
      long fastest = Long.MAX_VALUE;
      long slowest = 0;
      int fastestIndex = -1;
      int slowestIndex = -1;
      long lastTime = 0;
      for (int i = 0; i < findTimes.size(); i++) {
        long time = findTimes.get(i);
        long duration = time - lastTime;
        if (duration < fastest) {
          fastest = duration;
          fastestIndex = i;
        }
        if (duration > slowest) {
          slowest = duration;
          slowestIndex = i;
        }
        lastTime = time;
      }

      TextView outputTv = (TextView) findViewById(R.id.game_output);
      Game game = getGame();
      if (game instanceof ClassicGame) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(game.getTimeElapsed());
        if (seconds < 60) {
          outputTv.setText(getString(R.string.classic_completed_stats_seconds_format, seconds));
        } else {
          outputTv.setText(getString(R.string.classic_completed_stats_format, seconds / 60, seconds % 60));
        }
      } else if (game instanceof ArcadeGame) {
        outputTv.setText(getString(R.string.arcade_completed_stats, ((ArcadeGame) game).getNumTriplesFound()));
      }

      TextView fastestTv = (TextView) findViewById(R.id.fastest_triple);
      fastestTv.setText(formatElapsedTime(fastest));
      fastestTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.green_dot, 0, 0, 0);
      fastestTv.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.triple_dot_padding));

      TextView slowestTv = (TextView) findViewById(R.id.slowest_triple);
      slowestTv.setText(formatElapsedTime(slowest));
      slowestTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.red_dot, 0, 0, 0);
      slowestTv.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.triple_dot_padding));

      TimelineView timelineView = (TimelineView) findViewById(R.id.timeline);
      timelineView.setTripleFindTimes(findTimes, getGame().getTimeElapsed(), fastestIndex, slowestIndex);

      updatePerformanceDescription();
    }
  }

  private void updatePerformanceDescription() {
    TextView performanceTv = (TextView) findViewById(R.id.performance_description);
    Game game = getGame();
    if (game.areHintsUsed()) {
      performanceTv.setText(R.string.performance_hints_used);
      return;
    }

    Application app = Application.getInstance(this);
    if (game instanceof ArcadeGame) {
      ArcadeStatistics allTimeStats = app.getArcadeStatistics(Period.ALL_TIME);
      if (allTimeStats.getNumGames() <= 1) {
        performanceTv.setText(R.string.performance_first_game);
        return;
      }

      int currentFound = ((ArcadeGame) game).getNumTriplesFound();
      if (currentFound >= allTimeStats.getMostFound()) {
        performanceTv.setText(R.string.performance_arcade_new_best);
      } else if (currentFound >= app.getArcadeStatistics(DatePeriod.fromTimePeriod(TimeUnit.DAYS.toMillis(365))).getMostFound()) {
        performanceTv.setText(R.string.performance_arcade_best_year);
      } else if (currentFound >= app.getArcadeStatistics(DatePeriod.fromTimePeriod(TimeUnit.DAYS.toMillis(30))).getMostFound()) {
        performanceTv.setText(R.string.performance_arcade_best_month);
      } else if (currentFound >= app.getArcadeStatistics(DatePeriod.fromTimePeriod(TimeUnit.DAYS.toMillis(7))).getMostFound()) {
        performanceTv.setText(R.string.performance_arcade_best_week);
      } else if (currentFound >= app.getArcadeStatistics(DatePeriod.fromTimePeriod(TimeUnit.DAYS.toMillis(1))).getMostFound()) {
        performanceTv.setText(R.string.performance_arcade_best_day);
      } else if (currentFound > allTimeStats.getAverageFound()) {
        performanceTv.setText(R.string.performance_arcade_better_than_average);
      } else {
        performanceTv.setText(R.string.performance_arcade_worse_than_average);
      }
    } else if (game instanceof ClassicGame) {
      ClassicStatistics allTimeStats = app.getClassicStatistics(Period.ALL_TIME);
      if (allTimeStats.getNumGames() <= 1) {
        performanceTv.setText(R.string.performance_first_game);
        return;
      }

      long currentTime = game.getTimeElapsed();
      if (currentTime <= allTimeStats.getFastestTime()) {
        performanceTv.setText(R.string.performance_classic_new_best);
      } else if (currentTime <= app.getClassicStatistics(DatePeriod.fromTimePeriod(TimeUnit.DAYS.toMillis(365))).getFastestTime()) {
        performanceTv.setText(R.string.performance_classic_best_year);
      } else if (currentTime <= app.getClassicStatistics(DatePeriod.fromTimePeriod(TimeUnit.DAYS.toMillis(30))).getFastestTime()) {
        performanceTv.setText(R.string.performance_classic_best_month);
      } else if (currentTime <= app.getClassicStatistics(DatePeriod.fromTimePeriod(TimeUnit.DAYS.toMillis(7))).getFastestTime()) {
        performanceTv.setText(R.string.performance_classic_best_week);
      } else if (currentTime <= app.getClassicStatistics(DatePeriod.fromTimePeriod(TimeUnit.DAYS.toMillis(1))).getFastestTime()) {
        performanceTv.setText(R.string.performance_classic_best_day);
      } else if (currentTime < allTimeStats.getAverageTime()) {
        performanceTv.setText(R.string.performance_classic_better_than_average);
      } else {
        performanceTv.setText(R.string.performance_classic_worse_than_average);
      }
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
    logGameEvent(AnalyticsConstants.Event.NEW_GAME);
    startActivity(newGameIntent);
    finish();
  }

  protected abstract Intent createNewGame();

  public void showStatistics(View view) {
    Intent intent = new Intent(this, StatisticsActivity.class);
    intent.putExtra(
        StatisticsActivity.GAME_TYPE,
        getGame() instanceof ArcadeGame ? "Arcade" : "Classic");
    startActivity(intent);
  }

  public void rateApp(View view) {
    ReviewManager manager = ReviewManagerFactory.create(this);
    manager.requestReviewFlow().addOnCompleteListener(task -> {
      if (task.isSuccessful()) {
        // We can get the ReviewInfo object
        ReviewInfo reviewInfo = task.getResult();
        manager.launchReviewFlow(this, reviewInfo);
      } else {
        // There was some problem, log or handle
        Log.e("BaseGameActivity", "In-app review request failed", task.getException());
      }
    });
  }

  private void logGameEvent(String event) {
    Bundle bundle = new Bundle();
    bundle.putString(AnalyticsConstants.Param.GAME_TYPE, getGame().getGameTypeForAnalytics());
    mFirebaseAnalytics.logEvent(event, bundle);
  }
}
