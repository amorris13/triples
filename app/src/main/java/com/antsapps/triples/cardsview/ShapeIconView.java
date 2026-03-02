package com.antsapps.triples.cardsview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.view.View;

import com.antsapps.triples.R;

public class ShapeIconView extends View {

  private Shape mShape;
  private int mColor = Color.BLACK;
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
      mDrawable.getPaint().setStrokeWidth(2 * density);
      mDrawable.setBounds(new Rect(8, 8, getWidth() - 8, getHeight() - 8));
      mDrawable.draw(canvas);
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int size = MeasureSpec.getSize(widthMeasureSpec);
    if (size == 0) size = 100;
    setMeasuredDimension(size, size);
  }
}
