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
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.GameReconstructor;
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

  public static final String EXTRA_GAME_ID = "extra_game_id";
  public static final String EXTRA_GAME_TYPE = "extra_game_type";
  public static final String EXTRA_INITIAL_STEP = "extra_initial_step";

  private Game mGame;
  private List<Card> mFinalBoardState;

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

  /** Available triples on the final board, populated when the final board step is shown. */
  private List<Set<Card>> mCurrentFinalBoardTriples;

  /** Indices of final board triple slots already revealed by the user. */
  private final Set<Integer> mRevealedFinalBoardSlots = new HashSet<>();

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

    long gameId = getIntent().getLongExtra(EXTRA_GAME_ID, -1);
    String gameType = getIntent().getStringExtra(EXTRA_GAME_TYPE);
    int initialStep = getIntent().getIntExtra(EXTRA_INITIAL_STEP, 1);

    mGame = loadGame(gameId, gameType);
    if (mGame == null) {
      finish();
      return;
    }

    mAnalysisList = GameReconstructor.reconstruct(mGame);
    mFinalBoardState = GameReconstructor.getFinalBoardState(mGame);

    if (mAnalysisList == null || mAnalysisList.isEmpty()) {
      finish();
      return;
    }

    mCurrentStep = Math.max(0, Math.min(initialStep - 1, mAnalysisList.size() - 1));

    mFoundTriplesView.setCardsView(mCardsView);

    // Selecting a valid triple from the grid:
    // - unrevealed slot → fly cards to slot and reveal
    // - already-revealed slot → pulse cards in grid and pulse slot
    // - foundTriple (slot 0, always filled) → fly cards to slot 0 to confirm
    mCardsView.setOnValidTripleSelectedListener(
        selectedCards -> {
          Set<Card> triple = new HashSet<>(selectedCards);

          if (mCurrentStep >= mAnalysisList.size()) {
            // Final board: fly cards to the matching slot and reveal it.
            if (mCurrentFinalBoardTriples == null) return;
            int slotIndex = mCurrentFinalBoardTriples.indexOf(triple);
            if (slotIndex < 0) return;
            if (mRevealedFinalBoardSlots.contains(slotIndex)) {
              mCardsView.onAlreadyFoundTriple(triple);
              mFoundTriplesView.highlightStack(slotIndex);
            } else {
              final int capturedSlot = slotIndex;
              animateCardsToSlot(
                  getFinalBoardCards(),
                  triple,
                  capturedSlot,
                  () -> {
                    mRevealedFinalBoardSlots.add(capturedSlot);
                    TripleStackView target =
                        (TripleStackView) mFoundTriplesView.getChildAt(capturedSlot);
                    if (target != null) target.setTriple(triple);
                  });
            }
            return;
          }

          TripleAnalysis analysis = mAnalysisList.get(mCurrentStep);
          final int capturedStep = mCurrentStep;
          if (triple.equals(analysis.foundTriple)) {
            animateCardsToSlot(
                analysis.boardState, triple, 0, () -> mFoundTriplesView.highlightStack(0));
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

    // Tapping a filled slot pulses its cards in the grid and pulses the slot.
    // Tapping a placeholder reveals it (flies cards from grid to slot).
    // On the final board, tapping a placeholder flies cards to slot and reveals it.
    mFoundTriplesView.setOnSlotClickListener(
        (slotIndex, triple) -> {
          if (mCurrentStep >= mAnalysisList.size()) {
            // Final board
            if (mCurrentFinalBoardTriples == null || slotIndex >= mCurrentFinalBoardTriples.size())
              return;
            Set<Card> finalTriple = mCurrentFinalBoardTriples.get(slotIndex);
            if (triple != null) {
              // Already revealed — pulse
              mCardsView.onAlreadyFoundTriple(finalTriple);
              mFoundTriplesView.highlightStack(slotIndex);
            } else {
              // Placeholder — fly + populate
              animateCardsToSlot(
                  getFinalBoardCards(),
                  finalTriple,
                  slotIndex,
                  () -> {
                    mRevealedFinalBoardSlots.add(slotIndex);
                    TripleStackView target =
                        (TripleStackView) mFoundTriplesView.getChildAt(slotIndex);
                    if (target != null) target.setTriple(finalTriple);
                  });
            }
            return;
          }
          if (triple != null) {
            mCardsView.onAlreadyFoundTriple(triple);
            mFoundTriplesView.highlightStack(slotIndex);
          } else {
            List<Set<Card>> others = getOtherAlternatives(mAnalysisList.get(mCurrentStep));
            int othersIdx = slotIndex - 1;
            if (othersIdx >= 0 && othersIdx < others.size()) {
              revealAlternative(mCurrentStep, othersIdx, others.get(othersIdx));
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

  private Game loadGame(long gameId, String gameType) {
    Application app = Application.getInstance(this);
    if ("Classic".equalsIgnoreCase(gameType)) return app.getClassicGame(gameId);
    if ("Arcade".equalsIgnoreCase(gameType)) return app.getArcadeGame(gameId);
    if ("Daily".equalsIgnoreCase(gameType)) return app.getDailyGame(gameId);
    return null;
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
        if (prev.getIcon() != null) prev.getIcon().mutate().setAlpha(prevEnabled ? 255 : 96);
      }
      if (next != null) {
        next.setEnabled(nextEnabled);
        if (next.getIcon() != null) next.getIcon().mutate().setAlpha(nextEnabled ? 255 : 96);
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
      updateSubtitle(mCurrentStep, null);
      rebuildFoundTriplesPanel(mCurrentStep, null);
      invalidateOptionsMenu();
      mCardsView.animateTripleFoundToOffscreen(foundTriple);
      mCardsView.updateCardsInPlay(ImmutableList.copyOf(getFinalBoardCards()));
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
        SettingsFragment.getAnimationDuration(this));
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
      mCardsView.updateCardsInPlay(ImmutableList.copyOf(getFinalBoardCards()));
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
        SettingsFragment.getAnimationDuration(this));
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
      // Final board — show duration if available.
      if (mGame != null && !mAnalysisList.isEmpty()) {
        TripleAnalysis last = mAnalysisList.get(mAnalysisList.size() - 1);
        long durationMs = mGame.getTimeElapsed() - last.findTime;
        if (durationMs > 0) {
          actionBar.setSubtitle(
              getString(R.string.board_history_final_board) + " · " + formatDuration(durationMs));
          return;
        }
      }
      actionBar.setSubtitle(getString(R.string.board_history_final_board));
      return;
    }
    String subtitle =
        getString(
            R.string.board_history_subtitle,
            step + 1,
            mAnalysisList.size(),
            formatDuration(analysis.duration));
    actionBar.setSubtitle(subtitle);
  }

  private String formatDuration(long durationMs) {
    long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs);
    long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60;
    long hundredths = (durationMs % 1000) / 10;
    if (minutes > 0) {
      return String.format("%d:%02d.%02d", minutes, seconds, hundredths);
    } else {
      return String.format("%d.%02ds", seconds, hundredths);
    }
  }

  // ---- Possible Triples panel ----

  private void rebuildFoundTriplesPanel(int step, @Nullable TripleAnalysis analysis) {
    if (analysis == null) {
      // Final board: show available triples, revealing already-revealed slots.
      List<Card> finalBoard = getFinalBoardCards();
      mCurrentFinalBoardTriples = Game.getAllValidTriples(finalBoard);
      List<Set<Card>> slots = new ArrayList<>();
      for (int i = 0; i < mCurrentFinalBoardTriples.size(); i++) {
        slots.add(mRevealedFinalBoardSlots.contains(i) ? mCurrentFinalBoardTriples.get(i) : null);
      }
      mFoundTriplesView.setSlots(slots);
      TextView label = findViewById(R.id.alternatives_label);
      if (label != null) {
        label.setText(getString(R.string.analysis_alternatives, mCurrentFinalBoardTriples.size()));
      }
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

  /** Returns the final board state, using mFinalBoardState if set or computing from last step. */
  private List<Card> getFinalBoardCards() {
    if (mFinalBoardState != null) return mFinalBoardState;
    TripleAnalysis last = mAnalysisList.get(mAnalysisList.size() - 1);
    List<Card> fallback = new ArrayList<>(last.boardState);
    fallback.removeAll(last.foundTriple);
    return fallback;
  }

  /**
   * Animates the cards of {@code triple} flying from their grid positions (based on {@code
   * boardState}) to the given slot in the FoundTriplesView. The slot is not modified — call {@code
   * onComplete} to fill it if needed.
   */
  private void animateCardsToSlot(
      List<Card> boardState, Set<Card> triple, int slotIndex, @Nullable Runnable onComplete) {
    if (slotIndex < 0 || slotIndex >= mFoundTriplesView.getChildCount()) {
      if (onComplete != null) onComplete.run();
      return;
    }
    TripleStackView targetView = (TripleStackView) mFoundTriplesView.getChildAt(slotIndex);

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
      if (boardState.indexOf(card) >= 0 && targetBounds.containsKey(card)) {
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
      int cardIndex = boardState.indexOf(card);
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
      // Force layout so getWidth()/getHeight() are valid before starting the animation.
      floatCard.layout(0, 0, gridBounds.width(), gridBounds.height());
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
    List<Card> boardState = mAnalysisList.get(step).boardState;
    animateCardsToSlot(
        boardState,
        triple,
        slotIndex,
        () -> {
          mRevealedAlternatives.computeIfAbsent(step, k -> new HashSet<>()).add(alternativeIdx);
          TripleStackView target = (TripleStackView) mFoundTriplesView.getChildAt(slotIndex);
          if (target != null) target.setTriple(triple);
        });
  }
}
