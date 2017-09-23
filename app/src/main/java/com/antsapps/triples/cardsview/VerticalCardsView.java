package com.antsapps.triples.cardsview;

import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;

import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;

public class VerticalCardsView extends CardsView {

  private static final int HORIZONTAL = 0;
  private static final int VERTICAL = 1;

  private static final float HEIGHT_OVER_WIDTH = (float) ((Math.sqrt(5) - 1) / 2);

  public static final int COLUMNS = 3;

  private int mWidthOfCard;

  private int mHeightOfCard;

  private int mOrientation;

  public VerticalCardsView(Context context) {
    this(context, null);
  }

  public VerticalCardsView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void logValidTriple() {
    Log.v("ValidTriples", "valid positions:");
    List<Integer> validPositions = Game.getValidTriplePositions(mCards);
    for (int r = 0; r < mCards.size() / COLUMNS; r++) {
      StringBuilder sb = new StringBuilder();
      for (int c = 0; c < COLUMNS; c++) {
        sb.append(validPositions.contains(r * COLUMNS + c) ? "X" : ".");
        sb.append(" ");
      }
      Log.v("ValidTriples", sb.toString());
    }
  }

  @Override
  protected void updateMeasuredDimensions(final int widthMeasureSpec,
      final int heightMeasureSpec) {
    int widthOfCards = getDefaultSize(getMeasuredWidth(), widthMeasureSpec);
    int rows = mCards.size() / COLUMNS;
    int heightOfCards = (int) (widthOfCards / COLUMNS * HEIGHT_OVER_WIDTH * rows);
    setMeasuredDimension(
        widthOfCards,
        getDefaultSize(heightOfCards, heightMeasureSpec));
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right,
      int bottom) {
    if (!changed) {
      return;
    }
    Log.i("VCV", "oL: " + ", l = " + left + ", t = " + top + ", r = " + right
        + ", b = " + bottom);
    mWidthOfCard = (right - left) / COLUMNS;
    mHeightOfCard = (int) (mWidthOfCard * HEIGHT_OVER_WIDTH);
    mOffScreenLocation.set(right, bottom, right + mWidthOfCard, bottom + mHeightOfCard);
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
