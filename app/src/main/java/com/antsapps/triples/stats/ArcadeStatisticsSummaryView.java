package com.antsapps.triples.stats;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.ArcadeStatistics;
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
public class ArcadeStatisticsSummaryView extends BaseStatisticsSummaryView {
  private final BarChart mHistogramChart;
  private final ScatterChart mScatterChart;
  private final TextView mNumberOfGames;
  private final TextView mBest;
  private final TextView mAverage;
  private final TextView mP25;
  private final TextView mP50;
  private final TextView mP75;
  private final TextView mP95;
  public static final int MAX_POSSIBLE = 150;

  public ArcadeStatisticsSummaryView(Context context) {
    super(context);

    LayoutInflater inflater =
        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View v = inflater.inflate(R.layout.arcade_stats_summary, this);

    mHistogramChart = findViewById(R.id.histogram_chart);
    mScatterChart = findViewById(R.id.scatter_chart);
    mNumberOfGames = (TextView) findViewById(R.id.number_completed);
    mBest = (TextView) findViewById(R.id.best);
    mAverage = (TextView) findViewById(R.id.average);
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
    if (!(statistics instanceof ArcadeStatistics)) {
      return;
    }
    ArcadeStatistics arcadeStatistics = (ArcadeStatistics) statistics;

    int[] bins = new int[MAX_POSSIBLE + 1];
    Arrays.fill(bins, 0);
    int maxFound = 0;
    List<Entry> scatterEntries = new ArrayList<>();

    for (Game game : arcadeStatistics.getData()) {
      int numFound = (int) Math.min(((ArcadeGame) game).getNumTriplesFound(), MAX_POSSIBLE);
      maxFound = Math.max(maxFound, numFound);
      bins[numFound]++;

      scatterEntries.add(
          new Entry(
              (float) game.getDateStarted().getTime(),
              (float) ((ArcadeGame) game).getNumTriplesFound()));
    }
    Collections.sort(scatterEntries, Comparator.comparing(Entry::getX));

    if (arcadeStatistics.getData().isEmpty()) {
      mHistogramChart.clear();
      mScatterChart.clear();
      mNumberOfGames.setText("0");
      mBest.setText("-");
      mAverage.setText("-");
      mP25.setText("-");
      mP50.setText("-");
      mP75.setText("-");
      mP95.setText("-");
      return;
    }

    List<BarEntry> histogramEntries = new ArrayList<>();
    for (int i = 0; i <= maxFound; i++) {
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

    mScatterChart.getAxisLeft().setValueFormatter(new IntegerValueFormatter());
    mScatterChart.getAxisLeft().setGranularity(1f);
    mScatterChart.getAxisLeft().setAxisMinimum(0f);
    mScatterChart.getAxisLeft().setSpaceMax(1);

    mScatterChart.invalidate();

    int numGames = arcadeStatistics.getNumGames();
    mNumberOfGames.setText(String.valueOf(numGames));
    mBest.setText(
        numGames == 0
            ? "-"
            : String.valueOf(arcadeStatistics.getMostFound())
                + " ("
                + convertDateToString(getContext(), arcadeStatistics.getMostFoundDate())
                + ")");
    mAverage.setText(numGames != 0 ? String.valueOf(arcadeStatistics.getAverageFound()) : "-");

    if (numGames > 0) {
      mP25.setText(String.valueOf(arcadeStatistics.getP25()));
      mP50.setText(String.valueOf(arcadeStatistics.getP50()));
      mP75.setText(String.valueOf(arcadeStatistics.getP75()));
      mP95.setText(String.valueOf(arcadeStatistics.getP95()));
    } else {
      mP25.setText("-");
      mP50.setText("-");
      mP75.setText("-");
      mP95.setText("-");
    }
  }
}
