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

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/** Created by anthony on 2/12/13. */
public class ArcadeStatisticsSummaryView extends BaseStatisticsSummaryView {
  private final HistogramView mGraphView;
  private final TextView mNumberOfGames;
  private final TextView mBest;
  private final TextView mAverage;
  public static final int MAX_POSSIBLE = 150;

  public ArcadeStatisticsSummaryView(Context context) {
    super(context);

    LayoutInflater inflater =
        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View v = inflater.inflate(R.layout.arcade_stats_summary, this);

    mGraphView = (HistogramView) findViewById(R.id.graph);
    mNumberOfGames = (TextView) findViewById(R.id.number_completed);
    mBest = (TextView) findViewById(R.id.best);
    mAverage = (TextView) findViewById(R.id.average);
  }

  private static String convertTimeToString(long timeMS) {
    return DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(timeMS));
  }

  private static String convertDateToString(Context context, long dateMS) {
    return DateUtils.formatDateTime(context, dateMS, 0);
  }

  @Override
  public void onStatisticsChange(Statistics statistics) {
    ArcadeStatistics arcadeStatistics = (ArcadeStatistics) statistics;

    int[] bins = new int[MAX_POSSIBLE + 1];
    Arrays.fill(bins, 0);
    int maxFound = 0;
    for (Game game : arcadeStatistics.getData()) {
      int numFound = (int) Math.min(((ArcadeGame) game).getNumTriplesFound(), MAX_POSSIBLE);
      maxFound = Math.max(maxFound, numFound);
      bins[numFound]++;
    }

    mGraphView.setStatistics("Triples Found", Arrays.copyOfRange(bins, 0, maxFound + 1), true);

    int numGames = arcadeStatistics.getNumGames();
    mNumberOfGames.setText(String.valueOf(numGames));
    mBest.setText(
        numGames == 0
            ? "-"
            : arcadeStatistics.getMostFound()
                + " ("
                + convertDateToString(getContext(), arcadeStatistics.getMostFoundDate())
                + ")");
    mAverage.setText(numGames != 0 ? String.valueOf(arcadeStatistics.getAverageFound()) : "-");
  }
}
