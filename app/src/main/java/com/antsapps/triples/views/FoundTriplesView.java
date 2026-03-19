package com.antsapps.triples.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewGroup;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.cardsview.CardView;
import com.antsapps.triples.cardsview.CardsView;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A grid of {@link TripleStackView}s showing found triples and placeholder slots for unfound ones.
 * Uses a fixed 6-column layout.
 */
public class FoundTriplesView extends ViewGroup {

  private static final int COLUMNS = 6;

  private List<Set<Card>> mFoundTriples = new ArrayList<>();
  private int mTotalTriples = 0;

  /** Kept for API compatibility; no longer used for drawing since children handle their own. */
  @SuppressWarnings("unused")
  private CardsView mCardsView;

  // Geometry computed during onMeasure, stored for getCardBoundsInWindow
  private int mSlotWidth;
  private int mSlotHeight;
  private int mCardWidth;
  private int mCardHeight;
  private int mStackDisplacement;
  private final int mPadding;

  public FoundTriplesView(Context context) {
    this(context, null);
  }

  public FoundTriplesView(Context context, AttributeSet attrs) {
    super(context, attrs);
    float density = getResources().getDisplayMetrics().density;
    mPadding = (int) (1 * density);
  }

  public void setFoundTriples(List<Set<Card>> foundTriples, int totalTriples) {
    mFoundTriples = foundTriples;
    mTotalTriples = totalTriples;
    rebuildChildren();
  }

  public void setCardsView(CardsView cardsView) {
    mCardsView = cardsView;
  }

  private void rebuildChildren() {
    // Remove surplus children
    while (getChildCount() > mTotalTriples) {
      removeViewAt(getChildCount() - 1);
    }
    // Add or update children
    for (int i = 0; i < mTotalTriples; i++) {
      TripleStackView child;
      if (i < getChildCount()) {
        child = (TripleStackView) getChildAt(i);
      } else {
        child = new TripleStackView(getContext());
        addView(child);
      }
      if (i < mFoundTriples.size()) {
        child.setTriple(mFoundTriples.get(i));
      } else {
        child.setTriple(null);
      }
    }
    requestLayout();
    invalidate();
  }

  /** Pulses the stack at the given index to draw attention to it. */
  public void highlightStack(int index) {
    if (index >= 0 && index < getChildCount()) {
      ((TripleStackView) getChildAt(index)).animateHighlight();
    }
  }

  /**
   * Returns window-absolute bounds for each card in the triple at the given slot index. Used as
   * animation targets for flying cards into this view.
   */
  public Map<Card, Rect> getCardBoundsInWindow(int index, Set<Card> triple) {
    Preconditions.checkArgument(triple.size() == 3);
    Map<Card, Rect> cardBounds = new HashMap<>();

    int[] locationInWindow = new int[2];
    getLocationInWindow(locationInWindow);

    int row = index / COLUMNS;
    int col = index % COLUMNS;

    int slotLeft = col * mSlotWidth;
    int slotTop = row * mSlotHeight;

    List<Card> sortedTriples = TripleStackView.getSortedTriple(triple);

    for (int i = 0; i < 3; i++) {
      Card card = sortedTriples.get(i);
      int left = slotLeft + mPadding;
      int top = slotTop + mPadding + i * mStackDisplacement;

      cardBounds.put(
          card,
          new Rect(
              locationInWindow[0] + left,
              locationInWindow[1] + top,
              locationInWindow[0] + left + mCardWidth,
              locationInWindow[1] + top + mCardHeight));
    }
    return cardBounds;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    if (width <= 0) {
      setMeasuredDimension(0, 0);
      return;
    }

    mSlotWidth = width / COLUMNS;
    mCardWidth = mSlotWidth - 2 * mPadding;
    mCardHeight = (int) (mCardWidth * CardView.HEIGHT_OVER_WIDTH);
    mStackDisplacement = (int) (mCardHeight * TripleStackView.STACK_DISPLACEMENT_PERCENT);

    int stackHeight = mCardHeight + 2 * mStackDisplacement;
    mSlotHeight = stackHeight + 2 * mPadding;

    for (int i = 0; i < getChildCount(); i++) {
      getChildAt(i)
          .measure(
              MeasureSpec.makeMeasureSpec(mSlotWidth, MeasureSpec.EXACTLY),
              MeasureSpec.makeMeasureSpec(mSlotHeight, MeasureSpec.EXACTLY));
    }

    int rows = (int) Math.ceil((double) Math.max(mTotalTriples, 1) / COLUMNS);
    setMeasuredDimension(width, rows * mSlotHeight);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    for (int i = 0; i < getChildCount(); i++) {
      int col = i % COLUMNS;
      int row = i / COLUMNS;
      int left = col * mSlotWidth;
      int top = row * mSlotHeight;
      getChildAt(i).layout(left, top, left + mSlotWidth, top + mSlotHeight);
    }
  }
}
