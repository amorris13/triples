package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.ViewStub;
import android.widget.TextView;

import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnTimerTickListener;
import com.google.common.collect.ImmutableList;

import java.util.concurrent.TimeUnit;

public class DailyGameActivity extends BaseGameActivity
    implements OnTimerTickListener, Game.OnUpdateCardsInPlayListener {

  private DailyGame mGame;
  private Application mApplication;

  @Override
  protected void init(Bundle savedInstanceState) {
    mApplication = Application.getInstance(this);

    if (getIntent().hasExtra(Game.ID_TAG)) {
      mGame = mApplication.getDailyGame(getIntent().getLongExtra(Game.ID_TAG, 0));
    } else if (savedInstanceState != null) {
      mGame = mApplication.getDailyGame(savedInstanceState.getLong(Game.ID_TAG));
    } else {
      throw new IllegalArgumentException("No savedInstanceState or intent containing key");
    }

    ViewStub stub = (ViewStub) findViewById(R.id.status_bar);
    stub.setLayoutResource(R.layout.daily_statusbar);
    stub.inflate();
    mGame.addOnTimerTickListener(this);
    mGame.addOnUpdateCardsInPlayListener(this);
  }

  @Override
  protected int getAccentColor() {
    return androidx.core.content.ContextCompat.getColor(this, R.color.daily_accent);
  }

  @Override
  protected Game getGame() {
    return mGame;
  }

  @Override
  protected void saveGame() {
    mApplication.saveDailyGame(mGame);
  }

  @Override
  protected void onDestroy() {
    if (mGame != null) {
      mGame.removeOnUpdateCardsInPlayListener(this);
      mGame.removeOnTimerTickListener(this);
    }
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
    updateTriplesFoundText();
  }

  private void updateTriplesFoundText() {
    TextView triplesFoundText = (TextView) findViewById(R.id.triples_found_text);
    triplesFoundText.setText(mGame.getNumTriplesFound() + " / " + mGame.getTotalTriplesCount());
  }

  @Override
  protected void onResume() {
    super.onResume();
    updateTriplesFoundText();
  }

  @Override
  public void onCardHinted(Card hintedCard) {}

  @Override
  protected void submitScore() {
    // No leaderboard for daily yet
  }

  @Override
  protected Intent createNewGame() {
    // For daily, "New Game" means start the same daily puzzle again?
    // Usually it would mean tomorrow's, but we only have today's.
    // Let's just return to the same game for now, or the user can just finish it.
    Intent intent = new Intent(this, DailyGameActivity.class);
    intent.putExtra(Game.ID_TAG, mGame.getId());
    return intent;
  }
}
