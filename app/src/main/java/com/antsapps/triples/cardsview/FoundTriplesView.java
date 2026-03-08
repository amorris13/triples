package com.antsapps.triples.cardsview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.ScaleAnimation;

import com.antsapps.triples.CardCustomizationUtils;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Card;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FoundTriplesView extends View {

  private static final int COLUMNS = 7;
  private static final float ASPECT_RATIO = 3f / 5f;
  private static final int STACK_OVERLAP_DP = 20;

  private int mTotalTriplesCount;
  private List<Set<Card>> mFoundTriples = Lists.newArrayList();

  private int mWidthOfStack;
  private int mHeightOfStack;
  private int mStackOverlap;

  private final Paint mPlaceholderPaint = new Paint();
  private int mHighlightingStack = -1;

  public FoundTriplesView(Context context) {
    this(context, null);
  }

  public FoundTriplesView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mPlaceholderPaint.setStyle(Paint.Style.STROKE);
    mPlaceholderPaint.setColor(0x40000000);
    mPlaceholderPaint.setStrokeWidth(1);
    mStackOverlap = (int) (STACK_OVERLAP_DP * getResources().getDisplayMetrics().density);
  }

  public void setTotalTriplesCount(int count) {
    mTotalTriplesCount = count;
    requestLayout();
  }

  public void setFoundTriples(List<Set<Card>> foundTriples) {
    mFoundTriples = foundTriples;
    invalidate();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
    mWidthOfStack = width / COLUMNS;
    mHeightOfStack = (int) (mWidthOfStack / ASPECT_RATIO);
    int rows = (int) Math.ceil((double) mTotalTriplesCount / COLUMNS);
    int height = mHeightOfStack * rows;
    setMeasuredDimension(width, height);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    for (int i = 0; i < mTotalTriplesCount; i++) {
      int col = i % COLUMNS;
      int row = i / COLUMNS;
      int left = col * mWidthOfStack;
      int top = row * mHeightOfStack;

      if (i < mFoundTriples.size()) {
        drawTriple(canvas, mFoundTriples.get(i), left, top);
      } else {
        drawPlaceholder(canvas, left, top);
      }
    }
  }

  private void drawPlaceholder(Canvas canvas, int left, int top) {
    Rect rect = new Rect(left, top, left + mWidthOfStack, top + mHeightOfStack);
    rect.inset(5, 5);
    canvas.drawRect(rect, mPlaceholderPaint);
  }

  private void drawTriple(Canvas canvas, Set<Card> triple, int left, int top) {
    int sc = canvas.save();
    canvas.translate(left, top);

    // Scaling for crisp rendering: draw at 200x200 (or similar) and scale down.
    // Actually, CardDrawable.regenerateCachedDrawable already handles density-based scaling.
    // Let's draw them manually here for efficiency.
    float scale = (float) mWidthOfStack / 200f; // Assume base size of 200
    canvas.scale(scale, scale);

    Rect cardBounds = new Rect(0, 0, 200, (int) (200 / ASPECT_RATIO));
    int stackOverlap = (int) (mStackOverlap / scale);

    int i = 0;
    for (Card card : triple) {
      int cardTop = i * stackOverlap;
      Rect bounds = new Rect(0, cardTop, 200, cardTop + (int) (200 / ASPECT_RATIO));
      drawCard(canvas, card, bounds);
      i++;
    }

    canvas.restoreToCount(sc);
  }

  private void drawCard(Canvas canvas, Card card, Rect bounds) {
    // Basic card drawing logic
    Paint p = new Paint();
    p.setColor(0xFFFFFFFF);
    canvas.drawRect(bounds, p);
    p.setStyle(Paint.Style.STROKE);
    p.setColor(0xFF000000);
    p.setStrokeWidth(2);
    canvas.drawRect(bounds, p);

    // Draw symbols
    SymbolDrawable symbolDrawable = new SymbolDrawable(getContext(), card);
    for (Rect symbolBounds : CardDrawable.getBoundsForNumId(card.mNumber, bounds)) {
      symbolDrawable.setBounds(symbolBounds);
      symbolDrawable.draw(canvas);
    }
  }

  public Map<Card, Rect> getCardRectsInStack(int index) {
    int col = index % COLUMNS;
    int row = index / COLUMNS;
    int left = col * mWidthOfStack;
    int top = row * mHeightOfStack;

    int[] location = new int[2];
    getLocationOnScreen(location);

    Set<Card> triple = mFoundTriples.get(index);
    Map<Card, Rect> rects = Maps.newHashMap();

    int i = 0;
    for (Card card : triple) {
      int cardTop = top + i * mStackOverlap;
      Rect rect = new Rect(
          location[0] + left,
          location[1] + cardTop,
          location[0] + left + mWidthOfStack,
          location[1] + cardTop + (int) (mWidthOfStack / ASPECT_RATIO));
      rects.put(card, rect);
      i++;
    }

    return rects;
  }

  public void highlightStack(int index) {
    mHighlightingStack = index;
    // Simple throb animation
    Animation throb = new ScaleAnimation(1f, 1.1f, 1f, 1.1f,
        (index % COLUMNS + 0.5f) * mWidthOfStack,
        (index / COLUMNS + 0.5f) * mHeightOfStack);
    throb.setDuration(200);
    throb.setInterpolator(new CycleInterpolator(0.5f));
    startAnimation(throb);
  }
}
