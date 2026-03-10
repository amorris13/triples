package com.antsapps.triples.cardsview;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import java.util.List;

public class VerticalCardsView extends CardsView {

  public static final float HEIGHT_OVER_WIDTH = (float) ((Math.sqrt(5) - 1) / 2);

  public static final int COLUMNS = 3;

  private int mWidthOfCard;

  private int mHeightOfCard;

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
  protected void updateMeasuredDimensions(final int widthMeasureSpec, final int heightMeasureSpec) {
    int widthOfCards = getDefaultSize(getMeasuredWidth(), widthMeasureSpec);
    if (widthOfCards == 0) {
      if (getWidth() > 0) {
        widthOfCards = getWidth();
      } else {
        widthOfCards = getResources().getDisplayMetrics().widthPixels;
      }
    }
    mWidthOfCard = widthOfCards / COLUMNS;
    mHeightOfCard = (int) (mWidthOfCard * HEIGHT_OVER_WIDTH);

    int rows = (int) Math.ceil((double) mCards.size() / COLUMNS);
    int heightOfCards = mHeightOfCard * rows;
    if (mCards.isEmpty()) {
      heightOfCards = 0;
    }

    if (widthOfCards > 0 && heightOfCards > 0) {
      mOffScreenLocation.set(
          widthOfCards, heightOfCards, widthOfCards + mWidthOfCard, heightOfCards + mHeightOfCard);
    }

    setMeasuredDimension(widthOfCards, heightOfCards);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    mWidthOfCard = (right - left) / COLUMNS;
    mHeightOfCard = (int) (mWidthOfCard * HEIGHT_OVER_WIDTH);
    mOffScreenLocation.set(right, bottom, right + mWidthOfCard, bottom + mHeightOfCard);
    Log.i("VCV", "oL: mHOC = " + mHeightOfCard + ", mWOC = " + mWidthOfCard);

    for (int i = 0; i < mCards.size(); i++) {
      Card card = mCards.get(i);
      CardView child = mCardViews.get(card);
      if (child != null) {
        Rect bounds = calcBounds(i);
        child.layout(bounds.left, bounds.top, bounds.right, bounds.bottom);
      }
    }
  }

  @Override
  public Rect calcBounds(int i) {
    return new Rect(
        i % COLUMNS * mWidthOfCard,
        i / COLUMNS * mHeightOfCard,
        (i % COLUMNS + 1) * mWidthOfCard,
        (i / COLUMNS + 1) * mHeightOfCard);
  }

  @Override
  protected int cardWidth() {
      return mWidthOfCard;
  }

  @Override
  protected int cardHeight() {
      return mHeightOfCard;
  }
}
