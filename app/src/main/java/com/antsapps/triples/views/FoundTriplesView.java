package com.antsapps.triples.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.CycleInterpolator;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.cardsview.CardBackgroundDrawable;
import com.antsapps.triples.cardsview.SymbolDrawable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FoundTriplesView extends View {

  private static final float STACK_OVERLAP_PERCENT = 0.2f;
  private static final int COLUMNS = 6;
  private static final float HEIGHT_OVER_WIDTH = (float) ((Math.sqrt(5) - 1) / 2);

  private List<Set<Card>> mFoundTriples = new ArrayList<>();
  private int mTotalTriples = 0;

  private final Paint mPlaceholderPaint;
  private final CardBackgroundDrawable mCardBackground;
  private final Map<Card, SymbolDrawable> mSymbolDrawableCache = new HashMap<>();

  private int mCardWidth;
  private int mCardHeight;
  private int mStackOverlap;

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
    mPlaceholderPaint.setStrokeWidth(2 * getResources().getDisplayMetrics().density);
    mPlaceholderPaint.setPathEffect(new DashPathEffect(new float[] {10, 10}, 0));

    mCardBackground = new CardBackgroundDrawable(context);
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
    int gap = (int) (8 * getResources().getDisplayMetrics().density);
    mCardWidth = Math.max(0, (width - (COLUMNS + 1) * gap) / COLUMNS);
    mCardHeight = (int) (mCardWidth * HEIGHT_OVER_WIDTH);
    mStackOverlap = (int) (mCardHeight * STACK_OVERLAP_PERCENT);

    int rows = (int) Math.ceil((double) mTotalTriples / COLUMNS);
    int stackHeight = mCardHeight + 2 * mStackOverlap;
    int height = rows * (stackHeight + gap) + gap;

    // Enforce max height of roughly 2.5 rows as requested
    int maxHeight = (int) (2.5 * (stackHeight + gap) + gap);
    setMeasuredDimension(width, Math.min(height, maxHeight));
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    int gap = (int) (8 * getResources().getDisplayMetrics().density);

    // Use a reference card width for \"natural\" drawing size, then scale.
    // Let's assume natural width is 200px.
    float naturalWidth = 200f;
    float naturalHeight = naturalWidth * HEIGHT_OVER_WIDTH;
    float naturalOverlap = naturalHeight * STACK_OVERLAP_PERCENT;
    float scale = (float) mCardWidth / naturalWidth;

    for (int i = 0; i < mTotalTriples; i++) {
      int row = i / COLUMNS;
      int col = i % COLUMNS;

      float left = col * (mCardWidth + gap) + gap;
      float top = row * (mCardHeight + 2 * mStackOverlap + gap) + gap;

      canvas.save();
      canvas.translate(left, top);

      float currentScale = (i == mHighlightIndex) ? mHighlightScale : 1.0f;
      canvas.scale(
          scale * currentScale,
          scale * currentScale,
          naturalWidth / 2f,
          (naturalHeight + 2 * naturalOverlap) / 2f);

      if (mFoundTriples != null && i < mFoundTriples.size()) {
        drawTripleStack(
            canvas,
            mFoundTriples.get(i),
            (int) naturalWidth,
            (int) naturalHeight,
            (int) naturalOverlap);
      } else {
        drawPlaceholder(canvas, (int) naturalWidth, (int) naturalHeight, (int) naturalOverlap);
      }

      canvas.restore();
    }
  }

  private void drawTripleStack(
      Canvas canvas, Set<Card> triple, int width, int height, int overlap) {
    int i = 0;
    for (Card card : triple) {
      Rect bounds = new Rect(0, i * overlap, width, i * overlap + height);
      mCardBackground.setBounds(bounds);
      mCardBackground.draw(canvas);

      SymbolDrawable symbolDrawable = mSymbolDrawableCache.get(card);
      if (symbolDrawable == null) {
        symbolDrawable = new SymbolDrawable(getContext(), card);
        mSymbolDrawableCache.put(card, symbolDrawable);
      }
      for (Rect symbolBounds :
          com.antsapps.triples.CardCustomizationUtils.getBoundsForNumId(card.mNumber, bounds)) {
        symbolDrawable.setBounds(symbolBounds);
        symbolDrawable.draw(canvas);
      }
      i++;
    }
  }

  private void drawPlaceholder(Canvas canvas, int width, int height, int overlap) {
    RectF rect = new RectF(0, 0, width, height + 2 * overlap);
    canvas.drawRoundRect(
        rect,
        8 * getResources().getDisplayMetrics().density,
        8 * getResources().getDisplayMetrics().density,
        mPlaceholderPaint);
  }

  public void highlightStack(int index) {
    if (mHighlightAnimator != null) {
      mHighlightAnimator.cancel();
    }
    mHighlightIndex = index;
    mHighlightAnimator = ValueAnimator.ofFloat(1.0f, 1.2f);
    mHighlightAnimator.setDuration(400);
    mHighlightAnimator.setInterpolator(new CycleInterpolator(1));
    mHighlightAnimator.addUpdateListener(
        animation -> {
          mHighlightScale = (float) animation.getAnimatedValue();
          invalidate();
        });
    mHighlightAnimator.start();
  }

  public Rect getStackBounds(int index) {
    int gap = (int) (8 * getResources().getDisplayMetrics().density);
    int row = index / COLUMNS;
    int col = index % COLUMNS;
    int left = col * (mCardWidth + gap) + gap;
    int top = row * (mCardHeight + 2 * mStackOverlap + gap) + gap;

    int[] location = new int[2];
    getLocationInWindow(location);

    return new Rect(
        location[0] + left,
        location[1] + top,
        location[0] + left + mCardWidth,
        location[1] + top + mCardHeight + 2 * mStackOverlap);
  }
}
