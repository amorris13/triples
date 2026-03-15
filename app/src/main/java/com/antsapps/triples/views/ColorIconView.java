package com.antsapps.triples.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.antsapps.triples.cardsview.CardView;

public class ColorIconView extends View {
  private int mColor;
  private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  public ColorIconView(Context context) {
    this(context, null);
  }

  public ColorIconView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public void setColor(int color) {
    mColor = color;
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    mPaint.setColor(mColor);
    mPaint.setStyle(Paint.Style.FILL);
    int symbolSize = getWidth() / 5;
    int left = (getWidth() - symbolSize) / 2;
    int top = (getHeight() - symbolSize) / 2;
    canvas.drawRect(left, top, left + symbolSize, top + symbolSize, mPaint);
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
}
