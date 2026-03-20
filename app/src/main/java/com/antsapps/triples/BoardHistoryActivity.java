package com.antsapps.triples;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.TripleAnalysis;
import com.antsapps.triples.cardsview.CardView;
import com.antsapps.triples.cardsview.VerticalCardsView;
import com.antsapps.triples.views.FoundTriplesView;
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
  private FoundTriplesView mFoundTriplesView;

  private List<TripleAnalysis> mAnalysisList;

  /** 0-based index into mAnalysisList. */
  private int mCurrentStep;

  /**
   * Per step (0-based), the set of indices (into the "other alternatives" list) that have already
   * been revealed by the user.
   */
  private final Map<Integer, Set<Integer>> mRevealedAlternatives = new HashMap<>();

  private Menu mMenu;

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
    mFoundTriplesView = findViewById(R.id.found_triples_view);

    if (sAnalysisList == null || sAnalysisList.isEmpty()) {
      finish();
      return;
    }

    mAnalysisList = sAnalysisList;
    mCurrentStep = Math.max(0, Math.min(sInitialStep - 1, mAnalysisList.size() - 1));

    mFoundTriplesView.setCardsView(mCardsView);

    // Allow card selection: finding a valid triple pulses the cards and reveals/pulses the slot.
    mCardsView.setOnValidTripleSelectedListener(
        selectedCards -> {
          Set<Card> triple = new HashSet<>(selectedCards);
          TripleAnalysis analysis = mAnalysisList.get(mCurrentStep);
          mCardsView.onAlreadyFoundTriple(triple);
          if (triple.equals(analysis.foundTriple)) {
            mFoundTriplesView.highlightStack(0);
          } else {
            List<Set<Card>> others = getOtherAlternatives(analysis);
            int othersIdx = others.indexOf(triple);
            if (othersIdx >= 0) {
              int slotIdx = othersIdx + 1;
              Set<Integer> revealed =
                  mRevealedAlternatives.getOrDefault(mCurrentStep, new HashSet<>());
              if (revealed.contains(othersIdx)) {
                mFoundTriplesView.highlightStack(slotIdx);
              } else {
                TripleStackView target = (TripleStackView) mFoundTriplesView.getChildAt(slotIdx);
                revealAlternative(mCurrentStep, othersIdx, triple, target);
              }
            }
          }
        });

    // Tapping a filled slot pulses its cards in the grid; tapping a placeholder reveals it.
    mFoundTriplesView.setOnSlotClickListener(
        (slotIndex, triple) -> {
          if (triple != null) {
            mCardsView.onAlreadyFoundTriple(triple);
            mFoundTriplesView.highlightStack(slotIndex);
          } else {
            List<Set<Card>> others = getOtherAlternatives(mAnalysisList.get(mCurrentStep));
            int othersIdx = slotIndex - 1; // slot 0 is always foundTriple
            if (othersIdx >= 0 && othersIdx < others.size()) {
              Set<Card> tripleToReveal = others.get(othersIdx);
              TripleStackView target = (TripleStackView) mFoundTriplesView.getChildAt(slotIndex);
              revealAlternative(mCurrentStep, othersIdx, tripleToReveal, target);
            }
          }
        });

    mCardsView
        .getViewTreeObserver()
        .addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
              @Override
              public void onGlobalLayout() {
                if (mCardsView.getWidth() > 0 && mCardsView.getHeight() > 0) {
                  mCardsView.refreshDrawables();
                  mCardsView.updateBounds();
                  showStep(mCurrentStep);
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
    Set<Card> foundTriple = mAnalysisList.get(mCurrentStep).foundTriple;
    mCurrentStep++;
    TripleAnalysis nextAnalysis = mAnalysisList.get(mCurrentStep);

    // Update subtitle and alternatives panel immediately.
    updateSubtitle(mCurrentStep, nextAnalysis);
    rebuildFoundTriplesPanel(mCurrentStep, nextAnalysis);
    invalidateOptionsMenu();

    // Fly the found-triple cards off screen (removes them from mCardViews before animating).
    mCardsView.animateTripleFoundToOffscreen(foundTriple);

    // Update the board immediately: new cards start fading in while fly-off plays.
    mCardsView.updateCardsInPlay(ImmutableList.copyOf(nextAnalysis.boardState));

    // Pulse the found triple after animations settle.
    final int pulseStep = mCurrentStep;
    mCardsView.postDelayed(
        () -> {
          if (mCurrentStep == pulseStep) pulseFoundTriple(pulseStep);
        },
        SettingsFragment.getAnimationDuration(this) * 2L);
  }

  private void navigateToPrevStep() {
    if (mCurrentStep <= 0) return;
    int prevStep = mCurrentStep - 1;
    TripleAnalysis prevAnalysis = mAnalysisList.get(prevStep);
    TripleAnalysis currentAnalysis = mAnalysisList.get(mCurrentStep);

    // Fade out the replacement cards (in current board but not in prev board).
    Set<Card> toFade = new HashSet<>(currentAnalysis.boardState);
    toFade.removeAll(prevAnalysis.boardState);

    mCardsView.fadeOutCardsAndThen(
        toFade,
        () -> {
          mCardsView.markCardsForReverseAnimation(prevAnalysis.foundTriple);
          mCurrentStep = prevStep;
          showStep(mCurrentStep);
        });
  }

  /**
   * Updates the board and alternatives panel to show the given step. Triggers a delayed pulse of
   * the found triple after animations settle.
   */
  private void showStep(int step) {
    TripleAnalysis analysis = mAnalysisList.get(step);

    mCardsView.updateCardsInPlay(ImmutableList.copyOf(analysis.boardState));
    updateSubtitle(step, analysis);
    rebuildFoundTriplesPanel(step, analysis);
    invalidateOptionsMenu();

    final int pulseStep = step;
    mCardsView.postDelayed(
        () -> {
          if (mCurrentStep == pulseStep) pulseFoundTriple(pulseStep);
        },
        SettingsFragment.getAnimationDuration(this) * 2L);
  }

  private void pulseFoundTriple(int step) {
    if (step < 0 || step >= mAnalysisList.size()) return;
    TripleAnalysis analysis = mAnalysisList.get(step);
    mCardsView.onAlreadyFoundTriple(analysis.foundTriple);
    mFoundTriplesView.highlightStack(0);
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

  // ---- Possible Triples panel ----

  private void rebuildFoundTriplesPanel(int step, TripleAnalysis analysis) {
    List<Set<Card>> others = getOtherAlternatives(analysis);
    Set<Integer> revealed = mRevealedAlternatives.getOrDefault(step, new HashSet<>());

    List<Set<Card>> slots = new ArrayList<>();
    slots.add(analysis.foundTriple); // slot 0: always revealed
    for (int i = 0; i < others.size(); i++) {
      slots.add(revealed.contains(i) ? others.get(i) : null);
    }

    mFoundTriplesView.setSlots(slots);
    mFoundTriplesView.setHighlightedSlot(0);

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

    Map<Card, android.graphics.Rect> targetBounds = targetView.computeCardBoundsInWindow(triple);

    ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
    FrameLayout overlay = new FrameLayout(this);
    decorView.addView(
        overlay,
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    int[] cardsViewLoc = new int[2];
    mCardsView.getLocationInWindow(cardsViewLoc);

    List<Card> sorted = TripleStackView.getSortedTriple(triple);

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
