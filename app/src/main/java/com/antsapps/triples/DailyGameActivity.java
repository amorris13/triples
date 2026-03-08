package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.ViewStub;
import android.widget.TextView;
import android.widget.Toast;

import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Card;
import android.graphics.Rect;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnTimerTickListener;
import com.antsapps.triples.backend.OnValidTripleSelectedListener;
import com.antsapps.triples.cardsview.CardsView;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Map;
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

    CardsView cardsView = findViewById(R.id.cards_view);
    cardsView.setOnValidTripleSelectedListener(this);

    TextView dateText = findViewById(R.id.daily_date_text);
    dateText.setText(java.text.DateFormat.getDateInstance().format(mGame.getDateStarted()));

    updateFoundTriplesView();
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
    updateSummaryText();
  }

  private void updateSummaryText() {
    TextView summary = findViewById(R.id.triples_found_summary);
    if (summary != null) {
      summary.setText(mGame.getNumTriplesFound() + " / " + mGame.getTotalTriplesCount());
    }
  }

  private void updateFoundTriplesView() {
    FoundTriplesView foundTriplesView = findViewById(R.id.found_triples_view);
    if (foundTriplesView != null) {
      foundTriplesView.setTriples(mGame.getAllTriples(), mGame.getFoundTriples());
    }
    updateSummaryText();
  }

  @Override
  public void onCardHinted(Card hintedCard) {
    updateHintUsedIndicator();
  }

  @Override
  public void onTripleFound(Set<Card> triple) {
    CardsView cardsView = findViewById(R.id.cards_view);
    FoundTriplesView foundTriplesView = findViewById(R.id.found_triples_view);
    Map<Card, Rect> cardLocations = foundTriplesView.getCardLocations(triple);
    if (!cardLocations.isEmpty()) {
      int[] location = new int[2];
      cardsView.getLocationInWindow(location);
      Map<Card, Rect> offsetLocations = Maps.newHashMap();
      for (Map.Entry<Card, Rect> entry : cardLocations.entrySet()) {
        Rect rect = new Rect(entry.getValue());
        rect.offset(-location[0], -location[1]);
        offsetLocations.put(entry.getKey(), rect);
      }
      cardsView.animateTripleFound(offsetLocations);
    } else {
      cardsView.animateTripleFound(triple);
    }

    // Delay updating the FoundTriplesView until animation is finished
    findViewById(R.id.status_bar).postDelayed(() -> {
      updateFoundTriplesView();
      mGame.notifyCardsInPlayUpdate();
    }, 1000);
  }

  @Override
  public void onValidTripleSelected(Collection<Card> cards) {
    Set<Card> triple = Sets.newHashSet(cards);
    if (mGame.getFoundTriples().contains(triple)) {
      Toast.makeText(this, "Triple already found!", Toast.LENGTH_SHORT).show();
      FoundTriplesView foundTriplesView = findViewById(R.id.found_triples_view);
      foundTriplesView.highlightTriple(triple);
      CardsView cardsView = findViewById(R.id.cards_view);
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
