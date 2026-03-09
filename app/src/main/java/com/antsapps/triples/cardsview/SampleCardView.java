package com.antsapps.triples.cardsview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import com.antsapps.triples.backend.Card;

public class SampleCardView extends CardView {

  public SampleCardView(Context context) {
    this(context, null);
  }

  public SampleCardView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = (int) (width * ((Math.sqrt(5) - 1) / 2));
    setMeasuredDimension(width, height);
  }
}
