package com.antsapps.triples;

import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game.OnUpdateGameStateListener;
import com.antsapps.triples.backend.OnTimerTickListener;
import com.google.common.collect.ImmutableList;

public class StatusBar extends RelativeLayout implements OnTimerTickListener,
    OnUpdateGameStateListener {

  private final TextView mTimerText;
  private final TextView mCardsRemainingText;

  public StatusBar(Context context, AttributeSet attrs) {
    super(context, attrs);
    ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
        .inflate(R.layout.statusbar, this);
    mTimerText = (TextView) findViewById(R.id.timer_value_text);
    mCardsRemainingText = (TextView) findViewById(R.id.cards_remaining_text);
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
  public void onUpdateCardsInPlay(ImmutableList<Card> newCards,
      ImmutableList<Card> oldCards, int numRemaining) {
    mCardsRemainingText.setText(String.valueOf(numRemaining));
  }

  @Override
  public void onFinish() {
  }
}
