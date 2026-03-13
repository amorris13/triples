package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.ZenGame;
import com.antsapps.triples.util.AnalyticsUtil;
import com.google.android.material.button.MaterialButton;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class MainActivity extends BaseTriplesActivity {

  private Application mApplication;
  private MainViewModel mViewModel;

  private MaterialButton mClassicResumeButton;
  private MaterialButton mArcadeResumeButton;
  private MaterialButton mClassicNewGameButton;
  private MaterialButton mArcadeNewGameButton;
  private MaterialButton mDailyPlayButton;
  private MaterialButton mDailyStatisticsButton;
  private MaterialButton mZenButton;
  private MaterialButton mClassicStatisticsButton;
  private MaterialButton mArcadeStatisticsButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayShowHomeEnabled(true);
      getSupportActionBar().setIcon(R.drawable.ic_launcher_foreground_48dp);
    }

    mApplication = Application.getInstance(getApplication());
    mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
    mViewModel.init(mApplication);

    mClassicResumeButton = findViewById(R.id.classic_resume_button);
    mClassicResumeButton.setOnClickListener(
        v ->
            resumeGame(
                mApplication.getCurrentClassicGames(),
                ClassicGameActivity.class,
                ClassicGame.GAME_TYPE_FOR_ANALYTICS));

    mClassicNewGameButton = findViewById(R.id.classic_new_game_button);
    mClassicNewGameButton.setOnClickListener(v -> startNewClassicGame());

    mClassicStatisticsButton = findViewById(R.id.classic_statistics_button);
    mClassicStatisticsButton.setOnClickListener(v -> showStatistics("Classic"));

    mArcadeResumeButton = findViewById(R.id.arcade_resume_button);
    mArcadeResumeButton.setOnClickListener(
        v ->
            resumeGame(
                mApplication.getCurrentArcadeGames(),
                ArcadeGameActivity.class,
                ArcadeGame.GAME_TYPE_FOR_ANALYTICS));

    mArcadeNewGameButton = findViewById(R.id.arcade_new_game_button);
    mArcadeNewGameButton.setOnClickListener(v -> startNewArcadeGame());

    mArcadeStatisticsButton = findViewById(R.id.arcade_statistics_button);
    mArcadeStatisticsButton.setOnClickListener(v -> showStatistics("Arcade"));

    mDailyPlayButton = findViewById(R.id.daily_play_button);
    mDailyPlayButton.setOnClickListener(v -> playDailyGame());

    mDailyStatisticsButton = findViewById(R.id.daily_statistics_button);
    mDailyStatisticsButton.setOnClickListener(v -> showStatistics("Daily"));

    mZenButton = findViewById(R.id.zen_button);
    mZenButton.setOnClickListener(v -> playZenGame(false));

    mViewModel
        .getDailyCompleted()
        .observe(
            this,
            dailyCompleted -> {
              findViewById(R.id.daily_play_button)
                  .setVisibility(dailyCompleted ? View.GONE : View.VISIBLE);
              findViewById(R.id.daily_completed_text)
                  .setVisibility(dailyCompleted ? View.VISIBLE : View.GONE);
            });

    mViewModel
        .getClassicResumeState()
        .observe(
            this,
            state -> {
              if (state.visible) {
                mClassicResumeButton.setVisibility(View.VISIBLE);
                mClassicResumeButton.setText(
                    getString(R.string.resume_game_classic_format, Integer.parseInt(state.text)));
                mClassicNewGameButton.setText(R.string.start_again);
              } else {
                mClassicResumeButton.setVisibility(View.GONE);
                mClassicNewGameButton.setText(R.string.new_game);
              }
            });

    mViewModel
        .getArcadeResumeState()
        .observe(
            this,
            state -> {
              if (state.visible) {
                mArcadeResumeButton.setVisibility(View.VISIBLE);
                mArcadeResumeButton.setText(
                    getString(R.string.resume_game_arcade_format, Integer.parseInt(state.text)));
                mArcadeNewGameButton.setText(R.string.start_again);
              } else {
                mArcadeResumeButton.setVisibility(View.GONE);
                mArcadeNewGameButton.setText(R.string.new_game);
              }
            });

    mViewModel
        .getClassicCompletedCount()
        .observe(
            this,
            count -> {
              mClassicStatisticsButton.setText(getString(R.string.statistics_format, count));
            });

    mViewModel
        .getArcadeCompletedCount()
        .observe(
            this,
            count -> {
              mArcadeStatisticsButton.setText(getString(R.string.statistics_format, count));
            });

    mViewModel
        .getDailyCompletedCount()
        .observe(
            this,
            count -> {
              mDailyStatisticsButton.setText(getString(R.string.statistics_format, count));
            });
  }

  private void launchGame(Intent intent, String gameType, String event) {
    AnalyticsUtil.logGameEvent(mFirebaseAnalytics, event, gameType);
    startActivity(intent);
  }

  private void resumeGame(
      Iterable<? extends Game> currentGames, Class<?> activityClass, String gameType) {
    Game game = Iterables.getFirst(currentGames, null);
    if (game != null) {
      Intent intent = new Intent(this, activityClass);
      intent.putExtra(Game.ID_TAG, game.getId());
      launchGame(intent, gameType, AnalyticsConstants.Event.RESUME_GAME);
    }
  }

  private void startNewClassicGame() {
    for (ClassicGame game : Lists.newArrayList(mApplication.getCurrentClassicGames())) {
      mApplication.deleteClassicGame(game);
    }
    ClassicGame game = ClassicGame.createFromSeed(System.currentTimeMillis());
    mApplication.addClassicGame(game);
    Intent intent = new Intent(this, ClassicGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());
    launchGame(intent, ClassicGame.GAME_TYPE_FOR_ANALYTICS, AnalyticsConstants.Event.NEW_GAME);
  }

  private void playZenGame(boolean isBeginner) {
    Intent intent = new Intent(this, ZenGameActivity.class);
    intent.putExtra(ZenGameActivity.IS_BEGINNER, isBeginner);
    String gameType = ZenGame.GAME_TYPE_FOR_ANALYTICS + (isBeginner ? "_beginner" : "");
    launchGame(intent, gameType, AnalyticsConstants.Event.NEW_GAME);
  }

  private void playDailyGame() {
    DailyGame game = mApplication.getDailyGameForDate(DailyGame.Day.forToday());
    Intent intent = new Intent(this, DailyGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());
    launchGame(intent, DailyGame.GAME_TYPE_FOR_ANALYTICS, AnalyticsConstants.Event.NEW_GAME);
  }

  private void startNewArcadeGame() {
    for (ArcadeGame game : Lists.newArrayList(mApplication.getCurrentArcadeGames())) {
      mApplication.deleteArcadeGame(game);
    }
    ArcadeGame game = ArcadeGame.createFromSeed(System.currentTimeMillis());
    mApplication.addArcadeGame(game);
    Intent intent = new Intent(this, ArcadeGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());
    launchGame(intent, ArcadeGame.GAME_TYPE_FOR_ANALYTICS, AnalyticsConstants.Event.NEW_GAME);
  }

  private void showStatistics(String gameType) {
    Intent intent = new Intent(this, StatisticsActivity.class);
    intent.putExtra(StatisticsActivity.GAME_TYPE, gameType);
    startActivity(intent);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.statistics, menu); // Reuse statistics menu for main activity
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    MenuItem exportItem = menu.findItem(R.id.export_to_csv);
    if (exportItem != null) {
      exportItem.setVisible(false);
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == R.id.help) {
      Intent helpIntent = new Intent(getBaseContext(), HelpActivity.class);
      startActivity(helpIntent);
      return true;
    } else if (itemId == R.id.settings) {
      Intent settingsIntent = new Intent(getBaseContext(), SettingsActivity.class);
      startActivity(settingsIntent);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onSignInSucceeded() {
    super.onSignInSucceeded();
    invalidateOptionsMenu();
  }

  @Override
  public void onSignOut() {
    super.onSignOut();
    invalidateOptionsMenu();
  }
}
