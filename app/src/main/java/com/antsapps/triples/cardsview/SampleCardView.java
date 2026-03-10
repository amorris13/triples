package com.antsapps.triples.cardsview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import com.antsapps.triples.backend.Card;

public class SampleCardView extends View {

  private Card mCard;
  private CardView mCardView;

  public SampleCardView(Context context) {
    this(context, null);
  }

  public SampleCardView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setCard(Card card) {
    mCard = card;
    mCardView = new CardView(getContext(), mCard);
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (mCardView != null) {
      mCardView.drawCardContent(canvas, new Rect(0, 0, getWidth(), getHeight()));
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = width * 2 / 3;
    setMeasuredDimension(width, height);
  }
}
