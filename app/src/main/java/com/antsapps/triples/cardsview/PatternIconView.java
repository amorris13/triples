package com.antsapps.triples.cardsview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class PatternIconView extends View {

  private String mPattern = "stripes";
  private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  public PatternIconView(Context context) {
    this(context, null);
  }

  public PatternIconView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setPattern(String pattern) {
    mPattern = pattern;
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    mPaint.setColor(Color.BLACK);
    float density = getResources().getDisplayMetrics().density;
    int size = getWidth();
    int margin = (int) (4 * density);
    Rect r = new Rect(margin, margin, size - margin, size - margin);

    if (mPattern.equals("stripes")) {
      float thickness = 2 * density;
      for (float i = r.left; i < r.right; i += 2 * thickness) {
        canvas.drawRect(i, r.top, i + thickness, r.bottom, mPaint);
      }
    } else if (mPattern.equals("dots")) {
      float radius = 2 * density;
      for (float x = r.left + radius; x < r.right; x += 4 * radius) {
        for (float y = r.top + radius; y < r.bottom; y += 4 * radius) {
          canvas.drawCircle(x, y, radius, mPaint);
        }
      }
    } else if (mPattern.equals("lighter")) {
      mPaint.setAlpha(128);
      canvas.drawRect(r, mPaint);
    } else if (mPattern.equals("crosshatch")) {
      mPaint.setStrokeWidth( density);
      for (int i = -size; i < size; i += (int) (4 * density)) {
        canvas.drawLine(r.left + i, r.top, r.left + i + size, r.bottom, mPaint);
        canvas.drawLine(r.left + i + size, r.top, r.left + i, r.bottom, mPaint);
      }
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int size = MeasureSpec.getSize(widthMeasureSpec);
    if (size == 0) size = 100;
    setMeasuredDimension(size, size);
  }
}
