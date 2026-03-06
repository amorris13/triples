package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.ViewStub;
import android.widget.TextView;

import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Card;
import android.graphics.Rect;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnTimerTickListener;
import com.antsapps.triples.backend.OnValidTripleSelectedListener;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DailyGameActivity extends BaseGameActivity
    implements OnTimerTickListener, Game.OnUpdateCardsInPlayListener, DailyGame.OnTripleFoundListener, OnValidTripleSelectedListener {

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
    mGame.setOnTripleFoundListener(this);

    com.antsapps.triples.cardsview.CardsView cardsView = findViewById(R.id.cards_view);
    cardsView.setOnValidTripleSelectedListener(this);

    TextView dateText = findViewById(R.id.daily_date_text);
    dateText.setText(java.text.DateFormat.getDateInstance().format(mGame.getDateStarted()));
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
    updateFoundTriplesView();
  }

  private void updateFoundTriplesView() {
    FoundTriplesView foundTriplesView = findViewById(R.id.found_triples_view);
    if (foundTriplesView != null) {
      foundTriplesView.setTriples(mGame.getAllTriples(), mGame.getFoundTriples());
    }
  }

  @Override
  public void onCardHinted(Card hintedCard) {
    updateHintUsedIndicator();
  }

  @Override
  public void onTripleFound(Set<Card> triple) {
    com.antsapps.triples.cardsview.CardsView cardsView = findViewById(R.id.cards_view);
    FoundTriplesView foundTriplesView = findViewById(R.id.found_triples_view);
    Rect targetRect = foundTriplesView.getTripleLocation(triple);
    if (targetRect != null) {
      // The targetRect is in window coordinates, CardsView.animateTripleFound expects view coordinates.
      int[] location = new int[2];
      cardsView.getLocationInWindow(location);
      targetRect.offset(-location[0], -location[1]);
      cardsView.animateTripleFound(triple, targetRect);
    } else {
      cardsView.animateTripleFound(triple);
    }
  }

  @Override
  public void onValidTripleSelected(Collection<Card> cards) {
    Set<Card> triple = com.google.common.collect.Sets.newHashSet(cards);
    if (mGame.getFoundTriples().contains(triple)) {
      FoundTriplesView foundTriplesView = findViewById(R.id.found_triples_view);
      foundTriplesView.highlightTriple(triple);
      com.antsapps.triples.cardsview.CardsView cardsView = findViewById(R.id.cards_view);
      cardsView.clearSelectedCards();
    } else {
      mGame.onValidTripleSelected(cards);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    updateFoundTriplesView();
    updateDailyUi();
  }

  @Override
  public void onUpdateGameState(Game.GameState state) {
    super.onUpdateGameState(state);
    updateDailyUi();
  }

  private void updateDailyUi() {
    if (mGame.getGameState() == Game.GameState.COMPLETED) {
      findViewById(R.id.status_bar).setVisibility(android.view.View.GONE);
      findViewById(R.id.bottom_separator).setVisibility(android.view.View.GONE);
      findViewById(R.id.new_game_button).setVisibility(android.view.View.GONE);
    }
  }

  @Override
  protected void submitScore() {
    // No leaderboard for daily yet
  }

  @Override
  protected Intent createNewGame() {
    // Not used in daily mode
    return null;
  }
}
