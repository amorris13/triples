package com.antsapps.triples;

import android.content.Context;
import android.graphics.Canvas;
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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FoundTriplesView extends View {

  private static final float CARD_ASPECT_RATIO = (float) ((Math.sqrt(5) - 1) / 2);
  private static final int COLUMNS = 6;
  private static final int PADDING_DP = 8;
  private static final int STACK_OVERLAP_DP = 16;
  private static final int HIGHLIGHT_DURATION_MS = 1000;

  private final Paint mPlaceholderPaint;
  private final Paint mBackgroundPaint;
  private final Paint mHighlightPaint;
  private final int mPadding;
  private final int mOverlap;

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
    float density = getResources().getDisplayMetrics().density;
    mPadding = (int) (PADDING_DP * density);
    mOverlap = (int) (STACK_OVERLAP_DP * density);

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

  public Map<Card, Rect> getCardLocations(Set<Card> triple) {
    // Left-to-right logic: Found triples occupy the first available slots.
    // If it's already in mFoundTriples, use its index there.
    // If it's a NEW found triple, it will occupy index = mFoundTriples.size().
    int visualIndex = mFoundTriples.indexOf(triple);
    if (visualIndex == -1) {
      visualIndex = mFoundTriples.size();
    }
    if (visualIndex >= mAllTriples.size()) return Collections.emptyMap();

    int column = visualIndex % COLUMNS;
    int row = visualIndex / COLUMNS;

    int width = getWidth();
    int availableWidth = width - (COLUMNS + 1) * mPadding;
    int cardWidth = availableWidth / COLUMNS;
    int cardHeight = (int) (cardWidth * CARD_ASPECT_RATIO);
    int stackHeight = cardHeight + mOverlap * 2;

    int x = mPadding + column * (cardWidth + mPadding);
    int y = mPadding + row * (stackHeight + mPadding);

    int[] location = new int[2];
    getLocationInWindow(location);

    List<Card> cards = Lists.newArrayList(triple);
    Collections.sort(cards, Card.COMPARATOR);

    ImmutableMap.Builder<Card, Rect> builder = ImmutableMap.builder();
    for (int i = 0; i < 3; i++) {
      Rect rect = new Rect(x, y + i * mOverlap, x + cardWidth, y + i * mOverlap + cardHeight);
      rect.offset(location[0], location[1]);
      builder.put(cards.get(i), rect);
    }
    return builder.build();
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
    int stackHeight = cardHeight + mOverlap * 2;

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
    int stackHeight = cardHeight + mOverlap * 2;

    // First draw found triples
    for (int i = 0; i < mFoundTriples.size(); i++) {
      int column = i % COLUMNS;
      int row = i / COLUMNS;
      int x = mPadding + column * (cardWidth + mPadding);
      int y = mPadding + row * (stackHeight + mPadding);

      Set<Card> triple = mFoundTriples.get(i);
      drawTripleStack(canvas, triple, x, y, cardWidth, cardHeight);

      if (triple.equals(mHighlightedTriple)) {
        canvas.drawRoundRect(new RectF(x - 2, y - 2, x + cardWidth + 2, y + stackHeight + 2), 8, 8, mHighlightPaint);
      }
    }

    // Then draw placeholders for remaining triples
    for (int i = mFoundTriples.size(); i < mAllTriples.size(); i++) {
      int column = i % COLUMNS;
      int row = i / COLUMNS;
      int x = mPadding + column * (cardWidth + mPadding);
      int y = mPadding + row * (stackHeight + mPadding);

      drawPlaceholderStack(canvas, x, y, cardWidth, cardHeight);
    }
  }

  private void drawTripleStack(Canvas canvas, Set<Card> triple, int x, int y, int w, int h) {
    List<Card> cards = Lists.newArrayList(triple);
    Collections.sort(cards, Card.COMPARATOR);

    for (int i = 0; i < 3; i++) {
      Rect cardRect = new Rect(x, y + i * mOverlap, x + w, y + i * mOverlap + h);
      canvas.drawRoundRect(new RectF(cardRect), 8, 8, mBackgroundPaint);
      canvas.drawRoundRect(new RectF(cardRect), 8, 8, mPlaceholderPaint); // border

      SymbolDrawable symbol = mSymbolDrawables.get(cards.get(i));
      Rect relativeCardRect = new Rect(0, 0, w, h);
      List<Rect> symbolBounds = com.antsapps.triples.cardsview.CardDrawable.getBoundsForNumId(cards.get(i).mNumber, relativeCardRect);
      for (Rect sb : symbolBounds) {
        sb.offset(cardRect.left, cardRect.top);
        symbol.setBounds(sb);
        symbol.draw(canvas);
      }
    }
  }

  private void drawPlaceholderStack(Canvas canvas, int x, int y, int w, int h) {
    RectF rect = new RectF(x, y, x + w, y + h + mOverlap * 2);
    canvas.drawRoundRect(rect, 8, 8, mPlaceholderPaint);
  }
}
