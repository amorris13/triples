package com.antsapps.triples.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
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
 * A grid of {@link TripleStackView}s showing triples (filled) and placeholder slots (null entries).
 * Uses a fixed 6-column layout.
 */
public class FoundTriplesView extends ViewGroup {

  public interface OnSlotClickListener {
    void onSlotClick(int slotIndex, @Nullable Set<Card> triple);
  }

  private static final int COLUMNS = 6;

  /** Each entry is a triple to show (non-null = filled stack, null = placeholder). */
  private List<Set<Card>> mSlots = new ArrayList<>();

  private CardsView mCardsView;
  private OnSlotClickListener mOnSlotClickListener;
  private int mHighlightedSlot = -1;

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

  /**
   * Sets the full list of slots. Null entries are rendered as placeholders; non-null as stacks.
   * Replaces any previous data.
   */
  public void setSlots(List<Set<Card>> slots) {
    mSlots = new ArrayList<>(slots);
    rebuildChildren();
  }

  /**
   * Backward-compatible helper for game activities: places found triples in the first N slots and
   * fills the remaining (totalTriples - foundTriples.size()) slots with placeholders.
   */
  public void setFoundTriples(List<Set<Card>> foundTriples, int totalTriples) {
    List<Set<Card>> slots = new ArrayList<>(foundTriples);
    while (slots.size() < totalTriples) {
      slots.add(null);
    }
    setSlots(slots);
  }

  public void setCardsView(CardsView cardsView) {
    mCardsView = cardsView;
    for (int i = 0; i < getChildCount(); i++) {
      ((TripleStackView) getChildAt(i)).setNaturalCardDimensionsProvider(cardsView);
    }
  }

  public void setOnSlotClickListener(OnSlotClickListener listener) {
    mOnSlotClickListener = listener;
  }

  /** Sets the slot at the given index as highlighted (drawn with a border), clears others. */
  public void setHighlightedSlot(int index) {
    if (mHighlightedSlot != index) {
      int old = mHighlightedSlot;
      mHighlightedSlot = index;
      if (old >= 0 && old < getChildCount()) {
        ((TripleStackView) getChildAt(old)).setHighlighted(false);
      }
      if (index >= 0 && index < getChildCount()) {
        ((TripleStackView) getChildAt(index)).setHighlighted(true);
      }
    }
  }

  private void rebuildChildren() {
    // Remove surplus children
    while (getChildCount() > mSlots.size()) {
      removeViewAt(getChildCount() - 1);
    }
    // Add or update children
    for (int i = 0; i < mSlots.size(); i++) {
      TripleStackView child;
      if (i < getChildCount()) {
        child = (TripleStackView) getChildAt(i);
      } else {
        child = new TripleStackView(getContext());
        if (mCardsView != null) {
          child.setNaturalCardDimensionsProvider(mCardsView);
        }
        addView(child);
      }
      child.setTriple(mSlots.get(i));
      child.setHighlighted(i == mHighlightedSlot);
      final int slotIndex = i;
      final TripleStackView c = child;
      child.setOnClickListener(
          v -> {
            if (mOnSlotClickListener != null) {
              mOnSlotClickListener.onSlotClick(slotIndex, c.getTriple());
            }
          });
    }
    requestLayout();
    invalidate();
  }

  /** Pulses the stack at the given slot index to draw attention to it. */
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

    int rows = (int) Math.ceil((double) Math.max(mSlots.size(), 1) / COLUMNS);
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
