package com.antsapps.triples.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.cardsview.CardView;

public class SingleScaledCardView extends View {

  private Card mCard;
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

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = (int) (width * CardView.HEIGHT_OVER_WIDTH);
    setMeasuredDimension(width, height);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (mCard == null || mCardView == null) {
      return;
    }

    int width = getWidth();
    int height = getHeight();
    mCardView.drawCardContent(canvas, new Rect(0, 0, width, height));
  }
}
