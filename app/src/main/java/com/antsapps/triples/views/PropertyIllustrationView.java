package com.antsapps.triples.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.Nullable;
import com.antsapps.triples.CardCustomizationUtils;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.cardsview.CardDimensionsProvider;
import com.antsapps.triples.cardsview.SymbolDrawable;

public class PropertyIllustrationView extends View {

  private Card.PropertyType mPropertyType = Card.PropertyType.NUMBER;
  private int mValue;
  private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final ShapeDrawable mShapeDrawable = new ShapeDrawable();
  private CardDimensionsProvider mNaturalCardDimensionsProvider;
  private String mCachedPattern;
  private int mOnSurfaceColor;

  public PropertyIllustrationView(Context context) {
    this(context, null);
  }

  public PropertyIllustrationView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    mCachedPattern =
        getContext()
            .getSharedPreferences(
                getContext().getPackageName() + "_preferences", Context.MODE_PRIVATE)
            .getString("pref_shaded_pattern", "stripes");

    TypedValue typedValue = new TypedValue();
    if (getContext()
        .getTheme()
        .resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)) {
      mOnSurfaceColor = typedValue.data;
    } else {
      mOnSurfaceColor = 0xFF000000; // Fallback to black
    }
  }

  public void setNaturalCardDimensionsProvider(CardDimensionsProvider cardDimensionsProvider) {
    mNaturalCardDimensionsProvider = cardDimensionsProvider;
    invalidate();
  }

  public void setPropertyType(Card.PropertyType type) {
    mPropertyType = type;
    invalidate();
  }

  public void setPropertyValue(int value) {
    mValue = value;
    if (mPropertyType == Card.PropertyType.SHAPE) {
      mShapeDrawable.setShape(CardCustomizationUtils.getShapeForId(getContext(), mValue));
    }
    invalidate();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
      width = dpToPx(24);
    }
    setMeasuredDimension(width, width / 5);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int naturalWidth =
        mNaturalCardDimensionsProvider != null ? mNaturalCardDimensionsProvider.cardWidth() : 0;
    if (naturalWidth == 0) {
      naturalWidth = dpToPx(120);
    }

    int width = getWidth();
    int height = getHeight();
    if (width == 0 || height == 0) return;

    float scale = (float) width / naturalWidth;
    int naturalHeight = (int) (height / scale);

    canvas.save();
    canvas.scale(scale, scale);

    // Matching CardView symbol sizing: cardWidth / 5 diameter (radius = cardWidth / 10)
    int symbolSize = naturalWidth / 5;
    int centerX = naturalWidth / 2;
    int centerY = naturalHeight / 2;

    switch (mPropertyType) {
      case NUMBER:
        drawNumber(canvas, mValue, centerX, centerY, symbolSize);
        break;
      case SHAPE:
        drawShape(canvas, centerX, centerY, symbolSize);
        break;
      case PATTERN:
        drawPattern(canvas, mValue, centerX, centerY, symbolSize);
        break;
      case COLOR:
        drawColor(canvas, mValue, centerX, centerY, symbolSize);
        break;
    }
    canvas.restore();
  }

  private void drawNumber(Canvas canvas, int value, int centerX, int centerY, int symbolSize) {
    mPaint.setShader(null);
    mPaint.setColor(mOnSurfaceColor);
    mPaint.setStyle(Paint.Style.FILL);
    int halfSideLength = symbolSize / 4;
    int gap = halfSideLength / 2;

    switch (value) {
      case 0:
        canvas.drawCircle(centerX, centerY, halfSideLength, mPaint);
        break;
      case 1:
        canvas.drawCircle(centerX - gap / 2 - halfSideLength, centerY, halfSideLength, mPaint);
        canvas.drawCircle(centerX + gap / 2 + halfSideLength, centerY, halfSideLength, mPaint);
        break;
      case 2:
        canvas.drawCircle(centerX - gap - halfSideLength * 2, centerY, halfSideLength, mPaint);
        canvas.drawCircle(centerX, centerY, halfSideLength, mPaint);
        canvas.drawCircle(centerX + gap + halfSideLength * 2, centerY, halfSideLength, mPaint);
        break;
    }
  }

  private void drawShape(Canvas canvas, int centerX, int centerY, int symbolSize) {
    mShapeDrawable.getPaint().setColor(mOnSurfaceColor);
    mShapeDrawable.getPaint().setStyle(Paint.Style.STROKE);
    mShapeDrawable.getPaint().setStrokeWidth(dpToPx(SymbolDrawable.OUTLINE_WIDTH));
    int left = centerX - symbolSize / 2;
    int top = centerY - symbolSize / 2;
    mShapeDrawable.setBounds(new Rect(left, top, left + symbolSize, top + symbolSize));
    mShapeDrawable.draw(canvas);
  }

  private void drawPattern(Canvas canvas, int value, int centerX, int centerY, int symbolSize) {
    int left = centerX - symbolSize / 2;
    int top = centerY - symbolSize / 2;
    int right = left + symbolSize;
    int bottom = top + symbolSize;

    if (value == 0) {
      // Empty - do nothing as requested
    } else if (value == 1) {
      mPaint.setShader(
          CardCustomizationUtils.getCustomShadedShader(
              getContext(), mOnSurfaceColor, mCachedPattern));
      mPaint.setStyle(Paint.Style.FILL);
      canvas.drawRect(left, top, right, bottom, mPaint);
    } else {
      mPaint.setShader(null);
      mPaint.setColor(mOnSurfaceColor);
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

  private int dpToPx(float dp) {
    return (int)
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics());
  }
}
