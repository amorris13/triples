package com.antsapps.triples.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.antsapps.triples.CardCustomizationUtils;

public class ColorIconView extends View {
  private int mColor;
  private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  public ColorIconView(@NonNull Context context) {
    this(context, null);
  }

  public ColorIconView(@NonNull Context context, @Nullable AttributeSet attrs) {
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
    int margin = (int) (CardCustomizationUtils.ICON_MARGIN_DP * density);
    canvas.drawRect(margin, margin, getWidth() - margin, getHeight() - margin, mPaint);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    if (width == 0) width = 100;
    setMeasuredDimension(width, width);
  }
}
