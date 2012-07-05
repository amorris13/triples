package com.antsapps.triples;

import java.util.concurrent.TimeUnit;

import android.text.format.DateUtils;
import android.widget.TextView;

import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Game.OnUpdateGameStateListener;
import com.antsapps.triples.backend.OnTimerTickListener;

public class StatusBar implements OnTimerTickListener,
    OnUpdateGameStateListener {

  private final TextView mTimerText;
  private final TextView mCardsRemainingText;

  public StatusBar(TextView timerText, TextView cardsRemainingText) {
    mTimerText = timerText;
    mCardsRemainingText = cardsRemainingText;
  }

  @Override
  public void onTimerTick(final long elapsedTime) {
    mTimerText.post(new Runnable() {
      @Override
      public void run() {
        mTimerText.setText(DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS
            .toSeconds(elapsedTime)));
      }
    });
  }

  @Override
  public void onUpdateGameState(Game game) {
    mCardsRemainingText.setText(String.valueOf(game.getCardsRemaining()));
  }

}
