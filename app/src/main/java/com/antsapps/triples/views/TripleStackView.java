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
import androidx.annotation.Nullable;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.cardsview.CardView;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TripleStackView extends View {

  private static final float STACK_DISPLACEMENT_PERCENT = 0.65f;

  private Set<Card> mTriple;
  private boolean mIsPlaceholder = true;
  private final Paint mPlaceholderPaint;
  private final Map<Card, CardView> mCardViewCache = new HashMap<>();
  private int mNaturalCardWidth;
  private int mNaturalCardHeight;

  public TripleStackView(Context context) {
    this(context, null);
  }

  public TripleStackView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    float density = getResources().getDisplayMetrics().density;
    mPlaceholderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mPlaceholderPaint.setStyle(Paint.Style.STROKE);
    mPlaceholderPaint.setColor(getResources().getColor(R.color.colorOutlineVariant));
    mPlaceholderPaint.setStrokeWidth(3 * density);
    mPlaceholderPaint.setPathEffect(new DashPathEffect(new float[] {8 * density, 8 * density}, 0));

    TypedValue outValue = new TypedValue();
    getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
    setBackgroundResource(outValue.resourceId);
  }

  public void setTriple(Set<Card> triple, boolean isPlaceholder, int naturalWidth, int naturalHeight) {
    mTriple = triple;
    mIsPlaceholder = isPlaceholder;
    mNaturalCardWidth = naturalWidth;
    mNaturalCardHeight = naturalHeight;
    setClickable(isPlaceholder);
    invalidate();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = (int) (width * CardView.HEIGHT_OVER_WIDTH + 2 * (width * CardView.HEIGHT_OVER_WIDTH * STACK_DISPLACEMENT_PERCENT));
    setMeasuredDimension(width, height);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int width = getWidth();
    int height = getHeight();

    if (mIsPlaceholder) {
      float inset = INSET_DP * getResources().getDisplayMetrics().density;
      RectF rect = new RectF(inset, inset, width - inset, height - inset);
      canvas.drawRoundRect(rect, 10, 10, mPlaceholderPaint);
    } else if (mTriple != null) {
      float scale = (float) width / mNaturalCardWidth;
      int naturalDisplacement = (int) (mNaturalCardHeight * STACK_DISPLACEMENT_PERCENT);

      canvas.save();
      canvas.scale(scale, scale);
      int i = 0;
      Rect bounds = new Rect(0, 0, mNaturalCardWidth, mNaturalCardHeight);
      for (Card card : getSortedTriples(mTriple)) {
        CardView cardView = mCardViewCache.get(card);
        if (cardView == null) {
          cardView = new CardView(getContext(), card);
          mCardViewCache.put(card, cardView);
        }
        canvas.save();
        canvas.translate(0, i * naturalDisplacement);
        cardView.drawCardContent(canvas, bounds);
        canvas.restore();
        i++;
      }
      canvas.restore();
    }
  }

  private static List<Card> getSortedTriples(Set<Card> triple) {
    List<Card> sortedTriples = Lists.newArrayList(triple);
    Collections.sort(sortedTriples);
    return sortedTriples;
  }
}
