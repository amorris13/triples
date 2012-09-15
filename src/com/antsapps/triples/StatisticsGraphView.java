package com.antsapps.triples;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.antsapps.triples.backend.Statistics;

public class StatisticsGraphView extends FrameLayout implements OnStatisticsChangeListener{

  private final HistogramView mGraphView;

  public StatisticsGraphView(Context context) {
    this(context, null);
  }

  public StatisticsGraphView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public StatisticsGraphView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
        Context.LAYOUT_INFLATER_SERVICE);
    View v = inflater.inflate(R.layout.stats_graph, this);

    mGraphView = (HistogramView) findViewById(R.id.graph);
  }

  @Override
  public void onStatisticsChange(Statistics statistics) {
    mGraphView.setStatistics(statistics);
  }
}
