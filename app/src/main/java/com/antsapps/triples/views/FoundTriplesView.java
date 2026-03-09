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
import java.util.List;
import java.util.Set;

public class FoundTriplesView extends View {

  private static final float STACK_OVERLAP_PERCENT = 0.2f;
  private static final int COLUMNS = 6;
  private static final int CARD_ASPECT_RATIO_NUM = 3;
  private static final int CARD_ASPECT_RATIO_DEN = 2; // 3:2 aspect ratio

  private List<Set<Card>> mFoundTriples = new ArrayList<>();
  private int mTotalTriples = 0;

  private final Paint mPlaceholderPaint;
  private final CardBackgroundDrawable mCardBackground;
  private SymbolDrawable mSymbolDrawable;

  private int mCardWidth;
  private int mCardHeight;
  private int mStackOverlap;
  private float mScale;

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
    // We need a rough estimate of mCardWidth to calculate mStackOverlap,
    // and mStackOverlap to calculate mCardWidth.
    // Width = COLUMNS * mCardWidth + (COLUMNS + 1) * Gap
    // Let Gap = some fixed small value, say 8dp
    int gap = (int) (8 * getResources().getDisplayMetrics().density);
    mCardWidth = Math.max(0, (width - (COLUMNS + 1) * gap) / COLUMNS);
    mCardHeight = mCardWidth * CARD_ASPECT_RATIO_NUM / CARD_ASPECT_RATIO_DEN;
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

    for (int i = 0; i < mTotalTriples; i++) {
      int row = i / COLUMNS;
      int col = i % COLUMNS;

      int left = col * (mCardWidth + gap) + gap;
      int top = row * (mCardHeight + 2 * mStackOverlap + gap) + gap;

      float currentScale = (i == mHighlightIndex) ? mHighlightScale : 1.0f;
      if (currentScale != 1.0f) {
        canvas.save();
        canvas.scale(
            currentScale,
            currentScale,
            left + mCardWidth / 2f,
            top + mCardHeight / 2f + mStackOverlap);
      }

      if (mFoundTriples != null && i < mFoundTriples.size()) {
        drawTripleStack(canvas, mFoundTriples.get(i), left, top);
      } else {
        drawPlaceholder(canvas, left, top);
      }

      if (currentScale != 1.0f) {
        canvas.restore();
      }
    }
  }

  private void drawTripleStack(Canvas canvas, Set<Card> triple, int left, int top) {
    int i = 0;
    for (Card card : triple) {
      Rect bounds =
          new Rect(
              left,
              top + i * mStackOverlap,
              left + mCardWidth,
              top + i * mStackOverlap + mCardHeight);
      mCardBackground.setBounds(bounds);
      mCardBackground.draw(canvas);

      mSymbolDrawable = new SymbolDrawable(getContext(), card);
      for (Rect symbolBounds : getBoundsForNumId(card.mNumber, bounds)) {
        mSymbolDrawable.setBounds(symbolBounds);
        mSymbolDrawable.draw(canvas);
      }
      i++;
    }
  }

  private void drawPlaceholder(Canvas canvas, int left, int top) {
    RectF rect = new RectF(left, top, left + mCardWidth, top + mCardHeight + 2 * mStackOverlap);
    canvas.drawRoundRect(
        rect,
        8 * getResources().getDisplayMetrics().density,
        8 * getResources().getDisplayMetrics().density,
        mPlaceholderPaint);
  }

  private List<Rect> getBoundsForNumId(int id, Rect bounds) {
    List<Rect> rects = new ArrayList<>();
    int width = bounds.width();
    int height = bounds.height();
    int halfSideLength = width / 10;
    int gap = halfSideLength / 2;
    switch (id) {
      case 0:
        rects.add(squareFromCenterAndRadius(bounds.centerX(), bounds.centerY(), halfSideLength));
        break;
      case 1:
        rects.add(
            squareFromCenterAndRadius(
                bounds.centerX() - gap / 2 - halfSideLength, bounds.centerY(), halfSideLength));
        rects.add(
            squareFromCenterAndRadius(
                bounds.centerX() + gap / 2 + halfSideLength, bounds.centerY(), halfSideLength));
        break;
      case 2:
        rects.add(
            squareFromCenterAndRadius(
                bounds.centerX() - gap - halfSideLength * 2, bounds.centerY(), halfSideLength));
        rects.add(squareFromCenterAndRadius(bounds.centerX(), bounds.centerY(), halfSideLength));
        rects.add(
            squareFromCenterAndRadius(
                bounds.centerX() + gap + halfSideLength * 2, bounds.centerY(), halfSideLength));
        break;
    }
    return rects;
  }

  private Rect squareFromCenterAndRadius(int centerX, int centerY, int radius) {
    return new Rect(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
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
