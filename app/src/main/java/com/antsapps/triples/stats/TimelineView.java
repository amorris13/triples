package com.antsapps.triples.stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.core.content.ContextCompat;
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

    int colorFastest = ContextCompat.getColor(getContext(), com.antsapps.triples.R.color.color_fastest);
    int colorSlowest = ContextCompat.getColor(getContext(), com.antsapps.triples.R.color.color_slowest);
    int colorDefault = ContextCompat.getColor(getContext(), com.antsapps.triples.R.color.color_timeline_point);

    for (int i = 0; i < mTripleFindTimes.size(); i++) {
      long time = mTripleFindTimes.get(i);
      if (i == mFastestIndex) {
        pointPaint.setColor(colorFastest);
      } else if (i == mSlowestIndex) {
        pointPaint.setColor(colorSlowest);
      } else {
        pointPaint.setColor(colorDefault);
      }
      float x = padding + (float) time / mMaxTime * availableWidth;
      canvas.drawPoint(x, height / 2, pointPaint);
    }

    Paint textPaint = new Paint();
    textPaint.setColor(Color.GRAY);
    textPaint.setTextSize(10 * density);
    textPaint.setAntiAlias(true);
    textPaint.setTextAlign(Paint.Align.CENTER);

    canvas.drawText("0:00", padding, height / 2 + 20 * density, textPaint);

    String endTime = android.text.format.DateUtils.formatElapsedTime(mMaxTime / 1000);
    canvas.drawText(endTime, width - padding, height / 2 + 20 * density, textPaint);
  }
}
