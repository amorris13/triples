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
    float cx = rect.centerX();
    float cy = rect.centerY();
    float s = Math.min(height / 2f, (float) (width / Math.sqrt(3)));

    float dx = (float) (s * Math.sqrt(3) / 2);
    float dy = s / 2;

    mPath = new Path();
    mPath.moveTo(cx, cy - s);
    mPath.lineTo(cx + dx, cy - dy);
    mPath.lineTo(cx + dx, cy + dy);
    mPath.lineTo(cx, cy + s);
    mPath.lineTo(cx - dx, cy + dy);
    mPath.lineTo(cx - dx, cy - dy);
    mPath.close();
  }

  @Override
  public HexagonShape clone() throws CloneNotSupportedException {
    return (HexagonShape) super.clone();
  }
}
