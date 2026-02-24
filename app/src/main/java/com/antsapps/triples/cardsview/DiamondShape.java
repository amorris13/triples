package com.antsapps.triples.cardsview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.shapes.RectShape;

/** Diamond Shape */
public class DiamondShape extends RectShape {
  private Path mPath;

  public DiamondShape() {}

  @Override
  public void draw(Canvas canvas, Paint paint) {
    canvas.drawPath(mPath, paint);
  }

  @Override
  protected void onResize(float width, float height) {
    super.onResize(width, height);

    RectF rect = rect();
    mPath = new Path();
    mPath.moveTo(rect.centerX(), 0);
    mPath.lineTo(rect.width(), rect.centerY());
    mPath.lineTo(rect.centerX(), rect.height());
    mPath.lineTo(0, rect.centerY());
    mPath.close();
  }

  @Override
  public DiamondShape clone() throws CloneNotSupportedException {
    return (DiamondShape) super.clone();
  }
}
