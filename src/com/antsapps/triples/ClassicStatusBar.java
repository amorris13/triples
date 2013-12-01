package com.antsapps.triples;

import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.antsapps.triples.backend.Card;
import com.google.common.collect.ImmutableList;

public class ClassicStatusBar extends StatusBar {

  private final TextView mTimerText;
  private final TextView mCardsRemainingText;

  public ClassicStatusBar(Context context, AttributeSet attrs) {
    super(context, attrs);
    ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
        .inflate(R.layout.classic_statusbar, this);
    mTimerText = (TextView) findViewById(R.id.timer_value_text);
    mCardsRemainingText = (TextView) findViewById(R.id.cards_remaining_text);
  }

  @Override
  public void onTimerTick(final long elapsedTime) {
    mTimerText.setText(DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS
        .toSeconds(elapsedTime)));
  }

  @Override
  public void onUpdateCardsInPlay(ImmutableList<Card> newCards,
                                  ImmutableList<Card> oldCards,
                                  int numRemaining,
                                  int numTriplesFound) {
    mCardsRemainingText.setText(String.valueOf(numRemaining));
  }
}
