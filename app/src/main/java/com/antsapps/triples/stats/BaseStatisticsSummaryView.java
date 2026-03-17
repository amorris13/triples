package com.antsapps.triples.stats;

import android.content.Context;
import android.util.TypedValue;
import android.widget.FrameLayout;
import androidx.core.content.ContextCompat;
import com.antsapps.triples.R;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.XAxis;

abstract class BaseStatisticsSummaryView extends FrameLayout implements OnStatisticsChangeListener {

  private int mAccentColor;

  public BaseStatisticsSummaryView(Context context) {
    super(context);
  }

  protected void setAccentColor(int accentColor) {
    mAccentColor = accentColor;
  }

  protected int getAccentColor() {
    return mAccentColor;
  }

  protected int getOnSurfaceColor() {
    TypedValue typedValue = new TypedValue();
    getContext()
        .getTheme()
        .resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
    return typedValue.data;
  }

  protected void styleChart(BarLineChartBase<?> chart) {
    int onSurface = getOnSurfaceColor();
    chart.getLegend().setTextColor(onSurface);
    chart.getDescription().setEnabled(false);

    XAxis xAxis = chart.getXAxis();
    xAxis.setTextColor(onSurface);
    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    xAxis.setDrawGridLines(false);

    chart.getAxisLeft().setTextColor(onSurface);
    chart.getAxisLeft().setDrawGridLines(true);
    chart.getAxisLeft().setGridColor(ContextCompat.getColor(getContext(), R.color.color_separator));

    chart.getAxisRight().setEnabled(false);
  }
}
