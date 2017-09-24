package com.antsapps.triples.cardsview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.shapes.RectShape;

/** Created by anthonymorris on 24/9/17. */
public class TriangleShape extends RectShape {
  private Path mPath;

  public TriangleShape() {}

  @Override
  public void draw(Canvas canvas, Paint paint) {
    canvas.drawPath(mPath, paint);
  }

  @Override
  protected void onResize(float width, float height) {
    super.onResize(width, height);

    RectF rect = rect();
    mPath = new Path();
    mPath.moveTo(0, rect.height());
    mPath.lineTo(rect.centerX(), 0);
    mPath.lineTo(rect.width(), rect.height());
    mPath.close();
  }

  @Override
  public TriangleShape clone() throws CloneNotSupportedException {
    return (TriangleShape) super.clone();
  }
}
