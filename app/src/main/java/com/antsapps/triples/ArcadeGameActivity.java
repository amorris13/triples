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
import com.antsapps.triples.backend.OnTimerTickListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
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

  protected Class<? extends BaseTriplesActivity> getParentClass() {
    return MainActivity.class;
  }

  protected void submitScore() {
    if (mGame.getGameState() != Game.GameState.COMPLETED) {
      return;
    }

    Games.Leaderboards.submitScoreImmediate(
            mGoogleApiClient, GamesServices.Leaderboard.ARCADE, mGame.getNumTriplesFound())
        .setResultCallback(
            new ResultCallback<Leaderboards.SubmitScoreResult>() {
              @Override
              public void onResult(@NonNull Leaderboards.SubmitScoreResult submitScoreResult) {
                String message = null;
                switch (submitScoreResult.getStatus().getStatusCode()) {
                  case GamesStatusCodes.STATUS_OK:
                    if (submitScoreResult
                        .getScoreData()
                        .getScoreResult(LeaderboardVariant.TIME_SPAN_ALL_TIME)
                        .newBest) {
                      message = "Congratulations! That's your best score ever.";
                    } else if (submitScoreResult
                        .getScoreData()
                        .getScoreResult(LeaderboardVariant.TIME_SPAN_WEEKLY)
                        .newBest) {
                      message = "Well Done! That's your best score this week.";
                    } else if (submitScoreResult
                        .getScoreData()
                        .getScoreResult(LeaderboardVariant.TIME_SPAN_DAILY)
                        .newBest) {
                      message = "Nice! That's your best score today.";
                    } else {
                      message = "You've done better today - keep trying!";
                    }
                    break;
                  case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_DEFERRED:
                    message = "Score will be submitted when next connected.";
                    break;
                  default:
                    message = "Score could not be submitted";
                    break;
                }
                Toast.makeText(ArcadeGameActivity.this, message, Toast.LENGTH_LONG).show();
              }
            });
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
