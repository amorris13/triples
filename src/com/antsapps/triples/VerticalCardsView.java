package com.antsapps.triples;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;

import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;

public class VerticalCardsView extends CardsView {

  private static final float HEIGHT_OVER_WIDTH = (float) ((Math.sqrt(5) - 1) / 2);

  public static final int COLUMNS = 3;

  private static final int MIN_ROWS = Game.MIN_CARDS_IN_PLAY / COLUMNS;

  private int mWidthOfCard;

  private int mHeightOfCard;

  int mRows;

  public VerticalCardsView(Context context) {
    this(context, null);
  }

  public VerticalCardsView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void updateMeasuredDimensions(final int widthMeasureSpec,
      final int heightMeasureSpec) {
    Log.i("VCV", "uMD: gMW = " + getMeasuredWidth() + ", gMH = "
        + getMeasuredHeight());
    int widthOfCards = getDefaultSize(getMeasuredWidth(), widthMeasureSpec);
    int rows = Math.max(mRows, MIN_ROWS);
    int heightOfCards = (int) (widthOfCards / COLUMNS * HEIGHT_OVER_WIDTH * rows);
    setMeasuredDimension(
        widthOfCards,
        getDefaultSize(heightOfCards, heightMeasureSpec));
    Log.i("VCV", "uMD: gMW = " + getMeasuredWidth() + ", gMH = "
        + getMeasuredHeight() + " woc = " + widthOfCards + " hoc = " + heightOfCards);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right,
      int bottom) {
    Log.i("VCV", "oL: " + ", l = " + left + ", t = " + top + ", r = " + right
        + ", b = " + bottom);
    mWidthOfCard = (right - left) / COLUMNS;
    mHeightOfCard = (int) (mWidthOfCard * HEIGHT_OVER_WIDTH);
    Log.i("VCV", "oL: mHOC = " + mHeightOfCard + ", mWOC = " + mWidthOfCard);
    updateBounds();
  }

  @Override
  protected Rect calcBounds(int i) {
    return new Rect(i % COLUMNS * mWidthOfCard, i / COLUMNS * mHeightOfCard, (i
        % COLUMNS + 1)
        * mWidthOfCard, (i / COLUMNS + 1) * mHeightOfCard);
  }

  @Override
  protected Card getCardForPosition(int x, int y) {
    int position = y / mHeightOfCard * COLUMNS + x / mWidthOfCard;
    if (position < mCards.size()) {
      return mCards.get(position);
    } else {
      return null;
    }
  }
}
