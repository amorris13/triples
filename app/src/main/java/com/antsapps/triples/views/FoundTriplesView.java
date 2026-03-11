package com.antsapps.triples.views;

import static com.antsapps.triples.cardsview.CardBackgroundDrawable.INSET_DP;
import static com.antsapps.triples.cardsview.CardDrawable.DEFAULT_ANIMATION_DURATION_MS;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
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
import com.antsapps.triples.cardsview.CardsView;
import com.antsapps.triples.cardsview.VerticalCardsView;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class FoundTriplesView extends View {

  private static final float STACK_DISPLACEMENT_PERCENT = 0.65f;
  private static final int COLUMNS = 6;

  private List<Set<Card>> mFoundTriples = new ArrayList<>();
  private int mTotalTriples = 0;

  private CardsView mCardsView;

  private final Paint mPlaceholderPaint;
  private final Map<Card, CardDrawable> mCardDrawableCache = new HashMap<>();

  private int mCardWidth;
  private int mCardHeight;
  private int mStackDisplacement;
  private int mPadding;

  private int mHighlightIndex = -1;
  private float mHighlightScale = 1.0f;
  private ValueAnimator mHighlightAnimator;
  private int mSlotWidth;
  private int mSlotHeight;

  public FoundTriplesView(Context context) {
    this(context, null);
  }

  public FoundTriplesView(Context context, AttributeSet attrs) {
    super(context, attrs);
    float density = getResources().getDisplayMetrics().density;

    mPlaceholderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mPlaceholderPaint.setStyle(Paint.Style.STROKE);
    mPlaceholderPaint.setColor(getResources().getColor(R.color.colorOutlineVariant));
    mPlaceholderPaint.setStrokeWidth(3 * density);
    mPlaceholderPaint.setPathEffect(new DashPathEffect(new float[] {8 * density, 8 * density}, 0));
    mPadding = (int) (1 * density);
  }

  public void setFoundTriples(List<Set<Card>> foundTriples, int totalTriples) {
    mFoundTriples = foundTriples;
    mTotalTriples = totalTriples;
    requestLayout();
    invalidate();
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
    mCardHeight = (int) (mCardWidth * VerticalCardsView.HEIGHT_OVER_WIDTH);
    mStackDisplacement = (int) (mCardHeight * STACK_DISPLACEMENT_PERCENT);

    int rows = (int) Math.ceil((double) mTotalTriples / COLUMNS);
    int stackHeight = mCardHeight + 2 * mStackDisplacement;
    mSlotHeight = stackHeight + 2 * mPadding;
    int height = rows * mSlotHeight;

    setMeasuredDimension(width, height);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    Rect existingBounds = mCardsView.calcBounds(0);
    int naturalWidth = existingBounds.width();
    int naturalHeight = existingBounds.height();
    int naturalDisplacement = (int) (naturalHeight * STACK_DISPLACEMENT_PERCENT);
    float scale = (float) mCardWidth / naturalWidth;
    float centerX = naturalWidth / 2f;
    float centerY = (naturalHeight + 2 * naturalDisplacement) / 2f;

    for (int i = 0; i < mTotalTriples; i++) {
      int row = i / COLUMNS;
      int col = i % COLUMNS;

      float left = col * mSlotWidth + mPadding;
      float top = row * mSlotHeight + mPadding;

      canvas.save();
      canvas.translate(left, top);
      canvas.scale(scale, scale);
      if (i == mHighlightIndex) {
        canvas.scale(mHighlightScale, mHighlightScale, centerX, centerY);
      }

      if (mFoundTriples != null && i < mFoundTriples.size()) {
        drawTripleStack(
            canvas, mFoundTriples.get(i), naturalWidth, naturalHeight, naturalDisplacement);
      } else {
        drawPlaceholder(canvas, naturalWidth, naturalHeight, naturalDisplacement);
      }

      canvas.restore();
    }
  }

  private void drawTripleStack(
      Canvas canvas, Set<Card> triple, int width, int height, int displacement) {
    int i = 0;
    for (Card card : getSortedTriples(triple)) {
      Rect bounds = new Rect(0, i * displacement, width, i * displacement + height);
      CardDrawable cardDrawable = mCardDrawableCache.get(card);
      if (cardDrawable == null) {
        cardDrawable = new CardDrawable(getContext(), null, card);
        mCardDrawableCache.put(card, cardDrawable);
      }
      cardDrawable.updateBounds(bounds, false);
      cardDrawable.draw(canvas);
      i++;
    }
  }

  private void drawPlaceholder(Canvas canvas, int width, int height, int displacement) {
    float inset = INSET_DP * getContext().getResources().getDisplayMetrics().density;
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
        .getInt(
            getContext().getString(R.string.pref_animation_speed), DEFAULT_ANIMATION_DURATION_MS);
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
