package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.ViewStub;
import android.widget.TextView;

import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnTimerTickListener;
import com.antsapps.triples.backend.TutorialGame;
import com.google.common.collect.ImmutableList;

import java.util.concurrent.TimeUnit;

public class TutorialGameActivity extends BaseGameActivity
    implements OnTimerTickListener, Game.OnUpdateCardsInPlayListener {

  private TutorialGame mGame;
  private Application mApplication;

  @Override
  protected void init(Bundle savedInstanceState) {
    mApplication = Application.getInstance(this);
    mGame = mApplication.getTutorialGame();
    if (mGame == null) {
        mGame = TutorialGame.createFromSeed(System.currentTimeMillis());
        mApplication.setTutorialGame(mGame);
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
    // Tutorial game is not saved to DB
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
    TextView numRemainingText = (TextView) findViewById(R.id.cards_remaining_text);
    numRemainingText.setText(String.valueOf(numRemaining));
  }

  @Override
  public void onCardHinted(Card hintedCard) {}

  @Override
  protected void submitScore() {
    // No scores for tutorial
  }

  @Override
  protected Class<? extends BaseTriplesActivity> getParentClass() {
    return MainActivity.class;
  }

  @Override
  protected Intent createNewGame() {
    TutorialGame game = TutorialGame.createFromSeed(System.currentTimeMillis());
    mApplication.setTutorialGame(game);
    return new Intent(getBaseContext(), TutorialGameActivity.class);
  }
}
