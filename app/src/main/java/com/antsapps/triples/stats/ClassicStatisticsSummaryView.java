package com.antsapps.triples.stats;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.antsapps.triples.R;
import com.antsapps.triples.backend.ClassicStatistics;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Statistics;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/** Created by anthony on 2/12/13. */
public class ClassicStatisticsSummaryView extends BaseStatisticsSummaryView {
  private final HistogramView mGraphView;
  private final TextView mNumberOfGames;
  private final TextView mFastestTime;
  private final TextView mAverageTime;

  public ClassicStatisticsSummaryView(Context context) {
    super(context);

    LayoutInflater inflater =
        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View v = inflater.inflate(R.layout.classic_stats_summary, this);

    mGraphView = (HistogramView) findViewById(R.id.graph);
    mNumberOfGames = (TextView) findViewById(R.id.number_completed);
    mFastestTime = (TextView) findViewById(R.id.best);
    mAverageTime = (TextView) findViewById(R.id.average);
  }

  private static String convertTimeToString(long timeMS) {
    return DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(timeMS));
  }

  private static String convertDateToString(Context context, long dateMS) {
    return DateUtils.formatDateTime(context, dateMS, 0);
  }

  @Override
  public void onStatisticsChange(Statistics statistics) {
    ClassicStatistics classicStatistics = (ClassicStatistics) statistics;
    int maxTime = 30;
    int[] bins = new int[maxTime + 1];
    Arrays.fill(bins, 0);
    int maxMinutes = 0;
    for (Game game : classicStatistics.getData()) {
      int minutes = (int) Math.min(TimeUnit.MILLISECONDS.toMinutes(game.getTimeElapsed()), maxTime);
      maxMinutes = Math.max(maxMinutes, minutes);
      bins[minutes]++;
    }

    mGraphView.setStatistics("Time (minutes)", Arrays.copyOfRange(bins, 0, maxMinutes + 1), false);

    int numGames = classicStatistics.getNumGames();
    mNumberOfGames.setText(String.valueOf(numGames));
    mFastestTime.setText(
        numGames == 0
            ? "-"
            : convertTimeToString(classicStatistics.getFastestTime())
                + " ("
                + convertDateToString(getContext(), classicStatistics.getFastestDate())
                + ")");
    mAverageTime.setText(
        numGames != 0 ? convertTimeToString(classicStatistics.getAverageTime()) : "-");
  }
}
