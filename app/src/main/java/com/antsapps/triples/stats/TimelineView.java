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
  private int mFastestIndex = -1;
  private int mSlowestIndex = -1;

  public TimelineView(Context context) {
    super(context);
  }

  public TimelineView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setTripleFindTimes(List<Long> tripleFindTimes, long maxTime, int fastestIndex, int slowestIndex) {
    mTripleFindTimes = tripleFindTimes;
    mMaxTime = maxTime;
    mFastestIndex = fastestIndex;
    mSlowestIndex = slowestIndex;
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
    pointPaint.setStrokeWidth(8 * density);
    pointPaint.setStrokeCap(Paint.Cap.ROUND);

    for (int i = 0; i < mTripleFindTimes.size(); i++) {
      long time = mTripleFindTimes.get(i);
      if (i == mFastestIndex) {
        pointPaint.setColor(0xFF4CAF50); // Green
      } else if (i == mSlowestIndex) {
        pointPaint.setColor(0xFFF44336); // Red
      } else {
        pointPaint.setColor(0xFF33B5E5); // Blue
      }
      float x = padding + (float) time / mMaxTime * availableWidth;
      canvas.drawPoint(x, height / 2, pointPaint);
    }

    Paint textPaint = new Paint();
    textPaint.setColor(Color.GRAY);
    textPaint.setTextSize(10 * density);
    textPaint.setAntiAlias(true);

    canvas.drawText("0:00", padding, height / 2 + 20 * density, textPaint);

    String endTime = android.text.format.DateUtils.formatElapsedTime(mMaxTime / 1000);
    float endTimeWidth = textPaint.measureText(endTime);
    canvas.drawText(endTime, width - padding - endTimeWidth, height / 2 + 20 * density, textPaint);
  }
}
