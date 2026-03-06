package com.antsapps.triples;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.antsapps.triples.backend.Card;
import com.antsapps.triples.cardsview.SymbolDrawable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FoundTriplesView extends View {

  private static final float CARD_ASPECT_RATIO = (float) ((Math.sqrt(5) - 1) / 2);
  private static final int STACK_OVERLAP_FACTOR = 4; // 1/4 of card height
  private static final int COLUMNS = 5;
  private static final int PADDING_DP = 8;
  private static final int HIGHLIGHT_DURATION_MS = 1000;

  private final Paint mPlaceholderPaint;
  private final Paint mBackgroundPaint;
  private final Paint mHighlightPaint;
  private final int mPadding;

  private List<Set<Card>> mAllTriples = Lists.newArrayList();
  private List<Set<Card>> mFoundTriples = Lists.newArrayList();
  private final Map<Card, SymbolDrawable> mSymbolDrawables = Maps.newHashMap();

  private Set<Card> mHighlightedTriple = null;
  private final Handler mHandler = new Handler();

  public FoundTriplesView(Context context) {
    this(context, null);
  }

  public FoundTriplesView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mPadding = (int) (PADDING_DP * getResources().getDisplayMetrics().density);

    mPlaceholderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mPlaceholderPaint.setStyle(Paint.Style.STROKE);
    mPlaceholderPaint.setColor(ContextCompat.getColor(context, R.color.colorOutlineVariant));
    mPlaceholderPaint.setStrokeWidth(2);
    mPlaceholderPaint.setPathEffect(new DashPathEffect(new float[]{10, 5}, 0));

    mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mBackgroundPaint.setColor(ContextCompat.getColor(context, R.color.card_background));
    mBackgroundPaint.setShadowLayer(4, 2, 2, ContextCompat.getColor(context, R.color.card_shadow));

    mHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mHighlightPaint.setColor(ContextCompat.getColor(context, R.color.card_selected_outline));
    mHighlightPaint.setStyle(Paint.Style.STROKE);
    mHighlightPaint.setStrokeWidth(6);

    setLayerType(LAYER_TYPE_SOFTWARE, null);
  }

  public void setTriples(List<Set<Card>> allTriples, List<Set<Card>> foundTriples) {
    mAllTriples = allTriples;
    mFoundTriples = foundTriples;
    for (Set<Card> triple : allTriples) {
      for (Card card : triple) {
        if (!mSymbolDrawables.containsKey(card)) {
          mSymbolDrawables.put(card, new SymbolDrawable(getContext(), card));
        }
      }
    }
    requestLayout();
    invalidate();
  }

  public void highlightTriple(Set<Card> triple) {
    mHighlightedTriple = triple;
    invalidate();
    mHandler.removeCallbacksAndMessages(null);
    mHandler.postDelayed(() -> {
      mHighlightedTriple = null;
      invalidate();
    }, HIGHLIGHT_DURATION_MS);
  }

  public Rect getTripleLocation(Set<Card> triple) {
    int index = mAllTriples.indexOf(triple);
    if (index == -1) return null;

    int column = index % COLUMNS;
    int row = index / COLUMNS;

    int width = getWidth();
    int availableWidth = width - (COLUMNS + 1) * mPadding;
    int cardWidth = availableWidth / COLUMNS;
    int cardHeight = (int) (cardWidth * CARD_ASPECT_RATIO);
    int stackHeight = cardHeight + (cardHeight / STACK_OVERLAP_FACTOR) * 2;

    int x = mPadding + column * (cardWidth + mPadding);
    int y = mPadding + row * (stackHeight + mPadding);

    Rect rect = new Rect(x, y, x + cardWidth, y + stackHeight);
    int[] location = new int[2];
    getLocationInWindow(location);
    rect.offset(location[0], location[1]);
    return rect;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    if (width == 0) {
      width = getResources().getDisplayMetrics().widthPixels;
    }

    int availableWidth = width - (COLUMNS + 1) * mPadding;
    int cardWidth = availableWidth / COLUMNS;
    int cardHeight = (int) (cardWidth * CARD_ASPECT_RATIO);
    int stackHeight = cardHeight + (cardHeight / STACK_OVERLAP_FACTOR) * 2;

    int rows = (int) Math.ceil((double) mAllTriples.size() / COLUMNS);
    int height = rows * (stackHeight + mPadding) + mPadding;

    setMeasuredDimension(width, height);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (mAllTriples.isEmpty()) return;

    int availableWidth = getWidth() - (COLUMNS + 1) * mPadding;
    int cardWidth = availableWidth / COLUMNS;
    int cardHeight = (int) (cardWidth * CARD_ASPECT_RATIO);
    int overlap = cardHeight / STACK_OVERLAP_FACTOR;

    for (int i = 0; i < mAllTriples.size(); i++) {
      int column = i % COLUMNS;
      int row = i / COLUMNS;

      int x = mPadding + column * (cardWidth + mPadding);
      int y = mPadding + row * (cardHeight + overlap * 2 + mPadding);

      Set<Card> triple = mAllTriples.get(i);
      if (mFoundTriples.contains(triple)) {
        drawTripleStack(canvas, triple, x, y, cardWidth, cardHeight, overlap);
      } else {
        drawPlaceholderStack(canvas, x, y, cardWidth, cardHeight, overlap);
      }

      if (triple.equals(mHighlightedTriple)) {
        canvas.drawRoundRect(new RectF(x - 2, y - 2, x + cardWidth + 2, y + cardHeight + overlap * 2 + 2), 8, 8, mHighlightPaint);
      }
    }
  }

  private void drawTripleStack(Canvas canvas, Set<Card> triple, int x, int y, int w, int h, int overlap) {
    List<Card> cards = Lists.newArrayList(triple);
    // Sort to ensure consistent stack order
    java.util.Collections.sort(cards, Card.COMPARATOR);

    for (int i = 0; i < 3; i++) {
      Rect cardRect = new Rect(x, y + i * overlap, x + w, y + i * overlap + h);
      canvas.drawRoundRect(new RectF(cardRect), 8, 8, mBackgroundPaint);

      SymbolDrawable symbol = mSymbolDrawables.get(cards.get(i));
      // Reusing CardDrawable's logic for symbol bounds would be better, but we'll approximate for now
      // or we can use the actual method if we pass a scaled rect.
      List<Rect> symbolBounds = getSymbolBounds(cards.get(i), cardRect);
      for (Rect sb : symbolBounds) {
        symbol.setBounds(sb);
        symbol.draw(canvas);
      }
    }
  }

  private List<Rect> getSymbolBounds(Card card, Rect cardRect) {
    // This is a simplified version of CardDrawable.getBoundsForNumId
    List<Rect> rects = Lists.newArrayList();
    int width = cardRect.width();
    int height = cardRect.height();
    int halfSideLength = width / 10;
    int gap = halfSideLength / 2;

    int centerX = cardRect.left + width / 2;
    int centerY = cardRect.top + height / 2;

    switch (card.mNumber) {
      case 0:
        rects.add(squareFromCenterAndRadius(centerX, centerY, halfSideLength));
        break;
      case 1:
        rects.add(squareFromCenterAndRadius(centerX - gap / 2 - halfSideLength, centerY, halfSideLength));
        rects.add(squareFromCenterAndRadius(centerX + gap / 2 + halfSideLength, centerY, halfSideLength));
        break;
      case 2:
        rects.add(squareFromCenterAndRadius(centerX - gap - halfSideLength * 2, centerY, halfSideLength));
        rects.add(squareFromCenterAndRadius(centerX, centerY, halfSideLength));
        rects.add(squareFromCenterAndRadius(centerX + gap + halfSideLength * 2, centerY, halfSideLength));
        break;
    }
    return rects;
  }

  private static Rect squareFromCenterAndRadius(int centerX, int centerY, int radius) {
    return new Rect(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
  }

  private void drawPlaceholderStack(Canvas canvas, int x, int y, int w, int h, int overlap) {
    RectF rect = new RectF(x, y, x + w, y + h + overlap * 2);
    canvas.drawRoundRect(rect, 8, 8, mPlaceholderPaint);
  }
}
