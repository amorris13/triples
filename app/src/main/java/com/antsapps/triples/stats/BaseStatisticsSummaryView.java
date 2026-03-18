package com.antsapps.triples.stats;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.widget.FrameLayout;
import androidx.core.content.ContextCompat;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.Date;

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
    chart.getLegend().setEnabled(false);
    chart.getDescription().setEnabled(false);

    XAxis xAxis = chart.getXAxis();
    xAxis.setTextColor(onSurface);
    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    xAxis.setDrawGridLines(false);

    chart.getAxisLeft().setTextColor(onSurface);
    chart.getAxisLeft().setDrawGridLines(true);
    chart
        .getAxisLeft()
        .setGridColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));

    chart.getAxisRight().setEnabled(false);
  }

  protected float calculateScatterPointSize(int count) {
    if (count < 10) return dpToPx(6);
    if (count < 50) return dpToPx(4);
    return dpToPx(2);
  }

  protected static class DateValueFormatter extends ValueFormatter {
    private final java.text.DateFormat mFormat;

    public DateValueFormatter(Context context) {
      mFormat = DateFormat.getDateFormat(context);
    }

    @Override
    public String getFormattedValue(float value) {
      return mFormat.format(new Date((long) value));
    }
  }

  protected static class TimeValueFormatter extends ValueFormatter {
    @Override
    public String getFormattedValue(float value) {
      return DateUtils.formatElapsedTime((long) value);
    }
  }

  protected static class IntegerValueFormatter extends ValueFormatter {
    @Override
    public String getFormattedValue(float value) {
      return String.valueOf((int) value);
    }
  }

  private int dpToPx(float dp) {
    return (int)
            TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics());
  }
}
