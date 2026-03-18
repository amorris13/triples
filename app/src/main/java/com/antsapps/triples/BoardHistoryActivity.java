package com.antsapps.triples;

import android.os.Bundle;
import android.view.Menu;
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
import com.antsapps.triples.views.FoundTriplesView;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;

public class BoardHistoryActivity extends BaseTriplesActivity {

  public static List<TripleAnalysis> sAnalysisList;
  public static int sCurrentStepIndex;

  private CardsView mCardsView;
  private FoundTriplesView mFoundTriplesView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.board_history);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    mCardsView = findViewById(R.id.cards_view);
    mCardsView.setEnabled(true);
    mFoundTriplesView = findViewById(R.id.found_triples_view);
    mFoundTriplesView.setCardsView(mCardsView);
    mFoundTriplesView.setOnPlaceholderClickListener(this::onPlaceholderClick);

    if (sAnalysisList == null) {
      finish();
      return;
    }

    updateUi(false, false);

    mCardsView
        .getViewTreeObserver()
        .addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
              @Override
              public void onGlobalLayout() {
                if (mCardsView.getWidth() > 0 && mCardsView.getHeight() > 0) {
                  mCardsView.refreshDrawables();
                  mCardsView.updateBounds();
                  mCardsView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
              }
            });
  }

  private void updateUi(boolean animateNext, boolean animatePrevious) {
    TripleAnalysis analysis = sAnalysisList.get(sCurrentStepIndex);
    setTitle(
        getString(
            R.string.analysis_step_of_total_format,
            sCurrentStepIndex + 1,
            sAnalysisList.size()));

    mCardsView.updateCardsInPlay(ImmutableList.copyOf(analysis.boardState));

    List<Set<Card>> triples = Lists.newArrayList();
    triples.add(analysis.foundTriple);
    for (Set<Card> triple : analysis.allAvailableTriples) {
      if (!triple.equals(analysis.foundTriple)) {
        triples.add(triple);
      }
    }
    mFoundTriplesView.setFoundTriples(triples, triples.size());

    if (animatePrevious) {
      mCardsView.animateTripleBackFromOffscreen(analysis.foundTriple, null);
    }
    invalidateOptionsMenu();
  }

  private void onPlaceholderClick(int index) {
    TripleAnalysis analysis = sAnalysisList.get(sCurrentStepIndex);
    // Re-calculate the triples list to match what was set in updateUi
    List<Set<Card>> triples = Lists.newArrayList();
    triples.add(analysis.foundTriple);
    for (Set<Card> t : analysis.allAvailableTriples) {
      if (!t.equals(analysis.foundTriple)) {
        triples.add(t);
      }
    }

    Set<Card> triple = triples.get(index);
    mFoundTriplesView.revealAlternative(index);
    mCardsView.pulseCards(triple);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.board_history, menu);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.findItem(R.id.previous).setEnabled(sCurrentStepIndex > 0);
    menu.findItem(R.id.next).setEnabled(sCurrentStepIndex < sAnalysisList.size() - 1);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    if (item.getItemId() == R.id.next) {
      mCardsView.animateTripleFoundToOffscreen(
          sAnalysisList.get(sCurrentStepIndex).foundTriple,
          () -> {
            sCurrentStepIndex++;
            updateUi(true, false);
          });
      return true;
    }
    if (item.getItemId() == R.id.previous) {
      sCurrentStepIndex--;
      updateUi(false, true);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // Don't leak the static analysis object
    sAnalysisList = null;
  }
}
