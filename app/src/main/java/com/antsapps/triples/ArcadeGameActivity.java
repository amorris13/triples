package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.ViewStub;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.ArcadeStatistics;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.DatePeriod;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnTimerTickListener;
import com.antsapps.triples.backend.Period;
import com.google.android.gms.games.PlayGames;
import com.google.common.collect.ImmutableList;
import java.util.concurrent.TimeUnit;

/** Arcade Game */
public class ArcadeGameActivity extends BaseGameActivity
    implements OnTimerTickListener, Game.OnUpdateCardsInPlayListener {

  private ArcadeGame mGame;
  private Application mApplication;

  @Override
  protected void init(Bundle savedInstanceState) {
    mApplication = Application.getInstance(this);

    if (getIntent().hasExtra(Game.ID_TAG)) {
      // We are being created from the game list.
      mGame = mApplication.getArcadeGame(getIntent().getLongExtra(Game.ID_TAG, 0));
    } else if (savedInstanceState != null) {
      // We are being restored
      mGame = mApplication.getArcadeGame(savedInstanceState.getLong(Game.ID_TAG));
    } else {
      throw new IllegalArgumentException("No savedInstanceState or intent containing key");
    }

    ViewStub stub = (ViewStub) findViewById(R.id.status_bar);
    stub.setLayoutResource(R.layout.arcade_statusbar);
    stub.inflate();
    mGame.addOnTimerTickListener(this);
    mGame.addOnUpdateCardsInPlayListener(this);
  }

  @Override
  protected int getAccentColor() {
    return ContextCompat.getColor(this, R.color.arcade_accent);
  }

  @Override
  protected String getCompletedStats() {
    return getString(R.string.arcade_completed_stats, mGame.getNumTriplesFound());
  }

  @Override
  protected void updatePerformanceDescriptionInternal(TextView performanceTv) {
    Application app = Application.getInstance(this);
    ArcadeStatistics allTimeStats = app.getArcadeStatistics(Period.ALL_TIME, true);
    if (allTimeStats.getNumGames() <= 1) {
      performanceTv.setText(R.string.performance_first_game);
      return;
    }

    int currentFound = mGame.getNumTriplesFound();
    allTimeStats = app.getArcadeStatistics(Period.ALL_TIME, false);
    if (currentFound >= allTimeStats.getMostFound()) {
      performanceTv.setText(R.string.performance_arcade_new_best);
    } else if (currentFound
        >= app.getArcadeStatistics(DatePeriod.fromTimePeriod(TimeUnit.DAYS.toMillis(365)), false)
            .getMostFound()) {
      performanceTv.setText(R.string.performance_arcade_best_year);
    } else if (currentFound
        >= app.getArcadeStatistics(DatePeriod.fromTimePeriod(TimeUnit.DAYS.toMillis(30)), false)
            .getMostFound()) {
      performanceTv.setText(R.string.performance_arcade_best_month);
    } else if (currentFound
        >= app.getArcadeStatistics(DatePeriod.fromTimePeriod(TimeUnit.DAYS.toMillis(7)), false)
            .getMostFound()) {
      performanceTv.setText(R.string.performance_arcade_best_week);
    } else if (currentFound
        >= app.getArcadeStatistics(DatePeriod.fromTimePeriod(TimeUnit.DAYS.toMillis(1)), false)
            .getMostFound()) {
      performanceTv.setText(R.string.performance_arcade_best_day);
    } else if (currentFound > allTimeStats.getAverageFound()) {
      performanceTv.setText(R.string.performance_arcade_better_than_average);
    } else {
      performanceTv.setText(R.string.performance_arcade_worse_than_average);
    }
  }

  @Override
  protected String getGameType() {
    return "Arcade";
  }

  @Override
  protected void awardAchievements() {
    AchievementManager.awardArcadeAchievements(this, mGame.getNumTriplesFound());
  }

  @Override
  protected Game getGame() {
    return mGame;
  }

  @Override
  protected void saveGame() {
    mApplication.saveArcadeGame(mGame);
  }

  @Override
  protected void onDestroy() {
    mGame.removeOnUpdateCardsInPlayListener(this);
    mGame.removeOnTimerTickListener(this);
    super.onDestroy();
  }

  @Override
  public void onTimerTick(final long elapsedTime) {
    TextView timer = (TextView) findViewById(R.id.timer_value_text);
    timer.setText(
        DateUtils.formatElapsedTime(
            TimeUnit.MILLISECONDS.toSeconds(ArcadeGame.TIME_LIMIT_MS - elapsedTime)));
  }

  @Override
  public void onUpdateCardsInPlay(
      ImmutableList<Card> newCards,
      ImmutableList<Card> oldCards,
      int numRemaining,
      int numTriplesFound) {
    TextView triplesFound = (TextView) findViewById(R.id.triples_found_text);
    triplesFound.setText(String.valueOf(numTriplesFound));
  }

  protected void submitScore() {
    if (mGame.getGameState() != Game.GameState.COMPLETED || mGame.areHintsUsed()) {
      return;
    }

    PlayGames.getLeaderboardsClient(this)
        .submitScore(getString(R.string.leaderboard_arcade_game), mGame.getNumTriplesFound());
  }

  @Override
  protected Intent createNewGame() {
    ArcadeGame game = ArcadeGame.createFromSeed(Application.getTimeProvider().currentTimeMillis());
    mApplication.addArcadeGame(game);
    Intent newGameIntent = new Intent(getBaseContext(), ArcadeGameActivity.class);
    newGameIntent.putExtra(Game.ID_TAG, game.getId());
    return newGameIntent;
  }
}
