package com.antsapps.triples.views;

import static com.antsapps.triples.cardsview.CardBackgroundDrawable.INSET_DP;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import com.antsapps.triples.SettingsFragment;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.cardsview.CardView;
import com.antsapps.triples.cardsview.CardsView;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class FoundTriplesView extends ViewGroup {

  public interface OnPlaceholderClickListener {
    void onPlaceholderClick(int index);
  }

  private static final float STACK_DISPLACEMENT_PERCENT = 0.65f;
  private static final int COLUMNS = 6;

  private List<Set<Card>> mFoundTriples = new ArrayList<>();
  private int mTotalTriples = 0;
  private final Set<Integer> mRevealedIndices = new HashSet<>();
  private OnPlaceholderClickListener mOnPlaceholderClickListener;

  private CardsView mCardsView;

  private int mCardWidth;
  private int mCardHeight;
  private int mStackDisplacement;
  private int mPadding;

  private int mSlotWidth;
  private int mSlotHeight;

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
    mRevealedIndices.clear();
    if (mTotalTriples > 0) {
      mRevealedIndices.add(0);
    }
    syncViews();
  }

  private void syncViews() {
    removeAllViews();
    for (int i = 0; i < mTotalTriples; i++) {
      TripleStackView view = new TripleStackView(getContext());
      final int index = i;
      view.setOnClickListener(
          v -> {
            if (mOnPlaceholderClickListener != null && !mRevealedIndices.contains(index)) {
              mOnPlaceholderClickListener.onPlaceholderClick(index);
            }
          });
      addView(view);
    }
    requestLayout();
  }

  public void setOnPlaceholderClickListener(OnPlaceholderClickListener listener) {
    mOnPlaceholderClickListener = listener;
  }

  public void revealAlternative(int index) {
    mRevealedIndices.add(index);
    updateTripleStackView(index);
  }

  public int getNextPlaceholderIndex() {
    for (int i = 0; i < mTotalTriples; i++) {
      if (!mRevealedIndices.contains(i)) {
        return i;
      }
    }
    return -1;
  }

  private void updateTripleStackView(int index) {
    if (index >= 0 && index < getChildCount()) {
      TripleStackView view = (TripleStackView) getChildAt(index);
      view.setTriple(
          index < mFoundTriples.size() ? mFoundTriples.get(index) : null,
          !mRevealedIndices.contains(index),
          mCardsView.cardWidth(),
          mCardsView.cardHeight());
    }
  }

  public void setCardsView(CardsView cardsView) {
    mCardsView = cardsView;
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
    mStackDisplacement = (int) (mCardHeight * STACK_DISPLACEMENT_PERCENT);

    int rows = (int) Math.ceil((double) mTotalTriples / COLUMNS);
    int stackHeight = mCardHeight + 2 * mStackDisplacement;
    mSlotHeight = stackHeight + 2 * mPadding;

    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      child.measure(
          MeasureSpec.makeMeasureSpec(mCardWidth, MeasureSpec.EXACTLY),
          MeasureSpec.makeMeasureSpec(stackHeight, MeasureSpec.EXACTLY));
      updateTripleStackView(i);
    }

    setMeasuredDimension(width, rows * mSlotHeight);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    for (int i = 0; i < getChildCount(); i++) {
      int row = i / COLUMNS;
      int col = i % COLUMNS;

      int left = col * mSlotWidth + mPadding;
      int top = row * mSlotHeight + mPadding;

      View child = getChildAt(i);
      child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
    }
  }

  public void highlightStack(int index) {
    if (index >= 0 && index < getChildCount()) {
      View child = getChildAt(index);
      child
          .animate()
          .scaleX(1.2f)
          .scaleY(1.2f)
          .setDuration(SettingsFragment.getAnimationDuration(getContext()))
          .setInterpolator(new CycleInterpolator(0.5f))
          .withEndAction(
              () -> {
                child.setScaleX(1.0f);
                child.setScaleY(1.0f);
              })
          .start();
    }
  }

  public Map<Card, Rect> getCardBoundsInWindow(int index, Set<Card> triple) {
    Preconditions.checkArgument(triple.size() == 3);
    Map<Card, Rect> cardBounds = new HashMap<>();

    int[] locationInWindow = new int[2];
    getLocationInWindow(locationInWindow);

    int row = index / COLUMNS;
    int col = index % COLUMNS;

    int slotLeft = col * mSlotWidth;
    int slotTop = row * mSlotHeight;

    List<Card> sortedTriples = getSortedTriples(triple);

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

  @NotNull
  private static List<Card> getSortedTriples(Set<Card> triple) {
    List<Card> sortedTriples = Lists.newArrayList(triple);
    Collections.sort(sortedTriples);
    return sortedTriples;
  }
}
