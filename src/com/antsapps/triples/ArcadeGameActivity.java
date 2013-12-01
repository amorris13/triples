package com.antsapps.triples;

import java.util.concurrent.TimeUnit;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.ViewStub;
import android.widget.TextView;

import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnTimerTickListener;
import com.google.common.collect.ImmutableList;

/**
 * Arcade Game
 */
public class ArcadeGameActivity extends BaseGameActivity implements OnTimerTickListener,
    Game.OnUpdateCardsInPlayListener {

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
      throw new IllegalArgumentException(
          "No savedInstanceState or intent containing key");
    }

    ViewStub stub = (ViewStub) findViewById(R.id.status_bar);
    stub.setLayoutResource(R.layout.arcade_statusbar);
    stub.inflate();
    mGame.setOnTimerTickListener(this);
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
    getGame().removeOnUpdateCardsInPlayListener(this);
    super.onDestroy();
  }

  @Override
  public void onTimerTick(final long elapsedTime) {
    TextView timer = (TextView) findViewById(R.id.timer_value_text);
    timer.setText(DateUtils.formatElapsedTime(TimeUnit
        .MILLISECONDS
        .toSeconds(elapsedTime)));
  }

  @Override
  public void onUpdateCardsInPlay(ImmutableList<Card> newCards,
                                  ImmutableList<Card> oldCards,
                                  int numRemaining,
                                  int numTriplesFound) {
    TextView numRemainingText = (TextView) findViewById(R.id.cards_remaining_text);
    numRemainingText.setText(String.valueOf(numRemaining));
  }

  protected Class<? extends BaseGameListActivity> getParentClass() {
    return ClassicGameListActivity.class;
  }
}
