package com.antsapps.triples.cardsview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.CycleInterpolator;

import com.antsapps.triples.R;
import com.antsapps.triples.backend.Card;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FoundTriplesView extends View {

  private static final int COLUMNS = 7;
  private static final float ASPECT_RATIO = 3f / 5f;
  private static final float CARD_HEIGHT_OVER_WIDTH = (float) ((Math.sqrt(5) - 1) / 2);

  private int mTotalTriplesCount;
  private List<Set<Card>> mFoundTriples = Lists.newArrayList();

  private int mWidthOfStack;
  private int mHeightOfStack;

  private final Paint mPlaceholderPaint = new Paint();
  private CardBackgroundDrawable mCardBackgroundDrawable;
  private SymbolDrawable mSymbolDrawable;

  private int mHighlightingStack = -1;
  private float mHighlightScale = 1f;

  public FoundTriplesView(Context context) {
    this(context, null);
  }

  public FoundTriplesView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mPlaceholderPaint.setStyle(Paint.Style.STROKE);
    mPlaceholderPaint.setColor(0x40000000);
    mPlaceholderPaint.setStrokeWidth(1);

    mCardBackgroundDrawable = new CardBackgroundDrawable(context);
    mSymbolDrawable = new SymbolDrawable(context, new Card(0, 0, 0, 0));
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

      int sc = canvas.save();
      if (i == mHighlightingStack) {
        canvas.scale(mHighlightScale, mHighlightScale, left + mWidthOfStack / 2f, top + mHeightOfStack / 2f);
      }

      Rect stackRect = new Rect(left, top, left + mWidthOfStack, top + mHeightOfStack);
      stackRect.inset(5, 5);

      if (i < mFoundTriples.size()) {
        drawTriple(canvas, mFoundTriples.get(i), stackRect);
      } else {
        canvas.drawRect(stackRect, mPlaceholderPaint);
      }
      canvas.restoreToCount(sc);
    }
  }

  private void drawTriple(Canvas canvas, Set<Card> triple, Rect stackRect) {
    int cardHeight = (int) (stackRect.width() * CARD_HEIGHT_OVER_WIDTH);
    int offset = (stackRect.height() - cardHeight) / 2;

    int i = 0;
    for (Card card : triple) {
      Rect bounds = new Rect(
          stackRect.left,
          stackRect.top + i * offset,
          stackRect.right,
          stackRect.top + i * offset + cardHeight);
      drawCard(canvas, card, bounds);
      i++;
    }
  }

  private void drawCard(Canvas canvas, Card card, Rect bounds) {
    mCardBackgroundDrawable.setBounds(bounds);
    mCardBackgroundDrawable.draw(canvas);

    mSymbolDrawable.setCard(card);
    for (Rect symbolBounds : CardDrawable.getBoundsForNumId(card.mNumber, bounds)) {
      mSymbolDrawable.setBounds(symbolBounds);
      mSymbolDrawable.draw(canvas);
    }
  }

  public Map<Card, Rect> getCardRectsInStack(int index) {
    int col = index % COLUMNS;
    int row = index / COLUMNS;
    int left = col * mWidthOfStack;
    int top = row * mHeightOfStack;

    int[] location = new int[2];
    getLocationOnScreen(location);

    Rect stackRect = new Rect(left, top, left + mWidthOfStack, top + mHeightOfStack);
    stackRect.inset(5, 5);

    int cardHeight = (int) (stackRect.width() * CARD_HEIGHT_OVER_WIDTH);
    int offset = (stackRect.height() - cardHeight) / 2;

    Set<Card> triple = mFoundTriples.get(index);
    Map<Card, Rect> rects = Maps.newHashMap();

    int i = 0;
    for (Card card : triple) {
      Rect rect = new Rect(
          location[0] + stackRect.left,
          location[1] + stackRect.top + i * offset,
          location[0] + stackRect.right,
          location[1] + stackRect.top + i * offset + cardHeight);
      rects.put(card, rect);
      i++;
    }

    return rects;
  }

  public void highlightStack(int index) {
    mHighlightingStack = index;
    ValueAnimator animator = ValueAnimator.ofFloat(1f, 1.15f);
    animator.setDuration(300);
    animator.setInterpolator(new CycleInterpolator(0.5f));
    animator.addUpdateListener(animation -> {
      mHighlightScale = (float) animation.getAnimatedValue();
      invalidate();
    });
    animator.start();
  }
}
