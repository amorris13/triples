package com.antsapps.triples.stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.antsapps.triples.R;
import com.google.common.primitives.Ints;

class HistogramView extends View {

  private static final int NUM_X_LABELS = 5;
  private static final int NUM_GRIDLINES = 4;

  private static final float HEIGHT_OVER_WIDTH = (float) ((Math.sqrt(5) - 1) / 2);
  private String mXAxisTitle;
  private float mYLabelWidth;

  private static final Paint TEXT_PAINT = new Paint();

  private static final int BUFFER_DP = 4;
  private final float mBufferPx;

  private static final int COLUMN_PADDING_DP = 1;
  private final float mColumnPaddingPx;

  private static final int TEXT_HEIGHT_SP = 12;
  private final float mTextHeightPx;

  private final int mVertSpacePx;

  private int[] mBins;
  private int mMaxX;
  private int mMaxY;
  private float mGraphWidth;
  private float mGraphHeight;
  private boolean mCentreXAxisLabels;

  public HistogramView(Context context) {
    this(context, null);
  }

  public HistogramView(Context context, AttributeSet attrs) {
    super(context, attrs);

    DisplayMetrics dm = context.getResources().getDisplayMetrics();
    mBufferPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BUFFER_DP, dm);
    mColumnPaddingPx =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, COLUMN_PADDING_DP, dm);
    mTextHeightPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, TEXT_HEIGHT_SP, dm);

    mVertSpacePx = context.getResources().getDimensionPixelSize(R.dimen.stats_vert_padding);

    TEXT_PAINT.setTextSize(mTextHeightPx);
  }

  void setStatistics(String xLabel, int[] bins, boolean centreXAxisLabels) {
    mXAxisTitle = xLabel;
    mBins = bins;
    mMaxX = roundUpToNearestMultiple(mBins.length - 1, NUM_X_LABELS);
    mMaxY = roundUpToNearestMultiple(Ints.max(mBins), NUM_GRIDLINES);

    mCentreXAxisLabels = centreXAxisLabels;

    invalidate();
  }

  private static int roundUpToNearestMultiple(float number, int factor) {
    return (int) (Math.ceil(number / factor)) * factor;
  }

  @Override
  protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    updateMeasuredDimensions(widthMeasureSpec, heightMeasureSpec);
  }

  private void updateMeasuredDimensions(final int widthMeasureSpec, final int heightMeasureSpec) {
    int widthOfCards = getDefaultSize(getMeasuredWidth(), widthMeasureSpec);
    int heightOfCards = (int) (widthOfCards * HEIGHT_OVER_WIDTH);
    setMeasuredDimension(widthOfCards, getDefaultSize(heightOfCards, heightMeasureSpec));
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (mBins == null || mBins.length == 0 || mMaxY == 0) {
      drawNoGames(canvas);
    } else {
      calcDimens();
      drawLabels(canvas);
      drawGridlines(canvas);
      drawPlot(canvas);
      drawAxes(canvas);
      drawAxesTitles(canvas);
    }
  }

  private void drawNoGames(Canvas canvas) {
    Paint paint = new Paint(TEXT_PAINT);
    paint.setTextAlign(Align.CENTER);
    canvas.drawText("No games completed", getWidth() / 2, getHeight() / 2, paint);
  }

  private void calcDimens() {
    mYLabelWidth = 0;
    for (int i = 0; i <= NUM_GRIDLINES; i++) {
      mYLabelWidth =
          Math.max(mYLabelWidth, TEXT_PAINT.measureText(String.valueOf(calcNumForGridline(i))));
    }
    mGraphWidth = getWidth() - getYLabelWidth() - mBufferPx;
    mGraphHeight =
        getHeight() - getXLabelHeight() - getXTitleHeight() - mBufferPx - 2 * mVertSpacePx;
  }

  private void drawAxes(Canvas canvas) {
    Paint axesPaint = new Paint();
    axesPaint.setStrokeWidth(4);
    axesPaint.setColor(Color.BLACK);
    axesPaint.setStrokeCap(Paint.Cap.SQUARE);
    canvas.drawLine(
        calcXForMinutes(0),
        calcYForNumber(0),
        calcXForMinutes(mMaxX + 1),
        calcYForNumber(0),
        axesPaint);
  }

  private float getXTitleHeight() {
    return mTextHeightPx + mBufferPx;
  }

  private float getXLabelHeight() {
    return mTextHeightPx + mBufferPx;
  }

  private float getYLabelWidth() {
    return mBufferPx + mYLabelWidth;
  }

  private void drawAxesTitles(Canvas canvas) {
    Paint xTitlePaint = new Paint(TEXT_PAINT);
    xTitlePaint.setTextAlign(Paint.Align.CENTER);
    canvas.drawText(
        mXAxisTitle,
        mYLabelWidth + mBufferPx + mGraphWidth / 2,
        getHeight() - mVertSpacePx,
        xTitlePaint);
  }

  private void drawLabels(Canvas canvas) {
    // Y Axis Labels (number)
    Paint yTextPaint = new Paint(TEXT_PAINT);
    yTextPaint.setTextAlign(Paint.Align.RIGHT);
    for (int i = 0; i <= NUM_GRIDLINES; i++) {
      int number = calcNumForGridline(i);
      canvas.drawText(
          String.valueOf(number),
          mYLabelWidth,
          calcYForNumber(number) + mTextHeightPx / 3,
          yTextPaint);
    }

    // X Axis Labels (minutes)
    Paint xTextPaint = new Paint(TEXT_PAINT);
    xTextPaint.setTextAlign(Paint.Align.CENTER);
    for (int i = 0; i <= NUM_X_LABELS; i++) {
      int minutes = mMaxX / NUM_X_LABELS * i;
      canvas.drawText(
          String.valueOf(minutes),
          calcXForMinutes(mCentreXAxisLabels ? minutes + 0.5f : minutes),
          getHeight() - getXTitleHeight() - mVertSpacePx,
          xTextPaint);
    }
  }

  private float calcXForMinutes(float minutes) {
    return mYLabelWidth + mBufferPx + mGraphWidth / (mMaxX + 1) * minutes;
  }

  private void drawGridlines(Canvas canvas) {
    Paint gridlinePaint = new Paint();
    gridlinePaint.setColor(0x40000000);
    gridlinePaint.setStrokeWidth(2);
    gridlinePaint.setStrokeCap(Paint.Cap.SQUARE);
    for (int i = 1; i <= NUM_GRIDLINES; i++) {
      int num = calcNumForGridline(i);
      canvas.drawLine(
          calcXForMinutes(0),
          calcYForNumber(num),
          calcXForMinutes(mMaxX + 1),
          calcYForNumber(num),
          gridlinePaint);
    }
  }

  private int calcNumForGridline(int i) {
    return mMaxY / NUM_GRIDLINES * i;
  }

  private void drawPlot(Canvas canvas) {
    Paint columnPaint = new Paint();
    columnPaint.setColor(getResources().getColor(android.R.color.holo_blue_light));
    columnPaint.setStrokeWidth(2);
    columnPaint.setStyle(Paint.Style.FILL);
    for (int i = 0; i <= mMaxX; i++) {
      canvas.drawRect(
          calcXForMinutes(i) + mColumnPaddingPx,
          calcYForNumber(i < mBins.length ? mBins[i] : 0),
          calcXForMinutes(i + 1) - mColumnPaddingPx,
          calcYForNumber(0),
          columnPaint);
    }
  }

  private float calcYForNumber(int number) {
    return getHeight()
        - (getXLabelHeight() + getXTitleHeight() + mVertSpacePx + mGraphHeight / mMaxY * number);
  }
}
