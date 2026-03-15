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
import com.antsapps.triples.cardsview.CardView;
import com.antsapps.triples.cardsview.CardsView;
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
  private CardsView mCardsView;
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
    getContext()
        .getTheme()
        .resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
    mOnSurfaceColor = typedValue.data;
  }

  public void setCardsView(CardsView cardsView) {
    mCardsView = cardsView;
    invalidate();
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
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int cardWidth = (mCardsView != null) ? mCardsView.cardWidth() : 0;
    if (cardWidth == 0) {
      cardWidth = (int) (120 * getResources().getDisplayMetrics().density);
    }
    int cardHeight = (int) (cardWidth * CardView.HEIGHT_OVER_WIDTH);

    int width = getWidth();
    int height = getHeight();
    if (width == 0 || height == 0) return;

    float scaleX = (float) width / cardWidth;
    float scaleY = (float) height / cardHeight;
    float scale = Math.min(scaleX, scaleY);

    canvas.save();
    canvas.translate((width - cardWidth * scale) / 2, (height - cardHeight * scale) / 2);
    canvas.scale(scale, scale);

    // Matching CardView symbol sizing: cardWidth / 5 diameter (radius = cardWidth / 10)
    int symbolSize = cardWidth / 5;
    int centerX = cardWidth / 2;
    int centerY = cardHeight / 2;
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
    canvas.restore();
  }

  private void drawNumber(Canvas canvas, int value, int centerX, int centerY, int symbolSize) {
    mPaint.setShader(null);
    mPaint.setColor(mOnSurfaceColor);
    mPaint.setStyle(Paint.Style.FILL);
    int halfSideLength = symbolSize / 2;
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

  private void drawShape(Canvas canvas, int centerX, int centerY, int symbolSize, float density) {
    mShapeDrawable.getPaint().setColor(mOnSurfaceColor);
    mShapeDrawable.getPaint().setStyle(Paint.Style.STROKE);
    mShapeDrawable.getPaint().setStrokeWidth(SymbolDrawable.OUTLINE_WIDTH * density / 2);
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
}
