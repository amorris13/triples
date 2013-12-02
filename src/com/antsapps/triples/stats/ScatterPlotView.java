package com.antsapps.triples.stats;

import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;

import com.antsapps.triples.backend.ClassicStatistics;
import com.antsapps.triples.backend.Game;

class ScatterPlotView extends View {

  private static final float TEXT_HEIGHT = 24f;
  private static final float HEIGHT_OVER_WIDTH = (float) ((Math.sqrt(5) - 1) / 2);
  private ClassicStatistics mStatistics;
  private String mSlowestTimeLabel;
  private String mAverageTimeLabel;
  private String mFastestTimeLabel;
  private String mStartDateLabel;
  private String mFinishDateLabel;
  private String mFastestDateLabel;
  private float mYLabelWidth;

  private static final Paint TEXT_PAINT = new Paint();
  static {
    TEXT_PAINT.setTextSize(TEXT_HEIGHT);
  }

  private static final float AXES_BUFFER = 8f;

  public ScatterPlotView(Context context) {
    super(context);
    // TODO Auto-generated constructor stub
  }

  public ScatterPlotView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  void setStatistics(ClassicStatistics statistics) {
    mStatistics = statistics;
    invalidate();
  }

  @Override
  protected void onMeasure(final int widthMeasureSpec,
      final int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    updateMeasuredDimensions(widthMeasureSpec, heightMeasureSpec);
  }

  private void updateMeasuredDimensions(final int widthMeasureSpec,
      final int heightMeasureSpec) {
    int widthOfCards = getDefaultSize(getMeasuredWidth(), widthMeasureSpec);
    int heightOfCards = (int) (widthOfCards * HEIGHT_OVER_WIDTH);
    setMeasuredDimension(
        widthOfCards,
        getDefaultSize(heightOfCards, heightMeasureSpec));
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (mStatistics != null) {
      calcWidths();
      drawAxes(canvas);
      drawLabels(canvas);
      drawGridlines(canvas);
      drawPlot(canvas);
    }
  }

  private void calcWidths() {
    generateLabels();
    mYLabelWidth = Math.max(
        TEXT_PAINT.measureText(mSlowestTimeLabel),
        Math.max(
            TEXT_PAINT.measureText(mAverageTimeLabel),
            TEXT_PAINT.measureText(mFastestTimeLabel)));
  }

  private void generateLabels() {
    mSlowestTimeLabel = convertTimeToString(mStatistics.getSlowestTime());
    mAverageTimeLabel = convertTimeToString(mStatistics.getAverageTime());
    mFastestTimeLabel = convertTimeToString(mStatistics.getFastestTime());

    mStartDateLabel = convertDateToString(mStatistics.getStartDate());
    mFinishDateLabel = convertDateToString(mStatistics.getFinishDate());
    mFastestDateLabel = convertDateToString(mStatistics.getFastestDate());
  }

  private String convertTimeToString(long timeMS) {
    return DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(timeMS));
  }

  private String convertDateToString(long dateMS) {
    return DateUtils.formatDateTime(
        getContext(),
        dateMS,
        DateUtils.FORMAT_ABBREV_ALL).toUpperCase();
  }

  private void drawAxes(Canvas canvas) {
    Paint axesPaint = new Paint();
    axesPaint.setStrokeWidth(4);
    axesPaint.setColor(Color.GRAY);
    axesPaint.setStrokeCap(Paint.Cap.SQUARE);
    canvas.drawLine(
        AXES_BUFFER + mYLabelWidth,
        0,
        AXES_BUFFER + mYLabelWidth,
        getHeight() - TEXT_PAINT.getTextSize() - AXES_BUFFER,
        axesPaint);
    canvas.drawLine(
        AXES_BUFFER + mYLabelWidth,
        getHeight() - TEXT_PAINT.getTextSize() - AXES_BUFFER,
        getWidth(),
        getHeight() - TEXT_PAINT.getTextSize() - AXES_BUFFER,
        axesPaint);
  }

  private void drawLabels(Canvas canvas) {
    // Y Axis Labels (times)
    Paint yTextPaint = new Paint(TEXT_PAINT);
    yTextPaint.setTextAlign(Paint.Align.RIGHT);
    canvas.drawText(mSlowestTimeLabel, mYLabelWidth, TEXT_HEIGHT, yTextPaint);

    canvas.drawText(
        mFastestTimeLabel,
        mYLabelWidth,
        convertTimeToYCoord(mStatistics.getFastestTime()),
        yTextPaint);

    // X Axis Labels (dates)
    Paint xTextPaint = new Paint(TEXT_PAINT);
    xTextPaint.setTextAlign(Paint.Align.LEFT);
    canvas.drawText(
        mStartDateLabel,
        getGraphXOffset(),
        getHeight(),
        xTextPaint);

    xTextPaint.setTextAlign(Paint.Align.RIGHT);
    canvas.drawText(mFinishDateLabel, getWidth(), getHeight(), xTextPaint);
  }

  private float getGraphXOffset() {
    return mYLabelWidth + 2 * AXES_BUFFER;
  }

  private float getGraphWidth() {
    return getWidth() - getGraphXOffset();
  }

  private float convertTimeToYCoord(long time) {
    return getGraphHeight()
        - getGraphHeight()
        * ((float) time / mStatistics
            .getSlowestTime());
  }

  private float convertDateToXCoord(long date) {
    return getGraphXOffset() + getGraphWidth()
        * (date - mStatistics.getStartDate())
        / (mStatistics.getFinishDate() - mStatistics.getStartDate());
  }

  private float getGraphHeight() {
    return getHeight() - TEXT_HEIGHT - AXES_BUFFER;
  }

  private void drawGridlines(Canvas canvas) {

  }

  private void drawPlot(Canvas canvas) {
    List<Game> rawData = mStatistics.getData();
    float[] scaledData = new float[rawData.size() * 2];
    int i = 0;
    for(Game game : rawData) {
//      scaledData[2*i] = getGraphXOffset() + i * getGraphWidth() / rawData.length;
      scaledData[2*i] = convertDateToXCoord(game.getDateStarted().getTime());
      scaledData[2*i + 1] = convertTimeToYCoord(game.getTimeElapsed());
      i++;
    }
    Paint pointPaint = new Paint();
    pointPaint.setStrokeWidth(7);
    pointPaint.setStrokeCap(Paint.Cap.ROUND);
    pointPaint.setColor(0xFF33B5E5);
    canvas.drawPoints(scaledData, pointPaint);
  }
}
