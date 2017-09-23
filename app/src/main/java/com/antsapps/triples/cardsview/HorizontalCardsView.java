package com.antsapps.triples.cardsview;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;

import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;

import java.util.List;

public class HorizontalCardsView extends CardsView {

  private static final float WIDTH_OVER_HEIGHT = (float) ((Math.sqrt(5) + 1) / 2);

  public static final int ROWS = 3;

  private int mWidthOfCard;

  private int mHeightOfCard;

  public HorizontalCardsView(Context context) {
    this(context, null);
  }

  public HorizontalCardsView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void logValidTriple() {
    Log.v("ValidTriples", "valid positions:");
    List<Integer> validPositions = Game.getValidTriplePositions(mCards);
    for (int r = 0; r < ROWS; r++) {
      StringBuilder sb = new StringBuilder();
      for (int c = 0; c < mCards.size() / ROWS; c++) {
        sb.append(validPositions.contains(c * ROWS + r) ? "X" : ".");
        sb.append(" ");
      }
      Log.v("ValidTriples", sb.toString());
    }
  }

  @Override
  protected void updateMeasuredDimensions(final int widthMeasureSpec, final int heightMeasureSpec) {
    int heightOfCards = getDefaultSize(getMeasuredHeight(), heightMeasureSpec);
    int columns = mCards.size() / ROWS;
    int widthOfCards = (int) (heightOfCards / ROWS * WIDTH_OVER_HEIGHT * columns);

    setMeasuredDimension(getDefaultSize(widthOfCards, widthMeasureSpec), heightOfCards);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    if (!changed) {
      return;
    }
    Log.i("HCV", "oL: " + ", l = " + left + ", t = " + top + ", r = " + right + ", b = " + bottom);
    mHeightOfCard = (bottom - top) / ROWS;
    mWidthOfCard = (int) (mHeightOfCard * WIDTH_OVER_HEIGHT);
    mOffScreenLocation.set(right, bottom, right + mWidthOfCard, bottom + mHeightOfCard);
    Log.i("HCV", "oL: mHOC = " + mHeightOfCard + ", mWOC = " + mWidthOfCard);
    updateBounds();
  }

  @Override
  protected Rect calcBounds(int i) {
    return new Rect(
        i / ROWS * mWidthOfCard,
        i % ROWS * mHeightOfCard,
        (i / ROWS + 1) * mWidthOfCard,
        (i % ROWS + 1) * mHeightOfCard);
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
