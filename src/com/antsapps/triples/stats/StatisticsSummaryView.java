package com.antsapps.triples.stats;

import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.antsapps.triples.R;
import com.antsapps.triples.backend.Statistics;

class StatisticsSummaryView extends FrameLayout implements
    OnStatisticsChangeListener {

  private final HistogramView mGraphView;

  private final TextView mNumberOfGames;
  private final TextView mFastestTime;
  private final TextView mAverageTime;

  public StatisticsSummaryView(Context context) {
    this(context, null);
  }

  public StatisticsSummaryView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public StatisticsSummaryView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
        Context.LAYOUT_INFLATER_SERVICE);
    View v = inflater.inflate(R.layout.stats_summary, this);

    mGraphView = (HistogramView) findViewById(R.id.graph);
    mNumberOfGames = (TextView) findViewById(R.id.number_completed);
    mFastestTime = (TextView) findViewById(R.id.fastest_time);
    mAverageTime = (TextView) findViewById(R.id.average_time);
  }

  @Override
  public void onStatisticsChange(Statistics statistics) {
    mGraphView.setStatistics(statistics);

    int numGames = statistics.getNumGames();
    mNumberOfGames.setText(String.valueOf(numGames));
    mFastestTime.setText(numGames == 0 ? "-" : convertTimeToString(statistics
        .getFastestTime())
        + " ("
        + convertDateToString(getContext(), statistics.getFastestDate()) + ")");
    mAverageTime.setText(numGames != 0 ? convertTimeToString(statistics
        .getAverageTime()) : "-");
  }

  private static String convertTimeToString(long timeMS) {
    return DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(timeMS));
  }

  private static String convertDateToString(Context context, long dateMS) {
    return DateUtils.formatDateTime(context, dateMS, 0);
  }
}
