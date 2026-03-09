package com.antsapps.triples.cardsview;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.CycleInterpolator;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Card;
import com.google.common.collect.Lists;
import java.util.List;

/**
 * A View representing a single Card.
 */
public class CardView extends View {

  private Card mCard;
  private CardBackgroundDrawable mCardBackground;
  private SymbolDrawable mSymbol;
  private boolean mSelected;
  private boolean mHinted;

  private static final int DEFAULT_ANIMATION_DURATION_MS = 800;

  public CardView(Context context) {
    this(context, null);
  }

  public CardView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CardView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    mCardBackground = new CardBackgroundDrawable(getContext());
    // Ripple effect
    int[] attrs = new int[]{android.R.attr.selectableItemBackground};
    android.content.res.TypedArray ta = getContext().obtainStyledAttributes(attrs);
    setForeground(ta.getDrawable(0));
    ta.recycle();
    setClickable(true);
    setFocusable(true);
  }

  public void setCard(Card card) {
    mCard = card;
    if (mCard != null) {
      mSymbol = new SymbolDrawable(getContext(), mCard);
    } else {
      mSymbol = null;
    }
    invalidate();
  }

  public Card getCard() {
    return mCard;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (mCard == null) return;

    Rect bounds = new Rect(0, 0, getWidth(), getHeight());
    mCardBackground.setBounds(bounds);
    mCardBackground.setSelected(mSelected);
    mCardBackground.setHinted(mHinted);
    mCardBackground.draw(canvas);

    if (mSymbol != null) {
      for (Rect rect : getBoundsForNumId(mCard.mNumber, bounds)) {
        mSymbol.setBounds(rect);
        mSymbol.draw(canvas);
      }
    }
    super.onDraw(canvas); // Draw foreground (ripple)
  }

  @Override
  public void setSelected(boolean selected) {
    if (mSelected != selected) {
      mSelected = selected;
      invalidate();
    }
  }

  public void setHinted(boolean hinted) {
    if (mHinted != hinted) {
      mHinted = hinted;
      invalidate();
      if (mHinted) {
        startThrobAnimation();
      } else {
        animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
      }
    }
  }

  private void startThrobAnimation() {
    animate()
        .scaleX(1.15f)
        .scaleY(1.15f)
        .setDuration(getAnimationDuration() / 2)
        .setInterpolator(new CycleInterpolator(0.5f))
        .start();
  }

  public void animateIncorrect() {
    animate()
        .rotation(5f)
        .setDuration(getAnimationDuration())
        .setInterpolator(new CycleInterpolator(4))
        .withEndAction(() -> setRotation(0))
        .start();
  }

  private int getAnimationDuration() {
    return PreferenceManager.getDefaultSharedPreferences(getContext())
        .getInt(getContext().getString(R.string.pref_animation_speed), DEFAULT_ANIMATION_DURATION_MS);
  }

  private static List<Rect> getBoundsForNumId(int id, Rect bounds) {
    List<Rect> rects = Lists.newArrayList();

    int width = bounds.width();
    int height = bounds.height();
    int halfSideLength = width / 10;
    int gap = halfSideLength / 2;
    switch (id) {
      case 0:
        rects.add(squareFromCenterAndRadius(width / 2, height / 2, halfSideLength));
        break;
      case 1:
        rects.add(
            squareFromCenterAndRadius(
                width / 2 - gap / 2 - halfSideLength, height / 2, halfSideLength));
        rects.add(
            squareFromCenterAndRadius(
                width / 2 + gap / 2 + halfSideLength, height / 2, halfSideLength));
        break;
      case 2:
        rects.add(
            squareFromCenterAndRadius(
                width / 2 - gap - halfSideLength * 2, height / 2, halfSideLength));
        rects.add(squareFromCenterAndRadius(width / 2, height / 2, halfSideLength));
        rects.add(
            squareFromCenterAndRadius(
                width / 2 + gap + halfSideLength * 2, height / 2, halfSideLength));
        break;
    }
    return rects;
  }

  private static Rect squareFromCenterAndRadius(int centerX, int centerY, int radius) {
    return new Rect(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
  }
}
