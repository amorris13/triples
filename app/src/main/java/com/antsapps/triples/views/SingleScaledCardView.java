package com.antsapps.triples.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.cardsview.CardView;
import com.antsapps.triples.cardsview.CardsView;

public class SingleScaledCardView extends View {

  private Card mCard;
  private CardsView mCardsView;
  private CardView mCardView;

  public SingleScaledCardView(Context context) {
    this(context, null);
  }

  public SingleScaledCardView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public void setCard(Card card) {
    mCard = card;
    if (mCard != null) {
      mCardView = new CardView(getContext(), mCard);
    } else {
      mCardView = null;
    }
    invalidate();
  }

  public void setCardsView(CardsView cardsView) {
    mCardsView = cardsView;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);

    int width;
    int height;

    if (heightMode == MeasureSpec.EXACTLY) {
      height = heightSize;
      width = (int) (height / CardView.HEIGHT_OVER_WIDTH);
    } else if (widthMode == MeasureSpec.EXACTLY) {
      width = widthSize;
      height = (int) (width * CardView.HEIGHT_OVER_WIDTH);
    } else {
      // Default to 24dp height
      height = (int) (24 * getResources().getDisplayMetrics().density);
      width = (int) (height / CardView.HEIGHT_OVER_WIDTH);
    }

    setMeasuredDimension(width, height);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (mCard == null || mCardsView == null || mCardView == null) {
      return;
    }

    int height = getHeight();
    int width = (int) (height / CardView.HEIGHT_OVER_WIDTH);
    int left = (getWidth() - width) / 2;

    mCardView.drawCardContent(canvas, new Rect(left, 0, left + width, height));
  }
}
