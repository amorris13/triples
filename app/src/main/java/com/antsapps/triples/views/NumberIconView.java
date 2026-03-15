package com.antsapps.triples.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.antsapps.triples.cardsview.CardView;

public class NumberIconView extends View {
  private int mNumber;
  private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  public NumberIconView(Context context) {
    this(context, null);
  }

  public NumberIconView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public void setNumber(int number) {
    mNumber = number;
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    mPaint.setColor(Color.DKGRAY);
    mPaint.setStyle(Paint.Style.FILL);

    int symbolSize = getWidth() / 5;
    int gap = symbolSize / 4;

    int centerX = getWidth() / 2;
    int centerY = getHeight() / 2;
    int radius = symbolSize / 2;

    switch (mNumber) {
      case 0:
        drawCircle(canvas, centerX, centerY, radius);
        break;
      case 1:
        drawCircle(canvas, centerX - gap / 2 - radius, centerY, radius);
        drawCircle(canvas, centerX + gap / 2 + radius, centerY, radius);
        break;
      case 2:
        drawCircle(canvas, centerX - gap - radius * 2, centerY, radius);
        drawCircle(canvas, centerX, centerY, radius);
        drawCircle(canvas, centerX + gap + radius * 2, centerY, radius);
        break;
    }
  }

  private void drawCircle(Canvas canvas, int centerX, int centerY, int radius) {
    canvas.drawCircle(centerX, centerY, radius, mPaint);
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
