package com.antsapps.triples;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.TripleAnalysis;
import com.antsapps.triples.cardsview.CardView;
import com.antsapps.triples.cardsview.VerticalCardsView;
import com.antsapps.triples.views.TripleStackView;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BoardHistoryActivity extends BaseTriplesActivity {

  /** Set by the caller (GameAnalysisActivity) before starting this activity. */
  public static List<TripleAnalysis> sAnalysisList;

  /** 1-based step to open initially. */
  public static int sInitialStep;

  private VerticalCardsView mCardsView;
  private LinearLayout mAlternativesContainer;
  private HorizontalScrollView mAlternativesScroll;

  private List<TripleAnalysis> mAnalysisList;

  /** 0-based index into mAnalysisList. */
  private int mCurrentStep;

  /**
   * Per step (0-based), the set of indices (into the "other alternatives" list) that have already
   * been revealed by the user.
   */
  private final Map<Integer, Set<Integer>> mRevealedAlternatives = new HashMap<>();

  private Menu mMenu;

  /** Width in pixels for each TripleStackView in the alternatives panel. */
  private int mStackSlotWidthPx;

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
    mAlternativesContainer = findViewById(R.id.alternatives_container);
    mAlternativesScroll = findViewById(R.id.alternatives_scroll);

    if (sAnalysisList == null || sAnalysisList.isEmpty()) {
      finish();
      return;
    }

    mAnalysisList = sAnalysisList;
    mCurrentStep = Math.max(0, Math.min(sInitialStep - 1, mAnalysisList.size() - 1));

    float density = getResources().getDisplayMetrics().density;
    mStackSlotWidthPx = (int) (80 * density);

    mCardsView.setEnabled(false); // read-only board

    mCardsView
        .getViewTreeObserver()
        .addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
              @Override
              public void onGlobalLayout() {
                if (mCardsView.getWidth() > 0 && mCardsView.getHeight() > 0) {
                  mCardsView.refreshDrawables();
                  mCardsView.updateBounds();
                  showStep(mCurrentStep, false);
                  mCardsView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
              }
            });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.board_history, menu);
    mMenu = menu;
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    if (mAnalysisList != null) {
      MenuItem prev = menu.findItem(R.id.prev_step);
      MenuItem next = menu.findItem(R.id.next_step);
      if (prev != null) prev.setEnabled(mCurrentStep > 0);
      if (next != null) next.setEnabled(mCurrentStep < mAnalysisList.size() - 1);
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == android.R.id.home) {
      finish();
      return true;
    } else if (id == R.id.prev_step) {
      navigateToPrevStep();
      return true;
    } else if (id == R.id.next_step) {
      navigateToNextStep();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void navigateToNextStep() {
    if (mCurrentStep >= mAnalysisList.size() - 1) return;
    TripleAnalysis currentAnalysis = mAnalysisList.get(mCurrentStep);
    // Animate the found triple flying off, then load the next step
    mCardsView.animateTripleFoundToOffscreen(
        currentAnalysis.foundTriple,
        () -> {
          mCurrentStep++;
          showStep(mCurrentStep, false);
        });
  }

  private void navigateToPrevStep() {
    if (mCurrentStep <= 0) return;
    int prevStep = mCurrentStep - 1;
    TripleAnalysis prevAnalysis = mAnalysisList.get(prevStep);
    // Mark the found triple to fly in from off-screen when the board is updated
    mCardsView.markCardsForReverseAnimation(prevAnalysis.foundTriple);
    mCurrentStep = prevStep;
    showStep(mCurrentStep, false);
  }

  /**
   * Updates the board and alternatives panel to show the given step.
   *
   * @param step 0-based step index
   * @param animate whether to animate in (unused; animations are managed by caller)
   */
  private void showStep(int step, boolean animate) {
    TripleAnalysis analysis = mAnalysisList.get(step);

    // Update board
    mCardsView.updateCardsInPlay(ImmutableList.copyOf(analysis.boardState));
    highlightFoundTriple(analysis.foundTriple);

    // Update subtitle
    updateSubtitle(step, analysis);

    // Update alternatives panel
    rebuildAlternativesPanel(step, analysis);

    // Update nav arrows
    invalidateOptionsMenu();
  }

  private void highlightFoundTriple() {
    if (mAnalysisList == null) return;
    highlightFoundTriple(mAnalysisList.get(mCurrentStep).foundTriple);
  }

  private void highlightFoundTriple(Set<Card> foundTriple) {
    mCardsView.clearSelectedCards();
    for (Card card : foundTriple) {
      mCardsView.setSelected(card, true);
    }
  }

  private void updateSubtitle(int step, TripleAnalysis analysis) {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar == null) return;
    long durationMs = analysis.duration;
    long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs);
    long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60;
    long hundredths = (durationMs % 1000) / 10;
    String durationStr;
    if (minutes > 0) {
      durationStr = String.format("%d:%02d.%02d", minutes, seconds, hundredths);
    } else {
      durationStr = String.format("%d.%02ds", seconds, hundredths);
    }
    String subtitle =
        getString(R.string.board_history_subtitle, step + 1, mAnalysisList.size(), durationStr);
    actionBar.setSubtitle(subtitle);
  }

  // ---- Alternatives panel ----

  private void rebuildAlternativesPanel(int step, TripleAnalysis analysis) {
    mAlternativesContainer.removeAllViews();

    // Slot 0: the found triple (highlighted, always filled)
    TripleStackView foundView = new TripleStackView(this);
    foundView.setNaturalCardDimensionsProvider(mCardsView);
    foundView.setTriple(analysis.foundTriple);
    foundView.setHighlighted(true);
    mAlternativesContainer.addView(
        foundView,
        new LinearLayout.LayoutParams(mStackSlotWidthPx, LinearLayout.LayoutParams.WRAP_CONTENT));

    // Remaining slots: other available triples as placeholders (or revealed)
    List<Set<Card>> others = getOtherAlternatives(analysis);
    Set<Integer> revealed = mRevealedAlternatives.getOrDefault(step, new HashSet<>());

    for (int i = 0; i < others.size(); i++) {
      final int idx = i;
      final Set<Card> triple = others.get(i);
      TripleStackView stackView = new TripleStackView(this);
      stackView.setNaturalCardDimensionsProvider(mCardsView);
      if (revealed.contains(i)) {
        stackView.setTriple(triple);
      } else {
        stackView.setTriple(null); // placeholder
        stackView.setOnPlaceholderClickListener(
            () -> revealAlternative(step, idx, triple, stackView));
      }
      mAlternativesContainer.addView(
          stackView,
          new LinearLayout.LayoutParams(mStackSlotWidthPx, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    // Update label
    TextView label = findViewById(R.id.alternatives_label);
    if (label != null) {
      label.setText(getString(R.string.analysis_alternatives, analysis.allAvailableTriples.size()));
    }
  }

  private List<Set<Card>> getOtherAlternatives(TripleAnalysis analysis) {
    List<Set<Card>> others = new ArrayList<>();
    for (Set<Card> triple : analysis.allAvailableTriples) {
      if (!triple.equals(analysis.foundTriple)) {
        others.add(triple);
      }
    }
    return others;
  }

  /**
   * Animates the given alternative triple's cards flying from their positions in the grid to the
   * placeholder slot, then fills the slot.
   */
  private void revealAlternative(
      int step, int alternativeIdx, Set<Card> triple, TripleStackView targetView) {
    TripleAnalysis analysis = mAnalysisList.get(step);

    // Get target bounds (where the cards should fly to)
    // We need the view to be laid out first; since the user just tapped it, it is.
    Map<Card, android.graphics.Rect> targetBounds = targetView.computeCardBoundsInWindow(triple);

    // For each card, find its position in the grid and create a floating overlay card
    ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
    FrameLayout overlay = new FrameLayout(this);
    decorView.addView(
        overlay,
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    int[] cardsViewLoc = new int[2];
    mCardsView.getLocationInWindow(cardsViewLoc);

    List<Card> sorted = TripleStackView.getSortedTriple(triple);

    // Count only cards that have both a grid position and a target bound (will actually animate)
    int animateCount = 0;
    for (Card card : sorted) {
      if (analysis.boardState.indexOf(card) >= 0 && targetBounds.containsKey(card)) {
        animateCount++;
      }
    }

    if (animateCount == 0) {
      decorView.removeView(overlay);
      mRevealedAlternatives.computeIfAbsent(step, k -> new HashSet<>()).add(alternativeIdx);
      targetView.setTriple(triple);
      return;
    }

    final int totalToAnimate = animateCount;
    AtomicInteger doneCount = new AtomicInteger(0);

    for (Card card : sorted) {
      int cardIndex = analysis.boardState.indexOf(card);
      if (cardIndex < 0) continue;

      android.graphics.Rect target = targetBounds.get(card);
      if (target == null) continue;

      android.graphics.Rect gridBounds = mCardsView.calcBounds(cardIndex);
      int startX = cardsViewLoc[0] + gridBounds.left;
      int startY = cardsViewLoc[1] + gridBounds.top;

      CardView floatCard = new CardView(this, card);
      floatCard.setSelected(true);
      overlay.addView(
          floatCard, new FrameLayout.LayoutParams(gridBounds.width(), gridBounds.height()));
      floatCard.setX(startX);
      floatCard.setY(startY);

      floatCard.animateFoundCard(
          target,
          new DecelerateInterpolator(),
          () -> {
            if (doneCount.incrementAndGet() == totalToAnimate) {
              decorView.removeView(overlay);
              // Mark as revealed and fill the slot
              mRevealedAlternatives.computeIfAbsent(step, k -> new HashSet<>()).add(alternativeIdx);
              targetView.setTriple(triple);
            }
          });
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    sAnalysisList = null;
  }
}
