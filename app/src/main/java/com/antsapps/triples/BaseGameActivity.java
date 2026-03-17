package com.antsapps.triples;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import androidx.appcompat.app.ActionBar;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Game.GameState;
import com.antsapps.triples.backend.Game.OnUpdateCardsInPlayListener;
import com.antsapps.triples.backend.Game.OnUpdateGameStateListener;
import com.antsapps.triples.cardsview.CardsView;
import com.antsapps.triples.cardsview.CardsView.OnIncorrectTripleSelectedListener;
import com.antsapps.triples.stats.TimelineView;
import com.antsapps.triples.util.AnalyticsUtil;
import com.antsapps.triples.views.TripleExplanationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class BaseGameActivity extends BaseTriplesActivity
    implements OnUpdateGameStateListener,
        OnUpdateCardsInPlayListener,
        OnIncorrectTripleSelectedListener {

  public static final int VIEW_CARDS = 0;
  public static final int VIEW_PAUSED = 1;
  public static final int VIEW_COMPLETED = 2;

  private ViewAnimator mViewAnimator;
  protected CardsView mCardsView;
  private GameState mGameState;

  protected TripleExplanationView mExplanationView;

  private boolean shouldSubmitScoreOnSignIn = false;

  private int mIncorrectConsecutiveCount = 0;
  private boolean mHasShownExplanationSnackbar = false;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.game);

    mCardsView = (CardsView) findViewById(R.id.cards_view);
    mViewAnimator = findViewById(R.id.view_switcher);
    mExplanationView = findViewById(R.id.triple_explanation);
    mExplanationView.setNaturalCardDimensionsProvider(mCardsView);

    init(savedInstanceState);

    getGame().addOnUpdateGameStateListener(this);
    GameState originalGameState = getGame().getGameState();

    if (mCardsView.getOnValidTripleSelectedListener() == null) {
      mCardsView.setOnValidTripleSelectedListener(getGame());
    }
    mCardsView.setEnabled(originalGameState != GameState.COMPLETED);
    getGame().setGameRenderer(mCardsView);

    mCardsView
        .getViewTreeObserver()
        .addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
              @Override
              public void onGlobalLayout() {
                // Ensure width and height are greater than 0 before refreshing drawables
                if (mCardsView.getWidth() > 0 && mCardsView.getHeight() > 0) {
                  mCardsView.refreshDrawables();
                  mCardsView.updateBounds();
                  mCardsView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
              }
            });

    mCardsView.setOnSelectionChangedListener(this::onSelectionChanged);
    mCardsView.setOnIncorrectTripleSelectedListener(this);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    findViewById(R.id.bottom_separator).setBackgroundColor(getAccentColor());
    ((TextView) findViewById(R.id.paused)).setTextColor(getAccentColor());
    findViewById(R.id.statistics_button)
        .setBackgroundTintList(ColorStateList.valueOf(getAccentColor()));
    findViewById(R.id.new_game_button)
        .setBackgroundTintList(ColorStateList.valueOf(getAccentColor()));

    mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

    getGame().begin();

    if (originalGameState == GameState.STARTING) {
      mCardsView.shouldSlideIn();
    }
  }

  protected abstract Game getGame();

  protected abstract int getAccentColor();

  protected abstract String getCompletedStats();

  protected abstract void updatePerformanceDescriptionInternal(TextView performanceTv);

  protected abstract String getGameType();

  protected abstract void awardAchievements();

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
    menu.findItem(R.id.shuffle).setVisible(mGameState == GameState.ACTIVE);

    MenuItem explanationItem = menu.findItem(R.id.explanation);
    explanationItem.setChecked(mExplanationView.getVisibility() == View.VISIBLE);

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
    } else if (itemId == R.id.shuffle) {
      getGame().shuffleCardsInPlay();
      logGameEvent(AnalyticsConstants.Event.SHUFFLE_CARDS);
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
    } else if (itemId == R.id.explanation) {
      toggleExplanation(AnalyticsConstants.Param.TRIGGER_SOURCE_MENU);
      return true;
    } else if (itemId == R.id.settings) {
      Intent settingsIntent = new Intent(getBaseContext(), SettingsActivity.class);
      startActivity(settingsIntent);
      return true;
    } else if (itemId == android.R.id.home) { // app icon in action bar clicked; go up one level
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onUpdateCardsInPlay(
      ImmutableList<Card> newCards,
      ImmutableList<Card> oldCards,
      int numRemaining,
      int numTriplesFound) {}

  @Override
  public void animateFoundTriple(Set<Card> triple, boolean hintUsed) {
    mIncorrectConsecutiveCount = 0;
    mCardsView.animateTripleFoundToOffscreen(triple);
    logTripleFoundEvent(hintUsed);
  }

  protected void logTripleFoundEvent(boolean hintUsed) {
    Bundle bundle = new Bundle();
    bundle.putString(AnalyticsConstants.Param.GAME_TYPE, getGame().getGameTypeForAnalytics());
    bundle.putBoolean(AnalyticsConstants.Param.HINT_USED, hintUsed);
    mFirebaseAnalytics.logEvent(AnalyticsConstants.Event.FIND_TRIPLE, bundle);
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
          (dialog, which) -> {
            if (checkBox.isChecked()) {
              sharedPref
                  .edit()
                  .putBoolean(getString(R.string.pref_dont_ask_for_hint), true)
                  .commit();
            }
            getGame().addHint();
            logGameEvent(AnalyticsConstants.Event.USE_HINT);
            updateHintUsedIndicator();
          });
      builder.setNegativeButton(R.string.no, (dialog, which) -> {});
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
    updateStatusBarVisibility();

    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    if (sharedPref.getBoolean(getString(R.string.pref_screen_lock), true)) {
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    if (mCardsView.getWidth() > 0 && mCardsView.getHeight() > 0) {
      mCardsView.refreshDrawables();
    }
    updateViewSwitcher();
  }

  private void updateStatusBarVisibility() {
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    boolean hideTimer = sharedPref.getBoolean(getString(R.string.pref_hide_timer), false);
    boolean hideCount = sharedPref.getBoolean(getString(R.string.pref_hide_count), false);

    updateViewVisibility(R.id.timer_value_text, hideTimer);
    updateViewVisibility(R.id.timer_key_text, hideTimer);

    updateViewVisibility(R.id.cards_remaining_text, hideCount);
    updateViewVisibility(R.id.progress_key_text, hideCount);
    updateViewVisibility(R.id.triples_found_text, hideCount);
    updateViewVisibility(R.id.triples_found_key_text, hideCount);
  }

  private void updateViewVisibility(int viewId, boolean hide) {
    View view = findViewById(viewId);
    if (view != null) {
      view.setVisibility(hide ? View.GONE : View.VISIBLE);
    }
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
    updateHintUsedIndicator();

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
      if (!getGame().areHintsUsed()) {
        awardAchievements();
      }
      Application application = Application.getInstance(this);
      AchievementManager.awardCountAchievements(this, application);
      application.uploadToCloud(this);
    } else {
      shouldSubmitScoreOnSignIn = true;
    }
    requestInAppReview();
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

  protected static String formatElapsedTime(long elapsedMillis) {
    long hours = TimeUnit.MILLISECONDS.toHours(elapsedMillis);
    long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) % 60;
    long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60;
    long hundredths = (elapsedMillis % 1000) / 10;
    if (hours == 0) {
      return String.format("%d:%02d.%02d", minutes, seconds, hundredths);
    } else {
      return String.format("%d:%02d:%02d.%02d", hours, minutes, seconds, hundredths);
    }
  }

  protected String formatClassicCompletedStats(long timeElapsed) {
    long seconds = TimeUnit.MILLISECONDS.toSeconds(timeElapsed);
    if (seconds < 60) {
      return getString(R.string.classic_completed_stats_seconds_format, seconds);
    } else {
      return getString(R.string.classic_completed_stats_format, seconds / 60, seconds % 60);
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
      outputTv.setText(getCompletedStats());

      TextView fastestTv = (TextView) findViewById(R.id.fastest_triple);
      fastestTv.setText(formatElapsedTime(fastest));
      fastestTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.green_dot, 0, 0, 0);
      fastestTv.setCompoundDrawablePadding(
          getResources().getDimensionPixelSize(R.dimen.triple_dot_padding));

      TextView slowestTv = (TextView) findViewById(R.id.slowest_triple);
      slowestTv.setText(formatElapsedTime(slowest));
      slowestTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.red_dot, 0, 0, 0);
      slowestTv.setCompoundDrawablePadding(
          getResources().getDimensionPixelSize(R.dimen.triple_dot_padding));

      TimelineView timelineView = (TimelineView) findViewById(R.id.timeline);
      timelineView.setTripleFindTimes(
          findTimes, getGame().getTimeElapsed(), fastestIndex, slowestIndex);

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

    updatePerformanceDescriptionInternal(performanceTv);
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
    intent.putExtra(StatisticsActivity.GAME_TYPE, getGameType());
    startActivity(intent);
  }

  private void requestInAppReview() {
    ReviewManager manager = ReviewManagerFactory.create(this);
    manager
        .requestReviewFlow()
        .addOnCompleteListener(
            task -> {
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
    AnalyticsUtil.logGameEvent(mFirebaseAnalytics, event, getGame().getGameTypeForAnalytics());
  }

  protected void onSelectionChanged(Set<Card> selectedCards) {
    updateExplanation(selectedCards);
  }

  protected void updateExplanation(Set<Card> selectedCards) {
    mExplanationView.setCards(ImmutableSet.copyOf(selectedCards));
  }

  protected void toggleExplanation(String source) {
    if (mExplanationView.getVisibility() == View.GONE) {
      logExplanationEvent(source);
      mExplanationView.setVisibility(View.VISIBLE);
    } else {
      mExplanationView.setVisibility(View.GONE);
    }
    invalidateOptionsMenu();
  }

  protected void logExplanationEvent(String source) {
    Bundle bundle = new Bundle();
    bundle.putString(AnalyticsConstants.Param.GAME_TYPE, getGame().getGameTypeForAnalytics());
    bundle.putString(AnalyticsConstants.Param.TRIGGER_SOURCE, source);
    mFirebaseAnalytics.logEvent(AnalyticsConstants.Event.SHOW_EXPLANATION, bundle);
  }

  @Override
  public void onIncorrectTripleSelected() {
    mIncorrectConsecutiveCount++;
    if (mIncorrectConsecutiveCount >= 2
        && !mHasShownExplanationSnackbar
        && mExplanationView.getVisibility() == View.GONE) {
      mHasShownExplanationSnackbar = true;
      Snackbar snackbar =
          Snackbar.make(
              findViewById(R.id.view_switcher),
              R.string.incorrect_triples_snackbar_message,
              Snackbar.LENGTH_LONG);
      snackbar.setAction(
          R.string.incorrect_triples_snackbar_action,
          v -> {
            if (mExplanationView.getVisibility() == View.GONE) {
              toggleExplanation(AnalyticsConstants.Param.TRIGGER_SOURCE_SNACKBAR);
            }
          });
      snackbar.show();
    }
  }
}
