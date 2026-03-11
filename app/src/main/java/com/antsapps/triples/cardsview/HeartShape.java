package com.antsapps.triples.cardsview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.shapes.RectShape;

/** Heart Shape */
public class HeartShape extends RectShape {
  private Path mPath;

  public HeartShape() {}

  @Override
  public void draw(Canvas canvas, Paint paint) {
    if (mPath != null) {
      canvas.drawPath(mPath, paint);
    }
  }

  @Override
  protected void onResize(float width, float height) {
    super.onResize(width, height);

    RectF rect = rect();
    mPath = new Path();

    float w = rect.width();
    float h = rect.height();

    mPath.moveTo(w / 2, h / 4);
    mPath.cubicTo(w / 2, 0, 0, 0, 0, h / 2);
    mPath.cubicTo(0, h * 3 / 4, w / 2, h, w / 2, h);
    mPath.cubicTo(w / 2, h, w, h * 3 / 4, w, h / 2);
    mPath.cubicTo(w, 0, w / 2, 0, w / 2, h / 4);
    mPath.close();
  }

  @Override
  public HeartShape clone() throws CloneNotSupportedException {
    return (HeartShape) super.clone();
  }
}
