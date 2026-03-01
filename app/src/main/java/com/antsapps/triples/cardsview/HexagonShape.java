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
    mPath.moveTo(rect.centerX(), rect.top);
    mPath.lineTo(rect.right, rect.top + rect.height() / 4);
    mPath.lineTo(rect.right, rect.top + 3 * rect.height() / 4);
    mPath.lineTo(rect.centerX(), rect.bottom);
    mPath.lineTo(rect.left, rect.top + 3 * rect.height() / 4);
    mPath.lineTo(rect.left, rect.top + rect.height() / 4);
    mPath.close();
  }

  @Override
  public HexagonShape clone() throws CloneNotSupportedException {
    return (HexagonShape) super.clone();
  }
}
