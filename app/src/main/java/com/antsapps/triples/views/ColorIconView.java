package com.antsapps.triples.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

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
    float density = getResources().getDisplayMetrics().density;
    int symbolSize = (int) (8 * density);
    int left = (getWidth() - symbolSize) / 2;
    int top = (getHeight() - symbolSize) / 2;
    canvas.drawRect(left, top, left + symbolSize, top + symbolSize, mPaint);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
    int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
    setMeasuredDimension(width, height);
  }
}
