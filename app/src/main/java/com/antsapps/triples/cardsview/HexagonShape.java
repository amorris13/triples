package com.antsapps.triples.cardsview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.shapes.RectShape;

/** Hexagon Shape */
public class HexagonShape extends RectShape {
  private Path mPath;

  public HexagonShape() {}

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
    mPath.lineTo(rect.width(), rect.height() / 4);
    mPath.lineTo(rect.width(), 3 * rect.height() / 4);
    mPath.lineTo(rect.centerX(), rect.height());
    mPath.lineTo(0, 3 * rect.height() / 4);
    mPath.lineTo(0, rect.height() / 4);
    mPath.close();
  }

  @Override
  public HexagonShape clone() throws CloneNotSupportedException {
    return (HexagonShape) super.clone();
  }
}
