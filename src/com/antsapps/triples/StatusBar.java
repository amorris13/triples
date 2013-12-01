package com.antsapps.triples;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnTimerTickListener;
import com.google.common.collect.ImmutableList;

/**
 * Created by anthony on 1/12/13.
 */
public abstract class StatusBar extends RelativeLayout
    implements OnTimerTickListener, Game.OnUpdateCardsInPlayListener {
  public StatusBar(Context context, AttributeSet attrs) {super(context, attrs);}

  @Override
  public abstract void onTimerTick(long elapsedTime);

  @Override
  public abstract void onUpdateCardsInPlay(ImmutableList<Card> newCards,
                                           ImmutableList<Card> oldCards,
                                           int numRemaining,
                                           int numTriplesFound);
}
