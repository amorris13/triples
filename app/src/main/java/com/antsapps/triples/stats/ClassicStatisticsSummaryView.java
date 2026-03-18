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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Created by anthony on 2/12/13. */
public class ClassicStatisticsSummaryView extends BaseStatisticsSummaryView {
  private final BarChart mHistogramChart;
  private final ScatterChart mScatterChart;
  private final TextView mNumberOfGames;
  private final TextView mFastestTime;
  private final TextView mAverageTime;
  private final TextView mP25;
  private final TextView mP50;
  private final TextView mP75;
  private final TextView mP95;

  public ClassicStatisticsSummaryView(Context context) {
    super(context);

    LayoutInflater inflater =
        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View v = inflater.inflate(R.layout.classic_stats_summary, this);

    mHistogramChart = findViewById(R.id.histogram_chart);
    mScatterChart = findViewById(R.id.scatter_chart);
    mNumberOfGames = (TextView) findViewById(R.id.number_completed);
    mFastestTime = (TextView) findViewById(R.id.best);
    mAverageTime = (TextView) findViewById(R.id.average);
    mP25 = findViewById(R.id.p25);
    mP50 = findViewById(R.id.p50);
    mP75 = findViewById(R.id.p75);
    mP95 = findViewById(R.id.p95);
  }

  @Override
  protected void setAccentColor(int accentColor) {
    super.setAccentColor(accentColor);
  }

  private static String convertTimeToString(long timeMS) {
    return DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(timeMS));
  }

  private static String convertDateToString(Context context, long dateMS) {
    return DateUtils.formatDateTime(context, dateMS, 0);
  }

  @Override
  public void onStatisticsChange(Statistics statistics) {
    if (!(statistics instanceof ClassicStatistics)) {
      return;
    }
    ClassicStatistics classicStatistics = (ClassicStatistics) statistics;
    int maxTime = 30;
    int[] bins = new int[maxTime + 1];
    Arrays.fill(bins, 0);
    int maxMinutes = 0;
    List<Entry> scatterEntries = new ArrayList<>();

    for (Game game : classicStatistics.getData()) {
      int minutes = (int) Math.min(TimeUnit.MILLISECONDS.toMinutes(game.getTimeElapsed()), maxTime);
      maxMinutes = Math.max(maxMinutes, minutes);
      bins[minutes]++;

      scatterEntries.add(
          new Entry(
              (float) game.getDateStarted().getTime(), (float) game.getTimeElapsed() / 1000.0f));
    }
    Collections.sort(scatterEntries, Comparator.comparing(Entry::getX));

    if (classicStatistics.getData().isEmpty()) {
      mHistogramChart.clear();
      mScatterChart.clear();
      mNumberOfGames.setText("0");
      mFastestTime.setText("-");
      mAverageTime.setText("-");
      mP25.setText("-");
      mP50.setText("-");
      mP75.setText("-");
      mP95.setText("-");
      return;
    }

    List<BarEntry> histogramEntries = new ArrayList<>();
    for (int i = 0; i <= maxMinutes; i++) {
      histogramEntries.add(new BarEntry((float) i, (float) bins[i]));
    }

    BarDataSet barDataSet = new BarDataSet(histogramEntries, "Games");
    barDataSet.setColor(getAccentColor());
    barDataSet.setDrawValues(false);
    mHistogramChart.setData(new BarData(barDataSet));
    styleChart(mHistogramChart);
    mHistogramChart.getXAxis().setGranularity(1f);
    mHistogramChart.getAxisLeft().setGranularity(1f);
    mHistogramChart.getAxisLeft().setAxisMinimum(0f);
    mHistogramChart.getAxisLeft().setValueFormatter(new IntegerValueFormatter());
    mHistogramChart.invalidate();

    ScatterDataSet scatterDataSet = new ScatterDataSet(scatterEntries, "Performance");
    scatterDataSet.setColor(getAccentColor());
    scatterDataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
    scatterDataSet.setScatterShapeSize(calculateScatterPointSize(scatterEntries.size()));
    scatterDataSet.setDrawValues(false);
    mScatterChart.setData(new ScatterData(scatterDataSet));
    styleChart(mScatterChart);
    mScatterChart.getXAxis().setValueFormatter(new DateValueFormatter(getContext()));
    float xRange = scatterDataSet.getXMax() - scatterDataSet.getXMin();
    mScatterChart.getXAxis().setSpaceMin(xRange / 40);
    mScatterChart.getXAxis().setSpaceMax(xRange / 40);

    mScatterChart.getAxisLeft().setValueFormatter(new TimeValueFormatter());
    mScatterChart.getAxisLeft().setAxisMinimum(0f);
    mScatterChart
        .getAxisLeft()
        .setSpaceMax((scatterDataSet.getYMax() - scatterDataSet.getYMin()) / 20);

    mScatterChart.invalidate();

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

    if (numGames > 0) {
      mP25.setText(convertTimeToString(classicStatistics.getP25()));
      mP50.setText(convertTimeToString(classicStatistics.getP50()));
      mP75.setText(convertTimeToString(classicStatistics.getP75()));
      mP95.setText(convertTimeToString(classicStatistics.getP95()));
    } else {
      mP25.setText("-");
      mP50.setText("-");
      mP75.setText("-");
      mP95.setText("-");
    }
  }
}
