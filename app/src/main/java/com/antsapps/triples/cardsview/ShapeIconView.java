package com.antsapps.triples.cardsview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.view.View;

public class ShapeIconView extends View {

  private Shape mShape;
  private int mColor = android.graphics.Color.BLACK;
  private final ShapeDrawable mDrawable = new ShapeDrawable();

  public ShapeIconView(Context context) {
    this(context, null);
  }

  public ShapeIconView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setShape(String shapeName) {
    if (shapeName.equals("square")) mShape = new RectShape();
    else if (shapeName.equals("circle")) mShape = new OvalShape();
    else if (shapeName.equals("triangle")) mShape = new TriangleShape();
    else if (shapeName.equals("diamond")) mShape = new DiamondShape();
    else if (shapeName.equals("hexagon")) mShape = new HexagonShape();
    else if (shapeName.equals("star")) mShape = new StarShape();
    else if (shapeName.equals("heart")) mShape = new HeartShape();
    else mShape = new TriangleShape();
    mDrawable.setShape(mShape);
    invalidate();
  }

  public void setShape(Shape shape) {
    mShape = shape;
    mDrawable.setShape(mShape);
    invalidate();
  }

  public void setColor(int color) {
    mColor = color;
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (mShape != null) {
      mDrawable.getPaint().setColor(mColor);
      mDrawable.getPaint().setStyle(Paint.Style.STROKE);
      float density = getResources().getDisplayMetrics().density;
      mDrawable.getPaint().setStrokeWidth(SymbolDrawable.OUTLINE_WIDTH * density);

      int width = getWidth();
      int height = getHeight();
      int size = (int) (18 * density);

      int left = (width - size) / 2;
      int top = (height - size) / 2;
      int right = left + size;
      int bottom = top + size;

      mDrawable.setBounds(new Rect(left, top, right, bottom));
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
