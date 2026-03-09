package com.antsapps.triples.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.CycleInterpolator;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.cardsview.CardDrawable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FoundTriplesView extends View {

  private static final float STACK_DISPLACEMENT_PERCENT = 0.25f;
  private static final int COLUMNS = 6;
  private static final float HEIGHT_OVER_WIDTH = (float) ((Math.sqrt(5) - 1) / 2);

  private List<Set<Card>> mFoundTriples = new ArrayList<>();
  private int mTotalTriples = 0;

  private final Paint mPlaceholderPaint;
  private final Map<Card, CardDrawable> mCardDrawableCache = new HashMap<>();

  private int mCardWidth;
  private int mCardHeight;
  private int mStackOverlap;
  private int mPadding;

  private int mHighlightIndex = -1;
  private float mHighlightScale = 1.0f;
  private ValueAnimator mHighlightAnimator;

  public FoundTriplesView(Context context) {
    this(context, null);
  }

  public FoundTriplesView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mPlaceholderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mPlaceholderPaint.setStyle(Paint.Style.STROKE);
    mPlaceholderPaint.setColor(Color.LTGRAY);
    mPlaceholderPaint.setStrokeWidth(getResources().getDisplayMetrics().density);
    mPlaceholderPaint.setPathEffect(new DashPathEffect(new float[] {10, 10}, 0));
    mPadding = (int) (1 * getResources().getDisplayMetrics().density);
  }

  public void setFoundTriples(List<Set<Card>> foundTriples, int totalTriples) {
    mFoundTriples = foundTriples;
    mTotalTriples = totalTriples;
    requestLayout();
    invalidate();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    if (width <= 0) {
      setMeasuredDimension(0, 0);
      return;
    }
    int slotWidth = width / COLUMNS;
    mCardWidth = slotWidth - 2 * mPadding;
    mCardHeight = (int) (mCardWidth * HEIGHT_OVER_WIDTH);
    mStackOverlap = (int) (mCardHeight * STACK_DISPLACEMENT_PERCENT);

    int rows = (int) Math.ceil((double) mTotalTriples / COLUMNS);
    int stackHeight = mCardHeight + 2 * mStackOverlap;
    int height = rows * (stackHeight + 2 * mPadding);

    // Enforce max height of roughly 2.5 rows as requested
    int maxHeight = (int) (2.5 * (stackHeight + 2 * mPadding));
    setMeasuredDimension(width, Math.min(height, maxHeight));
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    int slotWidth = getWidth() / COLUMNS;
    int horizontalOffset = (getWidth() - slotWidth * COLUMNS) / 2;
    float naturalWidth = 200f;
    float naturalHeight = naturalWidth * HEIGHT_OVER_WIDTH;
    float naturalDisplacement = naturalHeight * STACK_DISPLACEMENT_PERCENT;
    float scale = (float) mCardWidth / naturalWidth;

    for (int i = 0; i < mTotalTriples; i++) {
      int row = i / COLUMNS;
      int col = i % COLUMNS;

      float left = horizontalOffset + col * slotWidth + mPadding;
      float top = row * (mCardHeight + 2 * mStackOverlap + 2 * mPadding) + mPadding;

      canvas.save();
      canvas.translate(left, top);

      float currentScale = (i == mHighlightIndex) ? mHighlightScale : 1.0f;
      canvas.scale(
          scale * currentScale,
          scale * currentScale,
          naturalWidth / 2f,
          (naturalHeight + 2 * naturalDisplacement) / 2f);

      if (mFoundTriples != null && i < mFoundTriples.size()) {
        drawTripleStack(
            canvas,
            mFoundTriples.get(i),
            (int) naturalWidth,
            (int) naturalHeight,
            (int) naturalDisplacement);
      } else {
        drawPlaceholder(canvas, (int) naturalWidth, (int) naturalHeight, (int) naturalDisplacement);
      }

      canvas.restore();
    }
  }

  private void drawTripleStack(
      Canvas canvas, Set<Card> triple, int width, int height, int displacement) {
    int i = 0;
    for (Card card : triple) {
      Rect bounds = new Rect(0, i * displacement, width, i * displacement + height);
      CardDrawable cardDrawable = mCardDrawableCache.get(card);
      if (cardDrawable == null) {
        cardDrawable = new CardDrawable(getContext(), null, card, null);
        mCardDrawableCache.put(card, cardDrawable);
      }
      cardDrawable.mBounds = bounds;
      cardDrawable.draw(canvas);
      i++;
    }
  }

  private void drawPlaceholder(Canvas canvas, int width, int height, int displacement) {
    float inset = mPlaceholderPaint.getStrokeWidth() / 2;
    RectF rect = new RectF(inset, inset, width - inset, height + 2 * displacement - inset);
    canvas.drawRoundRect(rect, 10, 10, mPlaceholderPaint);
  }

  public void highlightStack(int index) {
    if (mHighlightAnimator != null) {
      mHighlightAnimator.cancel();
    }
    mHighlightIndex = index;
    mHighlightAnimator = ValueAnimator.ofFloat(1.0f, 1.2f);
    mHighlightAnimator.setDuration(getAnimationDuration());
    mHighlightAnimator.setInterpolator(new CycleInterpolator(1));
    mHighlightAnimator.addUpdateListener(
        animation -> {
          mHighlightScale = (float) animation.getAnimatedValue();
          invalidate();
        });
    mHighlightAnimator.start();
  }

  private int getAnimationDuration() {
    return PreferenceManager.getDefaultSharedPreferences(getContext())
        .getInt(getContext().getString(R.string.pref_animation_speed), 800);
  }

  public Rect getStackBounds(int index) {
    int slotWidth = getWidth() / COLUMNS;
    int horizontalOffset = (getWidth() - slotWidth * COLUMNS) / 2;
    int row = index / COLUMNS;
    int col = index % COLUMNS;

    int left = horizontalOffset + col * slotWidth + mPadding;
    int top = row * (mCardHeight + 2 * mStackOverlap + 2 * mPadding) + mPadding;

    int[] location = new int[2];
    getLocationInWindow(location);

    return new Rect(
        location[0] + left,
        location[1] + top,
        location[0] + left + mCardWidth,
        location[1] + top + mCardHeight + 2 * mStackOverlap);
  }
}
