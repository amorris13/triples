package com.antsapps.triples.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.antsapps.triples.cardsview.CardView;
import com.antsapps.triples.cardsview.SymbolDrawable;

public class ShapeIconView extends View {
  private Shape mShape;
  private final ShapeDrawable mDrawable = new ShapeDrawable();

  public ShapeIconView(Context context) {
    this(context, null);
  }

  public ShapeIconView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public void setShape(Shape shape) {
    mShape = shape;
    mDrawable.setShape(mShape);
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (mShape != null) {
      mDrawable.getPaint().setColor(Color.DKGRAY);
      mDrawable.getPaint().setStyle(Paint.Style.STROKE);
      float density = getResources().getDisplayMetrics().density;
      mDrawable.getPaint().setStrokeWidth(SymbolDrawable.OUTLINE_WIDTH * density);
      int symbolSize = getWidth() / 5;
      int left = (getWidth() - symbolSize) / 2;
      int top = (getHeight() - symbolSize) / 2;
      mDrawable.setBounds(new Rect(left, top, left + symbolSize, top + symbolSize));
      mDrawable.draw(canvas);
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
