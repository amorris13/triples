package com.antsapps.triples;

import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.Card;
import com.google.common.collect.ImmutableList;

public class ArcadeStatusBar extends StatusBar {

  private final TextView mTimerText;
  private final TextView mTriplesFoundText;

  public ArcadeStatusBar(Context context, AttributeSet attrs) {
    super(context, attrs);
    ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
        .inflate(R.layout.arcade_statusbar, this);
    mTimerText = (TextView) findViewById(R.id.timer_value_text);
    mTriplesFoundText = (TextView) findViewById(R.id.triples_found_text);
  }

  @Override
  public void onTimerTick(final long elapsedTime) {
    mTimerText.setText(DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS
        .toSeconds(ArcadeGame.TIME_LIMIT_MS - elapsedTime)));
  }

  @Override
  public void onUpdateCardsInPlay(ImmutableList<Card> newCards,
                                  ImmutableList<Card> oldCards,
                                  int numRemaining,
                                  int numTriplesFound) {
    mTriplesFoundText.setText(String.valueOf(numTriplesFound));
  }
}
