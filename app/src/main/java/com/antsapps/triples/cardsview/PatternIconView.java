package com.antsapps.triples.cardsview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
    if (mPattern.equals("none")) {
      mPaint.setShader(null);
      mPaint.setColor(Color.TRANSPARENT);
    } else if (mPattern.equals("solid")) {
      mPaint.setShader(null);
      mPaint.setColor(Color.BLACK);
    } else {
      mPaint.setShader(
          CardCustomizationUtils.getCustomShadedShader(getContext(), Color.BLACK, mPattern));
    }
    mPaint.setStyle(Paint.Style.FILL);
    float density = getResources().getDisplayMetrics().density;
    int width = getWidth();
    int height = getHeight();
    int margin = (int) (CardCustomizationUtils.ICON_MARGIN_DP * density);
    canvas.drawRect(margin, margin, width - margin, height - margin, mPaint);

    // Draw outline for better visibility of the pattern area
    mPaint.setShader(null);
    mPaint.setColor(Color.BLACK);
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeWidth(SymbolDrawable.OUTLINE_WIDTH * density);
    canvas.drawRect(margin, margin, width - margin, height - margin, mPaint);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);

    int width;
    int height;

    if (widthMode == MeasureSpec.EXACTLY) {
      width = widthSize;
    } else {
      width = 100;
    }

    if (heightMode == MeasureSpec.EXACTLY) {
      height = heightSize;
    } else {
      height = width;
    }

    setMeasuredDimension(width, height);
  }
}
