package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.format.DateUtils;
import android.view.ViewStub;
import android.widget.TextView;

import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnTimerTickListener;
import com.google.android.gms.games.PlayGames;
import com.google.common.collect.ImmutableList;

import java.util.concurrent.TimeUnit;

/** Classic Game */
public class ClassicGameActivity extends BaseGameActivity
    implements OnTimerTickListener, Game.OnUpdateCardsInPlayListener {

  private ClassicGame mGame;
  private Application mApplication;

  @Override
  protected void init(Bundle savedInstanceState) {
    mApplication = Application.getInstance(this);

    if (getIntent().hasExtra(Game.ID_TAG)) {
      // We are being created from the game list.
      mGame = mApplication.getClassicGame(getIntent().getLongExtra(Game.ID_TAG, 0));
    } else if (savedInstanceState != null) {
      // We are being restored
      mGame = mApplication.getClassicGame(savedInstanceState.getLong(Game.ID_TAG));
    } else {
      throw new IllegalArgumentException("No savedInstanceState or intent containing key");
    }

    ViewStub stub = (ViewStub) findViewById(R.id.status_bar);
    stub.setLayoutResource(R.layout.classic_statusbar);
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
    mApplication.saveClassicGame(mGame);
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
    timer.setText(DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(elapsedTime)));
  }

  @Override
  public void onUpdateCardsInPlay(
      ImmutableList<Card> newCards,
      ImmutableList<Card> oldCards,
      int numRemaining,
      int numTriplesFound) {
    TextView numRemainingText = (TextView) findViewById(R.id.cards_remaining_text);
    numRemainingText.setText(String.valueOf(numRemaining));
  }

  @Override
  public void onCardHinted(Card hintedCard) {}

  protected void submitScore() {
    if (mGame.getGameState() != Game.GameState.COMPLETED || mGame.areHintsUsed()) {
      return;
    }
    PlayGames.getLeaderboardsClient(this)
        .submitScore(GamesServices.Leaderboard.CLASSIC, mGame.getTimeElapsed());
  }

  @Override
  protected Intent createNewGame() {
    ClassicGame game = ClassicGame.createFromSeed(System.currentTimeMillis());
    mApplication.addClassicGame(game);
    Intent newGameIntent = new Intent(getBaseContext(), ClassicGameActivity.class);
    newGameIntent.putExtra(Game.ID_TAG, game.getId());
    return newGameIntent;
  }
}
