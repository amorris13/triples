package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.format.DateUtils;
import android.view.ViewStub;
import android.widget.TextView;
import android.widget.Toast;

import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.ArcadeStatistics;
import com.antsapps.triples.backend.DatePeriod;
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

  @Override
  public void onCardHinted(Card hintedCard) {}

  @Override
  protected void showSuccessToast() {
    if (mGame.areHintsUsed()) {
      return;
    }

    String message = null;
    if (isNewBest(Period.ALL_TIME)) {
      message = "Congratulations! That's your best score ever.";
    } else if (isNewBest(DatePeriod.fromTimePeriod(DateUtils.WEEK_IN_MILLIS))) {
      message = "Well Done! That's your best score this week.";
    } else if (isNewBest(DatePeriod.fromTimePeriod(DateUtils.DAY_IN_MILLIS))) {
      message = "Nice! That's your best score today.";
    } else {
      message = "You've done better today - keep trying!";
    }

    Toast.makeText(ArcadeGameActivity.this, message, Toast.LENGTH_LONG).show();
  }

  private boolean isNewBest(Period period) {
    ArcadeStatistics stats = mApplication.getArcadeStatistics(period, mGame.getId());
    return stats.getNumGames() == 0 || mGame.getNumTriplesFound() > stats.getMostFound();
  }

  protected void submitScore() {
    if (mGame.getGameState() != Game.GameState.COMPLETED || mGame.areHintsUsed()) {
      return;
    }

    PlayGames.getLeaderboardsClient(this)
        .submitScore(GamesServices.Leaderboard.ARCADE, mGame.getNumTriplesFound());
  }

  @Override
  protected Intent createNewGame() {
    ArcadeGame game = ArcadeGame.createFromSeed(System.currentTimeMillis());
    mApplication.addArcadeGame(game);
    Intent newGameIntent = new Intent(getBaseContext(), ArcadeGameActivity.class);
    newGameIntent.putExtra(Game.ID_TAG, game.getId());
    return newGameIntent;
  }
}
