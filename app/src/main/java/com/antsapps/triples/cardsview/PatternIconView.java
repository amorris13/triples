package com.antsapps.triples.cardsview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import com.antsapps.triples.CardCustomizationUtils;

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
    mPaint.setShader(CardCustomizationUtils.getCustomShadedShader(getContext(), Color.BLACK, mPattern));
    mPaint.setStyle(Paint.Style.FILL);
    float density = getResources().getDisplayMetrics().density;
    int size = getWidth();
    int margin = (int) (4 * density);
    canvas.drawRect(margin, margin, size - margin, size - margin, mPaint);

    // Draw outline for better visibility of the pattern area
    mPaint.setShader(null);
    mPaint.setColor(Color.BLACK);
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeWidth(density);
    canvas.drawRect(margin, margin, size - margin, size - margin, mPaint);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int size = MeasureSpec.getSize(widthMeasureSpec);
    if (size == 0) size = 100;
    setMeasuredDimension(size, size);
  }
}
