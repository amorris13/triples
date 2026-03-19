package com.antsapps.triples.views;

import static com.antsapps.triples.cardsview.CardBackgroundDrawable.INSET_DP;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.CycleInterpolator;
import com.antsapps.triples.R;
import com.antsapps.triples.SettingsFragment;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.cardsview.CardView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A view that renders a single triple stack (3 stacked cards) or a placeholder dashed outline. This
 * is used both in FoundTriplesView (grid of found triples) and in the BoardHistoryActivity's
 * alternatives panel.
 */
public class TripleStackView extends View {

  static final float STACK_DISPLACEMENT_PERCENT = 0.65f;

  private Set<Card> mTriple = null;
  private boolean mHighlighted = false;
  private Runnable mOnPlaceholderClickListener;

  private final Paint mPlaceholderPaint;
  private final Paint mHighlightPaint;
  private final Map<Card, CardView> mCardViewCache = new HashMap<>();

  private int mCardWidth;
  private int mCardHeight;
  private int mStackDisplacement;
  private int mPadding;

  public TripleStackView(Context context) {
    this(context, null);
  }

  public TripleStackView(Context context, AttributeSet attrs) {
    super(context, attrs);
    float density = getResources().getDisplayMetrics().density;

    mPlaceholderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mPlaceholderPaint.setStyle(Paint.Style.STROKE);
    mPlaceholderPaint.setColor(getResources().getColor(R.color.colorOutlineVariant));
    mPlaceholderPaint.setStrokeWidth(3 * density);
    mPlaceholderPaint.setPathEffect(new DashPathEffect(new float[] {8 * density, 8 * density}, 0));

    mHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mHighlightPaint.setStyle(Paint.Style.STROKE);
    mHighlightPaint.setStrokeWidth(4 * density);
    TypedValue typedValue = new TypedValue();
    context.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
    mHighlightPaint.setColor(typedValue.data);

    mPadding = (int) (1 * density);

    setOnClickListener(
        v -> {
          if (mTriple == null && mOnPlaceholderClickListener != null) {
            mOnPlaceholderClickListener.run();
          }
        });
  }

  public void setTriple(Set<Card> triple) {
    mTriple = triple;
    invalidate();
  }

  public Set<Card> getTriple() {
    return mTriple;
  }

  public void setHighlighted(boolean highlighted) {
    mHighlighted = highlighted;
    invalidate();
  }

  public void setOnPlaceholderClickListener(Runnable listener) {
    mOnPlaceholderClickListener = listener;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    if (width <= 0) {
      setMeasuredDimension(0, 0);
      return;
    }
    updateDimensions(width);
    int height = mCardHeight + 2 * mStackDisplacement + 2 * mPadding;
    setMeasuredDimension(width, height);
  }

  private void updateDimensions(int width) {
    mCardWidth = width - 2 * mPadding;
    mCardHeight = (int) (mCardWidth * CardView.HEIGHT_OVER_WIDTH);
    mStackDisplacement = (int) (mCardHeight * STACK_DISPLACEMENT_PERCENT);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (mTriple != null) {
      drawTripleStack(canvas);
      if (mHighlighted) {
        drawHighlightBorder(canvas);
      }
    } else {
      drawPlaceholder(canvas);
    }
  }

  private void drawTripleStack(Canvas canvas) {
    List<Card> sorted = getSortedTriple(mTriple);
    Rect bounds = new Rect(mPadding, mPadding, mPadding + mCardWidth, mPadding + mCardHeight);
    for (int i = 0; i < sorted.size(); i++) {
      Card card = sorted.get(i);
      CardView cardView = mCardViewCache.get(card);
      if (cardView == null) {
        cardView = new CardView(getContext(), card);
        mCardViewCache.put(card, cardView);
      }
      canvas.save();
      canvas.translate(0, i * mStackDisplacement);
      cardView.drawCardContent(canvas, bounds);
      canvas.restore();
    }
  }

  private void drawHighlightBorder(Canvas canvas) {
    float inset = INSET_DP * getContext().getResources().getDisplayMetrics().density;
    int totalHeight = mCardHeight + 2 * mStackDisplacement;
    RectF rect =
        new RectF(
            mPadding + inset,
            mPadding + inset,
            mPadding + mCardWidth - inset,
            mPadding + totalHeight - inset);
    canvas.drawRoundRect(rect, 10, 10, mHighlightPaint);
  }

  private void drawPlaceholder(Canvas canvas) {
    float inset = INSET_DP * getContext().getResources().getDisplayMetrics().density;
    int totalHeight = mCardHeight + 2 * mStackDisplacement;
    RectF rect =
        new RectF(
            mPadding + inset,
            mPadding + inset,
            mPadding + mCardWidth - inset,
            mPadding + totalHeight - inset);
    canvas.drawRoundRect(rect, 10, 10, mPlaceholderPaint);
  }

  /**
   * Returns the window-absolute bounds for each card in the given triple, as laid out in this view.
   * Used as animation targets when animating cards to/from this slot.
   */
  public Map<Card, Rect> computeCardBoundsInWindow(Set<Card> triple) {
    Map<Card, Rect> cardBounds = new HashMap<>();
    int[] loc = new int[2];
    getLocationInWindow(loc);
    List<Card> sorted = getSortedTriple(triple);
    for (int i = 0; i < sorted.size(); i++) {
      Card card = sorted.get(i);
      int left = loc[0] + mPadding;
      int top = loc[1] + mPadding + i * mStackDisplacement;
      cardBounds.put(card, new Rect(left, top, left + mCardWidth, top + mCardHeight));
    }
    return cardBounds;
  }

  /** Temporarily pulses the scale of this view to draw attention to it. */
  public void animateHighlight() {
    animate()
        .scaleX(1.2f)
        .scaleY(1.2f)
        .setDuration(SettingsFragment.getAnimationDuration(getContext()))
        .setInterpolator(new CycleInterpolator(0.5f))
        .start();
  }

  static List<Card> getSortedTriple(Set<Card> triple) {
    List<Card> sorted = new ArrayList<>(triple);
    Collections.sort(sorted);
    return sorted;
  }
}
