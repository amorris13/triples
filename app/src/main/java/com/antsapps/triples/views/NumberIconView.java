package com.antsapps.triples.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.antsapps.triples.CardCustomizationUtils;
import com.antsapps.triples.R;

public class NumberIconView extends View {
  private int mNumber;
  private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  public NumberIconView(@NonNull Context context) {
    this(context, null);
  }

  public NumberIconView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public void setNumber(int number) {
    mNumber = number;
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    mPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_text_primary));
    mPaint.setStyle(Paint.Style.FILL);
    float density = getResources().getDisplayMetrics().density;
    int width = getWidth();
    int height = getHeight();
    int margin = (int) (CardCustomizationUtils.ICON_MARGIN_DP * density);

    int radius = (int) (6 * density);
    int centerX = width / 2;
    int centerY = height / 2;
    int gap = (int) (18 * density);

    switch (mNumber) {
      case 0:
        canvas.drawCircle(centerX, centerY, radius, mPaint);
        break;
      case 1:
        canvas.drawCircle(centerX - gap / 2, centerY, radius, mPaint);
        canvas.drawCircle(centerX + gap / 2, centerY, radius, mPaint);
        break;
      case 2:
        canvas.drawCircle(centerX - gap, centerY, radius, mPaint);
        canvas.drawCircle(centerX, centerY, radius, mPaint);
        canvas.drawCircle(centerX + gap, centerY, radius, mPaint);
        break;
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    if (width == 0) width = 100;
    setMeasuredDimension(width, width);
  }
}
