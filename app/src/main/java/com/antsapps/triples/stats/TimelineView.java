package com.antsapps.triples.stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class TimelineView extends View {
  private List<Long> mTripleFindTimes;
  private long mMaxTime;

  public TimelineView(Context context) {
    super(context);
  }

  public TimelineView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setTripleFindTimes(List<Long> tripleFindTimes, long maxTime) {
    mTripleFindTimes = tripleFindTimes;
    mMaxTime = maxTime;
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (mTripleFindTimes == null || mTripleFindTimes.isEmpty() || mMaxTime == 0) {
      return;
    }

    float density = getContext().getResources().getDisplayMetrics().density;
    float padding = 10 * density;

    int width = getWidth();
    int height = getHeight();
    float availableWidth = width - 2 * padding;

    Paint linePaint = new Paint();
    linePaint.setColor(Color.GRAY);
    linePaint.setStrokeWidth(2 * density);
    canvas.drawLine(padding, height / 2, width - padding, height / 2, linePaint);

    Paint pointPaint = new Paint();
    pointPaint.setColor(0xFF33B5E5);
    pointPaint.setStrokeWidth(8 * density);
    pointPaint.setStrokeCap(Paint.Cap.ROUND);

    for (long time : mTripleFindTimes) {
      float x = padding + (float) time / mMaxTime * availableWidth;
      canvas.drawPoint(x, height / 2, pointPaint);
    }
  }
}
