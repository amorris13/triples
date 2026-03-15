package com.antsapps.triples.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.antsapps.triples.CardCustomizationUtils;
import com.antsapps.triples.cardsview.CardView;
import com.antsapps.triples.cardsview.SymbolDrawable;

public class PatternIconView extends View {
  private int mPatternId;
  private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  public PatternIconView(Context context) {
    this(context, null);
  }

  public PatternIconView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public void setPattern(int patternId) {
    mPatternId = patternId;
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    float density = getResources().getDisplayMetrics().density;
    int symbolSize = getWidth() / 5;
    int left = (getWidth() - symbolSize) / 2;
    int top = (getHeight() - symbolSize) / 2;
    int right = left + symbolSize;
    int bottom = top + symbolSize;

    String pattern = "stripes";
    if (mPatternId == 1) {
      pattern =
          getContext()
              .getSharedPreferences(
                  getContext().getPackageName() + "_preferences", Context.MODE_PRIVATE)
              .getString("pref_shaded_pattern", "stripes");
    }

    if (mPatternId == 0) {
      // Empty: just outline
      mPaint.setShader(null);
      mPaint.setColor(Color.DKGRAY);
      mPaint.setStyle(Paint.Style.STROKE);
      mPaint.setStrokeWidth(SymbolDrawable.OUTLINE_WIDTH * density);
      canvas.drawRect(left, top, right, bottom, mPaint);
    } else if (mPatternId == 1) {
      // Shaded
      mPaint.setShader(
          CardCustomizationUtils.getCustomShadedShader(getContext(), Color.DKGRAY, pattern));
      mPaint.setStyle(Paint.Style.FILL);
      canvas.drawRect(left, top, right, bottom, mPaint);
    } else {
      // Solid
      mPaint.setShader(null);
      mPaint.setColor(Color.DKGRAY);
      mPaint.setStyle(Paint.Style.FILL);
      canvas.drawRect(left, top, right, bottom, mPaint);
    }
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
