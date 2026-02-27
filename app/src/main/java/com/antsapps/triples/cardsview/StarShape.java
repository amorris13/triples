package com.antsapps.triples.cardsview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.shapes.RectShape;

/** Star Shape */
public class StarShape extends RectShape {
  private Path mPath;

  public StarShape() {}

  @Override
  public void draw(Canvas canvas, Paint paint) {
    canvas.drawPath(mPath, paint);
  }

  @Override
  protected void onResize(float width, float height) {
    super.onResize(width, height);

    RectF rect = rect();
    float cx = rect.centerX();
    float cy = rect.centerY();
    float outerRx = width / 2f;
    float outerRy = height / 2f;
    float innerRx = outerRx * 0.4f; // Typical star ratio
    float innerRy = outerRy * 0.4f;

    mPath = new Path();
    double angle = Math.PI / 2; // Start at the top
    double step = Math.PI / 5;

    for (int i = 0; i < 10; i++) {
      float rx = (i % 2 == 0) ? outerRx : innerRx;
      float ry = (i % 2 == 0) ? outerRy : innerRy;
      float x = (float) (cx + rx * Math.cos(angle - i * step));
      float y = (float) (cy - ry * Math.sin(angle - i * step));
      if (i == 0) {
        mPath.moveTo(x, y);
      } else {
        mPath.lineTo(x, y);
      }
    }
    mPath.close();
  }

  @Override
  public StarShape clone() throws CloneNotSupportedException {
    return (StarShape) super.clone();
  }
}
