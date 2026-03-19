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

  public static final String GAME_ID = "game_id";
  public static final String GAME_TYPE = "game_type";
  public static final String STEP_INDEX = "step_index";

  private List<TripleAnalysis> mAnalysisList;
  private int mCurrentStepIndex;
  private List<Set<Card>> mTriples;

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

    mCardsView.setOnValidTripleSelectedListener(this::onValidTripleSelected);

    long gameId = getIntent().getLongExtra(GAME_ID, -1);
    String gameType = getIntent().getStringExtra(GAME_TYPE);
    mCurrentStepIndex = getIntent().getIntExtra(STEP_INDEX, 0);

    Game game = getGame(gameId, gameType);
    if (game == null) {
      finish();
      return;
    }

    mAnalysisList = com.antsapps.triples.backend.GameReconstructor.reconstruct(game);

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

  private void onValidTripleSelected(Set<Card> triple) {
    int index = -1;
    for (int i = 0; i < mTriples.size(); i++) {
      if (mTriples.get(i).equals(triple)) {
        index = i;
        break;
      }
    }

    if (index != -1) {
      final int revealIndex = index;
      mCardsView.animateTripleFound(
          mFoundTriplesView.getCardBoundsInWindow(index, triple),
          new android.view.animation.AccelerateDecelerateInterpolator(),
          () -> mFoundTriplesView.revealAlternative(revealIndex));
    }
  }

  private void updateUi(boolean animateNext, boolean animatePrevious) {
    TripleAnalysis analysis = mAnalysisList.get(mCurrentStepIndex);
    setTitle(getString(R.string.analysis_review_step_format, mCurrentStepIndex + 1));

    mCardsView.updateCardsInPlay(ImmutableList.copyOf(analysis.boardState));

    mTriples = Lists.newArrayList();
    mTriples.add(analysis.foundTriple);
    for (Set<Card> triple : analysis.allAvailableTriples) {
      if (!triple.equals(analysis.foundTriple)) {
        mTriples.add(triple);
      }
    }
    mFoundTriplesView.setFoundTriples(mTriples, mTriples.size());

    if (animatePrevious) {
      // Re-add cards if they were animated off in the previous step
      mCardsView.animateTripleBackFromOffscreen(analysis.foundTriple, null);
    }
    invalidateOptionsMenu();
  }

  private void onPlaceholderClick(int index) {
    Set<Card> triple = mTriples.get(index);
    mCardsView.animateTripleFound(
        mFoundTriplesView.getCardBoundsInWindow(index, triple),
        new android.view.animation.AccelerateDecelerateInterpolator(),
        () -> mFoundTriplesView.revealAlternative(index));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.board_history, menu);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.findItem(R.id.previous).setEnabled(mCurrentStepIndex > 0);
    menu.findItem(R.id.next).setEnabled(mCurrentStepIndex < mAnalysisList.size() - 1);
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
          mAnalysisList.get(mCurrentStepIndex).foundTriple,
          () -> {
            mCurrentStepIndex++;
            moveStep(true);
          });
      return true;
    }
    if (item.getItemId() == R.id.previous) {
      mCurrentStepIndex--;
      moveStep(false);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void moveStep(boolean forward) {
    updateUi(!forward, forward);
  }

  private Game getGame(long id, String type) {
    com.antsapps.triples.backend.Application app =
        com.antsapps.triples.backend.Application.getInstance(this);
    if ("Classic".equalsIgnoreCase(type)) return app.getClassicGame(id);
    if ("Arcade".equalsIgnoreCase(type)) return app.getArcadeGame(id);
    if ("Daily".equalsIgnoreCase(type)) return app.getDailyGame(id);
    return null;
  }
}
