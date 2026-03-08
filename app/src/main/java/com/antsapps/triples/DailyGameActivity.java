package com.antsapps.triples;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.ViewStub;
import android.widget.TextView;
import android.widget.Toast;

import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnTimerTickListener;
import com.antsapps.triples.backend.OnValidTripleSelectedListener;
import com.antsapps.triples.cardsview.FoundTriplesView;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

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

    FoundTriplesView foundTriplesView = findViewById(R.id.found_triples_view);
    foundTriplesView.setTotalTriplesCount(mGame.getTotalTriplesCount());
    foundTriplesView.setFoundTriples(mGame.getFoundTriples());
    mGame.addOnTimerTickListener(this);
    mGame.addOnUpdateCardsInPlayListener(this);
    mGame.setOnTripleFoundListener(this);

    TextView dateText = findViewById(R.id.daily_date_text);
    dateText.setText(java.text.DateFormat.getDateInstance().format(mGame.getDateStarted()));
  }

  @Override
  protected OnValidTripleSelectedListener getOnValidTripleSelectedListener() {
    return this;
  }

  @Override
  public void onValidTripleSelected(Collection<Card> cards) {
    Set<Card> selectedTriple = com.google.common.collect.Sets.newHashSet(cards);
    int indexFound = mGame.getFoundTriples().indexOf(selectedTriple);
    if (indexFound != -1) {
      Toast.makeText(this, R.string.triple_already_found, Toast.LENGTH_SHORT).show();
      FoundTriplesView foundTriplesView = findViewById(R.id.found_triples_view);
      foundTriplesView.highlightStack(indexFound);
      com.antsapps.triples.cardsview.CardsView cardsView = findViewById(R.id.cards_view);
      cardsView.clearSelectedCards();
    } else {
      mGame.commitTriple(cards.toArray(new Card[0]));
    }
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
    updateFoundTriplesView();
  }

  private void updateTriplesFoundText() {
    TextView triplesFoundText = (TextView) findViewById(R.id.triples_found_text);
    triplesFoundText.setText(mGame.getNumTriplesFound() + " / " + mGame.getTotalTriplesCount());
  }

  private void updateFoundTriplesView() {
    FoundTriplesView foundTriplesView = findViewById(R.id.found_triples_view);
    if (foundTriplesView != null) {
      foundTriplesView.setFoundTriples(mGame.getFoundTriples());
    }
  }

  @Override
  public void onCardHinted(Card hintedCard) {
    updateHintUsedIndicator();
  }

  @Override
  public void onTripleFound(Set<Card> triple) {
    updateFoundTriplesView();
    FoundTriplesView foundTriplesView = findViewById(R.id.found_triples_view);
    int index = mGame.getFoundTriples().indexOf(triple);
    Map<Card, Rect> targetRects = foundTriplesView.getCardRectsInStack(index);

    com.antsapps.triples.cardsview.CardsView cardsView = findViewById(R.id.cards_view);
    int[] locationOnScreen = new int[2];
    cardsView.getLocationOnScreen(locationOnScreen);

    Map<Card, Rect> localTargetRects = Maps.newHashMap();
    for (Map.Entry<Card, Rect> entry : targetRects.entrySet()) {
      Rect rect = new Rect(entry.getValue());
      rect.offset(-locationOnScreen[0], -locationOnScreen[1]);
      localTargetRects.put(entry.getKey(), rect);
    }

    cardsView.animateTripleFound(triple, localTargetRects);
  }

  @Override
  protected void onResume() {
    super.onResume();
    updateTriplesFoundText();
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
