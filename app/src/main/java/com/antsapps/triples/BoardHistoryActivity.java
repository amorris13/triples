package com.antsapps.triples;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
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

  /**
   * 0-based index into mAnalysisList. Valid range: 0..mAnalysisList.size() where
   * mAnalysisList.size() represents the "final board" (cards remaining after all triples found).
   */
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

    // Selecting a valid triple from the grid:
    // - unrevealed slot → fly cards to slot and reveal
    // - already-revealed slot → pulse cards in grid and pulse slot
    // - foundTriple (slot 0, always filled) → fly cards to slot 0 to confirm
    mCardsView.setOnValidTripleSelectedListener(
        selectedCards -> {
          if (mCurrentStep >= mAnalysisList.size()) return;
          Set<Card> triple = new HashSet<>(selectedCards);
          TripleAnalysis analysis = mAnalysisList.get(mCurrentStep);
          final int capturedStep = mCurrentStep;
          if (triple.equals(analysis.foundTriple)) {
            animateCardsToSlot(capturedStep, triple, 0, () -> mFoundTriplesView.highlightStack(0));
          } else {
            List<Set<Card>> others = getOtherAlternatives(analysis);
            int othersIdx = others.indexOf(triple);
            if (othersIdx >= 0) {
              int slotIdx = othersIdx + 1;
              Set<Integer> revealed =
                  mRevealedAlternatives.getOrDefault(capturedStep, new HashSet<>());
              if (revealed.contains(othersIdx)) {
                mCardsView.onAlreadyFoundTriple(triple);
                mFoundTriplesView.highlightStack(slotIdx);
              } else {
                revealAlternative(capturedStep, othersIdx, triple);
              }
            }
          }
        });

    // Tapping a filled slot pulses its cards in the grid; tapping a placeholder pulses the
    // placeholder and the corresponding cards in the grid.
    mFoundTriplesView.setOnSlotClickListener(
        (slotIndex, triple) -> {
          if (mCurrentStep >= mAnalysisList.size()) return;
          if (triple != null) {
            mCardsView.onAlreadyFoundTriple(triple);
            mFoundTriplesView.highlightStack(slotIndex);
          } else {
            mFoundTriplesView.highlightStack(slotIndex);
            List<Set<Card>> others = getOtherAlternatives(mAnalysisList.get(mCurrentStep));
            int othersIdx = slotIndex - 1;
            if (othersIdx >= 0 && othersIdx < others.size()) {
              mCardsView.onAlreadyFoundTriple(others.get(othersIdx));
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
      boolean prevEnabled = mCurrentStep > 0;
      boolean nextEnabled = mCurrentStep < mAnalysisList.size();
      if (prev != null) {
        prev.setEnabled(prevEnabled);
        if (prev.getIcon() != null) prev.getIcon().setAlpha(prevEnabled ? 255 : 96);
      }
      if (next != null) {
        next.setEnabled(nextEnabled);
        if (next.getIcon() != null) next.getIcon().setAlpha(nextEnabled ? 255 : 96);
      }
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
    if (mCurrentStep >= mAnalysisList.size()) return;

    TripleAnalysis currentAnalysis = mAnalysisList.get(mCurrentStep);
    Set<Card> foundTriple = currentAnalysis.foundTriple;
    mCurrentStep++;

    if (mCurrentStep == mAnalysisList.size()) {
      // Navigate to the final board (cards remaining after all triples found).
      List<Card> finalBoard = new ArrayList<>(currentAnalysis.boardState);
      finalBoard.removeAll(foundTriple);
      updateSubtitle(mCurrentStep, null);
      rebuildFoundTriplesPanel(mCurrentStep, null);
      invalidateOptionsMenu();
      mCardsView.animateTripleFoundToOffscreen(foundTriple);
      mCardsView.updateCardsInPlay(ImmutableList.copyOf(finalBoard));
      return;
    }

    TripleAnalysis nextAnalysis = mAnalysisList.get(mCurrentStep);
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

    if (mCurrentStep == mAnalysisList.size()) {
      // Navigating back from the final board: fly the found-triple back onto the board.
      mCardsView.markCardsForReverseAnimation(prevAnalysis.foundTriple);
      mCurrentStep = prevStep;
      showStep(mCurrentStep);
      return;
    }

    TripleAnalysis currentAnalysis = mAnalysisList.get(mCurrentStep);

    // Fade out cards that are in the current board but not in the previous board, then
    // immediately update the board state so the fly-in starts without waiting for the fade.
    Set<Card> toFade = new HashSet<>(currentAnalysis.boardState);
    toFade.removeAll(prevAnalysis.boardState);
    mCardsView.fadeOutAndRemoveCards(toFade);

    mCardsView.markCardsForReverseAnimation(prevAnalysis.foundTriple);
    mCurrentStep = prevStep;
    showStep(mCurrentStep);
  }

  /**
   * Updates the board and alternatives panel to show the given step. Triggers a delayed pulse of
   * the found triple after animations settle.
   */
  private void showStep(int step) {
    if (step == mAnalysisList.size()) {
      // Final board.
      TripleAnalysis lastAnalysis = mAnalysisList.get(step - 1);
      List<Card> finalBoard = new ArrayList<>(lastAnalysis.boardState);
      finalBoard.removeAll(lastAnalysis.foundTriple);
      mCardsView.updateCardsInPlay(ImmutableList.copyOf(finalBoard));
      updateSubtitle(step, null);
      rebuildFoundTriplesPanel(step, null);
      invalidateOptionsMenu();
      return;
    }

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

  private void updateSubtitle(int step, @Nullable TripleAnalysis analysis) {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar == null) return;
    if (analysis == null) {
      actionBar.setSubtitle(getString(R.string.board_history_final_board));
      return;
    }
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

  private void rebuildFoundTriplesPanel(int step, @Nullable TripleAnalysis analysis) {
    if (analysis == null) {
      mFoundTriplesView.setSlots(new ArrayList<>());
      TextView label = findViewById(R.id.alternatives_label);
      if (label != null) label.setText("");
      return;
    }

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
   * Animates the cards of {@code triple} flying from their grid positions to the given slot in the
   * FoundTriplesView. The slot is not modified — call {@code onComplete} to fill it if needed.
   */
  private void animateCardsToSlot(
      int step, Set<Card> triple, int slotIndex, @Nullable Runnable onComplete) {
    if (slotIndex < 0 || slotIndex >= mFoundTriplesView.getChildCount()) {
      if (onComplete != null) onComplete.run();
      return;
    }
    TripleStackView targetView = (TripleStackView) mFoundTriplesView.getChildAt(slotIndex);
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
      if (onComplete != null) onComplete.run();
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
              if (onComplete != null) onComplete.run();
            }
          });
    }

    mCardsView.clearSelectedCards();
  }

  /**
   * Animates the given alternative triple's cards flying from their positions in the grid to the
   * placeholder slot, then fills the slot.
   */
  private void revealAlternative(int step, int alternativeIdx, Set<Card> triple) {
    int slotIndex = alternativeIdx + 1;
    animateCardsToSlot(
        step,
        triple,
        slotIndex,
        () -> {
          mRevealedAlternatives.computeIfAbsent(step, k -> new HashSet<>()).add(alternativeIdx);
          TripleStackView target = (TripleStackView) mFoundTriplesView.getChildAt(slotIndex);
          if (target != null) target.setTriple(triple);
        });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    sAnalysisList = null;
  }
}
