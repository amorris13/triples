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
      int symbolSize = (int) (12 * density);
      int left = (getWidth() - symbolSize) / 2;
      int top = (getHeight() - symbolSize) / 2;
      mDrawable.setBounds(new Rect(left, top, left + symbolSize, top + symbolSize));
      mDrawable.draw(canvas);
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
    int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
    setMeasuredDimension(width, height);
  }
}
