package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnTimerTickListener;
import com.antsapps.triples.backend.OnValidTripleSelectedListener;
import com.antsapps.triples.views.FoundTriplesView;
import com.google.common.collect.ImmutableList;
import java.text.DateFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DailyGameActivity extends BaseGameActivity
    implements OnTimerTickListener,
        Game.OnUpdateCardsInPlayListener,
        DailyGame.OnTripleFoundListener {

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

    TextView dateText = findViewById(R.id.daily_date_text);
    dateText.setText(DateFormat.getDateInstance().format(mGame.getDateStarted()));

    FoundTriplesView foundTriplesView = findViewById(R.id.found_triples_view);
    if (foundTriplesView != null) {
      foundTriplesView.setFoundTriples(mGame.getFoundTriples(), mGame.getTotalTriplesCount());
    }

    mCardsView = findViewById(R.id.cards_view);
    mCardsView.setOnValidTripleSelectedListener(
        new OnValidTripleSelectedListener() {
          @Override
          public void onValidTripleSelected(java.util.Collection<Card> tripleCollection) {
            Set<Card> triple = com.google.common.collect.Sets.newHashSet(tripleCollection);
            List<Set<Card>> foundTriples = mGame.getFoundTriples();
            if (foundTriples.contains(triple)) {
              mCardsView.onAlreadyFoundTriple(triple);
              FoundTriplesView ftv = findViewById(R.id.found_triples_view);
              if (ftv != null) {
                ftv.highlightStack(foundTriples.indexOf(triple));
              }
            } else {
              mGame.onValidTripleSelected(tripleCollection);
            }
          }
        });
  }

  @Override
  protected int getAccentColor() {
    return ContextCompat.getColor(this, R.color.daily_accent);
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
  }

  private void updateTriplesFoundText() {
    TextView triplesFoundText = (TextView) findViewById(R.id.triples_found_text);
    triplesFoundText.setText(mGame.getNumTriplesFound() + " / " + mGame.getTotalTriplesCount());
  }

  @Override
  public void onCardHinted(Card hintedCard) {
    updateHintUsedIndicator();
  }

  @Override
  public void onTripleFound(final Set<Card> triple) {
    final FoundTriplesView foundTriplesView = findViewById(R.id.found_triples_view);
    if (foundTriplesView != null) {
      int index = mGame.getFoundTriples().indexOf(triple);
      mCardsView.animateTripleFound(
          triple,
          foundTriplesView.getStackBounds(index),
          () ->
              foundTriplesView.setFoundTriples(
                  mGame.getFoundTriples(), mGame.getTotalTriplesCount()));
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    updateTriplesFoundText();
    updateDailyUi();
    FoundTriplesView foundTriplesView = findViewById(R.id.found_triples_view);
    if (foundTriplesView != null) {
      foundTriplesView.setFoundTriples(mGame.getFoundTriples(), mGame.getTotalTriplesCount());
    }
  }

  @Override
  public void onUpdateGameState(Game.GameState state) {
    super.onUpdateGameState(state);
    updateDailyUi();
  }

  private void updateDailyUi() {
    if (mGame.getGameState() == Game.GameState.COMPLETED) {
      findViewById(R.id.status_bar).setVisibility(View.GONE);
      findViewById(R.id.bottom_separator).setVisibility(View.GONE);
      findViewById(R.id.new_game_button).setVisibility(View.GONE);
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
