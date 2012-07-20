package com.antsapps.triples;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;

import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;

public class HorizontalCardsView extends GameCardsView {

  private static final float WIDTH_OVER_HEIGHT = (float) ((Math.sqrt(5) + 1) / 2);

  public static final int ROWS = 3;

  private static final int MIN_COLUMNS = Game.MIN_CARDS_IN_PLAY / ROWS;

  private int mWidthOfCard;

  private int mHeightOfCard;

  public HorizontalCardsView(Context context) {
    this(context, null);
  }

  public HorizontalCardsView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void updateMeasuredDimensions(final int widthMeasureSpec,
      final int heightMeasureSpec) {
    int heightOfCards = getDefaultSize(getMeasuredHeight(), heightMeasureSpec);
    int columns = Math.max(mCards.size() / ROWS, MIN_COLUMNS);
    int widthOfCards = (int) (heightOfCards / ROWS * WIDTH_OVER_HEIGHT * columns);

    setMeasuredDimension(
        getDefaultSize(widthOfCards, widthMeasureSpec),
        heightOfCards);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right,
      int bottom) {
    if (!changed) {
      return;
    }
    Log.i("HCV", "oL: " + ", l = " + left + ", t = " + top + ", r = " + right
        + ", b = " + bottom);
    mHeightOfCard = (bottom - top) / ROWS;
    mWidthOfCard = (int) (mHeightOfCard * WIDTH_OVER_HEIGHT);
    mOffScreenLocation.set(right, bottom, right + mWidthOfCard, bottom + mHeightOfCard);
    Log.i("HCV", "oL: mHOC = " + mHeightOfCard + ", mWOC = " + mWidthOfCard);
    updateBounds();
  }

  @Override
  protected Rect calcBounds(int i) {
    return new Rect(i / ROWS * mWidthOfCard, i % ROWS * mHeightOfCard, (i
        / ROWS + 1)
        * mWidthOfCard, (i % ROWS + 1) * mHeightOfCard);
  }

  @Override
  protected Card getCardForPosition(int x, int y) {
    int position = x / mWidthOfCard * ROWS + y / mHeightOfCard;
    if (position < mCards.size()) {
      return mCards.get(position);
    } else {
      return null;
    }
  }
}
