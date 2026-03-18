package com.antsapps.triples;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.TripleAnalysis;
import com.antsapps.triples.cardsview.CardsView;
import com.antsapps.triples.views.TripleExplanationView;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Set;

public class BoardHistoryActivity extends BaseTriplesActivity {

  public static TripleAnalysis sAnalysis;
  public static int sStep;

  private CardsView mCardsView;
  private TripleExplanationView mExplanationView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.board_history);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setTitle(R.string.analysis_view_board);
    }

    mCardsView = findViewById(R.id.cards_view);
    mExplanationView = findViewById(R.id.triple_explanation);
    mExplanationView.setNaturalCardDimensionsProvider(mCardsView);

    if (sAnalysis == null) {
      finish();
      return;
    }

    mCardsView.updateCardsInPlay(ImmutableList.copyOf(sAnalysis.boardState));
    mCardsView.setEnabled(true);

    ((TextView) findViewById(R.id.step_label))
        .setText(getString(R.string.analysis_step_format, sStep));
    ((TextView) findViewById(R.id.alternatives_label))
        .setText(getString(R.string.analysis_alternatives, sAnalysis.allAvailableTriples.size()));

    mCardsView
        .getViewTreeObserver()
        .addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
              @Override
              public void onGlobalLayout() {
                if (mCardsView.getWidth() > 0 && mCardsView.getHeight() > 0) {
                  mCardsView.refreshDrawables();
                  mCardsView.updateBounds();
                  highlightFoundTriple();
                  mCardsView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
              }
            });

    mCardsView.setOnSelectionChangedListener(this::updateExplanation);
  }

  private void highlightFoundTriple() {
    for (Card card : sAnalysis.foundTriple) {
      mCardsView.setSelected(card, true);
    }
    updateExplanation(sAnalysis.foundTriple);
    mExplanationView.setVisibility(View.VISIBLE);
    mExplanationView.setTitle(getString(R.string.analysis_found_triple));
  }

  private void updateExplanation(Set<Card> selectedCards) {
    mExplanationView.setCards(ImmutableSet.copyOf(selectedCards));
    if (selectedCards.size() == 3) {
      if (selectedCards.equals(sAnalysis.foundTriple)) {
        mExplanationView.setTitle(getString(R.string.analysis_found_triple));
      } else if (Game.isValidTriple(selectedCards)) {
        mExplanationView.setTitle(
            getString(R.string.analysis_alternatives, sAnalysis.allAvailableTriples.size()));
      } else {
        mExplanationView.setTitle(null);
      }
    } else {
      mExplanationView.setTitle(null);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // Don't leak the static analysis object
    sAnalysis = null;
  }
}
