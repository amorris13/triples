package com.antsapps.triples.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.antsapps.triples.CardCustomizationUtils;
import com.antsapps.triples.cardsview.SymbolDrawable;

public class PropertyIllustrationView extends View {

  public enum PropertyType {
    NUMBER,
    SHAPE,
    PATTERN,
    COLOR
  }

  private PropertyType mPropertyType = PropertyType.NUMBER;
  private int mValue;
  private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final ShapeDrawable mShapeDrawable = new ShapeDrawable();
  private String mCachedPattern;

  public PropertyIllustrationView(Context context) {
    this(context, null);
  }

  public PropertyIllustrationView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    mCachedPattern =
        getContext()
            .getSharedPreferences(getContext().getPackageName() + "_preferences", Context.MODE_PRIVATE)
            .getString("pref_shaded_pattern", "stripes");
  }

  public void setProperty(PropertyType type, int value) {
    mPropertyType = type;
    mValue = value;
    if (mPropertyType == PropertyType.SHAPE) {
      mShapeDrawable.setShape(CardCustomizationUtils.getShapeForId(getContext(), mValue));
    }
    invalidate();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    // Use a ratio that makes it shorter than a card.
    // Card ratio is 0.618. Let's use 0.3 for property illustrations.
    int height = (int) (width * 0.3f);
    setMeasuredDimension(width, height);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int width = getWidth();
    int height = getHeight();
    if (width == 0 || height == 0) return;

    // Matching CardView symbol sizing: width / 5
    int symbolSize = width / 5;
    int centerX = width / 2;
    int centerY = height / 2;
    float density = getResources().getDisplayMetrics().density;

    switch (mPropertyType) {
      case NUMBER:
        drawNumber(canvas, mValue, centerX, centerY, symbolSize);
        break;
      case SHAPE:
        drawShape(canvas, centerX, centerY, symbolSize, density);
        break;
      case PATTERN:
        drawPattern(canvas, mValue, centerX, centerY, symbolSize, density);
        break;
      case COLOR:
        drawColor(canvas, mValue, centerX, centerY, symbolSize);
        break;
    }
  }

  private void drawNumber(Canvas canvas, int value, int centerX, int centerY, int symbolSize) {
    mPaint.setShader(null);
    mPaint.setColor(Color.DKGRAY);
    mPaint.setStyle(Paint.Style.FILL);
    int radius = symbolSize / 2;
    int gap = symbolSize / 4;

    switch (value) {
      case 0:
        canvas.drawCircle(centerX, centerY, radius, mPaint);
        break;
      case 1:
        canvas.drawCircle(centerX - gap / 2 - radius, centerY, radius, mPaint);
        canvas.drawCircle(centerX + gap / 2 + radius, centerY, radius, mPaint);
        break;
      case 2:
        canvas.drawCircle(centerX - gap - radius * 2, centerY, radius, mPaint);
        canvas.drawCircle(centerX, centerY, radius, mPaint);
        canvas.drawCircle(centerX + gap + radius * 2, centerY, radius, mPaint);
        break;
    }
  }

  private void drawShape(Canvas canvas, int centerX, int centerY, int symbolSize, float density) {
    mShapeDrawable.getPaint().setColor(Color.DKGRAY);
    mShapeDrawable.getPaint().setStyle(Paint.Style.STROKE);
    mShapeDrawable.getPaint().setStrokeWidth(SymbolDrawable.OUTLINE_WIDTH * density);
    int left = centerX - symbolSize / 2;
    int top = centerY - symbolSize / 2;
    mShapeDrawable.setBounds(new Rect(left, top, left + symbolSize, top + symbolSize));
    mShapeDrawable.draw(canvas);
  }

  private void drawPattern(
      Canvas canvas, int value, int centerX, int centerY, int symbolSize, float density) {
    int left = centerX - symbolSize / 2;
    int top = centerY - symbolSize / 2;
    int right = left + symbolSize;
    int bottom = top + symbolSize;

    if (value == 0) {
      mPaint.setShader(null);
      mPaint.setColor(Color.DKGRAY);
      mPaint.setStyle(Paint.Style.STROKE);
      mPaint.setStrokeWidth(SymbolDrawable.OUTLINE_WIDTH * density);
      canvas.drawRect(left, top, right, bottom, mPaint);
    } else if (value == 1) {
      String pattern =
          getContext()
              .getSharedPreferences(
                  getContext().getPackageName() + "_preferences", Context.MODE_PRIVATE)
              .getString("pref_shaded_pattern", "stripes");
      mPaint.setShader(
          CardCustomizationUtils.getCustomShadedShader(getContext(), Color.DKGRAY, pattern));
      mPaint.setStyle(Paint.Style.FILL);
      canvas.drawRect(left, top, right, bottom, mPaint);
    } else {
      mPaint.setShader(null);
      mPaint.setColor(Color.DKGRAY);
      mPaint.setStyle(Paint.Style.FILL);
      canvas.drawRect(left, top, right, bottom, mPaint);
    }
  }

  private void drawColor(Canvas canvas, int value, int centerX, int centerY, int symbolSize) {
    mPaint.setShader(null);
    mPaint.setColor(CardCustomizationUtils.getColorForId(getContext(), value));
    mPaint.setStyle(Paint.Style.FILL);
    int left = centerX - symbolSize / 2;
    int top = centerY - symbolSize / 2;
    canvas.drawRect(left, top, left + symbolSize, top + symbolSize, mPaint);
  }
}
