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
import com.antsapps.triples.cardsview.CardDimensionsProvider;
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

  private final Paint mPlaceholderPaint;
  private final Paint mHighlightPaint;
  private final Map<Card, CardView> mCardViewCache = new HashMap<>();

  private CardDimensionsProvider mNaturalCardDimensionsProvider;

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
    mPlaceholderPaint.setStrokeWidth(1.5f * density);
    mPlaceholderPaint.setPathEffect(new DashPathEffect(new float[] {8 * density, 8 * density}, 0));

    mHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mHighlightPaint.setStyle(Paint.Style.STROKE);
    mHighlightPaint.setStrokeWidth(2 * density);
    TypedValue typedValue = new TypedValue();
    context.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
    mHighlightPaint.setColor(typedValue.data);

    mPadding = (int) (1 * density);

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      TypedValue outValue = new TypedValue();
      context.getTheme().resolveAttribute(android.R.attr.colorControlHighlight, outValue, true);
      android.graphics.drawable.RippleDrawable ripple =
          new android.graphics.drawable.RippleDrawable(
              android.content.res.ColorStateList.valueOf(outValue.data), null, null);
      setForeground(ripple);
      setOutlineProvider(
          new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(View view, android.graphics.Outline outline) {
              if (mCardWidth <= 0 || mCardHeight <= 0) return;
              float d = view.getResources().getDisplayMetrics().density;
              float cornerRadius = 4 * d * getScaleFactor();
              int totalHeight = mCardHeight + 2 * mStackDisplacement;
              outline.setRoundRect(
                  mPadding, mPadding, mPadding + mCardWidth, mPadding + totalHeight, cornerRadius);
            }
          });
      setClipToOutline(true);
    } else {
      TypedValue rippleValue = new TypedValue();
      context
          .getTheme()
          .resolveAttribute(android.R.attr.selectableItemBackground, rippleValue, true);
      setForeground(context.getDrawable(rippleValue.resourceId));
    }
    setClickable(true);
    setFocusable(true);
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

  public void setNaturalCardDimensionsProvider(CardDimensionsProvider provider) {
    mNaturalCardDimensionsProvider = provider;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      invalidateOutline();
    }
    invalidate();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    if (width <= 0) {
      setMeasuredDimension(0, 0);
      return;
    }
    updateDimensions(width);
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      invalidateOutline();
    }
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
    // Draw at the natural grid card size then scale down so that fixed-dp elements in
    // CardBackgroundDrawable (insets, corner radii, stroke widths) shrink proportionally,
    // matching the appearance of cards in the main grid.
    int refWidth =
        mNaturalCardDimensionsProvider != null
            ? mNaturalCardDimensionsProvider.cardWidth()
            : mCardWidth;
    int refHeight =
        mNaturalCardDimensionsProvider != null
            ? mNaturalCardDimensionsProvider.cardHeight()
            : mCardHeight;
    float scale = (float) mCardWidth / refWidth;

    List<Card> sorted = getSortedTriple(mTriple);
    Rect bounds = new Rect(0, 0, refWidth, refHeight);
    for (int i = 0; i < sorted.size(); i++) {
      Card card = sorted.get(i);
      CardView cardView = mCardViewCache.get(card);
      if (cardView == null) {
        cardView = new CardView(getContext(), card);
        mCardViewCache.put(card, cardView);
      }
      canvas.save();
      canvas.translate(mPadding, mPadding + i * mStackDisplacement);
      canvas.scale(scale, scale);
      cardView.drawCardContent(canvas, bounds);
      canvas.restore();
    }
  }

  private float getScaleFactor() {
    if (mNaturalCardDimensionsProvider != null
        && mNaturalCardDimensionsProvider.cardWidth() > 0
        && mCardWidth > 0) {
      return (float) mCardWidth / mNaturalCardDimensionsProvider.cardWidth();
    }
    return 1f;
  }

  private void drawHighlightBorder(Canvas canvas) {
    float density = getContext().getResources().getDisplayMetrics().density;
    float inset = INSET_DP * density * getScaleFactor();
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
    float density = getContext().getResources().getDisplayMetrics().density;
    float inset = INSET_DP * density * getScaleFactor();
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

  public static List<Card> getSortedTriple(Set<Card> triple) {
    List<Card> sorted = new ArrayList<>(triple);
    Collections.sort(sorted);
    return sorted;
  }
}
